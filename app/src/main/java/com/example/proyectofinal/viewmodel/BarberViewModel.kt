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
import com.example.proyectofinal.data.Dia
import com.example.proyectofinal.data.Disponibilidad
import com.example.proyectofinal.data.Horario
import com.example.proyectofinal.data.Resena
import com.example.proyectofinal.data.Servicio
import com.example.proyectofinal.data.mapper.SIN_PREFERENCIA
import com.example.proyectofinal.data.mapper.fechaCorta
import com.example.proyectofinal.data.mapper.hora24
import com.example.proyectofinal.data.mapper.sumarMinutos
import com.example.proyectofinal.data.model.BarberiaEntity
import com.example.proyectofinal.data.model.CitaDetalle
import com.example.proyectofinal.data.model.CitaEntity
import com.example.proyectofinal.data.model.EstadoCitaDb
import com.example.proyectofinal.data.model.HorarioAtencionEntity
import com.example.proyectofinal.data.model.NotificacionEntity
import com.example.proyectofinal.data.model.ResenaEntity
import com.example.proyectofinal.data.model.SuscripcionEntity
import com.example.proyectofinal.data.model.TipoNotificacion
import com.example.proyectofinal.data.model.UsuarioEntity
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
import kotlinx.coroutines.flow.flowOf
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
 * Los días y horas de la reserva se calculan con la BD (ver data/Disponibilidad.kt).
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

    // ---------------- Barberías (barberia + ciudad + horario_atencion + reseña) ----------------
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

    // ---------------- Selección del flujo de reserva ----------------
    private val _servicioSel = MutableStateFlow<Servicio?>(null)
    val servicioSel: StateFlow<Servicio?> = _servicioSel
    private val _barberoSel = MutableStateFlow<Barbero?>(null)
    val barberoSel: StateFlow<Barbero?> = _barberoSel
    private val _diaSel = MutableStateFlow<Dia?>(null)
    val diaSel: StateFlow<Dia?> = _diaSel
    private val _horaSel = MutableStateFlow<Horario?>(null)
    val horaSel: StateFlow<Horario?> = _horaSel

    /** id de la cita que se está modificando (null = cita nueva). */
    private val _editandoId = MutableStateFlow<Int?>(null)

    // ---------------- Días y horas disponibles (calculados con la BD) ----------------
    private val horariosBarberiaActual: Flow<List<HorarioAtencionEntity>> =
        idBarberiaActual.flatMapLatest { repo.horariosDeBarberia(it) }

    /** Próximos días en que abre la barbería elegida. */
    val dias: StateFlow<List<Dia>> = horariosBarberiaActual
        .map { Disponibilidad.proximosDias(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Franjas del día elegido: salen del horario de la barbería y se marcan como no disponibles
     * si ya pasaron, si se cruzan con otra cita activa del barbero (o con otra "Sin preferencia"
     * de la barbería) o si caen en un bloqueo_horario.
     */
    val horarios: StateFlow<List<Horario>> = combine(
        idBarberiaActual, _diaSel, _barberoSel, _servicioSel, _editandoId
    ) { idBarberia, dia, barbero, servicio, editando ->
        SolicitudHorario(idBarberia, dia, barbero, servicio, editando)
    }.flatMapLatest { sol ->
        val dia = sol.dia
        val barbero = sol.barbero
        if (dia == null || barbero == null || dia.iso.isEmpty()) {
            flowOf(emptyList())
        } else {
            val citasDelDia =
                if (barbero.id == 0) repo.citasSinBarbero(sol.idBarberia, dia.iso)
                else repo.citasActivasDeBarbero(barbero.id, dia.iso)
            combine(
                repo.horariosDeBarberia(sol.idBarberia),
                citasDelDia,
                repo.bloqueosDeBarberia(sol.idBarberia)
            ) { horariosBd, citas, bloqueos ->
                Disponibilidad.horarios(
                    dia = dia,
                    horariosBarberia = horariosBd,
                    duracionMin = sol.servicio?.duracionMin ?: 0,
                    citas = citas,
                    bloqueos = bloqueos,
                    excluirCita = sol.editando
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private data class SolicitudHorario(
        val idBarberia: Int,
        val dia: Dia?,
        val barbero: Barbero?,
        val servicio: Servicio?,
        val editando: Int?
    )

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

    // ---------------- Acciones de la reserva ----------------
    // (cambiar servicio, barbero o día cambia las horas libres: la hora elegida se borra)
    fun elegirServicio(s: Servicio) {
        if (_servicioSel.value?.id != s.id) _horaSel.value = null
        _servicioSel.value = s
    }
    fun elegirBarbero(b: Barbero) {
        if (_barberoSel.value?.id != b.id) _horaSel.value = null
        _barberoSel.value = b
    }
    fun elegirDia(d: Dia) {
        if (_diaSel.value != d) _horaSel.value = null
        _diaSel.value = d
    }
    fun elegirHora(h: Horario) { if (h.disponible) _horaSel.value = h }

    fun limpiarSeleccion() {
        _servicioSel.value = null
        _barberoSel.value = null
        _diaSel.value = null
        _horaSel.value = null
        _editandoId.value = null
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
        _editandoId.value = c.id
    }

    /** Resultado de intentar guardar la reserva. */
    enum class ResultadoReserva { OK, FALTAN_DATOS, HORARIO_OCUPADO, ERROR }

    /**
     * Guarda la cita en la BD (INSERT), o actualiza la que se está modificando (UPDATE),
     * y crea una notificación para el cliente.
     *
     * Antes de guardar vuelve a revisar en la BD que la franja siga libre: otra persona pudo
     * reservarla mientras el cliente estaba en la pantalla de confirmación.
     * El resultado llega en [onResultado] (en el hilo principal).
     */
    fun confirmarCita(onResultado: (ResultadoReserva) -> Unit) {
        val s = _servicioSel.value
        val b = _barberoSel.value
        val d = _diaSel.value
        val h = _horaSel.value
        val idCliente = _idCliente.value
        val idBarberia = barberia.id
        if (s == null || b == null || d == null || h == null || d.iso.isEmpty() ||
            idCliente <= 0 || idBarberia <= 0
        ) {
            onResultado(ResultadoReserva.FALTAN_DATOS)
            return
        }

        val idBarbero: Int? = if (b.id == 0) null else b.id        // null = sin preferencia
        val estado = if (b.id == 0) EstadoCitaDb.PENDIENTE else EstadoCitaDb.CONFIRMADA
        val fecha = d.iso
        val horaInicio = hora24(h.completa)
        val horaFin = sumarMinutos(horaInicio, if (s.duracionMin > 0) s.duracionMin else Disponibilidad.INTERVALO_MIN)
        val editId = _editandoId.value
        val resumen = "${s.nombre} el ${fechaCorta(fecha)} a las ${h.completa}"

        viewModelScope.launch {
            val ocupado = runCatching {
                repo.hayConflicto(idBarbero, idBarberia, fecha, horaInicio, horaFin, editId ?: -1)
            }.getOrDefault(false)
            if (ocupado) {
                _horaSel.value = null
                onResultado(ResultadoReserva.HORARIO_OCUPADO)
                return@launch
            }

            // runCatching: si la sesión guardada apunta a un usuario que ya no existe (BD recreada),
            // la llave foránea falla; así no se cierra la app.
            val guardado = runCatching {
                if (editId != null) {
                    repo.modificarCita(editId, s.id, idBarbero, fecha, horaInicio, horaFin, estado, s.precioValor)
                    notificarCita(idCliente, "Cita modificada: $resumen")
                } else {
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
            }.isSuccess

            if (guardado) {
                limpiarSeleccion()
                onResultado(ResultadoReserva.OK)
            } else {
                onResultado(ResultadoReserva.ERROR)
            }
        }
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

    // ---------------------------------------------------------------------------
    //  Administración (gestión de usuarios, barberías y suscripciones)
    // ---------------------------------------------------------------------------

    val todosLosUsuarios: StateFlow<List<UsuarioEntity>> = repo.usuarios()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val todasLasBarberiasAdmin: StateFlow<List<BarberiaEntity>> = repo.barberias()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val todasLasSuscripcionesAdmin: StateFlow<List<SuscripcionEntity>> = repo.todasLasSuscripciones()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun cambiarEstadoUsuario(usuario: UsuarioEntity, nuevoEstado: String) {
        viewModelScope.launch {
            runCatching {
                repo.actualizarUsuario(usuario.copy(estado = nuevoEstado))
            }
        }
    }

    fun cambiarEstadoBarberia(barberia: BarberiaEntity, nuevoEstado: String) {
        viewModelScope.launch {
            runCatching {
                repo.actualizarBarberia(barberia.copy(estadoAprobacion = nuevoEstado))
            }
        }
    }

    fun cambiarEstadoSuscripcion(idSuscripcion: Int, nuevoEstado: String) {
        viewModelScope.launch {
            runCatching {
                repo.cambiarEstadoSuscripcion(idSuscripcion, nuevoEstado)
            }
        }
    }
}
