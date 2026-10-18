package com.example.proyectofinal.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.proyectofinal.data.model.BarberiaEntity
import com.example.proyectofinal.data.model.BarberiaResumen
import kotlinx.coroutines.flow.Flow

@Dao
interface BarberiaDao {

    @Insert
    suspend fun insertar(barberia: BarberiaEntity): Long

    @Query("SELECT * FROM barberia ORDER BY nombre")
    fun obtenerTodas(): Flow<List<BarberiaEntity>>

    @Query("SELECT * FROM barberia WHERE id_barberia = :id")
    suspend fun obtenerPorId(id: Int): BarberiaEntity?

    @Query("SELECT * FROM barberia WHERE id_ciudad = :idCiudad AND estado_aprobacion = 'APROBADA' ORDER BY nombre")
    fun deCiudad(idCiudad: Int): Flow<List<BarberiaEntity>>

    @Query("SELECT * FROM barberia WHERE id_propietario = :idUsuario ORDER BY nombre")
    fun dePropietario(idUsuario: Int): Flow<List<BarberiaEntity>>

    /** Para el administrador: PENDIENTE / APROBADA / RECHAZADA. */
    @Query("SELECT * FROM barberia WHERE estado_aprobacion = :estado ORDER BY fecha_registro")
    fun porEstado(estado: String): Flow<List<BarberiaEntity>>

    /** Lista de la pantalla de inicio: barberías aprobadas + ciudad + promedio de reseñas. */
    @Query(
        """
        SELECT b.id_barberia AS id_barberia,
               b.nombre      AS nombre,
               b.direccion   AS direccion,
               c.nombre      AS ciudad,
               (SELECT AVG(r.calificacion) FROM resena r WHERE r.id_barberia = b.id_barberia) AS promedio
        FROM barberia b
        JOIN ciudad c ON c.id_ciudad = b.id_ciudad
        WHERE b.estado_aprobacion = 'APROBADA'
        ORDER BY b.id_barberia
        """
    )
    fun aprobadas(): Flow<List<BarberiaResumen>>

    @Update
    suspend fun actualizar(barberia: BarberiaEntity)

    @Delete
    suspend fun eliminar(barberia: BarberiaEntity)

    @Query("DELETE FROM barberia")
    suspend fun eliminarTodas()
}
