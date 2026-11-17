package com.example.proyectofinal.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.proyectofinal.data.model.HorarioAtencionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HorarioAtencionDao {

    @Insert
    suspend fun insertar(horario: HorarioAtencionEntity): Long

    @Query("SELECT * FROM horario_atencion ORDER BY id_barberia, dia_semana")
    fun obtenerTodos(): Flow<List<HorarioAtencionEntity>>

    @Query("SELECT * FROM horario_atencion WHERE id_horario = :id")
    suspend fun obtenerPorId(id: Int): HorarioAtencionEntity?

    @Query("SELECT * FROM horario_atencion WHERE id_barberia = :idBarberia ORDER BY dia_semana")
    fun deBarberia(idBarberia: Int): Flow<List<HorarioAtencionEntity>>

    @Update
    suspend fun actualizar(horario: HorarioAtencionEntity)

    @Delete
    suspend fun eliminar(horario: HorarioAtencionEntity)

    @Query("DELETE FROM horario_atencion")
    suspend fun eliminarTodos()
}
