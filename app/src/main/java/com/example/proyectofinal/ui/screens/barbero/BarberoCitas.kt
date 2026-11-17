package com.example.proyectofinal.ui.screens.barbero

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.Disponibilidad
import com.example.proyectofinal.data.Gris
import com.example.proyectofinal.data.model.EstadoCitaDb
import com.example.proyectofinal.ui.screens.Pestanas
import com.example.proyectofinal.viewmodel.BarberoViewModel

/**
 * Mockup "Citas recibidas": las citas de hoy con Aceptar / Rechazar para las pendientes.
 * Pestañas extra: Próximas (días siguientes) e Historial (días anteriores).
 */
@Composable
fun BarberoCitasScreen(nav: NavController, vm: BarberoViewModel) {
    val context = LocalContext.current
    val citas by vm.citas.collectAsState()
    val estadoSus by vm.estadoSuscripcion.collectAsState()
    var tab by remember { mutableIntStateOf(0) }

    val hoy = Disponibilidad.hoyIso()
    val lista = when (tab) {
        0 -> citas.filter { it.fecha == hoy }
        1 -> citas.filter { it.fecha > hoy }
        else -> citas.filter { it.fecha < hoy }.reversed()
    }
    val titulos = listOf("Citas de hoy", "Próximas citas", "Historial")

    fun avisar(error: String?, ok: String) {
        Toast.makeText(context, error ?: ok, Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        containerColor = Crema,
        topBar = { CabeceraBarbero(titulos[tab]) },
        bottomBar = { NavegacionBarbero(PestanaBarbero.CITAS, nav) }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            // Sin suscripción vigente no se pueden consultar ni gestionar las citas
            if (!suscripcionVigente(estadoSus)) {
                item { AvisoSuscripcion(estadoSus, nav) }
                return@LazyColumn
            }
            item { Pestanas(listOf("Hoy", "Próximas", "Historial"), tab) { tab = it } }
            if (lista.isEmpty()) {
                item { Text("No hay citas aquí", color = Gris, fontSize = 14.sp, modifier = Modifier.padding(16.dp)) }
            }
            items(lista, key = { it.idCita }) { c ->
                // Solo se gestionan citas de hoy o futuras
                val gestionable = c.fecha >= hoy
                TarjetaCitaBarbero(
                    cita = c,
                    mostrarFecha = tab != 0,
                    onAceptar = if (gestionable) ({ vm.aceptarCita(c) { avisar(it, "Cita confirmada") } }) else null,
                    onRechazar = if (gestionable) ({ vm.rechazarCita(c) { avisar(it, "Cita rechazada") } }) else null,
                    onCompletar = if (c.fecha <= hoy && c.estado == EstadoCitaDb.CONFIRMADA) ({ vm.completarCita(c) }) else null
                )
            }
        }
    }
}
