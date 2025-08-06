package com.example.cicove

import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cicove.DB.AppDatabase
import com.example.cicove.DB.Cita
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ActualizarCitaActivity : AppCompatActivity() {

    private var usuarioID: Int = -1
    private var citaID: Int = -1
    private var mascotaIDSeleccionada: Int = -1

    private lateinit var adapterHora: ArrayAdapter<String>
    private lateinit var spinnerMascotas: Spinner
    private lateinit var editTextFecha: EditText
    private lateinit var spinnerHora: Spinner
    private lateinit var editTextMotivo: EditText
    private lateinit var btnActualizar: Button
    private lateinit var btnCancelarCita:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.agendar_cita)

        usuarioID = intent.getIntExtra("usuarioID", -1)
        citaID = intent.getIntExtra("citaID", -1)

        if (usuarioID == -1 || citaID == -1) {
            Toast.makeText(this, "Error: datos no recibidos", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        spinnerMascotas = findViewById(R.id.spinnerMascotas)
        editTextFecha = findViewById(R.id.editTextFecha)
        spinnerHora = findViewById(R.id.spinnerHora)
        editTextMotivo = findViewById(R.id.editTextMotivo)
        btnActualizar = findViewById(R.id.btnAgendarCita)

        btnCancelarCita = findViewById(R.id.btnCancelarCita)
        btnCancelarCita.visibility = View.VISIBLE


        val horarios = listOf("10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00")
        adapterHora = ArrayAdapter(this, android.R.layout.simple_spinner_item, horarios)
        adapterHora.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerHora.adapter = adapterHora

        editTextFecha.setOnClickListener { mostrarSelectorFecha() }

        cargarDatosCita()

        btnActualizar.setOnClickListener { actualizarCita() }
        btnCancelarCita.setOnClickListener { cancelarCita() }
    }

    private fun cancelarCita() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@ActualizarCitaActivity)
            withContext(Dispatchers.IO) {
                db.citaDao().cancelarCita(citaID, "Cancelado")
            }
            Toast.makeText(this@ActualizarCitaActivity, "Cita cancelada correctamente", Toast.LENGTH_LONG).show()
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun mostrarSelectorFecha() {
        val calendario = Calendar.getInstance()
        val datePicker = DatePickerDialog(this, { _, year, month, day ->
            val fechaSeleccionada = String.format("%04d-%02d-%02d", year, month + 1, day)
            editTextFecha.setText(fechaSeleccionada)
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH))

        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }

    private fun cargarDatosCita() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val cita = withContext(Dispatchers.IO) {
                db.citaDao().obtenerCitaPorID(citaID)
            }

            if (cita == null) {
                Toast.makeText(this@ActualizarCitaActivity, "Cita no encontrada", Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }

            val mascotas = db.mascotaDao().obtenerMascotasPorUsuario(usuarioID)
            val nombresMascotas = mascotas.map { it.nombreMascota }

            val adapter = ArrayAdapter(this@ActualizarCitaActivity, android.R.layout.simple_spinner_item, nombresMascotas)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerMascotas.adapter = adapter

            val index = mascotas.indexOfFirst { it.mascotaID == cita.mascotaID }
            if (index != -1) {
                spinnerMascotas.setSelection(index)
                mascotaIDSeleccionada = mascotas[index].mascotaID
            }

            spinnerMascotas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: android.view.View, position: Int, id: Long) {
                    mascotaIDSeleccionada = mascotas[position].mascotaID
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            editTextFecha.setText(cita.fecha)
            spinnerHora.setSelection(adapterHora.getPosition(cita.hora))
            editTextMotivo.setText(cita.motivoConsulta)
        }
    }

    private fun actualizarCita() {
        val fecha = editTextFecha.text.toString()
        val hora = spinnerHora.selectedItem?.toString() ?: ""
        val motivo = editTextMotivo.text.toString()

        if (mascotaIDSeleccionada == -1 || fecha.isBlank() || hora.isBlank() || motivo.isBlank()) {
            Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val sdfFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val fechaActual = sdfFecha.format(Date())
        val horaActual = sdfHora.format(Date())

        if (fecha == fechaActual && hora < horaActual) {
            Toast.makeText(this, "No puedes asignar una hora que ya pasó", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val citaExistente = db.citaDao().existenciaCitas2(fecha, hora, mascotaIDSeleccionada)
                if (citaExistente > 0) {
                    Toast.makeText(this@ActualizarCitaActivity, "Horario ocupado. Elige otra hora.", Toast.LENGTH_LONG).show()
                    return@launch
                }

                db.citaDao().actualizarCitaPorID(citaID, mascotaIDSeleccionada, fecha, hora, motivo)

                Toast.makeText(this@ActualizarCitaActivity, "Cita actualizada correctamente", Toast.LENGTH_LONG).show()
                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ActualizarCitaActivity, "Error al actualizar cita", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
