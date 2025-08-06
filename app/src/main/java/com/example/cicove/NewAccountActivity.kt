package com.example.cicove

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cicove.DB.AppDatabase
import com.example.cicove.DB.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.net.Uri
import android.provider.MediaStore

class NewAccountActivity : AppCompatActivity() {

    private lateinit var etNombreCompleto: EditText
    private lateinit var etEmail: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText

    private lateinit var btnSubirFoto: Button
    private var imagenUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_account)

        etNombreCompleto = findViewById(R.id.nombreCompleto)
        etEmail = findViewById(R.id.emailAddress)
        etTelefono = findViewById(R.id.editPhone)
        etPassword = findViewById(R.id.userPassword)
        etConfirmPassword = findViewById(R.id.confirmarPassword)
        btnSubirFoto = findViewById(R.id.btnSubirFoto)

        val btnRegistrar = findViewById<Button>(R.id.registrarUsuario)
        btnRegistrar.setOnClickListener {
            registrarUsuario()
        }

        btnSubirFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
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

    private fun registrarUsuario() {
        val nombre = etNombreCompleto.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        when {
            nombre.isEmpty() -> {
                etNombreCompleto.error = "Ingrese su nombre completo"
                return
            }
            email.isEmpty() -> {
                etEmail.error = "Ingrese su correo electrónico"
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                etEmail.error = "Correo electrónico no válido"
                return
            }
            telefono.isEmpty() -> {
                etTelefono.error = "Ingrese su teléfono"
                return
            }
            password.isEmpty() -> {
                etPassword.error = "Ingrese una contraseña"
                return
            }
            password.length < 6 -> {
                etPassword.error = "La contraseña debe tener al menos 6 caracteres"
                return
            }
            confirmPassword.isEmpty() -> {
                etConfirmPassword.error = "Confirme su contraseña"
                return
            }
            password != confirmPassword -> {
                etConfirmPassword.error = "Las contraseñas no coinciden"
                return
            }
            imagenUri == null -> {
                Toast.makeText(this, "Debe seleccionar una foto", Toast.LENGTH_SHORT).show()
                return
            }
            else -> {
                val rol = if (email.endsWith("@veterinaria.angel.com")) "veterinario" else "cliente"

                Toast.makeText(this, "Validaciones completas", Toast.LENGTH_SHORT).show()

                lifecycleScope.launch {
                    try {
                        val db = AppDatabase.getDatabase(this@NewAccountActivity)
                        val usuario = Usuario(
                            nombreUsuario = nombre,
                            correo = email,
                            celular = telefono,
                            foto = imagenUri?.toString(),
                            password = password,
                            rol = rol
                        )

                        withContext(Dispatchers.IO) {
                            val existe = db.usuarioDao().obtenerPorCorreo(email)
                            if (existe == null) {
                                db.usuarioDao().insertarUsuario(usuario)
                                runOnUiThread {
                                    Toast.makeText(this@NewAccountActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@NewAccountActivity, LoginActivity::class.java))
                                    finish()
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@NewAccountActivity, "El correo ya existe", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(
                                this@NewAccountActivity,
                                "Error: ${e.message ?: "Falló el registro"}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        Log.e("REGISTRO", "Error completo:", e)
                    }
                }
            }
        }
    }
}
