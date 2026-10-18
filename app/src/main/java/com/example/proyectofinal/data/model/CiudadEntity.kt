package com.example.proyectofinal.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ciudad",
    indices = [Index(value = ["nombre", "departamento"], unique = true)]
)
data class CiudadEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_ciudad") val idCiudad: Int = 0,
    val nombre: String,
    val departamento: String
)
