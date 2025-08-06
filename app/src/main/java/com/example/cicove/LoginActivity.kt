package com.example.cicove

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.cicove.DB.AppDatabase
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    // Declaración de variables para los EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        /*Pedir permiso para notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1002
                )
            }
        }*/

        //Boton para ir a la interfaz de registro
        val goToRegister = findViewById<TextView>(R.id.textViewRegistrarse)
        goToRegister.setOnClickListener {
            goToRegister()
        }

        //Boton para ir a la interfaz de contraseña olvidada
        val goToNewPassword = findViewById<TextView>(R.id.passwordOlvidada)
        goToNewPassword.setOnClickListener {
            goToNewPassword()
        }

        //Inicializacion de los EditText
        etEmail = findViewById<EditText>(R.id.etEmail)
        etPassword = findViewById<EditText>(R.id.usuarioPassword)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            iniciarSesion()
        }
    }

    private fun goToRegister() {
        val i = Intent(this, NewAccountActivity::class.java)
        startActivity(i)
    }

    private fun goToNewPassword() {
        val i = Intent(this, RecoverPassword1Activity::class.java)
        startActivity(i)
    }

    private fun iniciarSesion() {
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Ingrese email y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@LoginActivity)
                val usuario = db.usuarioDao().obtenerPorCorreo(email)

                if (usuario != null && usuario.password == password) {
                    //Redirigir según rol
                    val intent = when (usuario.rol) {
                        "veterinario" -> Intent(this@LoginActivity, MenuColabActivity::class.java)
                        else -> Intent(this@LoginActivity, UsuarioMenuActivity::class.java)
                    }

                    //Mensaje para confirmar el rol asignado al usuario
                    runOnUiThread {
                        Toast.makeText(
                            this@LoginActivity,
                            "Bienvenido: ${usuario.rol}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    //Envio de correo y usuarioID
                    intent.putExtra("USER_EMAIL", usuario.correo)
                    intent.putExtra("usuarioID", usuario.usuarioID)
                    startActivity(intent)
                    finish()
                } else {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }
}