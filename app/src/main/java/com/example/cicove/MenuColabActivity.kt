package com.example.cicove

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cicove.DB.AppDatabase
import com.example.cicove.DB.CitaConMascota
import com.example.cicove.adapters.CitaAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MenuColabActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var citaAdapter: CitaAdapter
    private var listaCitas: List<CitaConMascota> = listOf()
    private var usuarioID: Int = -1
    private var clicBloqueado = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_personal_vet)

        usuarioID = intent.getIntExtra("usuarioID", -1)

        recyclerView = findViewById(R.id.recyclerMascotas)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val historialMascotaHoy = findViewById<ImageView>(R.id.mascotaHistorial)
        historialMascotaHoy.setOnClickListener {
            historialMascotaHoy()
        }

        val perfilVeterinario = findViewById<ImageView>(R.id.perfilVeterinario)
        perfilVeterinario.setOnClickListener {
            perfilVeterinario()
        }
    }

    override fun onResume() {
        super.onResume()
        clicBloqueado = false  // Reinicia el bloqueo al regresar
        obtenerCitasDelDia()
    }


    private fun obtenerCitasDelDia() {
        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val tvMensajeVacio = findViewById<TextView>(R.id.tvMensajeVacio)

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@MenuColabActivity)
            val citasHoy = withContext(Dispatchers.IO) {
                db.citaDao().obtenerCitasDelDia(fechaActual)
            }

            if (citasHoy.isEmpty()) {
                tvMensajeVacio.visibility = View.VISIBLE
                recyclerView.adapter = null
            } else {
                tvMensajeVacio.visibility = View.GONE
                listaCitas = citasHoy
                citaAdapter = CitaAdapter(listaCitas) { citaSeleccionada ->
                    if (clicBloqueado) return@CitaAdapter  // Prevenir doble toque rápido
                    clicBloqueado = true

                    val intent = Intent(this@MenuColabActivity, DiagnosticoCitaActivity::class.java).apply {
                        putExtra("citaID", citaSeleccionada.citaID)
                        putExtra("nombreMascota", citaSeleccionada.nombreMascota)
                        putExtra("fecha", citaSeleccionada.fecha)
                        putExtra("hora", citaSeleccionada.hora)
                        putExtra("malestar", citaSeleccionada.motivoConsulta)
                        putExtra("estado", citaSeleccionada.estadoCita)
                        putExtra("observaciones", citaSeleccionada.observaciones ?: "")
                    }
                    startActivity(intent)
                }

                recyclerView.adapter = citaAdapter
            }
        }
    }

    private fun historialMascotaHoy() {
        val i = Intent(this, HistorialPacientesHoyActivity::class.java)
        startActivity(i)
    }

    private fun perfilVeterinario() {
        val i = Intent(this, PerfilUsuarioActivity::class.java)
        i.putExtra("usuarioID", usuarioID)
        startActivity(i)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
