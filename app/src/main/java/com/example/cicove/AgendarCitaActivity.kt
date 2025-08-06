package com.example.cicove

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cicove.DB.AppDatabase
import com.example.cicove.DB.Cita
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AgendarCitaActivity : AppCompatActivity() {

    private var usuarioID: Int = -1
    private var mascotaIDSeleccionada: Int = -1
    private lateinit var nombreMascotaSeleccionada: String

    private lateinit var spinnerMascotas: Spinner
    private lateinit var editTextFecha: EditText
    private lateinit var spinnerHora: Spinner
    private lateinit var editTextMotivo: EditText
    private lateinit var btnAgendar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.agendar_cita)

        usuarioID = intent.getIntExtra("usuarioID", -1)
        if (usuarioID == -1) {
            Toast.makeText(this, "Error: ID de usuario no recibido", Toast.LENGTH_LONG).show()
            finish()
        }

        spinnerMascotas = findViewById(R.id.spinnerMascotas)
        editTextFecha = findViewById(R.id.editTextFecha)
        spinnerHora = findViewById(R.id.spinnerHora)
        editTextMotivo = findViewById(R.id.editTextMotivo)
        btnAgendar = findViewById(R.id.btnAgendarCita)

        cargarMascotasDelUsuario()

        val horarios = listOf("10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00")
        val adapterHora = ArrayAdapter(this, android.R.layout.simple_spinner_item, horarios)
        adapterHora.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerHora.adapter = adapterHora

        editTextFecha.setOnClickListener {
            mostrarSelectorFecha()
        }

        btnAgendar.setOnClickListener {
            agendarCita()
        }
    }

    private fun cargarMascotasDelUsuario() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val mascotas = db.mascotaDao().obtenerMascotasPorUsuario(usuarioID)

            if (mascotas.isEmpty()) {
                runOnUiThread {
                    Toast.makeText(this@AgendarCitaActivity, "No tienes mascotas registradas", Toast.LENGTH_LONG).show()
                    finish()
                }
                return@launch
            }

            val nombresMascotas = mascotas.map { it.nombreMascota }
            runOnUiThread {
                val adapter = ArrayAdapter(this@AgendarCitaActivity, android.R.layout.simple_spinner_item, nombresMascotas)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerMascotas.adapter = adapter

                spinnerMascotas.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: android.view.View, position: Int, id: Long) {
                        mascotaIDSeleccionada = mascotas[position].mascotaID
                        nombreMascotaSeleccionada = mascotas[position].nombreMascota
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        mascotaIDSeleccionada = -1
                    }
                })
            }
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

    private fun agendarCita() {
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
            Toast.makeText(this, "No puedes agendar una hora que ya pasó", Toast.LENGTH_LONG).show()
            return
        }

        val nuevaCita = Cita(
            mascotaID = mascotaIDSeleccionada,
            fecha = fecha,
            hora = hora,
            motivoConsulta = motivo,
            estadoCita = "Pendiente",
            observaciones = null
        )

        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val citaDao = db.citaDao()

                val citaExistente = citaDao.existenciaCitas(fecha, hora)
                if (citaExistente > 0) {
                    runOnUiThread {
                        Toast.makeText(this@AgendarCitaActivity, "El horario ya está ocupado. Selecciona otra fecha y hora.", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                citaDao.insertarCita(nuevaCita)

                //Mostrar mensaje
                runOnUiThread {
                    Toast.makeText(this@AgendarCitaActivity, "Cita agendada correctamente", Toast.LENGTH_LONG).show()

                    //Redirigir automáticamente al intent de Google Calendar
                    abrirGoogleCalendar(nombreMascotaSeleccionada, fecha, hora)
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@AgendarCitaActivity, "Error al agendar cita", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //Funcion para abrir Google Calendar
    private fun abrirGoogleCalendar(nombreMascota: String, fecha: String, hora: String) {
        try {
            //Separar fecha y hora
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val startCalendar = Calendar.getInstance()
            startCalendar.time = sdf.parse("$fecha $hora") ?: return

            val endCalendar = startCalendar.clone() as Calendar
            endCalendar.add(Calendar.MINUTE, 30) // Duración estimada 30 mins

            //Crear Intent con los datos del evento
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = Uri.parse("content://com.android.calendar/events")
                putExtra("title", "Cita: $nombreMascota")
                putExtra("beginTime", startCalendar.timeInMillis)
                putExtra("endTime", endCalendar.timeInMillis)
                putExtra("allDay", false)
            }

            //Verificamos que pueda abrir Google Calendar
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                //De lo contrario, intentamos abrir la versión web de Google Calendar
                val urlWeb = Uri.parse(
                    "https://calendar.google.com/calendar/render?action=TEMPLATE" +
                            "&text=Cita:+$nombreMascota" +
                            "&dates=${formatoGoogleCalendar(startCalendar)}/${formatoGoogleCalendar(endCalendar)}"
                )
                val webIntent = Intent(Intent.ACTION_VIEW, urlWeb)
                startActivity(webIntent)
            }

            finish() //Cerramos esta pantalla para volver al menú automáticamente despues de regresar de Google Calendar

        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir Google Calendar", Toast.LENGTH_SHORT).show()
        }
    }

    //Formato de fecha que necesita Google Calendar en su URL
    private fun formatoGoogleCalendar(cal: Calendar): String {
        val formato = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
        formato.timeZone = TimeZone.getTimeZone("UTC")
        return formato.format(cal.time)
    }
}
