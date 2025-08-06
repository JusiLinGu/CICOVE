package com.example.cicove

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cicove.DB.AppDatabase
import kotlinx.coroutines.launch

class RecoverPassword2Activity : AppCompatActivity(){

    private lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recover_password2)

        //Obtener el correo electrónico del Intent anterior
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: run {
            Toast.makeText(this, "Error: correo no recibido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val etNewPassword = findViewById<EditText>(R.id.newPassword)
        val btnCambiar = findViewById<Button>(R.id.cambiarPassword)

        btnCambiar.setOnClickListener {
            val newPassword = etNewPassword.text.toString()

            if (newPassword.length < 6) {
                etNewPassword.error = "La contraseña debe tener al menos 6 caracteres"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getDatabase(applicationContext)
                    val dao = db.usuarioDao()

                    dao.actualizarPassword(userEmail, newPassword)

                    runOnUiThread {
                        Toast.makeText(this@RecoverPassword2Activity, "Contraseña actualizada correctamente", Toast.LENGTH_LONG).show()
                        finish() // cerrar pantalla
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this@RecoverPassword2Activity, "Error al actualizar: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    }
            }
        }

    }
}