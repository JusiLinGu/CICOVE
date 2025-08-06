package com.example.cicove

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cicove.DB.AppDatabase
import kotlinx.coroutines.launch

class RecoverPassword1Activity  : AppCompatActivity() {

    private lateinit var etEmail: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recover_password1)

        etEmail = findViewById<EditText>(R.id.etEmail)

        val buscarCorreo = findViewById<TextView>(R.id.buscarCorreo)
        buscarCorreo.setOnClickListener {
            buscarUsuario()
        }
    }

    private fun buscarUsuario() {
        val email = etEmail.text.toString()

        if (email.isEmpty()) {
            Toast.makeText(this, "Ingrese un email valido porfavor", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@RecoverPassword1Activity)
                val usuario = db.usuarioDao().obtenerPorCorreo(email)

                if (usuario != null && usuario.correo == email) {
                    Toast.makeText(
                        this@RecoverPassword1Activity,
                        "Usuario encontrado: ${usuario.nombreUsuario}",
                        Toast.LENGTH_SHORT
                    ).show()
                    goToNewPassword2(email)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@RecoverPassword1Activity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun goToNewPassword2(email: String) {
        val intent = Intent(this, RecoverPassword2Activity::class.java).apply {
            putExtra("USER_EMAIL", email)
        }
        startActivity(intent)
        finish()
    }
}