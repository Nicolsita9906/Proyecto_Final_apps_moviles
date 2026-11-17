package com.example.proyectofinal.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.proyectofinal.data.model.BloqueoHorarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BloqueoHorarioDao {

    @Insert
    suspend fun insertar(bloqueo: BloqueoHorarioEntity): Long

    @Query("SELECT * FROM bloqueo_horario ORDER BY fecha_inicio")
    fun obtenerTodos(): Flow<List<BloqueoHorarioEntity>>

    @Query("SELECT * FROM bloqueo_horario WHERE id_bloqueo = :id")
    suspend fun obtenerPorId(id: Int): BloqueoHorarioEntity?

    @Query("SELECT * FROM bloqueo_horario WHERE id_barberia = :idBarberia ORDER BY fecha_inicio")
    fun deBarberia(idBarberia: Int): Flow<List<BloqueoHorarioEntity>>

    /** Bloqueos que se cruzan con el rango [desde, hasta) (milisegundos). */
    @Query(
        """
        SELECT * FROM bloqueo_horario
        WHERE id_barberia = :idBarberia AND fecha_inicio < :hasta AND fecha_fin > :desde
        ORDER BY fecha_inicio
        """
    )
    suspend fun enRango(idBarberia: Int, desde: Long, hasta: Long): List<BloqueoHorarioEntity>

    @Update
    suspend fun actualizar(bloqueo: BloqueoHorarioEntity)

    @Delete
    suspend fun eliminar(bloqueo: BloqueoHorarioEntity)

    @Query("DELETE FROM bloqueo_horario")
    suspend fun eliminarTodos()
}
