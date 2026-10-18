package com.example.proyectofinal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.data.Barbero
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.Dorado
import com.example.proyectofinal.data.Gris
import com.example.proyectofinal.navigation.Pantallas
import com.example.proyectofinal.viewmodel.BarberViewModel

/** Texto secundario del barbero: "★ 4.9 · Disponible hoy" (o solo el texto si no tiene calificación). */
fun subtituloBarbero(b: Barbero): String =
    if (b.calificacion > 0) "★ ${b.calificacion} · ${b.especialidad}" else b.especialidad

/** Pantalla 2 · Elegir servicio y barbero (Nueva reserva). */
@Composable
fun ServicioBarberoScreen(nav: NavController, vm: BarberViewModel) {
    val servicios by vm.servicios.collectAsState()
    val barberos by vm.barberos.collectAsState()
    val servicioSel by vm.servicioSel.collectAsState()
    val barberoSel by vm.barberoSel.collectAsState()

    Scaffold(
        containerColor = Crema,
        topBar = { CabeceraOscura("Nueva reserva", vm.barberia.nombre) },
        bottomBar = {
            Box(Modifier.background(Crema).padding(16.dp)) {
                BotonPrimario("Continuar", enabled = servicioSel != null && barberoSel != null) {
                    nav.navigate(Pantallas.FechaHora.ruta)
                }
            }
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            item { EtiquetaSeccion("Servicio") }
            items(servicios) { s ->
                TarjetaBlanca(
                    Modifier.padding(vertical = 4.dp),
                    seleccionada = servicioSel?.nombre == s.nombre,
                    onClick = { vm.elegirServicio(s) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(s.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(s.duracion, fontSize = 12.sp, color = Gris)
                        }
                        Text(s.precio, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
            item { EtiquetaSeccion("Barbero") }
            items(barberos) { br ->
                TarjetaBlanca(
                    Modifier.padding(vertical = 4.dp),
                    seleccionada = barberoSel?.id == br.id,
                    onClick = { vm.elegirBarbero(br) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconoCaja { Icon(Icons.Default.Person, null, tint = Dorado) }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(br.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(subtituloBarbero(br), fontSize = 12.sp, color = Gris)
                        }
                    }
                }
            }
        }
    }
}
