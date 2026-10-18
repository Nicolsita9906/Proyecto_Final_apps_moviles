package com.example.proyectofinal.data.repository

import android.content.Context
import com.example.proyectofinal.data.database.AppDatabase
import com.example.proyectofinal.data.database.DatabaseProvider
import com.example.proyectofinal.data.model.BarberiaEntity
import com.example.proyectofinal.data.model.BarberiaResumen
import com.example.proyectofinal.data.model.BloqueoHorarioEntity
import com.example.proyectofinal.data.model.CitaDetalle
import com.example.proyectofinal.data.model.CitaEntity
import com.example.proyectofinal.data.model.CiudadEntity
import com.example.proyectofinal.data.model.FavoritoEntity
import com.example.proyectofinal.data.model.FotoBarberiaEntity
import com.example.proyectofinal.data.model.HorarioAtencionEntity
import com.example.proyectofinal.data.model.NotificacionEntity
import com.example.proyectofinal.data.model.PagoSuscripcionEntity
import com.example.proyectofinal.data.model.ResenaConAutor
import com.example.proyectofinal.data.model.ResenaEntity
import com.example.proyectofinal.data.model.ServicioEntity
import com.example.proyectofinal.data.model.SuscripcionEntity
import com.example.proyectofinal.data.model.UsuarioEntity
import kotlinx.coroutines.flow.Flow

/**
 * REPOSITORY: capa que centraliza el acceso a los datos (como PersonaRepository de la guía de
 * Navigation Compose). Las pantallas y el ViewModel NO hablan con los DAO: le piden todo a esta clase.
 * Así, si mañana los datos vienen de una API en lugar de Room, solo se cambia este archivo.
 *
 *   Pantalla -> BarberViewModel -> BarberGoRepository -> DAO -> Room (SQLite)
 *
 * Una sola instancia para toda la app: BarberGoRepository.obtener(context).
 */
class BarberGoRepository private constructor(db: AppDatabase) {

    companion object {
        @Volatile
        private var INSTANCE: BarberGoRepository? = null

        fun obtener(context: Context): BarberGoRepository {
            return INSTANCE ?: synchronized(this) {
                val instancia = BarberGoRepository(DatabaseProvider.getDatabase(context))
                INSTANCE = instancia
                instancia
            }
        }
    }

    private val ciudadDao = db.ciudadDao()
    private val usuarioDao = db.usuarioDao()
    private val barberiaDao = db.barberiaDao()
    private val fotoDao = db.fotoBarberiaDao()
    private val servicioDao = db.servicioDao()
    private val horarioDao = db.horarioAtencionDao()
    private val bloqueoDao = db.bloqueoHorarioDao()
    private val citaDao = db.citaDao()
    private val suscripcionDao = db.suscripcionDao()
    private val pagoDao = db.pagoSuscripcionDao()
    private val resenaDao = db.resenaDao()
    private val favoritoDao = db.favoritoDao()
    private val notificacionDao = db.notificacionDao()

    // ------------------------------- ciudad -------------------------------
    fun ciudades(): Flow<List<CiudadEntity>> = ciudadDao.obtenerTodas()
    suspend fun ciudadPorId(id: Int): CiudadEntity? = ciudadDao.obtenerPorId(id)
    suspend fun buscarCiudad(nombre: String): CiudadEntity? = ciudadDao.buscarPorNombre(nombre)
    suspend fun agregarCiudad(ciudad: CiudadEntity): Long = ciudadDao.insertar(ciudad)
    suspend fun actualizarCiudad(ciudad: CiudadEntity) = ciudadDao.actualizar(ciudad)
    suspend fun eliminarCiudad(ciudad: CiudadEntity) = ciudadDao.eliminar(ciudad)

    // ------------------------------- usuario -------------------------------
    fun usuarios(): Flow<List<UsuarioEntity>> = usuarioDao.obtenerTodos()
    fun usuariosPorRol(rol: String): Flow<List<UsuarioEntity>> = usuarioDao.porRol(rol)
    fun barberos(): Flow<List<UsuarioEntity>> = usuarioDao.barberos()
    suspend fun usuarioPorId(id: Int): UsuarioEntity? = usuarioDao.obtenerPorId(id)
    suspend fun buscarUsuarioPorCorreo(correo: String): UsuarioEntity? = usuarioDao.buscarPorCorreo(correo)
    suspend fun registrarUsuario(usuario: UsuarioEntity): Long = usuarioDao.insertar(usuario)
    suspend fun actualizarUsuario(usuario: UsuarioEntity) = usuarioDao.actualizar(usuario)
    suspend fun eliminarUsuario(usuario: UsuarioEntity) = usuarioDao.eliminar(usuario)

