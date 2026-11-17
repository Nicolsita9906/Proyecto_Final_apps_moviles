package com.example.proyectofinal.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pago_suscripcion",
    foreignKeys = [
        ForeignKey(
            entity = SuscripcionEntity::class,
            parentColumns = ["id_suscripcion"],
            childColumns = ["id_suscripcion"]
        )
    ],
    indices = [Index(value = ["id_suscripcion"])]
)
data class PagoSuscripcionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_pago") val idPago: Int = 0,
    @ColumnInfo(name = "id_suscripcion") val idSuscripcion: Int,
    val monto: Double,
    @ColumnInfo(name = "fecha_pago") val fechaPago: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "metodo_pago") val metodoPago: String? = null,
    val referencia: String? = null
)
