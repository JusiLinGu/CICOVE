package com.example.cicove.DB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

data class CitaConMascota(
    val citaID: Int,
    val mascotaID: Int,
    val nombreMascota: String,
    val fecha: String,
    val hora: String,
    val motivoConsulta: String,
    val estadoCita: String,
    val observaciones: String?,
    val foto: String?
)

@Dao
interface CitaDao {

    @Insert
    suspend fun insertarCita(cita: Cita)

    //Para saber si hay citas disponibles en la fecha y hora seleccionada por el usuario
    @Query("SELECT COUNT(*) FROM Citas WHERE fecha = :fecha AND hora = :hora AND estadoCita = 'Pendiente'")
    suspend fun existenciaCitas(fecha: String, hora: String): Int

    //Nos ayudara a validar que la cita a actualizar no pertenezca a la mascota que desea actualizar su cita
    //De lo contrario se le arroja un mensaje al usuario indicandole que debe elegir otra fecha u horario
    @Query("SELECT COUNT(*) FROM Citas WHERE fecha = :fecha AND hora = :hora AND estadoCita = 'Pendiente' AND mascotaID != :id")
    suspend fun existenciaCitas2(fecha: String, hora: String, id: Int): Int

    //Consulta de citas de una mascota específica de un usuario
    @Query("""
        SELECT c.citaID, c.mascotaID, m.nombreMascota, c.fecha, c.hora, 
               c.motivoConsulta, c.estadoCita, c.observaciones, m.foto
        FROM   Citas c 
        INNER JOIN Mascota m ON c.mascotaID = m.mascotaID
        WHERE  m.usuarioID = :usuarioID AND m.mascotaID = :mascotaID
        ORDER BY c.fecha DESC, c.hora ASC
    """)
    suspend fun obtenerCitasPorMascota(usuarioID: Int, mascotaID: Int): List<CitaConMascota>

    //Consulta de citas del día actual
    @Query("""
        SELECT c.citaID, c.mascotaID, m.nombreMascota, c.fecha, c.hora, 
               c.motivoConsulta, c.estadoCita, c.observaciones, m.foto
        FROM   Citas c 
        INNER JOIN Mascota m ON c.mascotaID = m.mascotaID
        WHERE  c.fecha = :fechaActual AND c.estadoCita != 'Cancelado'
        ORDER BY c.hora ASC
    """)
    suspend fun obtenerCitasDelDia(fechaActual: String): List<CitaConMascota>

    //Consulta para historial completo de una mascota
    @Query("""
        SELECT c.citaID, c.mascotaID, m.nombreMascota, c.fecha, c.hora, 
               c.motivoConsulta, c.estadoCita, c.observaciones, m.foto
        FROM   Citas c 
        INNER JOIN Mascota m ON c.mascotaID = m.mascotaID
        WHERE  c.mascotaID = :mascotaID
        ORDER BY c.fecha DESC, c.hora ASC
    """)
    suspend fun obtenerHistorialPorMascota(mascotaID: Int): List<CitaConMascota>

    //El veterinario actualiza el estado de la cita y da un diagnostico u observacion
    @Query("UPDATE Citas SET estadoCita = :estado, observaciones = :observaciones WHERE citaID = :citaID")
    suspend fun actualizarDiagnostico(citaID: Int, estado: String, observaciones: String)

    @Query("SELECT * FROM Citas WHERE citaID = :id LIMIT 1")
    suspend fun obtenerCitaPorID(id: Int): Cita?

    @Query("""
    UPDATE Citas SET 
        mascotaID = :mascotaID, 
        fecha = :fecha, 
        hora = :hora, 
        motivoConsulta = :motivo 
    WHERE citaID = :id
""")
    suspend fun actualizarCitaPorID(id: Int, mascotaID: Int, fecha: String, hora: String, motivo: String)

    @Query("UPDATE Citas SET estadoCita = :estadoCita WHERE citaID = :id")
    suspend fun cancelarCita(id: Int, estadoCita: String)
}
