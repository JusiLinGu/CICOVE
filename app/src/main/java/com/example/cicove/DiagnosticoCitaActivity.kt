package com.example.cicove

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cicove.DB.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiagnosticoCitaActivity : AppCompatActivity() {

    private lateinit var etNombreMascota: EditText
    private lateinit var etFecha: EditText
    private lateinit var etHora: EditText
    private lateinit var etMalestar: EditText
    private lateinit var spinnerEstadoCita: Spinner
    private lateinit var etObservaciones: EditText
    private lateinit var btnGuardar: Button

    private var citaID: Int = -1
    private val estados = listOf("Pendiente", "Completado", "Cancelado")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.diagnostico_cita)

        etNombreMascota = findViewById(R.id.etNombreMascota)
        etFecha = findViewById(R.id.editTextFecha)
        etHora = findViewById(R.id.etHora)
        etMalestar = findViewById(R.id.etMalestar)
        spinnerEstadoCita = findViewById(R.id.spinnerEstadoCita)
        etObservaciones = findViewById(R.id.etObservaciones)
        btnGuardar = findViewById(R.id.btnGuardarDiagnostico)

        val intent = intent
        citaID = intent.getIntExtra("citaID", -1)
        val nombreMascota = intent.getStringExtra("nombreMascota") ?: ""
        val fecha = intent.getStringExtra("fecha") ?: ""
        val hora = intent.getStringExtra("hora") ?: ""
        val malestar = intent.getStringExtra("malestar") ?: ""
        val estado = intent.getStringExtra("estado") ?: ""
        val observaciones = intent.getStringExtra("observaciones") ?: ""

        if (citaID == -1) {
            Toast.makeText(this, "Error: cita no encontrada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        etNombreMascota.setText(nombreMascota)
        etFecha.setText(fecha)
        etHora.setText(hora)
        etMalestar.setText(malestar)
        etObservaciones.setText(observaciones)

        etNombreMascota.isEnabled = false
        etFecha.isEnabled = false
        etHora.isEnabled = false
        etMalestar.isEnabled = false

        //Configurar el Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, estados)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEstadoCita.adapter = adapter

        //Seleccionar el estado actual de la cita
        val estadoIndex = estados.indexOfFirst { it.equals(estado, ignoreCase = true) }
        if (estadoIndex >= 0) {
            spinnerEstadoCita.setSelection(estadoIndex)
        }

        btnGuardar.setOnClickListener {
            guardarDiagnostico()
        }
    }

    private fun guardarDiagnostico() {
        val nuevoEstado = spinnerEstadoCita.selectedItem.toString()
        val nuevasObservaciones = etObservaciones.text.toString().trim()

        if(nuevasObservaciones.isEmpty()) {
                Toast.makeText(this@DiagnosticoCitaActivity, "Ingrese observaciones", Toast.LENGTH_SHORT).show()
                return
        } else {
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(this@DiagnosticoCitaActivity)
                withContext(Dispatchers.IO) {
                    db.citaDao().actualizarDiagnostico(citaID, nuevoEstado, nuevasObservaciones)
                }
                Toast.makeText(this@DiagnosticoCitaActivity, "Diagnóstico guardado", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
