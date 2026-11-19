package com.example.proyectofinal.ui.screens.clientes

import com.example.proyectofinal.ui.screens.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.data.Disponibilidad
import com.example.proyectofinal.ui.navigation.Pantallas
import com.example.proyectofinal.data.*
import com.example.proyectofinal.viewmodel.BarberViewModel

/**
 * Pantalla 3 · Disponibilidad: fecha y hora.
 * Los días salen del horario de la barbería y las horas ocupadas (otra cita, bloqueo
 * o ya pasadas) aparecen tachadas y no se pueden elegir.
 */
@Composable
fun FechaHoraScreen(nav: NavController, vm: BarberViewModel) {
    val servicio by vm.servicioSel.collectAsState()
    val barbero by vm.barberoSel.collectAsState()
    val diaSel by vm.diaSel.collectAsState()
    val horaSel by vm.horaSel.collectAsState()
    val dias by vm.dias.collectAsState()
    val horarios by vm.horarios.collectAsState()

    // Si no hay día elegido (o el elegido ya no está en la lista), se elige el primero
    LaunchedEffect(dias) {
        val primero = dias.firstOrNull()
        if (primero != null && (diaSel == null || diaSel !in dias)) vm.elegirDia(primero)
    }

    val horaValida = horaSel != null && horarios.any { it == horaSel && it.disponible }

    Scaffold(
        containerColor = Crema,
        topBar = {
            CabeceraOscura("Elige fecha y hora", "${barbero?.nombre ?: ""} · ${servicio?.nombre ?: ""}")
        },
        bottomBar = {
            Box(Modifier.background(Crema).padding(16.dp)) {
                BotonPrimario("Continuar", enabled = diaSel != null && horaValida) {
                    // Siguiente paso: pantalla de confirmación (resumen de la cita)
                    nav.navigate(Pantallas.Confirmar.ruta)
                }
            }
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            if (dias.isEmpty()) {
                item {
                    Text(
                        "Esta barbería no tiene horario de atención disponible.",
                        color = Gris, fontSize = 14.sp, modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                return@LazyColumn
            }
            item {
                EtiquetaSeccion(Disponibilidad.textoMes(dias))
                Row(Modifier.fillMaxWidth()) {
                    dias.forEach { d ->
                        DiaChip(d, diaSel == d, Modifier.weight(1f)) { vm.elegirDia(d) }
                    }
                }
                EtiquetaSeccion("Horarios disponibles")
            }
            if (horarios.none { it.disponible }) {
                item {
                    Text(
                        "No quedan horarios libres este día. Prueba con otro.",
                        color = Gris, fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            items(horarios.chunked(3)) { fila ->
                Row(Modifier.fillMaxWidth()) {
                    fila.forEach { h ->
                        HoraChip(h, horaSel == h, Modifier.weight(1f)) { vm.elegirHora(h) }
                    }
                    repeat(3 - fila.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
