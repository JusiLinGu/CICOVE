package com.example.cicove.DB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MascotaDao {

    @Insert
    suspend fun insertarMascota(mascota: Mascota)

    @Query("SELECT * FROM Mascota WHERE usuarioID = :usuarioID")
    suspend fun obtenerMascotasPorUsuario(usuarioID: Int): List<Mascota>

    @Query("SELECT * FROM Mascota WHERE mascotaID = :id")
    suspend fun obtenerMascotaPorID(id: Int): Mascota?

    @Query("UPDATE Mascota SET nombreMascota = :nombre, especie = :especie, genero = :genero, foto = :fotoUri WHERE mascotaID = :id")
    suspend fun actualizarDatosMascota(id: Int, nombre: String, especie: String, genero: String, fotoUri: String?)

    @Query("SELECT foto FROM mascota WHERE mascotaID = :id")
    suspend fun obtenerFotoPorID(id: Int): String?

}
