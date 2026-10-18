package com.example.proyectofinal.viewmodel

import android.app.Application
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinal.data.Barberia
import com.example.proyectofinal.data.Barbero
import com.example.proyectofinal.data.Cita
import com.example.proyectofinal.data.DatosEstaticos
import com.example.proyectofinal.data.Dia
import com.example.proyectofinal.data.Horario
import com.example.proyectofinal.data.Resena
import com.example.proyectofinal.data.Servicio
import com.example.proyectofinal.data.mapper.SIN_PREFERENCIA
import com.example.proyectofinal.data.mapper.fechaIso
import com.example.proyectofinal.data.mapper.hora24
import com.example.proyectofinal.data.mapper.sumarMinutos
import com.example.proyectofinal.data.model.CitaDetalle
import com.example.proyectofinal.data.model.CitaEntity
import com.example.proyectofinal.data.model.EstadoCitaDb
import com.example.proyectofinal.data.model.NotificacionEntity
import com.example.proyectofinal.data.model.ResenaEntity
import com.example.proyectofinal.data.model.TipoNotificacion
import com.example.proyectofinal.data.obtenerIdUsuario
import com.example.proyectofinal.data.repository.BarberGoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.proyectofinal.data.mapper.aBarberia
import com.example.proyectofinal.data.mapper.aBarbero
import com.example.proyectofinal.data.mapper.aCita
import com.example.proyectofinal.data.mapper.aServicio

