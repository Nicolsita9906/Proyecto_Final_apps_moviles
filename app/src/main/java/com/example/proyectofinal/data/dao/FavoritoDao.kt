package com.example.proyectofinal.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.proyectofinal.data.model.FavoritoEntity
import kotlinx.coroutines.flow.Flow

/** `favorito` tiene llave primaria compuesta (id_cliente, id_barberia): no hay @Update ni id propio. */
@Dao
interface FavoritoDao {

    // IGNORE: si ya es favorita no hace nada (no hay error ni se duplica)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(favorito: FavoritoEntity): Long

    @Query("SELECT * FROM favorito ORDER BY fecha DESC")
    fun obtenerTodos(): Flow<List<FavoritoEntity>>

    @Query("SELECT * FROM favorito WHERE id_cliente = :idCliente ORDER BY fecha DESC")
    fun deCliente(idCliente: Int): Flow<List<FavoritoEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorito WHERE id_cliente = :idCliente AND id_barberia = :idBarberia)")
    fun esFavorita(idCliente: Int, idBarberia: Int): Flow<Boolean>

    @Delete
    suspend fun eliminar(favorito: FavoritoEntity)

    @Query("DELETE FROM favorito WHERE id_cliente = :idCliente AND id_barberia = :idBarberia")
    suspend fun quitar(idCliente: Int, idBarberia: Int)

    @Query("DELETE FROM favorito")
    suspend fun eliminarTodos()
}
