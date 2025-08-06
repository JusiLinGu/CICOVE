package com.example.cicove.DB

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "Mascota",
    foreignKeys = [ForeignKey(
        entity = Usuario::class,
        parentColumns = ["usuarioID"],
        childColumns = ["usuarioID"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Mascota(
    @PrimaryKey(autoGenerate = true) val mascotaID: Int = 0,
    val usuarioID: Int,
    val nombreMascota: String,
    val especie: String,
    val genero: String,
    val fechaNac: String, // Formato "YYYY-MM-DD"
    val foto: String? = null
)