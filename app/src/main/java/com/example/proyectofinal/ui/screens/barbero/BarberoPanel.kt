package com.example.proyectofinal.ui.screens.barbero

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.Disponibilidad
import com.example.proyectofinal.data.Gris
import com.example.proyectofinal.data.mapper.hora12
import com.example.proyectofinal.data.model.EstadoCitaDb
import com.example.proyectofinal.data.obtenerNombre
import com.example.proyectofinal.navigation.Pantallas
import com.example.proyectofinal.ui.screens.EtiquetaSeccion
import com.example.proyectofinal.ui.screens.Insignia
import com.example.proyectofinal.ui.screens.TarjetaBlanca
import com.example.proyectofinal.viewmodel.BarberoViewModel
import java.util.Calendar
import java.util.Locale

/** Mockup "Panel (Dashboard)" del barbero. */
@Composable
fun BarberoPanelScreen(nav: NavController, vm: BarberoViewModel) {
    val context = LocalContext.current
    val citas by vm.citas.collectAsState()
    val estadoSus by vm.estadoSuscripcion.collectAsState()
    val barberia by vm.barberia.collectAsState()

    val nombre = obtenerNombre(context).substringBefore(" ").ifBlank { "Barbero" }
    val hoy = Disponibilidad.hoyIso()
    val ahora = Calendar.getInstance().let { "%02d:%02d".format(it.get(Calendar.HOUR_OF_DAY), it.get(Calendar.MINUTE)) }
    val activas = setOf(EstadoCitaDb.PENDIENTE, EstadoCitaDb.CONFIRMADA, EstadoCitaDb.COMPLETADA)
    val citasHoy = citas.filter { it.fecha == hoy && it.estado in activas }
    val ingresosHoy = citasHoy
        .filter { it.estado == EstadoCitaDb.CONFIRMADA || it.estado == EstadoCitaDb.COMPLETADA }
        .sumOf { it.valorTotal }
    val proximas = citasHoy
        .filter { it.estado != EstadoCitaDb.COMPLETADA && it.horaInicio >= ahora }
        .take(3)
    val vigente = suscripcionVigente(estadoSus)

    Scaffold(
        containerColor = Crema,
        topBar = {
            CabeceraBarbero("Hola, $nombre") {
                val (texto, color) = estadoSuscripcionTexto(estadoSus)
                Insignia(texto, color)
            }
        },
        bottomBar = { NavegacionBarbero(PestanaBarbero.PANEL, nav) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // Aviso de la barbería (sin registrar / pendiente de aprobación)
            when {
                barberia == null -> SinBarberia(nav)
                barberia?.estadoAprobacion != "APROBADA" -> TarjetaBlanca(Modifier.padding(bottom = 12.dp)) {
                    Text("Barbería pendiente de aprobación", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        "Los clientes verán \"${barberia?.nombre}\" cuando el administrador la apruebe.",
                        color = Gris, fontSize = 13.sp
                    )
                }
            }

            // Métricas del día
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Metrica(if (vigente) citasHoy.size.toString() else "—", "Citas hoy", Modifier.weight(1f))
                Metrica(if (vigente) montoCorto(ingresosHoy) else "—", "Ingresos hoy", Modifier.weight(1f))
            }

            EtiquetaSeccion("Próximas citas")
            when {
                !vigente -> AvisoSuscripcion(estadoSus, nav)
                proximas.isEmpty() -> Text("No tienes más citas hoy", color = Gris, fontSize = 14.sp)
                else -> proximas.forEach { c ->
                    FilaPersona(Icons.Default.Person, c.clienteNombre, "${hora12(c.horaInicio)} · ${c.servicioNombre}")
                }
            }

            EtiquetaSeccion("Accesos")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AccesoBarbero(Icons.Outlined.Storefront, "Barbería") { nav.navigate(Pantallas.BarberoBarberia.ruta) }
                AccesoBarbero(Icons.Outlined.ContentCut, "Servicios") { nav.navigate(Pantallas.BarberoServicios.ruta) }
                AccesoBarbero(Icons.Outlined.Schedule, "Horarios") { nav.navigate(Pantallas.BarberoHorarios.ruta) }
                AccesoBarbero(Icons.Outlined.CreditCard, "Suscripción") { nav.navigate(Pantallas.BarberoSuscripcion.ruta) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Metrica(valor: String, titulo: String, modifier: Modifier) {
    TarjetaBlanca(modifier) {
        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(valor, fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Text(titulo, color = Gris, fontSize = 13.sp)
        }
    }
}

/** 180000 -> "$180K", 1250000 -> "$1.3M", 9500 -> "$9.5K". */
fun montoCorto(valor: Double): String = when {
    valor >= 1_000_000 -> "$" + String.format(Locale.US, "%.1f", valor / 1_000_000).removeSuffix(".0") + "M"
    valor >= 1_000 -> "$" + String.format(Locale.US, "%.1f", valor / 1_000).removeSuffix(".0") + "K"
    else -> "$" + valor.toInt()
}
