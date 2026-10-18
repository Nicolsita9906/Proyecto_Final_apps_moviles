package com.example.proyectofinal.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** dia_semana: 1 = lunes ... 7 = domingo. Horas en formato "HH:mm". */
@Entity(
    tableName = "horario_atencion",
    foreignKeys = [
        ForeignKey(
            entity = BarberiaEntity::class,
            parentColumns = ["id_barberia"],
            childColumns = ["id_barberia"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["id_barberia", "dia_semana"], unique = true)]
)
data class HorarioAtencionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_horario") val idHorario: Int = 0,
    @ColumnInfo(name = "id_barberia") val idBarberia: Int,
    @ColumnInfo(name = "dia_semana") val diaSemana: Int,
    @ColumnInfo(name = "hora_apertura") val horaApertura: String? = null,
    @ColumnInfo(name = "hora_cierre") val horaCierre: String? = null,
    val cerrado: Boolean = false
)
