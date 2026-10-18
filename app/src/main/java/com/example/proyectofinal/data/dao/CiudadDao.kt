package com.example.proyectofinal.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.proyectofinal.data.model.CiudadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CiudadDao {

    @Insert
    suspend fun insertar(ciudad: CiudadEntity): Long

    @Query("SELECT * FROM ciudad ORDER BY nombre")
    fun obtenerTodas(): Flow<List<CiudadEntity>>

    @Query("SELECT * FROM ciudad WHERE id_ciudad = :id")
    suspend fun obtenerPorId(id: Int): CiudadEntity?

    @Query("SELECT * FROM ciudad WHERE nombre = :nombre COLLATE NOCASE LIMIT 1")
    suspend fun buscarPorNombre(nombre: String): CiudadEntity?

    @Update
    suspend fun actualizar(ciudad: CiudadEntity)

    @Delete
    suspend fun eliminar(ciudad: CiudadEntity)

    @Query("DELETE FROM ciudad")
    suspend fun eliminarTodas()
}