    // ------------------------------- barberia -------------------------------
    fun barberias(): Flow<List<BarberiaEntity>> = barberiaDao.obtenerTodas()
    fun barberiasAprobadas(): Flow<List<BarberiaResumen>> = barberiaDao.aprobadas()
    fun barberiasDeCiudad(idCiudad: Int): Flow<List<BarberiaEntity>> = barberiaDao.deCiudad(idCiudad)
    fun barberiasDePropietario(idUsuario: Int): Flow<List<BarberiaEntity>> = barberiaDao.dePropietario(idUsuario)
    fun barberiasPorEstado(estado: String): Flow<List<BarberiaEntity>> = barberiaDao.porEstado(estado)
    suspend fun barberiaPorId(id: Int): BarberiaEntity? = barberiaDao.obtenerPorId(id)
    suspend fun agregarBarberia(barberia: BarberiaEntity): Long = barberiaDao.insertar(barberia)
    suspend fun actualizarBarberia(barberia: BarberiaEntity) = barberiaDao.actualizar(barberia)
    suspend fun eliminarBarberia(barberia: BarberiaEntity) = barberiaDao.eliminar(barberia)

    // ------------------------------- foto_barberia -------------------------------
    fun fotosDeBarberia(idBarberia: Int): Flow<List<FotoBarberiaEntity>> = fotoDao.deBarberia(idBarberia)
    suspend fun agregarFoto(foto: FotoBarberiaEntity): Long = fotoDao.insertar(foto)
    suspend fun eliminarFoto(foto: FotoBarberiaEntity) = fotoDao.eliminar(foto)

    // ------------------------------- horario_atencion -------------------------------
    fun horarios(): Flow<List<HorarioAtencionEntity>> = horarioDao.obtenerTodos()
    fun horariosDeBarberia(idBarberia: Int): Flow<List<HorarioAtencionEntity>> = horarioDao.deBarberia(idBarberia)
    suspend fun agregarHorario(horario: HorarioAtencionEntity): Long = horarioDao.insertar(horario)
    suspend fun actualizarHorario(horario: HorarioAtencionEntity) = horarioDao.actualizar(horario)
    suspend fun eliminarHorario(horario: HorarioAtencionEntity) = horarioDao.eliminar(horario)

    // ------------------------------- bloqueo_horario -------------------------------
    fun bloqueosDeBarberia(idBarberia: Int): Flow<List<BloqueoHorarioEntity>> = bloqueoDao.deBarberia(idBarberia)
    suspend fun bloqueosEnRango(idBarberia: Int, desde: Long, hasta: Long): List<BloqueoHorarioEntity> =
        bloqueoDao.enRango(idBarberia, desde, hasta)
    suspend fun agregarBloqueo(bloqueo: BloqueoHorarioEntity): Long = bloqueoDao.insertar(bloqueo)
    suspend fun actualizarBloqueo(bloqueo: BloqueoHorarioEntity) = bloqueoDao.actualizar(bloqueo)
    suspend fun eliminarBloqueo(bloqueo: BloqueoHorarioEntity) = bloqueoDao.eliminar(bloqueo)

    // ------------------------------- servicio -------------------------------
    fun serviciosDeBarberia(idBarberia: Int): Flow<List<ServicioEntity>> = servicioDao.deBarberia(idBarberia)
    fun todosLosServiciosDeBarberia(idBarberia: Int): Flow<List<ServicioEntity>> = servicioDao.todosDeBarberia(idBarberia)
    suspend fun servicioPorId(id: Int): ServicioEntity? = servicioDao.obtenerPorId(id)
    suspend fun agregarServicio(servicio: ServicioEntity): Long = servicioDao.insertar(servicio)
    suspend fun actualizarServicio(servicio: ServicioEntity) = servicioDao.actualizar(servicio)
    suspend fun eliminarServicio(servicio: ServicioEntity) = servicioDao.eliminar(servicio)

