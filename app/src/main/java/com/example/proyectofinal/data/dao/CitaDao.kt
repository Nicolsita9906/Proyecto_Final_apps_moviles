package com.example.proyectofinal.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.proyectofinal.data.model.CitaDetalle
import com.example.proyectofinal.data.model.CitaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CitaDao {

    @Insert
    suspend fun insertar(cita: CitaEntity): Long

    @Query("SELECT * FROM cita ORDER BY fecha DESC, hora_inicio DESC")
    fun obtenerTodas(): Flow<List<CitaEntity>>

    @Query("SELECT * FROM cita WHERE id_cita = :id")
    suspend fun obtenerPorId(id: Int): CitaEntity?

    /** Citas de un cliente con los datos de servicio, barbero y barbería ya unidos. */
    @Query(
        """
        SELECT c.id_cita AS id_cita,
               c.id_barberia AS id_barberia,
               c.id_servicio AS id_servicio,
               c.id_barbero AS id_barbero,
               c.fecha AS fecha,
               c.hora_inicio AS hora_inicio,
               c.estado AS estado,
               c.valor_total AS valor_total,
               s.nombre AS servicio_nombre,
               s.duracion_minutos AS duracion_minutos,
               u.nombre AS barbero_nombre,
               b.nombre AS barberia_nombre,
               b.direccion AS direccion,
               ci.nombre AS ciudad,
               ci.departamento AS departamento
        FROM cita c
        JOIN servicio s  ON s.id_servicio  = c.id_servicio
        JOIN barberia b  ON b.id_barberia  = c.id_barberia
        JOIN ciudad ci   ON ci.id_ciudad   = b.id_ciudad
        LEFT JOIN usuario u ON u.id_usuario = c.id_barbero
        WHERE c.id_cliente = :idCliente
        ORDER BY c.fecha DESC, c.hora_inicio DESC
        """
    )
    fun detalleDeCliente(idCliente: Int): Flow<List<CitaDetalle>>

    /** Agenda de un barbero en un día (sin canceladas ni rechazadas). */
    @Query(
        """
        SELECT * FROM cita
        WHERE id_barbero = :idBarbero AND fecha = :fecha AND estado NOT IN ('CANCELADA', 'RECHAZADA')
        ORDER BY hora_inicio
        """
    )
    fun activasDeBarbero(idBarbero: Int, fecha: String): Flow<List<CitaEntity>>

    /** > 0 si el barbero ya tiene una cita activa a esa hora (RNF4 del script). */
    @Query(
        """
        SELECT COUNT(*) FROM cita
        WHERE id_barbero = :idBarbero AND fecha = :fecha AND hora_inicio = :horaInicio
          AND estado NOT IN ('CANCELADA', 'RECHAZADA')
        """
    )
    suspend fun contarConflictos(idBarbero: Int, fecha: String, horaInicio: String): Int

    @Query("UPDATE cita SET estado = :estado WHERE id_cita = :idCita")
    suspend fun cambiarEstado(idCita: Int, estado: String)

    @Query("UPDATE cita SET estado = 'CANCELADA' WHERE id_cita = :idCita")
    suspend fun cancelar(idCita: Int)

    @Query(
        """
        UPDATE cita
        SET id_servicio = :idServicio,
            id_barbero  = :idBarbero,
            fecha       = :fecha,
            hora_inicio = :horaInicio,
            hora_fin    = :horaFin,
            estado      = :estado,
            valor_total = :valorTotal
        WHERE id_cita = :idCita
        """
    )
    suspend fun modificar(
        idCita: Int,
        idServicio: Int,
        idBarbero: Int?,
        fecha: String,
        horaInicio: String,
        horaFin: String,
        estado: String,
        valorTotal: Double
    )

    @Update
    suspend fun actualizar(cita: CitaEntity)

    @Delete
    suspend fun eliminar(cita: CitaEntity)

    @Query("DELETE FROM cita")
    suspend fun eliminarTodas()
}
