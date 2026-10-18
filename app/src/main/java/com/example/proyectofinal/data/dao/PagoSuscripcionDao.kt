package com.example.proyectofinal.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.proyectofinal.data.model.PagoSuscripcionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PagoSuscripcionDao {

    @Insert
    suspend fun insertar(pago: PagoSuscripcionEntity): Long

    @Query("SELECT * FROM pago_suscripcion ORDER BY fecha_pago DESC")
    fun obtenerTodos(): Flow<List<PagoSuscripcionEntity>>

    @Query("SELECT * FROM pago_suscripcion WHERE id_pago = :id")
    suspend fun obtenerPorId(id: Int): PagoSuscripcionEntity?

    @Query("SELECT * FROM pago_suscripcion WHERE id_suscripcion = :idSuscripcion ORDER BY fecha_pago DESC")
    fun deSuscripcion(idSuscripcion: Int): Flow<List<PagoSuscripcionEntity>>

    @Update
    suspend fun actualizar(pago: PagoSuscripcionEntity)

    @Delete
    suspend fun eliminar(pago: PagoSuscripcionEntity)

    @Query("DELETE FROM pago_suscripcion")
    suspend fun eliminarTodos()
}
