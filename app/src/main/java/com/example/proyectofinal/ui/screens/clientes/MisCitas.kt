package com.example.proyectofinal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.data.Cita
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.Gris
import com.example.proyectofinal.navigation.Pantallas
import com.example.proyectofinal.viewmodel.BarberViewModel

/** Pantalla 4 · Mis citas (Próximas / Historial). */
@Composable
fun MisCitasScreen(nav: NavController, vm: BarberViewModel) {
    val citas by vm.citas.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    val proximas = setOf("Confirmada", "Pendiente")
    val lista = citas.filter { (it.estado in proximas) == (tab == 0) }

    Scaffold(
        containerColor = Crema,
        topBar = { CabeceraOscura("Mis citas") },
        bottomBar = { BarraInferior(nav, Pantallas.MisCitas.ruta) }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            item { Pestanas(listOf("Próximas", "Historial"), tab) { tab = it } }
            if (lista.isEmpty()) {
                item { Text("No hay citas aquí", color = Gris, modifier = Modifier.padding(16.dp)) }
            }
            items(lista) { c ->
                CitaCard(c, vm.barberiaDe(c.id)) { nav.navigate(Pantallas.DetalleCita.crear(c.id)) }
            }
        }
    }
}

@Composable
fun CitaCard(cita: Cita, barberia: String, onClick: () -> Unit) {
    TarjetaBlanca(Modifier.padding(vertical = 5.dp), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(barberia, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                modifier = Modifier.weight(1f))
            Insignia(cita.estado, colorEstado(cita.estado))
        }
        Spacer(Modifier.height(4.dp))
        Text("${cita.servicio.nombre} · ${cita.barbero.nombre}", fontSize = 13.sp, color = Gris)
        Text("${cita.fecha} · ${cita.hora}", fontSize = 13.sp, color = Gris)
    }
}
