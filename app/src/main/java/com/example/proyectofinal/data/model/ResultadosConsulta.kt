package com.example.proyectofinal.data.model

import androidx.room.ColumnInfo

/**
 * Resultados de consultas con JOIN: no son tablas, solo agrupan columnas de varias tablas
 * para que la pantalla reciba todo en un solo objeto.
 */

data class BarberiaResumen(
    @ColumnInfo(name = "id_barberia") val idBarberia: Int,
    val nombre: String,
    val direccion: String,
    val ciudad: String,
    val promedio: Double?          // AVG de resena.calificacion; NULL si no hay reseñas
)

data class ResenaConAutor(
    val autor: String,
    val calificacion: Int,
    val comentario: String?
)

data class CitaDetalle(
    @ColumnInfo(name = "id_cita") val idCita: Int,
    @ColumnInfo(name = "id_barberia") val idBarberia: Int,
    @ColumnInfo(name = "id_servicio") val idServicio: Int,
    @ColumnInfo(name = "id_barbero") val idBarbero: Int?,
    val fecha: String,
    @ColumnInfo(name = "hora_inicio") val horaInicio: String,
    val estado: String,
    @ColumnInfo(name = "valor_total") val valorTotal: Double,
    @ColumnInfo(name = "servicio_nombre") val servicioNombre: String,
    @ColumnInfo(name = "duracion_minutos") val duracionMinutos: Int,
    @ColumnInfo(name = "barbero_nombre") val barberoNombre: String?,
    @ColumnInfo(name = "barberia_nombre") val barberiaNombre: String,
    val direccion: String,
    val ciudad: String,
    val departamento: String
)
