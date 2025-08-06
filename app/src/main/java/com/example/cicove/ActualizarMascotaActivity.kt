package com.example.cicove

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cicove.DB.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActualizarMascotaActivity : AppCompatActivity() {

    private var mascotaID: Int = -1  // Recibimos el mascotaID del intent
    private lateinit var etNombreMascota: EditText
    private lateinit var etEspecie: EditText
    private lateinit var etGenero: EditText
    private lateinit var btnGuardarCambios: Button
    private lateinit var imgFotoMascota: ImageView

    private var nuevaFotoUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actualizar_mascota)

        mascotaID = intent.getIntExtra("mascotaID", -1)
        /* Prueba para validar que recibimos el id de la mascota correctamente
        if (mascotaID == -1) {
            Toast.makeText(this, "Error: mascota no encontrada", Toast.LENGTH_SHORT).show()
            finish()
            return
        } */

        etNombreMascota = findViewById(R.id.etNombreMascota)
        etEspecie = findViewById(R.id.etEspecie)
        etGenero = findViewById(R.id.etGenero)
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios)
        imgFotoMascota = findViewById(R.id.imgFotoMascota)

        cargarDatosMascota(etNombreMascota, etEspecie, etGenero)

        // Al hacer clic en la imagen, abrir selector de imagen
        imgFotoMascota.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnGuardarCambios.setOnClickListener{
            guardarCambios()
        }
    }

    private fun cargarDatosMascota(etNombreMascota: EditText, etEspecie: EditText, etGenero: EditText) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@ActualizarMascotaActivity)
            val mascota = withContext(Dispatchers.IO) {
                db.mascotaDao().obtenerMascotaPorID(mascotaID)
            }
            if (mascota != null) {
                etNombreMascota.setText(mascota.nombreMascota)
                etEspecie.setText(mascota.especie)
                etGenero.setText(mascota.genero)
                if (!mascota.foto.isNullOrEmpty()) {
                    try {
                        val uri = Uri.parse(mascota.foto)
                        val inputStream = contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        imgFotoMascota.setImageBitmap(bitmap)
                        inputStream?.close()
                    } catch (e: Exception) {
                        //En caso de que no se pueda cargar la foto del usuario
                        imgFotoMascota.setImageResource(R.drawable.icon_foto)
                        Toast.makeText(this@ActualizarMascotaActivity, "No se pudo cargar la foto", Toast.LENGTH_SHORT).show()
                        Log.e("PerfilMascota", "Error al cargar imagen: ${e.message}")
                    }
                } else {
                    imgFotoMascota.setImageResource(R.drawable.icon_foto)
                }
            } else {
                Toast.makeText(this@ActualizarMascotaActivity, "Mascota no encontrada", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun guardarCambios() {
        val nombre = etNombreMascota.text.toString()
        val especie = etEspecie.text.toString()
        val genero = etGenero.text.toString()

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@ActualizarMascotaActivity)
            val mascotaDao = db.mascotaDao()

            // Si no hay nueva foto, conservar la foto actual
            val fotoUri: String? = withContext(Dispatchers.IO) {
                nuevaFotoUri?.toString() ?: mascotaDao.obtenerFotoPorID(mascotaID)
            }

            withContext(Dispatchers.IO) {
                mascotaDao.actualizarDatosMascota(mascotaID, nombre, especie, genero, fotoUri)
            }

            Toast.makeText(this@ActualizarMascotaActivity, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
            finish()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null) {
                try {
                    val takeFlags = data.flags and
                            (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    contentResolver.takePersistableUriPermission(uri, takeFlags)

                    val inputStream = contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    imgFotoMascota.setImageBitmap(bitmap)
                    inputStream?.close()

                    nuevaFotoUri = uri // Guardar la nueva URI para usarla al guardar cambios

                } catch (e: Exception) {
                    Log.e("PerfilUsuario", "Error al seleccionar nueva imagen: ${e.message}")
                    Toast.makeText(this, "No se pudo cargar la nueva imagen", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}