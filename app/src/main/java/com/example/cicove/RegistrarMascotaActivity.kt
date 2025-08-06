package com.example.cicove

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cicove.DB.AppDatabase
import com.example.cicove.DB.Mascota
import kotlinx.coroutines.launch

class RegistrarMascotaActivity : AppCompatActivity(){

    private lateinit var nombreMascota: EditText
    private lateinit var especieMascota: EditText
    private lateinit var generoMascota: EditText
    private lateinit var edadMascota: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var btnSubirFoto: Button
    private var imagenUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    private var usuarioID: Int = -1  // Recibimos el usuarioID del intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registrar_mascota)

        // Recibir ID del usuario logueado
        usuarioID = intent.getIntExtra("usuarioID", -1)

        /* Bloque de codigo solo para prueba de que se recibio correctamente el ID
        if (usuarioID == -1) {
            Toast.makeText(this, "Error al obtener el ID del usuario", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "ID recibido: $usuarioID", Toast.LENGTH_SHORT).show()
        }*/

        nombreMascota = findViewById(R.id.nombreMascota)
        especieMascota = findViewById(R.id.especieMascota)
        generoMascota = findViewById(R.id.generoMascota)
        edadMascota = findViewById(R.id.edadMascota)
        btnRegistrar = findViewById(R.id.registrarMascota)
        btnSubirFoto = findViewById(R.id.btnSubirFoto)

        btnSubirFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnRegistrar.setOnClickListener {
            registrarMascota()
        }
    }

    private fun registrarMascota() {
        val nombre = nombreMascota.text.toString().trim()
        val especie = especieMascota.text.toString().trim()
        val genero = generoMascota.text.toString().trim()
        val edad = edadMascota.text.toString().trim()

        if (nombre.isEmpty() || especie.isEmpty() || genero.isEmpty() || edad.isEmpty() || imagenUri == null) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        //Validamos que el usuario ingreso un numero
        if (edad.isEmpty() || !edad.all { it.isDigit() }) {
            edadMascota.error = "Ingrese una edad válida"
            return
        }

        //Se calcula la fecha de nacimiento en base a la edad ingresada
        val fechaNac = calcularFechaNacimiento(edad.toInt())

        lifecycleScope.launch {
            val mascota = Mascota(
                usuarioID = usuarioID,
                nombreMascota = nombre,
                especie = especie,
                genero = genero,
                fechaNac = fechaNac,
                foto = imagenUri?.toString()
            )

            val db = AppDatabase.getDatabase(applicationContext)
            db.mascotaDao().insertarMascota(mascota)

            runOnUiThread {
                Toast.makeText(this@RegistrarMascotaActivity, "Mascota registrada", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        
    }

    private fun calcularFechaNacimiento(edad: Int): String {
        val anioActual = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val anioNacimiento = anioActual - edad
        return "$anioNacimiento-01-01"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imagenUri = data.data

            try {
                imagenUri?.let { uri ->
                    val takeFlags = data.flags and
                            (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    contentResolver.takePersistableUriPermission(uri, takeFlags)
                }
            } catch (e: Exception) {
                Log.e("NewAccountActivity", "No se pudo obtener permiso persistente: ${e.message}")
            }
        }
    }
}