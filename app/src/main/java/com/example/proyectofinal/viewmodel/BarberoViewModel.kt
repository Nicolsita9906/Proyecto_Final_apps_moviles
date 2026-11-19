package com.example.proyectofinal.viewmodel

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinal.data.Disponibilidad
import com.example.proyectofinal.data.mapper.fechaCorta
import com.example.proyectofinal.data.mapper.hora12
import com.example.proyectofinal.data.model.BarberiaEntity
import com.example.proyectofinal.data.model.CitaRecibida
import com.example.proyectofinal.data.model.CiudadEntity
import com.example.proyectofinal.data.model.EstadoCitaDb
import com.example.proyectofinal.data.model.EstadoSuscripcion
import com.example.proyectofinal.data.model.HorarioAtencionEntity
import com.example.proyectofinal.data.model.NotificacionEntity
import com.example.proyectofinal.data.model.PagoSuscripcionEntity
import com.example.proyectofinal.data.model.ServicioEntity
import com.example.proyectofinal.data.model.SuscripcionEntity
import com.example.proyectofinal.data.model.TipoNotificacion
import com.example.proyectofinal.data.obtenerIdUsuario
import com.example.proyectofinal.data.repository.BarberGoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Estado de las pantallas del rol BARBERO: panel, citas recibidas, mi barbería,
 * servicios, horarios y suscripción. Igual que BarberViewModel, le pide todo al Repository.
 *
 * Relación barbero <-> barbería: el barbero es el PROPIETARIO (barberia.id_propietario).
 * Recibe las citas que lo eligieron a él y las "Sin preferencia" de sus barberías.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BarberoViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        /** Precio del plan mensual (mockup "Mi suscripción"). */
        const val PRECIO_PLAN_MENSUAL = 50_000.0

        /** Días antes del vencimiento en que la suscripción pasa a "Por vencer". */
        const val DIAS_AVISO = 7

        /** Estado según las fechas: VENCIDA si ya pasó, POR_VENCER si faltan <= DIAS_AVISO días. */
        fun estadoCalculado(s: SuscripcionEntity, hoy: Calendar = Calendar.getInstance()): String {
            val hoyIso = Disponibilidad.iso(hoy)
            if (s.fechaVencimiento < hoyIso) return EstadoSuscripcion.VENCIDA
            val aviso = (hoy.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, DIAS_AVISO) }
            return if (s.fechaVencimiento <= Disponibilidad.iso(aviso)) EstadoSuscripcion.POR_VENCER
            else EstadoSuscripcion.ACTIVA
        }
    }

    private val repo = BarberGoRepository.obtener(application)

    // ---------------- Barbero de la sesión ----------------
    private val prefsSesion = application.getSharedPreferences("sesion_prefs", 0)
    private val _idBarbero = MutableStateFlow(obtenerIdUsuario(application))
    private val escuchaSesion = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        _idBarbero.value = obtenerIdUsuario(application)
    }

    init {
        prefsSesion.registerOnSharedPreferenceChangeListener(escuchaSesion)
    }

    override fun onCleared() {
        prefsSesion.unregisterOnSharedPreferenceChangeListener(escuchaSesion)
        super.onCleared()
    }

    // ---------------- Mi barbería ----------------
    val misBarberias: StateFlow<List<BarberiaEntity>> = _idBarbero
        .flatMapLatest { repo.barberiasDePropietario(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _idBarberiaSel = MutableStateFlow<Int?>(null)

    /** Barbería que se está administrando (la elegida, o la primera del barbero). null = aún no tiene. */
    val barberia: StateFlow<BarberiaEntity?> = combine(misBarberias, _idBarberiaSel) { lista, sel ->
        lista.firstOrNull { it.idBarberia == sel } ?: lista.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun elegirBarberia(id: Int) { _idBarberiaSel.value = id }

    val ciudad: StateFlow<CiudadEntity?> = combine(barberia, repo.ciudades()) { b, ciudades ->
        ciudades.firstOrNull { it.idCiudad == b?.idCiudad }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Crea la barbería (queda PENDIENTE de aprobación del administrador) o actualiza la actual.
     * ciudadTexto: "Armenia, Quindío" (si la ciudad no existe se crea).
     * onResultado(null) = guardado; onResultado(mensaje) = error.
     */
    fun guardarBarberia(
        nombre: String,
        ciudadTexto: String,
        direccion: String,
        descripcion: String,
        onResultado: (String?) -> Unit
    ) {
        val idBarbero = _idBarbero.value
        if (idBarbero <= 0) return onResultado("No hay sesión de barbero")
        if (nombre.isBlank() || ciudadTexto.isBlank() || direccion.isBlank()) {
            return onResultado("Nombre, ciudad y dirección son obligatorios")
        }
        val actual = barberia.value
        viewModelScope.launch {
            val ok = runCatching {
                val idCiudad = obtenerOCrearCiudad(ciudadTexto)
                if (actual != null) {
                    repo.actualizarBarberia(
                        actual.copy(
                            nombre = nombre.trim(),
                            idCiudad = idCiudad,
                            direccion = direccion.trim(),
                            descripcion = descripcion.trim().ifBlank { null }
                        )
                    )
                } else {
                    val id = repo.agregarBarberia(
                        BarberiaEntity(
                            idPropietario = idBarbero,
                            idCiudad = idCiudad,
                            nombre = nombre.trim(),
                            direccion = direccion.trim(),
                            descripcion = descripcion.trim().ifBlank { null },
                            estadoAprobacion = "PENDIENTE"
                        )
                    ).toInt()
                    repo.crearHorarioInicial(id)
                    _idBarberiaSel.value = id
                }
            }.isSuccess
            onResultado(if (ok) null else "No se pudo guardar la barbería")
        }
    }

    private suspend fun obtenerOCrearCiudad(texto: String): Int {
        val partes = texto.split(",").map { it.trim() }
        val nombre = partes[0]
        val departamento = partes.getOrNull(1).orEmpty().ifBlank { "Quindío" }
        return repo.obtenerOCrearCiudad(nombre, departamento)
    }

    // ---------------- Servicios y precios ----------------
    val servicios: StateFlow<List<ServicioEntity>> = barberia
        .flatMapLatest { b -> if (b == null) flowOf(emptyList()) else repo.todosLosServiciosDeBarberia(b.idBarberia) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Crea (servicio == null) o edita un servicio. Los servicios no se borran porque las citas
     * guardadas los referencian: se desactivan (activo = false) y el cliente deja de verlos.
     */
    fun guardarServicio(
        servicio: ServicioEntity?,
        nombre: String,
        duracionMin: Int,
        precio: Double,
        activo: Boolean,
        onResultado: (String?) -> Unit
    ) {
        val b = barberia.value ?: return onResultado("Primero registra tu barbería")
        if (nombre.isBlank()) return onResultado("Escribe el nombre del servicio")
        if (duracionMin !in 5..480) return onResultado("La duración debe estar entre 5 y 480 minutos")
        if (precio <= 0) return onResultado("El precio debe ser mayor a 0")
        viewModelScope.launch {
            val ok = runCatching {
                if (servicio == null) {
                    repo.agregarServicio(
                        ServicioEntity(
                            idBarberia = b.idBarberia, nombre = nombre.trim(),
                            duracionMinutos = duracionMin, precio = precio, activo = activo
                        )
                    )
                } else {
                    repo.actualizarServicio(
                        servicio.copy(nombre = nombre.trim(), duracionMinutos = duracionMin, precio = precio, activo = activo)
                    )
                }
            }.isSuccess
            onResultado(if (ok) null else "No se pudo guardar el servicio")
        }
    }

    // ---------------- Horarios de atención ----------------
    val horarios: StateFlow<List<HorarioAtencionEntity>> = barberia
        .flatMapLatest { b -> if (b == null) flowOf(emptyList()) else repo.horariosDeBarberia(b.idBarberia) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Un día de la pantalla "Horarios" mientras se edita. */
    data class HorarioDia(val diaSemana: Int, val abierto: Boolean, val apertura: String, val cierre: String)

    fun guardarHorarios(dias: List<HorarioDia>, onResultado: (String?) -> Unit) {
        val b = barberia.value ?: return onResultado("Primero registra tu barbería")
        val formato = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")
        for (d in dias.filter { it.abierto }) {
            if (!formato.matches(d.apertura) || !formato.matches(d.cierre)) {
                return onResultado("Usa el formato HH:mm (ej. 09:00)")
            }
            if (d.apertura >= d.cierre) return onResultado("La hora de cierre debe ser después de la apertura")
        }
        val actuales = horarios.value
        viewModelScope.launch {
            val ok = runCatching {
                for (d in dias) {
                    val fila = HorarioAtencionEntity(
                        idBarberia = b.idBarberia,
                        diaSemana = d.diaSemana,
                        horaApertura = if (d.abierto) d.apertura else null,
                        horaCierre = if (d.abierto) d.cierre else null,
                        cerrado = !d.abierto
                    )
                    val existente = actuales.firstOrNull { it.diaSemana == d.diaSemana }
                    if (existente != null) repo.actualizarHorario(fila.copy(idHorario = existente.idHorario))
                    else repo.agregarHorario(fila)
                }
            }.isSuccess
            onResultado(if (ok) null else "No se pudo guardar el horario")
        }
    }

    // ---------------- Citas recibidas ----------------
    val citas: StateFlow<List<CitaRecibida>> = _idBarbero
        .flatMapLatest { repo.citasRecibidas(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Acepta una cita pendiente. Si era "Sin preferencia" queda asignada a este barbero,
     * siempre que él no tenga otra cita que se cruce a esa hora.
     */
    fun aceptarCita(cita: CitaRecibida, onResultado: (String?) -> Unit) {
        val idBarbero = _idBarbero.value
        viewModelScope.launch {
            val choca = runCatching {
                repo.hayConflicto(idBarbero, cita.idBarberia, cita.fecha, cita.horaInicio, cita.horaFin, cita.idCita)
            }.getOrDefault(false)
            if (choca) {
                onResultado("Ya tienes otra cita a esa hora")
                return@launch
            }
            val ok = runCatching {
                repo.cambiarEstadoYAsignar(cita.idCita, EstadoCitaDb.CONFIRMADA, idBarbero)
                notificarCliente(cita, "Tu cita de ${cita.servicioNombre} del ${textoFecha(cita)} fue confirmada")
            }.isSuccess
            onResultado(if (ok) null else "No se pudo aceptar la cita")
        }
    }

    fun rechazarCita(cita: CitaRecibida, onResultado: (String?) -> Unit) {
        viewModelScope.launch {
            val ok = runCatching {
                repo.cambiarEstadoCita(cita.idCita, EstadoCitaDb.RECHAZADA)
                notificarCliente(cita, "Tu cita de ${cita.servicioNombre} del ${textoFecha(cita)} fue rechazada")
            }.isSuccess
            onResultado(if (ok) null else "No se pudo rechazar la cita")
        }
    }

    /** Marca como atendida una cita confirmada (cuenta en "Ingresos hoy"). */
    fun completarCita(cita: CitaRecibida) {
        viewModelScope.launch { runCatching { repo.cambiarEstadoCita(cita.idCita, EstadoCitaDb.COMPLETADA) } }
    }

    private fun textoFecha(c: CitaRecibida) = "${fechaCorta(c.fecha)} a las ${hora12(c.horaInicio)}"

    private suspend fun notificarCliente(cita: CitaRecibida, mensaje: String) {
        repo.crearNotificacion(
            NotificacionEntity(idUsuario = cita.idCliente, tipo = TipoNotificacion.CITA, mensaje = mensaje)
        )
    }

    // ---------------- Suscripción ----------------
    val suscripcion: StateFlow<SuscripcionEntity?> = _idBarbero
        .flatMapLatest { repo.suscripcionVigente(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** ACTIVA / POR_VENCER / VENCIDA calculado con las fechas; null = nunca ha tenido suscripción. */
    val estadoSuscripcion: StateFlow<String?> = suscripcion
        .map { s -> s?.let { estadoCalculado(it) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val pagos: StateFlow<List<PagoSuscripcionEntity>> = suscripcion
        .flatMapLatest { s -> if (s == null) flowOf(emptyList()) else repo.pagosDeSuscripcion(s.idSuscripcion) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        // Mantiene la columna `estado` al día (la usa el administrador en "Suscripciones")
        viewModelScope.launch {
            suscripcion.collect { s ->
                if (s != null) {
                    val calculado = estadoCalculado(s)
                    if (calculado != s.estado) runCatching { repo.cambiarEstadoSuscripcion(s.idSuscripcion, calculado) }
                }
            }
        }
    }

    /**
     * Renueva (o activa por primera vez) el plan mensual: suma un mes desde el vencimiento
     * (o desde hoy si ya venció) y registra el pago en pago_suscripcion.
     */
    fun renovarSuscripcion(onResultado: (String?) -> Unit) {
        val idBarbero = _idBarbero.value
        if (idBarbero <= 0) return onResultado("No hay sesión de barbero")
        val actual = suscripcion.value
        viewModelScope.launch {
            val ok = runCatching {
                val hoy = Calendar.getInstance()
                val desde = actual
                    ?.takeIf { it.fechaVencimiento >= Disponibilidad.iso(hoy) }
                    ?.let { Disponibilidad.calendarDe(it.fechaVencimiento) }
                    ?: hoy
                val vence = (desde.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                val idSuscripcion = if (actual == null) {
                    repo.agregarSuscripcion(
                        SuscripcionEntity(
                            idBarbero = idBarbero,
                            fechaInicio = Disponibilidad.iso(hoy),
                            fechaVencimiento = Disponibilidad.iso(vence),
                            estado = EstadoSuscripcion.ACTIVA
                        )
                    ).toInt()
                } else {
                    val renovada = actual.copy(fechaVencimiento = Disponibilidad.iso(vence))
                    repo.actualizarSuscripcion(renovada.copy(estado = estadoCalculado(renovada)))
                    actual.idSuscripcion
                }
                repo.registrarPago(
                    PagoSuscripcionEntity(
                        idSuscripcion = idSuscripcion,
                        monto = PRECIO_PLAN_MENSUAL,
                        metodoPago = "PSE",
                        referencia = "REF-${System.currentTimeMillis() % 1_000_000}"
                    )
                )
            }.isSuccess
            onResultado(if (ok) null else "No se pudo renovar la suscripción")
        }
    }
}
