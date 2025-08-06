package com.example.cicove

import android.os.Bundle
import android.view.View
import android.widget.*
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

class HistorialPacientesHoyActivity : AppCompatActivity() {

    private lateinit var spinnerMascotas: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var citaAdapter: CitaAdapter
    private lateinit var db: AppDatabase

    data class MascotaSimple(val mascotaID: Int, val nombre: String)
    private var mascotasConCitaHoy: List<MascotaSimple> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.historial_citas)

        spinnerMascotas = findViewById(R.id.spinnerMascotas2)
        recyclerView = findViewById(R.id.recyclerMascotas)
        recyclerView.layoutManager = LinearLayoutManager(this)

        db = AppDatabase.getDatabase(this)

        obtenerMascotasConCitaHoy()
    }

    private fun obtenerMascotasConCitaHoy() {
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        lifecycleScope.launch {
            val citasHoy = withContext(Dispatchers.IO) {
                db.citaDao().obtenerCitasDelDia(fechaHoy)
            }

            val mascotasUnicas = citasHoy
                .map { MascotaSimple(it.mascotaID, it.nombreMascota) }
                .distinctBy { it.mascotaID }

            if (mascotasUnicas.isEmpty()) {
                Toast.makeText(this@HistorialPacientesHoyActivity, "No hay mascotas con citas hoy", Toast.LENGTH_SHORT).show()
                return@launch
            }

            mascotasConCitaHoy = mascotasUnicas

            val nombresMascotas = mascotasConCitaHoy.map { it.nombre }

            val adapter = ArrayAdapter(this@HistorialPacientesHoyActivity, android.R.layout.simple_spinner_item, nombresMascotas)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerMascotas.adapter = adapter

            spinnerMascotas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val mascotaSeleccionada = mascotasConCitaHoy[position]
                    cargarHistorialCitas(mascotaSeleccionada.mascotaID)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun cargarHistorialCitas(mascotaID: Int) {
        lifecycleScope.launch {
            val citas: List<CitaConMascota> = withContext(Dispatchers.IO) {
                db.citaDao().obtenerHistorialPorMascota(mascotaID)
            }
            citaAdapter = CitaAdapter(citas) { Toast.makeText(this@HistorialPacientesHoyActivity, "Para diagnosticar una cita, favor de regresar al menu", Toast.LENGTH_SHORT).show() }
            recyclerView.adapter = citaAdapter
        }
    }
}
