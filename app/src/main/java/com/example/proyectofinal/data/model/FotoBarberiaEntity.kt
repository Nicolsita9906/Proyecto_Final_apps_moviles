package com.example.proyectofinal.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "foto_barberia",
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
data class FotoBarberiaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_foto") val idFoto: Int = 0,
    @ColumnInfo(name = "id_barberia") val idBarberia: Int,
    val url: String
)
