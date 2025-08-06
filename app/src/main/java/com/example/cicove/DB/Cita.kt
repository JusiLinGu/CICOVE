package com.example.cicove.DB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Citas")
data class Cita(
    @PrimaryKey(autoGenerate = true) val citaID: Int = 0,
    val mascotaID: Int,
    val fecha: String,         // formato YYYY-MM-DD
    val hora: String,          // formato HH:MM
    val motivoConsulta: String,
    val estadoCita: String = "Pendiente", //Se puede cambiar a completado o cancelado despues
    val observaciones: String? = null  //Campo sera llenado por veterinario
)
