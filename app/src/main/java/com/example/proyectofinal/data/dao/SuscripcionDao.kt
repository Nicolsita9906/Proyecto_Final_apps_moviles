package com.example.proyectofinal.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.proyectofinal.data.model.SuscripcionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SuscripcionDao {

    @Insert
    suspend fun insertar(suscripcion: SuscripcionEntity): Long

    @Query("SELECT * FROM suscripcion ORDER BY fecha_vencimiento DESC")
    fun obtenerTodas(): Flow<List<SuscripcionEntity>>

    @Query("SELECT * FROM suscripcion WHERE id_suscripcion = :id")
    suspend fun obtenerPorId(id: Int): SuscripcionEntity?

    @Query("SELECT * FROM suscripcion WHERE id_barbero = :idBarbero ORDER BY fecha_vencimiento DESC")
    fun deBarbero(idBarbero: Int): Flow<List<SuscripcionEntity>>

    /** La suscripción más reciente del barbero (la vigente). */
    @Query("SELECT * FROM suscripcion WHERE id_barbero = :idBarbero ORDER BY fecha_vencimiento DESC LIMIT 1")
    fun vigenteDe(idBarbero: Int): Flow<SuscripcionEntity?>

    @Query("UPDATE suscripcion SET estado = :estado WHERE id_suscripcion = :idSuscripcion")
    suspend fun cambiarEstado(idSuscripcion: Int, estado: String)

    @Update
    suspend fun actualizar(suscripcion: SuscripcionEntity)

    @Delete
    suspend fun eliminar(suscripcion: SuscripcionEntity)

    @Query("DELETE FROM suscripcion")
    suspend fun eliminarTodas()
}
