package com.example.cicove

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cicove.DB.AppDatabase
import com.example.cicove.adapters.MascotaAdapter
import kotlinx.coroutines.launch

class PerfilMascotaActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mascotaAdapter: MascotaAdapter
    private var usuarioID: Int = -1  // Recibimos el usuarioID del intent
    private lateinit var btnEditarUsuario: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil_mascota)

        usuarioID = intent.getIntExtra("usuarioID", -1)
        btnEditarUsuario = findViewById(R.id.btnEditarUsuario)

        if (usuarioID == -1) {
            Toast.makeText(this, "Error al obtener el ID del usuario", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        recyclerView = findViewById(R.id.recyclerMascotas)
        recyclerView.layoutManager = LinearLayoutManager(this)


        mascotaAdapter = MascotaAdapter(emptyList()) { mascota ->
            val intent = Intent(this, ActualizarMascotaActivity::class.java)
            intent.putExtra("mascotaID", mascota.mascotaID)
            startActivity(intent)
        }

        recyclerView.adapter = mascotaAdapter

        // Cargar mascotas asociadas al usuarioID
        obtenerMascotasDelUsuario(usuarioID)

        btnEditarUsuario.setOnClickListener{
            editarUsuario()
        }
    }

    override fun onResume() {
        super.onResume()
        obtenerMascotasDelUsuario(usuarioID)// Recargar cada vez que se regrese a esta pantalla
    }

    private fun editarUsuario() {
        val i = Intent(this, PerfilUsuarioActivity::class.java)
        i.putExtra("usuarioID", usuarioID)
        startActivity(i)
    }

    private fun obtenerMascotasDelUsuario(usuarioID: Int) {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val mascotas = db.mascotaDao().obtenerMascotasPorUsuario(usuarioID)

                runOnUiThread {
                    mascotaAdapter.actualizarLista(mascotas)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@PerfilMascotaActivity, "Error al cargar mascotas", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
