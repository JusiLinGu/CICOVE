package com.example.cicove.DB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Usuario")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val usuarioID: Int = 0,
    val nombreUsuario: String,
    val correo: String,
    val celular: String,
    val foto: String? = null,  // Solo se almacenará la ruta
    val password: String,
    val rol: String // "veterinario" o "cliente"
) {
    //Copia para actualizar
    fun copy(
        nombreUsuario: String = this.nombreUsuario,
        correo: String = this.correo,
        celular: String = this.celular,
        fotoPath: String? = this.foto,
        password: String = this.password,
        rol: String = this.rol
    ) = Usuario(usuarioID, nombreUsuario, correo, celular, fotoPath, password, rol)
}