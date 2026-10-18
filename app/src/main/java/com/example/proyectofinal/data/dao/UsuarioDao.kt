package com.example.proyectofinal.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.proyectofinal.data.model.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    // Crear / Insertar (devuelve el id_usuario generado)
    @Insert
    suspend fun insertar(usuario: UsuarioEntity): Long

    // Leer todos (observables en tiempo real)
    @Query("SELECT * FROM usuario ORDER BY nombre")
    fun obtenerTodos(): Flow<List<UsuarioEntity>>

    // Leer por clave primaria
    @Query("SELECT * FROM usuario WHERE id_usuario = :id")
    suspend fun obtenerPorId(id: Int): UsuarioEntity?

    // Login / registro: buscar por correo (sin distinguir mayúsculas)
    @Query("SELECT * FROM usuario WHERE correo = :correo COLLATE NOCASE LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuario WHERE rol = :rol ORDER BY nombre")
    fun porRol(rol: String): Flow<List<UsuarioEntity>>

    /** Barberos activos (usuarios con rol BARBERO). */
    @Query("SELECT * FROM usuario WHERE rol = 'BARBERO' AND estado = 'ACTIVO' ORDER BY nombre")
    fun barberos(): Flow<List<UsuarioEntity>>

    @Update
    suspend fun actualizar(usuario: UsuarioEntity)

    @Delete
    suspend fun eliminar(usuario: UsuarioEntity)

    // Falla si hay filas de otras tablas que dependan de estos usuarios (llaves foráneas)
    @Query("DELETE FROM usuario")
    suspend fun eliminarTodos()
}
