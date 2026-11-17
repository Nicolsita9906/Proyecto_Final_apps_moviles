package com.example.proyectofinal.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notificacion",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_usuario"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["id_usuario"])]
)
data class NotificacionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_notificacion") val idNotificacion: Int = 0,
    @ColumnInfo(name = "id_usuario") val idUsuario: Int,
    val tipo: String = TipoNotificacion.GENERAL,
    val mensaje: String,
    val leida: Boolean = false,
    val fecha: Long = System.currentTimeMillis()
)
