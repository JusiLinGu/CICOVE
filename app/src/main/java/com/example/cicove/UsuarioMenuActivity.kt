package com.example.cicove

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class UsuarioMenuActivity : AppCompatActivity(){

    private var usuarioID: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.usuario_menu)
        usuarioID = intent.getIntExtra("usuarioID", -1)

        //Boton para ir a la interfaz de registro de mascota
        val registrarMascota = findViewById<ImageView>(R.id.registrarMascota)
        registrarMascota.setOnClickListener {
            registrarMascota()
        }

        //Boton para ir a la interfaz del perfil de mascotas
        val readMascotas = findViewById<ImageView>(R.id.mascotaPerfil)
        readMascotas.setOnClickListener {
            readMascotas()
        }

        //Boton para ir a la interfaz dpara agendar citas
        val agendarCita = findViewById<ImageView>(R.id.citaInterfaz)
        agendarCita.setOnClickListener {
            agendarCita()
        }

        //Boton para ir a la interfaz para consultar historial de citas
        val historialMascota = findViewById<ImageView>(R.id.historialVeterinario)
        historialMascota.setOnClickListener {
            historialMascota()
        }
    }

    private fun historialMascota() {
        val i = Intent(this, HistorialCitasActivity::class.java)
        i.putExtra("usuarioID", usuarioID)
        startActivity(i)
    }

    private fun registrarMascota() {
        val i = Intent(this, RegistrarMascotaActivity::class.java)
        intent.putExtra("soloEditarPendientes", true)
        //Al cambiar de interfaz para registrar mascotas mandamos el usuarioID al que se asociara
        i.putExtra("usuarioID", usuarioID)
        startActivity(i)
    }

    private fun readMascotas() {
        val i = Intent(this, PerfilMascotaActivity::class.java)
        i.putExtra("usuarioID", usuarioID)
        startActivity(i)
    }

    private fun agendarCita() {
        val i = Intent(this, AgendarCitaActivity::class.java)
        //Al cambiar de interfaz para agendar cita mandamos el usuarioID al que se asociara
        i.putExtra("usuarioID", usuarioID)
        startActivity(i)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}