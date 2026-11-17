package com.example.proyectofinal.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cita",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_cliente"]
        ),
        ForeignKey(
            entity = BarberiaEntity::class,
            parentColumns = ["id_barberia"],
            childColumns = ["id_barberia"]
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_barbero"]
        ),
        ForeignKey(
            entity = ServicioEntity::class,
            parentColumns = ["id_servicio"],
            childColumns = ["id_servicio"]
        )
    ],
    indices = [
        Index(value = ["id_cliente"]),
        Index(value = ["id_barberia"]),
        Index(value = ["id_barbero", "fecha", "hora_inicio"]),
        Index(value = ["id_servicio"])
    ]
)
data class CitaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_cita") val idCita: Int = 0,
    @ColumnInfo(name = "id_cliente") val idCliente: Int,
    @ColumnInfo(name = "id_barberia") val idBarberia: Int,
    @ColumnInfo(name = "id_barbero") val idBarbero: Int?,      // NULL = sin preferencia
    @ColumnInfo(name = "id_servicio") val idServicio: Int,
    val fecha: String,                                          // "2026-09-10"
    @ColumnInfo(name = "hora_inicio") val horaInicio: String,   // "10:30"
    @ColumnInfo(name = "hora_fin") val horaFin: String,         // "11:15"
    val estado: String = EstadoCitaDb.PENDIENTE,
    @ColumnInfo(name = "valor_total") val valorTotal: Double,
    @ColumnInfo(name = "fecha_creacion") val fechaCreacion: Long = System.currentTimeMillis()
)
