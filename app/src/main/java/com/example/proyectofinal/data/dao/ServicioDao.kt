package com.example.proyectofinal.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.proyectofinal.data.model.ServicioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServicioDao {

    @Insert
    suspend fun insertar(servicio: ServicioEntity): Long

    @Query("SELECT * FROM servicio ORDER BY id_barberia, id_servicio")
    fun obtenerTodos(): Flow<List<ServicioEntity>>

    @Query("SELECT * FROM servicio WHERE id_servicio = :id")
    suspend fun obtenerPorId(id: Int): ServicioEntity?

    /** Servicios ACTIVOS de una barbería (lo que ve el cliente). */
    @Query("SELECT * FROM servicio WHERE id_barberia = :idBarberia AND activo = 1 ORDER BY id_servicio")
    fun deBarberia(idBarberia: Int): Flow<List<ServicioEntity>>

    /** Todos los servicios (activos e inactivos): para el barbero que los administra. */
    @Query("SELECT * FROM servicio WHERE id_barberia = :idBarberia ORDER BY id_servicio")
    fun todosDeBarberia(idBarberia: Int): Flow<List<ServicioEntity>>

    @Update
    suspend fun actualizar(servicio: ServicioEntity)

    @Delete
    suspend fun eliminar(servicio: ServicioEntity)

    @Query("DELETE FROM servicio")
    suspend fun eliminarTodos()
}
