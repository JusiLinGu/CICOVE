package com.example.cicove

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cicove.DB.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PerfilUsuarioActivity : AppCompatActivity() {

    private var usuarioID: Int = -1
    private lateinit var etNombreUsuario: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etNumero: EditText
    private lateinit var btnGuardarCambios: Button
    private lateinit var imgFotoUsuario: ImageView

    private var nuevaFotoUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil_usuario)

        //Inicializar vistas
        etNombreUsuario = findViewById(R.id.etNombreUsuario)
        etCorreo = findViewById(R.id.etCorreo)
        etNumero = findViewById(R.id.etNumero)
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios)
        imgFotoUsuario = findViewById(R.id.imgFotoUsuario)

        //Obtener ID del usuario
        usuarioID = intent.getIntExtra("usuarioID", -1)

        cargarDatosUsuario()

        //Al hacer clic en la imagen, abrir selector de imagen
        imgFotoUsuario.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnGuardarCambios.setOnClickListener {
            guardarCambios()
        }
    }

    private fun cargarDatosUsuario() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@PerfilUsuarioActivity)
            val usuario = withContext(Dispatchers.IO) {
                db.usuarioDao().obtenerUsuarioPorID(usuarioID)
            }

            if (usuario != null) {
                etNombreUsuario.setText(usuario.nombreUsuario)
                etCorreo.setText(usuario.correo)
                etNumero.setText(usuario.celular)

                if (!usuario.foto.isNullOrEmpty()) {
                    try {
                        val uri = Uri.parse(usuario.foto)
                        val inputStream = contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        imgFotoUsuario.setImageBitmap(bitmap)
                        inputStream?.close()
                        nuevaFotoUri = uri
                    } catch (e: Exception) {
                        imgFotoUsuario.setImageResource(R.drawable.icon_foto)
                        Toast.makeText(this@PerfilUsuarioActivity, "No se pudo cargar la foto", Toast.LENGTH_SHORT).show()
                        Log.e("PerfilUsuario", "Error al cargar imagen: ${e.message}")
                    }
                } else {
                    imgFotoUsuario.setImageResource(R.drawable.icon_foto)
                }
            } else {
                Toast.makeText(this@PerfilUsuarioActivity, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                finish()
            }
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
                    imgFotoUsuario.setImageBitmap(bitmap)
                    inputStream?.close()

                    nuevaFotoUri = uri //Guardar la nueva URI para actualizar la foto

                } catch (e: Exception) {
                    Log.e("PerfilUsuario", "Error al seleccionar nueva imagen: ${e.message}")
                    Toast.makeText(this, "No se pudo cargar la nueva imagen", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun guardarCambios() {
        val nombre = etNombreUsuario.text.toString()
        val correo = etCorreo.text.toString()
        val numero = etNumero.text.toString()
        val foto = nuevaFotoUri?.toString()

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@PerfilUsuarioActivity)
            withContext(Dispatchers.IO) {
                db.usuarioDao().actualizarDatosUsuario(usuarioID, nombre, correo, numero, foto)
            }
            Toast.makeText(this@PerfilUsuarioActivity, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
