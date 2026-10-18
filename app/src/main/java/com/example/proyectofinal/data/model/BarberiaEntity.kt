package com.example.proyectofinal.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "barberia",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_propietario"]
        ),
        ForeignKey(
            entity = CiudadEntity::class,
            parentColumns = ["id_ciudad"],
            childColumns = ["id_ciudad"]
        )
    ],
    indices = [Index(value = ["id_propietario"]), Index(value = ["id_ciudad"])]
)
data class BarberiaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_barberia") val idBarberia: Int = 0,
    @ColumnInfo(name = "id_propietario") val idPropietario: Int,
    @ColumnInfo(name = "id_ciudad") val idCiudad: Int,
    val nombre: String,
    val direccion: String,
    val descripcion: String? = null,
    val telefono: String? = null,
    val foto: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    @ColumnInfo(name = "estado_aprobacion") val estadoAprobacion: String = "PENDIENTE",
    @ColumnInfo(name = "horas_limite_cancelacion") val horasLimiteCancelacion: Int = 2,
    @ColumnInfo(name = "fecha_registro") val fechaRegistro: Long = System.currentTimeMillis()
)