/**
 * Estado compartido de las pantallas de cliente: puente entre la UI y los datos.
 * Los datos los pide al Repository (BarberGoRepository), que usa los DAO de Room.
 * Solo siguen estáticos (DatosEstaticos): los días y horas disponibles de la reserva.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BarberViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = BarberGoRepository.obtener(application)

    // ---------------- Cliente de la sesión (se actualiza solo al iniciar/cerrar sesión) ----------------
    private val prefsSesion = application.getSharedPreferences("sesion_prefs", 0)
    private val _idCliente = MutableStateFlow(obtenerIdUsuario(application))
    private val escuchaSesion = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        _idCliente.value = obtenerIdUsuario(application)
    }

    init {
        prefsSesion.registerOnSharedPreferenceChangeListener(escuchaSesion)
    }

    override fun onCleared() {
        prefsSesion.unregisterOnSharedPreferenceChangeListener(escuchaSesion)
        super.onCleared()
    }

    // ---------------- Barberías (barberia + ciudad + horario_atencion + resena) ----------------
    val barberias: StateFlow<List<Barberia>> = combine(
        repo.barberiasAprobadas(),
        repo.horarios()
    ) { lista, horarios ->
        val porBarberia = horarios.groupBy { it.idBarberia }
        lista.map { it.aBarberia(porBarberia[it.idBarberia] ?: emptyList()) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Barbería elegida en la pantalla de inicio
    private val _barberiaSel = MutableStateFlow<Barberia?>(null)
    private val barberiaVacia = Barberia(0, "", "", "", false, "", "")

    /** Barbería elegida (o la primera mientras no se elija ninguna). */
    val barberia: Barberia
        get() = _barberiaSel.value ?: barberias.value.firstOrNull() ?: barberiaVacia

    fun elegirBarberia(id: Int) {
        barberias.value.firstOrNull { it.id == id }?.let { _barberiaSel.value = it }
    }

    private val idBarberiaActual: Flow<Int> = combine(_barberiaSel, barberias) { sel, lista ->
        sel?.id ?: lista.firstOrNull()?.id ?: 0
    }.distinctUntilChanged()

    /** true justo después de iniciar sesión o registrarse: muestra el Splash una vez. */
    var splashPendiente by mutableStateOf(false)

    // ---------------- Servicios y reseñas de la barbería actual ----------------
    val servicios: StateFlow<List<Servicio>> = idBarberiaActual
        .flatMapLatest { repo.serviciosDeBarberia(it) }
        .map { lista -> lista.map { it.aServicio() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val resenas: StateFlow<List<Resena>> = idBarberiaActual
        .flatMapLatest { repo.resenasDeBarberia(it) }
        .map { lista -> lista.map { Resena(it.autor, it.calificacion, it.comentario ?: "") } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Publica una reseña del cliente logueado en la barbería actual (tabla resena). */
    fun agregarResena(calificacion: Int, comentario: String) {
        val idCliente = _idCliente.value
        val idBarberia = barberia.id
        if (idCliente <= 0 || idBarberia <= 0) return
        viewModelScope.launch {
            runCatching {
                repo.agregarResena(
                    ResenaEntity(
                        idCliente = idCliente,
                        idBarberia = idBarberia,
                        calificacion = calificacion.coerceIn(1, 5),
                        comentario = comentario.ifBlank { null }
                    )
                )
            }
        }
    }

    // ---------------- Barberos (usuarios con rol BARBERO) + opción "Sin preferencia" ----------------
    // TODO: el script no relaciona barberos con barberías, por eso se listan todos los barberos activos.
    val barberos: StateFlow<List<Barbero>> = repo.barberos()
        .map { lista -> lista.map { it.aBarbero() } + SIN_PREFERENCIA }
        .stateIn(viewModelScope, SharingStarted.Eagerly, listOf(SIN_PREFERENCIA))

    // ---------------- Días y horas: siguen estáticos ----------------
    // TODO: calcular con horario_atencion + bloqueo_horario + las citas ya tomadas del barbero ese día
    val dias: List<Dia> = DatosEstaticos.dias
    val horarios: List<Horario> = DatosEstaticos.horarios

    // ---------------- Citas del cliente (tabla cita) ----------------
    private val detalles: StateFlow<List<CitaDetalle>> = _idCliente
        .flatMapLatest { repo.citasDeCliente(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val citas: StateFlow<List<Cita>> = detalles
        .map { lista -> lista.map { it.aCita() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun barberiaDe(citaId: Int): String =
        detalles.value.firstOrNull { it.idCita == citaId }?.barberiaNombre ?: barberia.nombre

    fun direccionDe(citaId: Int): String {
        val d = detalles.value.firstOrNull { it.idCita == citaId } ?: return ""
        return "${d.direccion}, ${d.ciudad}, ${d.departamento}"
    }

    fun citaPorId(id: Int): Cita? = citas.value.firstOrNull { it.id == id }

    // ---------------- Favoritos (tabla favorito) ----------------
    /** ids de las barberías que el cliente marcó como favoritas. */
    val favoritosIds: StateFlow<Set<Int>> = _idCliente
        .flatMapLatest { repo.favoritosDeCliente(it) }
        .map { lista -> lista.map { it.idBarberia }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    fun alternarFavorito(idBarberia: Int) {
        val idCliente = _idCliente.value
        if (idCliente <= 0) return
        viewModelScope.launch {
            runCatching {
                if (idBarberia in favoritosIds.value) repo.quitarFavorito(idCliente, idBarberia)
                else repo.agregarFavorito(idCliente, idBarberia)
            }
        }
    }

    // ---------------- Notificaciones (tabla notificacion) ----------------
    val notificaciones: StateFlow<List<NotificacionEntity>> = _idCliente
        .flatMapLatest { repo.notificacionesDeUsuario(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val noLeidas: StateFlow<Int> = _idCliente
        .flatMapLatest { repo.notificacionesNoLeidas(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun marcarNotificacionLeida(id: Int) {
        viewModelScope.launch { repo.marcarNotificacionLeida(id) }
    }

    fun marcarTodasLeidas() {
        val idCliente = _idCliente.value
        if (idCliente > 0) viewModelScope.launch { repo.marcarTodasLeidas(idCliente) }
    }

    private suspend fun notificarCita(idCliente: Int, mensaje: String) {
        repo.crearNotificacion(
            NotificacionEntity(idUsuario = idCliente, tipo = TipoNotificacion.CITA, mensaje = mensaje)
        )
    }

    // ---------------- Selección del flujo de reserva ----------------
    private val _servicioSel = MutableStateFlow<Servicio?>(null)
    val servicioSel: StateFlow<Servicio?> = _servicioSel
    private val _barberoSel = MutableStateFlow<Barbero?>(null)
    val barberoSel: StateFlow<Barbero?> = _barberoSel
    private val _diaSel = MutableStateFlow<Dia?>(null)
    val diaSel: StateFlow<Dia?> = _diaSel
    private val _horaSel = MutableStateFlow<Horario?>(null)
    val horaSel: StateFlow<Horario?> = _horaSel

    private var editandoId: Int? = null

    fun elegirServicio(s: Servicio) { _servicioSel.value = s }
    fun elegirBarbero(b: Barbero) { _barberoSel.value = b }
    fun elegirDia(d: Dia) { _diaSel.value = d }
    fun elegirHora(h: Horario) { _horaSel.value = h }

    fun limpiarSeleccion() {
        _servicioSel.value = null
        _barberoSel.value = null
        _diaSel.value = null
        _horaSel.value = null
        editandoId = null
    }

    /** Prepara la reserva con los datos de una cita existente (botón Modificar). */
    fun editarCita(id: Int) {
        val c = citaPorId(id) ?: return
        val detalle = detalles.value.firstOrNull { it.idCita == id }
        limpiarSeleccion()
        detalle?.let { d ->
            barberias.value.firstOrNull { it.id == d.idBarberia }?.let { _barberiaSel.value = it }
        }
        _servicioSel.value = c.servicio
        _barberoSel.value = c.barbero
        editandoId = c.id
    }

    /**
     * Guarda la cita en la BD (INSERT), o actualiza la que se está modificando (UPDATE),
     * y crea una notificación para el cliente.
     * Devuelve false si falta algún dato o no hay sesión.
     */
    fun confirmarCita(): Boolean {
        val s = _servicioSel.value ?: return false
        val b = _barberoSel.value ?: return false
        val d = _diaSel.value ?: return false
        val h = _horaSel.value ?: return false
        val idCliente = _idCliente.value
        val idBarberia = barberia.id
        if (idCliente <= 0 || idBarberia <= 0) return false

        val idBarbero: Int? = if (b.id == 0) null else b.id        // null = sin preferencia
        val estado = if (b.id == 0) EstadoCitaDb.PENDIENTE else EstadoCitaDb.CONFIRMADA
        val fecha = fechaIso(d.numero)
        val horaInicio = hora24(h.completa)
        val horaFin = sumarMinutos(horaInicio, s.duracionMin)
        val editId = editandoId
        val resumen = "${s.nombre} el ${d.numero} ${DatosEstaticos.MES_CORTO} a las ${h.completa}"

        viewModelScope.launch {
            // runCatching: si la sesión guardada apunta a un usuario que ya no existe (BD recreada),
            // la llave foránea falla; así no se cierra la app.
            runCatching {
                if (editId != null) {
                    repo.modificarCita(editId, s.id, idBarbero, fecha, horaInicio, horaFin, estado, s.precioValor)
                    notificarCita(idCliente, "Cita modificada: $resumen")
                } else {
                    // TODO: antes de insertar, validar repo.hayConflicto(idBarbero, fecha, horaInicio)
                    repo.agendarCita(
                        CitaEntity(
                            idCliente = idCliente,
                            idBarberia = idBarberia,
                            idBarbero = idBarbero,
                            idServicio = s.id,
                            fecha = fecha,
                            horaInicio = horaInicio,
                            horaFin = horaFin,
                            estado = estado,
                            valorTotal = s.precioValor
                        )
                    )
                    notificarCita(idCliente, "Cita agendada: $resumen")
                }
            }
        }
        limpiarSeleccion()
        return true
    }

    fun cancelarCita(id: Int) {
        val idCliente = _idCliente.value
        viewModelScope.launch {
            runCatching {
                repo.cancelarCita(id)
                if (idCliente > 0) notificarCita(idCliente, "Cita cancelada")
            }
        }
    }
}
