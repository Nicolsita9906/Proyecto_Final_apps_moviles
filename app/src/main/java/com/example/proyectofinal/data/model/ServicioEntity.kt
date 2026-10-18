package com.example.proyectofinal.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "servicio",
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
data class ServicioEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_servicio") val idServicio: Int = 0,
    @ColumnInfo(name = "id_barberia") val idBarberia: Int,
    val nombre: String,
    val descripcion: String? = null,
    @ColumnInfo(name = "duracion_minutos") val duracionMinutos: Int,
    val precio: Double,
    val activo: Boolean = true
)
