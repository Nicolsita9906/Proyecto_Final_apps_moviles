package com.example.proyectofinal.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Llave primaria compuesta: un cliente solo puede marcar una barbería una vez. */
@Entity(
    tableName = "favorito",
    primaryKeys = ["id_cliente", "id_barberia"],
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_cliente"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BarberiaEntity::class,
            parentColumns = ["id_barberia"],
            childColumns = ["id_barberia"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["id_barberia"])]
)
data class FavoritoEntity(
    @ColumnInfo(name = "id_cliente") val idCliente: Int,
    @ColumnInfo(name = "id_barberia") val idBarberia: Int,
    val fecha: Long = System.currentTimeMillis()
)
