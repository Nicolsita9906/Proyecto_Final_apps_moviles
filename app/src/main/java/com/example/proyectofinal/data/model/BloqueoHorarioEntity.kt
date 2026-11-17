package com.example.proyectofinal.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Bloqueos por descansos, festivos o ausencias (RF11). Fechas en milisegundos. */
@Entity(
    tableName = "bloqueo_horario",
    foreignKeys = [
        ForeignKey(
            entity = BarberiaEntity::class,
            parentColumns = ["id_barberia"],
            childColumns = ["id_barberia"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["id_barberia"])]
)
data class BloqueoHorarioEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_bloqueo") val idBloqueo: Int = 0,
    @ColumnInfo(name = "id_barberia") val idBarberia: Int,
    @ColumnInfo(name = "fecha_inicio") val fechaInicio: Long,
    @ColumnInfo(name = "fecha_fin") val fechaFin: Long,
    val motivo: String? = null
)
