package com.example.proyectofinal.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuario",
    indices = [Index(value = ["correo"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_usuario") val idUsuario: Int = 0,
    val nombre: String,
    val correo: String,
    val telefono: String? = null,
    @ColumnInfo(name = "contrasena_hash") val contrasenaHash: String,
    val foto: String? = null,
    val rol: String,                       // CLIENTE | BARBERO | ADMINISTRADOR
    val estado: String = "ACTIVO",         // ACTIVO | INACTIVO
    @ColumnInfo(name = "fecha_registro") val fechaRegistro: Long = System.currentTimeMillis()
)
