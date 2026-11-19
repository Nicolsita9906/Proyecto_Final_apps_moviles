package com.example.proyectofinal.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.proyectofinal.data.dao.BarberiaDao
import com.example.proyectofinal.data.dao.BloqueoHorarioDao
import com.example.proyectofinal.data.dao.CitaDao
import com.example.proyectofinal.data.dao.CiudadDao
import com.example.proyectofinal.data.dao.FavoritoDao
import com.example.proyectofinal.data.dao.FotoBarberiaDao
import com.example.proyectofinal.data.dao.HorarioAtencionDao
import com.example.proyectofinal.data.dao.NotificacionDao
import com.example.proyectofinal.data.dao.PagoSuscripcionDao
import com.example.proyectofinal.data.dao.ResenaDao
import com.example.proyectofinal.data.dao.ServicioDao
import com.example.proyectofinal.data.dao.SuscripcionDao
import com.example.proyectofinal.data.dao.UsuarioDao
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
 * version = 5: coordenadas (latitud/longitud) de las barberías de ejemplo para el mapa.
 * version = 4: barberos de ejemplo con contraseña y citas de ejemplo (rol Barbero).
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
    version = 5,
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
