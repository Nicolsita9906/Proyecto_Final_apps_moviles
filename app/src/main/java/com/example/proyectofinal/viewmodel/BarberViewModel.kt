package com.example.proyectofinal.viewmodel

import androidx.lifecycle.ViewModel
import com.example.proyectofinal.data.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Estado compartido de las pantallas de cliente (StateFlow + collectAsState).
 * Datos de DatosEstaticos; "TODO PERSISTENCIA" marca dónde conectar BD/API.
 */
class BarberViewModel : ViewModel() {

    // TODO PERSISTENCIA: cargar desde repositorio
    val barberias: List<Barberia> = DatosEstaticos.barberias

    // Barbería elegida en la pantalla de inicio
    private val _barberiaSel = MutableStateFlow(DatosEstaticos.barberias[0])
    val barberiaSel: StateFlow<Barberia> = _barberiaSel
    val barberia: Barberia get() = _barberiaSel.value
    fun elegirBarberia(id: Int) {
        barberias.firstOrNull { it.id == id }?.let { _barberiaSel.value = it }
    }

    /** true justo después de iniciar sesión o registrarse: muestra el Splash una vez. */
    var splashPendiente by mutableStateOf(false)

    fun direccionDe(citaId: Int): String {
        val nombre = barberiaDe(citaId)
        val dir = barberias.firstOrNull { it.nombre == nombre }?.direccion ?: ""
        return "$dir, Armenia, Quindío"
    }
    val resenas: List<Resena> = DatosEstaticos.resenas
    val dias: List<Dia> = DatosEstaticos.dias
    val horarios: List<Horario> = DatosEstaticos.horarios

    private val _servicios = MutableStateFlow(DatosEstaticos.servicios)
    val servicios: StateFlow<List<Servicio>> = _servicios

    private val _barberos = MutableStateFlow(DatosEstaticos.barberos)
    val barberos: StateFlow<List<Barbero>> = _barberos

    private val _citas = MutableStateFlow(DatosEstaticos.citasIniciales)
    val citas: StateFlow<List<Cita>> = _citas

    // Nombre de la barbería de cada cita (el modelo Cita no lo incluye)
    private val barberiaDeCita = DatosEstaticos.barberiaPorCita.toMutableMap()
    fun barberiaDe(citaId: Int): String = barberiaDeCita[citaId] ?: barberia.nombre

    // Selección del flujo de reserva
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
        limpiarSeleccion()
        _servicioSel.value = c.servicio
        _barberoSel.value = c.barbero
        editandoId = c.id
    }

    /** Crea (o reemplaza, si se está modificando) la cita con la selección actual. */
    fun confirmarCita(): Boolean {
        val s = _servicioSel.value ?: return false
        val b = _barberoSel.value ?: return false
        val d = _diaSel.value ?: return false
        val h = _horaSel.value ?: return false
        val estado = if (b.id == 0) "Pendiente" else "Confirmada"
        val fecha = "${d.numero} ${DatosEstaticos.MES_CORTO}"
        val editId = editandoId
        if (editId != null) {
            // TODO PERSISTENCIA: UPDATE cita SET servicio=?, barbero_id=?, fecha=?, hora=? WHERE id=?
            _citas.value = _citas.value.map {
                if (it.id == editId) it.copy(servicio = s, barbero = b, fecha = fecha, hora = h.completa, estado = estado) else it
            }
        } else {
            val nuevoId = (_citas.value.maxOfOrNull { it.id } ?: 0) + 1
            // TODO PERSISTENCIA: INSERT INTO cita (...) y usar el id generado
            barberiaDeCita[nuevoId] = barberia.nombre
            _citas.value = _citas.value + Cita(nuevoId, s, b, fecha, h.completa, estado)
        }
        limpiarSeleccion()
        return true
    }

    fun cancelarCita(id: Int) {
        // TODO PERSISTENCIA: UPDATE cita SET estado = 'Cancelada' WHERE id = ?
        _citas.value = _citas.value.map { if (it.id == id) it.copy(estado = "Cancelada") else it }
    }

    fun citaPorId(id: Int): Cita? = _citas.value.firstOrNull { it.id == id }
}
