package com.example.proyectofinal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectofinal.data.DatosEstaticos
import com.example.proyectofinal.navigation.Pantallas
import com.example.proyectofinal.data.*
import com.example.proyectofinal.viewmodel.BarberViewModel

/** Pantalla 3 · Disponibilidad: fecha y hora. Al continuar se crea la cita. */
@Composable
fun FechaHoraScreen(nav: NavController, vm: BarberViewModel) {
    val servicio by vm.servicioSel.collectAsState()
    val barbero by vm.barberoSel.collectAsState()
    val diaSel by vm.diaSel.collectAsState()
    val horaSel by vm.horaSel.collectAsState()

    Scaffold(
        containerColor = Crema,
        topBar = {
            CabeceraOscura("Elige fecha y hora", "${barbero?.nombre ?: ""} · ${servicio?.nombre ?: ""}")
        },
        bottomBar = {
            Box(Modifier.background(Crema).padding(16.dp)) {
                BotonPrimario("Continuar", enabled = diaSel != null && horaSel != null) {
                    // Siguiente paso: pantalla de confirmación (resumen de la cita)
                    nav.navigate(Pantallas.Confirmar.ruta)
                }
            }
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            item {
                EtiquetaSeccion(DatosEstaticos.MES)
                Row(Modifier.fillMaxWidth()) {
                    vm.dias.forEach { d ->
                        DiaChip(d, diaSel == d, Modifier.weight(1f)) { vm.elegirDia(d) }
                    }
                }
                EtiquetaSeccion("Horarios disponibles")
            }
            items(vm.horarios.chunked(3)) { fila ->
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
