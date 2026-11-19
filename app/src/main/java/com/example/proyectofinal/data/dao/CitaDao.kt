package com.example.proyectofinal.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.proyectofinal.data.model.CitaDetalle
import com.example.proyectofinal.data.model.CitaEntity
import com.example.proyectofinal.data.model.CitaRecibida
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

    /** Citas activas SIN barbero asignado ("Sin preferencia") de una barbería en un día. */
    @Query(
        """
        SELECT * FROM cita
        WHERE id_barbero IS NULL AND id_barberia = :idBarberia AND fecha = :fecha
          AND estado NOT IN ('CANCELADA', 'RECHAZADA')
        ORDER BY hora_inicio
        """
    )
    fun activasSinBarbero(idBarberia: Int, fecha: String): Flow<List<CitaEntity>>

    /**
     * > 0 si el barbero ya tiene una cita activa que se CRUZA con [horaInicio, horaFin) (RNF4).
     * Las horas son "HH:mm", así que se pueden comparar como texto.
     * excluir = id de la cita que se está modificando (para que no choque consigo misma).
     */
    @Query(
        """
        SELECT COUNT(*) FROM cita
        WHERE id_barbero = :idBarbero AND fecha = :fecha
          AND estado NOT IN ('CANCELADA', 'RECHAZADA')
          AND hora_inicio < :horaFin AND hora_fin > :horaInicio
          AND id_cita != :excluir
        """
    )
    suspend fun contarCruces(idBarbero: Int, fecha: String, horaInicio: String, horaFin: String, excluir: Int): Int

    /** Igual que contarCruces, pero entre las citas "Sin preferencia" de la barbería. */
    @Query(
        """
        SELECT COUNT(*) FROM cita
        WHERE id_barbero IS NULL AND id_barberia = :idBarberia AND fecha = :fecha
          AND estado NOT IN ('CANCELADA', 'RECHAZADA')
          AND hora_inicio < :horaFin AND hora_fin > :horaInicio
          AND id_cita != :excluir
        """
    )
    suspend fun contarCrucesSinBarbero(idBarberia: Int, fecha: String, horaInicio: String, horaFin: String, excluir: Int): Int

    /**
     * Citas que recibe un barbero: las asignadas a él y las "Sin preferencia"
     * de las barberías de las que es propietario. Con nombre del cliente y del servicio.
     */
    @Query(
        """
        SELECT c.id_cita AS id_cita,
               c.id_cliente AS id_cliente,
               c.id_barbero AS id_barbero,
               c.id_barberia AS id_barberia,
               c.fecha AS fecha,
               c.hora_inicio AS hora_inicio,
               c.hora_fin AS hora_fin,
               c.estado AS estado,
               c.valor_total AS valor_total,
               s.nombre AS servicio_nombre,
               u.nombre AS cliente_nombre,
               b.nombre AS barberia_nombre
        FROM cita c
        JOIN servicio s ON s.id_servicio = c.id_servicio
        JOIN usuario u  ON u.id_usuario  = c.id_cliente
        JOIN barberia b ON b.id_barberia = c.id_barberia
        WHERE c.id_barbero = :idBarbero OR (c.id_barbero IS NULL AND b.id_propietario = :idBarbero)
        ORDER BY c.fecha, c.hora_inicio
        """
    )
    fun recibidasPorBarbero(idBarbero: Int): Flow<List<CitaRecibida>>

    /** Acepta/rechaza una cita y deja asignado al barbero que la atendió. */
    @Query("UPDATE cita SET estado = :estado, id_barbero = :idBarbero WHERE id_cita = :idCita")
    suspend fun cambiarEstadoYAsignar(idCita: Int, estado: String, idBarbero: Int)

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

    /**
     * El cliente borra una cita de su historial. Solo se borra si es suya y ya no está activa
     * (CANCELADA o RECHAZADA). Las COMPLETADAS no se borran: el barbero las usa en sus ingresos.
     * Devuelve cuántas filas se borraron (0 = no se pudo).
     */
    @Query(
        """
        DELETE FROM cita
        WHERE id_cita = :idCita AND id_cliente = :idCliente
          AND estado IN ('CANCELADA', 'RECHAZADA')
        """
    )
    suspend fun eliminarDeHistorial(idCita: Int, idCliente: Int): Int

    @Query("DELETE FROM cita")
    suspend fun eliminarTodas()
}
