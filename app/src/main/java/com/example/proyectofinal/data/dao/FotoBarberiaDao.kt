package com.example.proyectofinal.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.proyectofinal.data.model.FotoBarberiaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FotoBarberiaDao {

    @Insert
    suspend fun insertar(foto: FotoBarberiaEntity): Long

    @Query("SELECT * FROM foto_barberia ORDER BY id_foto")
    fun obtenerTodas(): Flow<List<FotoBarberiaEntity>>

    @Query("SELECT * FROM foto_barberia WHERE id_foto = :id")
    suspend fun obtenerPorId(id: Int): FotoBarberiaEntity?

    @Query("SELECT * FROM foto_barberia WHERE id_barberia = :idBarberia ORDER BY id_foto")
    fun deBarberia(idBarberia: Int): Flow<List<FotoBarberiaEntity>>

    @Update
    suspend fun actualizar(foto: FotoBarberiaEntity)

    @Delete
    suspend fun eliminar(foto: FotoBarberiaEntity)

    @Query("DELETE FROM foto_barberia")
    suspend fun eliminarTodas()
}
