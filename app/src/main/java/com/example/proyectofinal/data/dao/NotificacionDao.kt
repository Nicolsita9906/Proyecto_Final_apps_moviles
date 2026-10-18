package com.example.proyectofinal.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.proyectofinal.data.model.NotificacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificacionDao {

    @Insert
    suspend fun insertar(notificacion: NotificacionEntity): Long

    @Query("SELECT * FROM notificacion ORDER BY fecha DESC")
    fun obtenerTodas(): Flow<List<NotificacionEntity>>

    @Query("SELECT * FROM notificacion WHERE id_notificacion = :id")
    suspend fun obtenerPorId(id: Int): NotificacionEntity?

    @Query("SELECT * FROM notificacion WHERE id_usuario = :idUsuario ORDER BY fecha DESC")
    fun deUsuario(idUsuario: Int): Flow<List<NotificacionEntity>>

    /** Cantidad de no leídas (para el globito rojo de la campana). */
    @Query("SELECT COUNT(*) FROM notificacion WHERE id_usuario = :idUsuario AND leida = 0")
    fun noLeidas(idUsuario: Int): Flow<Int>

    @Query("UPDATE notificacion SET leida = 1 WHERE id_notificacion = :idNotificacion")
    suspend fun marcarLeida(idNotificacion: Int)

    @Query("UPDATE notificacion SET leida = 1 WHERE id_usuario = :idUsuario")
    suspend fun marcarTodasLeidas(idUsuario: Int)

    @Update
    suspend fun actualizar(notificacion: NotificacionEntity)

    @Delete
    suspend fun eliminar(notificacion: NotificacionEntity)

    @Query("DELETE FROM notificacion")
    suspend fun eliminarTodas()
}
