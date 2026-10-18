package com.example.proyectofinal.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.proyectofinal.data.local.database.dao.BarberiaDao
import com.example.proyectofinal.data.local.database.dao.BloqueoHorarioDao
import com.example.proyectofinal.data.local.database.dao.CitaDao
import com.example.proyectofinal.data.local.database.dao.CiudadDao
import com.example.proyectofinal.data.local.database.dao.FavoritoDao
import com.example.proyectofinal.data.local.database.dao.FotoBarberiaDao
import com.example.proyectofinal.data.local.database.dao.HorarioAtencionDao
import com.example.proyectofinal.data.local.database.dao.NotificacionDao
import com.example.proyectofinal.data.local.database.dao.PagoSuscripcionDao
import com.example.proyectofinal.data.local.database.dao.ResenaDao
import com.example.proyectofinal.data.local.database.dao.ServicioDao
import com.example.proyectofinal.data.local.database.dao.SuscripcionDao
import com.example.proyectofinal.data.local.database.dao.UsuarioDao
import com.example.proyectofinal.data.model.BarberiaEntity
import com.example.proyectofinal.data.model.BloqueoHorarioEntity
import com.example.proyectofinal.data.model.CitaEntity
import com.example.proyectofinal.data.model.CiudadEntity
import com.example.proyectofinal.data.model.FavoritoEntity
import com.example.proyectofinal.data.model.FotoBarberiaEntity
import com.example.proyectofinal.data.model.HorarioAtencionEntity
import com.example.proyectofinal.data.model.NotificacionEntity
import com.example.proyectofinal.data.model.PagoSuscripcionEntity
import com.example.proyectofinal.data.model.ResenaEntity
import com.example.proyectofinal.data.model.ServicioEntity
import com.example.proyectofinal.data.model.SuscripcionEntity
import com.example.proyectofinal.data.model.UsuarioEntity

/**
 * Base de datos local de BarberGo: conecta las Entity con los DAO (igual que en la guía de Room).
 * Tiene las 13 tablas del script MySQL.
 *
 * version = 2: se agregaron las 6 tablas que faltaban (foto_barberia, bloqueo_horario,
 * suscripcion, pago_suscripcion, favorito, notificacion). Cada vez que cambies una Entity
 * hay que subir la versión (ver DatabaseProvider).
 */
@Database(
    entities = [
        CiudadEntity::class,
        UsuarioEntity::class,
        BarberiaEntity::class,
        FotoBarberiaEntity::class,
        ServicioEntity::class,
        HorarioAtencionEntity::class,
        BloqueoHorarioEntity::class,
        CitaEntity::class,
        SuscripcionEntity::class,
        PagoSuscripcionEntity::class,
        ResenaEntity::class,
        FavoritoEntity::class,
        NotificacionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ciudadDao(): CiudadDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun barberiaDao(): BarberiaDao
    abstract fun fotoBarberiaDao(): FotoBarberiaDao
    abstract fun servicioDao(): ServicioDao
    abstract fun horarioAtencionDao(): HorarioAtencionDao
    abstract fun bloqueoHorarioDao(): BloqueoHorarioDao
    abstract fun citaDao(): CitaDao
    abstract fun suscripcionDao(): SuscripcionDao
    abstract fun pagoSuscripcionDao(): PagoSuscripcionDao
    abstract fun resenaDao(): ResenaDao
    abstract fun favoritoDao(): FavoritoDao
    abstract fun notificacionDao(): NotificacionDao
}
