package com.example.proyectofinal.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Fechas como texto "2026-10-01". estado: ACTIVA | POR_VENCER | VENCIDA. */
@Entity(
    tableName = "suscripcion",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_barbero"]
        )
    ],
    indices = [Index(value = ["id_barbero"])]
)
data class SuscripcionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_suscripcion") val idSuscripcion: Int = 0,
    @ColumnInfo(name = "id_barbero") val idBarbero: Int,
    @ColumnInfo(name = "fecha_inicio") val fechaInicio: String,
    @ColumnInfo(name = "fecha_vencimiento") val fechaVencimiento: String,
    val estado: String = EstadoSuscripcion.ACTIVA
)