    // ------------------------------- cita -------------------------------
    fun citasDeCliente(idCliente: Int): Flow<List<CitaDetalle>> = citaDao.detalleDeCliente(idCliente)
    fun todasLasCitas(): Flow<List<CitaEntity>> = citaDao.obtenerTodas()
    fun citasActivasDeBarbero(idBarbero: Int, fecha: String): Flow<List<CitaEntity>> =
        citaDao.activasDeBarbero(idBarbero, fecha)
    suspend fun citaPorId(id: Int): CitaEntity? = citaDao.obtenerPorId(id)
    suspend fun hayConflicto(idBarbero: Int, fecha: String, horaInicio: String): Boolean =
        citaDao.contarConflictos(idBarbero, fecha, horaInicio) > 0
    suspend fun agendarCita(cita: CitaEntity): Long = citaDao.insertar(cita)
    suspend fun modificarCita(
        idCita: Int,
        idServicio: Int,
        idBarbero: Int?,
        fecha: String,
        horaInicio: String,
        horaFin: String,
        estado: String,
        valorTotal: Double
    ) = citaDao.modificar(idCita, idServicio, idBarbero, fecha, horaInicio, horaFin, estado, valorTotal)
    suspend fun cambiarEstadoCita(idCita: Int, estado: String) = citaDao.cambiarEstado(idCita, estado)
    suspend fun cancelarCita(idCita: Int) = citaDao.cancelar(idCita)
    suspend fun eliminarCita(cita: CitaEntity) = citaDao.eliminar(cita)

    // ------------------------------- suscripcion y pago_suscripcion -------------------------------
    fun suscripcionesDeBarbero(idBarbero: Int): Flow<List<SuscripcionEntity>> = suscripcionDao.deBarbero(idBarbero)
    fun suscripcionVigente(idBarbero: Int): Flow<SuscripcionEntity?> = suscripcionDao.vigenteDe(idBarbero)
    suspend fun agregarSuscripcion(suscripcion: SuscripcionEntity): Long = suscripcionDao.insertar(suscripcion)
    suspend fun actualizarSuscripcion(suscripcion: SuscripcionEntity) = suscripcionDao.actualizar(suscripcion)
    suspend fun cambiarEstadoSuscripcion(idSuscripcion: Int, estado: String) =
        suscripcionDao.cambiarEstado(idSuscripcion, estado)
    fun pagosDeSuscripcion(idSuscripcion: Int): Flow<List<PagoSuscripcionEntity>> = pagoDao.deSuscripcion(idSuscripcion)
    suspend fun registrarPago(pago: PagoSuscripcionEntity): Long = pagoDao.insertar(pago)

    // ------------------------------- resena -------------------------------
    fun resenasDeBarberia(idBarberia: Int): Flow<List<ResenaConAutor>> = resenaDao.deBarberia(idBarberia)
    fun promedioDeBarberia(idBarberia: Int): Flow<Double?> = resenaDao.promedioDeBarberia(idBarberia)
    suspend fun agregarResena(resena: ResenaEntity): Long = resenaDao.insertar(resena)
    suspend fun actualizarResena(resena: ResenaEntity) = resenaDao.actualizar(resena)
    suspend fun eliminarResena(resena: ResenaEntity) = resenaDao.eliminar(resena)

    // ------------------------------- favorito -------------------------------
    fun favoritosDeCliente(idCliente: Int): Flow<List<FavoritoEntity>> = favoritoDao.deCliente(idCliente)
    fun esFavorita(idCliente: Int, idBarberia: Int): Flow<Boolean> = favoritoDao.esFavorita(idCliente, idBarberia)
    suspend fun agregarFavorito(idCliente: Int, idBarberia: Int) =
        favoritoDao.insertar(FavoritoEntity(idCliente, idBarberia))
    suspend fun quitarFavorito(idCliente: Int, idBarberia: Int) = favoritoDao.quitar(idCliente, idBarberia)

    // ------------------------------- notificacion -------------------------------
    fun notificacionesDeUsuario(idUsuario: Int): Flow<List<NotificacionEntity>> = notificacionDao.deUsuario(idUsuario)
    fun notificacionesNoLeidas(idUsuario: Int): Flow<Int> = notificacionDao.noLeidas(idUsuario)
    suspend fun crearNotificacion(notificacion: NotificacionEntity): Long = notificacionDao.insertar(notificacion)
    suspend fun marcarNotificacionLeida(idNotificacion: Int) = notificacionDao.marcarLeida(idNotificacion)
    suspend fun marcarTodasLeidas(idUsuario: Int) = notificacionDao.marcarTodasLeidas(idUsuario)
    suspend fun eliminarNotificacion(notificacion: NotificacionEntity) = notificacionDao.eliminar(notificacion)
}
