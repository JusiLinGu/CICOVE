package com.example.cicove.DB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertarUsuario(usuario: Usuario)

    @Query("SELECT * FROM Usuario WHERE correo = :correo LIMIT 1")
    suspend fun obtenerPorCorreo(correo: String): Usuario?

    @Query("UPDATE Usuario SET password = :nuevaPassword WHERE correo = :correo")
    suspend fun actualizarPassword(correo: String, nuevaPassword: String)

    @Query("SELECT * FROM Usuario WHERE usuarioID = :id")
    suspend fun obtenerUsuarioPorID(id: Int): Usuario?

    @Query("""UPDATE Usuario SET nombreUsuario = :nombre, correo = :correo, celular = :numero, foto = :fotoUri WHERE usuarioID = :id""")
    suspend fun actualizarDatosUsuario(id: Int, nombre: String, correo: String, numero: String, fotoUri: String?)

}