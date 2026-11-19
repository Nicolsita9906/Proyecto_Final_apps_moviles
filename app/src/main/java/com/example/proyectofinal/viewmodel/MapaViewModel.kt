package com.example.proyectofinal.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinal.data.model.BarberiaEntity
import com.example.proyectofinal.data.model.Rol
import com.example.proyectofinal.data.model.UsuarioEntity
import com.example.proyectofinal.data.obtenerIdUsuario
import com.example.proyectofinal.data.obtenerRol
import com.example.proyectofinal.data.repository.BarberGoRepository
import com.example.proyectofinal.data.repository.MapaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Estado de la pantalla "Mapa de barberías" (Armenia).
 *
 *   PantallaMapa -> MapaViewModel -> MapaRepository    -> Nominatim (API, Retrofit)
 *                                 -> BarberGoRepository -> Room (barberías guardadas)
 *
 *  - CLIENTE: ve las barberías APROBADAS que tienen ubicación.
 *  - BARBERO: además ve las suyas aunque estén pendientes, y puede registrar/ubicar las suyas.
 *  - ADMINISTRADOR: ve todas y puede registrar una barbería para cualquier barbero (queda aprobada).
 */
class MapaViewModel(application: Application) : AndroidViewModel(application) {

    private val mapaRepo = MapaRepository()
    private val repo = BarberGoRepository.obtener(application)

    val rol: String = obtenerRol(application)
    private val idUsuario: Int = obtenerIdUsuario(application)

    /** Solo barberos y administradores pueden tocar el mapa para registrar una barbería. */
    val puedeRegistrar: Boolean = rol == Rol.BARBERO || rol == Rol.ADMINISTRADOR

    fun esPropia(b: BarberiaEntity): Boolean = b.idPropietario == idUsuario

    // ---------------- Barberías que se dibujan en el mapa ----------------
    val barberias: StateFlow<List<BarberiaEntity>> = repo.barberias()
        .map { lista ->
            lista.filter { b ->
                b.latitud != null && b.longitud != null && when (rol) {
                    Rol.ADMINISTRADOR -> true
                    Rol.BARBERO -> b.estadoAprobacion == "APROBADA" || b.idPropietario == idUsuario
                    else -> b.estadoAprobacion == "APROBADA"
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Barberías que se pueden mover al punto tocado: las propias (barbero) o todas (admin). */
    val barberiasUbicables: StateFlow<List<BarberiaEntity>> = repo.barberias()
        .map { lista -> if (rol == Rol.ADMINISTRADOR) lista else lista.filter { it.idPropietario == idUsuario } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Para el administrador: a qué barbero pertenece la barbería nueva. */
    val barberos: StateFlow<List<UsuarioEntity>> =
        (if (rol == Rol.ADMINISTRADOR) repo.usuariosPorRol(Rol.BARBERO) else flowOf(emptyList()))
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ---------------- Punto tocado + consulta a la API ----------------
    /** Coordenadas (lat, lon) del punto tocado. null = no hay ventana abierta. */
    var punto by mutableStateOf<Pair<Double, Double>?>(null)
        private set

    var uiState by mutableStateOf<UiState>(UiState.Idle)
        private set

    private var consulta: Job? = null

    /** Se llama al tocar el mapa: guarda el punto y le pide la dirección a la API. */
    fun seleccionarPunto(lat: Double, lon: Double) {
        if (!puedeRegistrar) return
        punto = lat to lon
        consulta?.cancel()            // si se toca otro punto antes de que responda, se ignora la anterior
        consulta = viewModelScope.launch {
            uiState = UiState.Loading
            uiState = try {
                UiState.Success(mapaRepo.getDireccion(lat, lon))
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                UiState.Error("No se pudo obtener la dirección. Revisa tu conexión o escríbela a mano.")
            }
        }
    }

    fun cerrarVentana() {
        consulta?.cancel()
        punto = null
        uiState = UiState.Idle
    }

    // ---------------- Guardar ----------------
    /**
     * idExistente = null -> crea una barbería nueva en el punto.
     * idExistente = id   -> solo mueve esa barbería al punto (y actualiza su dirección).
     * idPropietario: lo elige el administrador; para el barbero es él mismo.
     * onResultado(null) = guardado; onResultado(mensaje) = error.
     */
    fun guardar(
        idExistente: Int?,
        nombre: String,
        direccion: String,
        telefono: String,
        descripcion: String,
        idPropietario: Int?,
        onResultado: (String?) -> Unit
    ) {
        val (lat, lon) = punto ?: return onResultado("Toca un punto del mapa")
        if (direccion.isBlank()) return onResultado("La dirección es obligatoria")
        val esAdmin = rol == Rol.ADMINISTRADOR
        val dueno = if (esAdmin) idPropietario else idUsuario
        if (idExistente == null) {
            if (nombre.isBlank()) return onResultado("El nombre es obligatorio")
            if (dueno == null || dueno <= 0) return onResultado("Elige el barbero dueño de la barbería")
        }
        val respuesta = (uiState as? UiState.Success)?.data

        viewModelScope.launch {
            val ok = runCatching {
                if (idExistente != null) {
                    val actual = repo.barberiaPorId(idExistente) ?: error("No existe")
                    repo.actualizarBarberia(actual.copy(latitud = lat, longitud = lon, direccion = direccion.trim()))
                } else {
                    val idCiudad = repo.obtenerOCrearCiudad(
                        respuesta?.ciudad() ?: "Armenia",
                        respuesta?.departamento() ?: "Quindío"
                    )
                    val id = repo.agregarBarberia(
                        BarberiaEntity(
                            idPropietario = dueno!!,
                            idCiudad = idCiudad,
                            nombre = nombre.trim(),
                            direccion = direccion.trim(),
                            descripcion = descripcion.trim().ifBlank { null },
                            telefono = telefono.trim().ifBlank { null },
                            latitud = lat,
                            longitud = lon,
                            // La del barbero la revisa el administrador; la que crea el admin ya queda aprobada
                            estadoAprobacion = if (esAdmin) "APROBADA" else "PENDIENTE"
                        )
                    ).toInt()
                    repo.crearHorarioInicial(id)
                }
            }.isSuccess
            if (ok) cerrarVentana()
            onResultado(if (ok) null else "No se pudo guardar la barbería")
        }
    }
}
