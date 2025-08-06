package com.example.cicove

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cicove.DB.AppDatabase
import com.example.cicove.DB.CitaConMascota
import com.example.cicove.adapters.CitaAdapter
import kotlinx.coroutines.launch

class HistorialCitasActivity : AppCompatActivity() {

    private var usuarioID: Int = -1
    private lateinit var spinnerMascotas: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var citaAdapter: CitaAdapter

    private var mascotasID: List<Int> = listOf()
    private var clicBloqueado = false


    companion object {
        private const val REQUEST_ACTUALIZAR_CITA = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.historial_citas)

        usuarioID = intent.getIntExtra("usuarioID", -1)
        if (usuarioID == -1) {
            Toast.makeText(this, "Error: ID de usuario no recibido", Toast.LENGTH_LONG).show()
            finish()
        }

        spinnerMascotas = findViewById(R.id.spinnerMascotas2)
        recyclerView = findViewById(R.id.recyclerMascotas)
        recyclerView.layoutManager = LinearLayoutManager(this)

        cargarMascotas()
    }

    private fun cargarMascotas() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val mascotas = db.mascotaDao().obtenerMascotasPorUsuario(usuarioID)

            if (mascotas.isEmpty()) {
                runOnUiThread {
                    Toast.makeText(this@HistorialCitasActivity, "No tienes mascotas registradas", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return@launch
            }

            mascotasID = mascotas.map { it.mascotaID }
            val nombresMascotas = mascotas.map { it.nombreMascota }

            runOnUiThread {
                val adapter = ArrayAdapter(this@HistorialCitasActivity, android.R.layout.simple_spinner_item, nombresMascotas)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerMascotas.adapter = adapter

                spinnerMascotas.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: android.view.View, position: Int, id: Long) {
                        val mascotaID = mascotasID[position]
                        cargarCitas(mascotaID)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                })
            }
        }
    }

    private fun cargarCitas(mascotaID: Int) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val listaCitas: List<CitaConMascota> = db.citaDao().obtenerCitasPorMascota(usuarioID, mascotaID)

            runOnUiThread {
                citaAdapter = CitaAdapter(listaCitas) { citaSeleccionada ->
                    if (clicBloqueado) return@CitaAdapter  //Evita múltiples clics rápidos, para evitar que se abra 2 veces la activity

                    if (citaSeleccionada.estadoCita == "Pendiente") {
                        clicBloqueado = true  // Bloquea clics mientras abre

                        val intent = Intent(this@HistorialCitasActivity, ActualizarCitaActivity::class.java).apply {
                            putExtra("usuarioID", usuarioID)
                            putExtra("citaID", citaSeleccionada.citaID)
                        }
                        startActivityForResult(intent, REQUEST_ACTUALIZAR_CITA)
                    } else {
                        Toast.makeText(this@HistorialCitasActivity, "Solo puedes editar citas pendientes", Toast.LENGTH_SHORT).show()
                    }
                }
                recyclerView.adapter = citaAdapter
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        clicBloqueado = false  // Habilita clics de nuevo

        if (requestCode == REQUEST_ACTUALIZAR_CITA && resultCode == RESULT_OK) {
            val posicion = spinnerMascotas.selectedItemPosition
            if (posicion != -1 && posicion < mascotasID.size) {
                cargarCitas(mascotasID[posicion])
                Toast.makeText(this, "Cita actualizada", Toast.LENGTH_SHORT).show()
            }
        }
    }
}