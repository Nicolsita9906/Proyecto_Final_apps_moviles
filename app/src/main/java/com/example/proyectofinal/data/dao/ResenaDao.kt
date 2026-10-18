package com.example.proyectofinal.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.proyectofinal.data.model.ResenaConAutor
import com.example.proyectofinal.data.model.ResenaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResenaDao {

    @Insert
    suspend fun insertar(resena: ResenaEntity): Long

    @Query("SELECT * FROM resena ORDER BY fecha DESC")
    fun obtenerTodas(): Flow<List<ResenaEntity>>

    @Query("SELECT * FROM resena WHERE id_resena = :id")
    suspend fun obtenerPorId(id: Int): ResenaEntity?

    /** Reseñas de una barbería con el nombre de quien la escribió. */
    @Query(
        """
        SELECT u.nombre AS autor, r.calificacion AS calificacion, r.comentario AS comentario
        FROM resena r
        JOIN usuario u ON u.id_usuario = r.id_cliente
        WHERE r.id_barberia = :idBarberia
        ORDER BY r.fecha DESC
        """
    )
    fun deBarberia(idBarberia: Int): Flow<List<ResenaConAutor>>

    @Query("SELECT AVG(calificacion) FROM resena WHERE id_barberia = :idBarberia")
    fun promedioDeBarberia(idBarberia: Int): Flow<Double?>

    @Update
    suspend fun actualizar(resena: ResenaEntity)

    @Delete
    suspend fun eliminar(resena: ResenaEntity)

    @Query("DELETE FROM resena")
    suspend fun eliminarTodas()
}
