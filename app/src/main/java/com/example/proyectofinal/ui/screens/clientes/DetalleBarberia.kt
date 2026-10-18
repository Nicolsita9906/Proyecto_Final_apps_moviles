package com.example.proyectofinal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.Dorado
import com.example.proyectofinal.data.Gris
import com.example.proyectofinal.data.Oscuro
import com.example.proyectofinal.navigation.Pantallas
import com.example.proyectofinal.viewmodel.BarberViewModel

/** Pantalla 1 · Detalle de barbería (Home). Pestañas: Servicios / Barberos / Reseñas. */
@Composable
fun DetalleBarberiaScreen(nav: NavController, vm: BarberViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    val servicios by vm.servicios.collectAsState()
    val barberos by vm.barberos.collectAsState()
    val b = vm.barberia

    Scaffold(
        containerColor = Crema,
        bottomBar = {
            Box(Modifier.background(Crema).padding(16.dp)) {
                BotonPrimario("Reservar cita") {
                    vm.limpiarSeleccion()
                    nav.navigate(Pantallas.ServicioBarbero.ruta)
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Box(
                Modifier.fillMaxWidth().height(130.dp).background(Oscuro),
                contentAlignment = Alignment.Center
            ) { Text("✂", color = Dorado, fontSize = 44.sp) }

            LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(b.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f))
                        if (b.abierto) Insignia("ABIERTO", Dorado, Oscuro)
                    }
                    Text("★ ${b.rating} · ${b.distancia}", fontSize = 12.sp, color = Gris)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Gris, modifier = Modifier.size(14.dp))
                        Text(" ${b.direccion} · ${b.horario}", fontSize = 12.sp, color = Gris)
                    }
                    Pestanas(listOf("Servicios", "Barberos", "Reseñas"), tab) { tab = it }
                }
                when (tab) {
                    0 -> items(servicios) { s ->
                        Row(
                            Modifier.fillMaxWidth().clickable { vm.elegirServicio(s) }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconoCaja {
                                Icon(painterResource(s.icon), null, tint = Dorado, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(s.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(s.duracion, fontSize = 12.sp, color = Gris)
                            }
                            Text(s.precio, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    1 -> items(barberos.filter { it.id != 0 }) { br ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            IconoCaja { Icon(Icons.Default.Person, null, tint = Dorado) }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(br.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(subtituloBarbero(br), fontSize = 12.sp, color = Gris)
                            }
                        }
                    }
                    else -> items(vm.resenas) { r ->
                        TarjetaBlanca(Modifier.padding(vertical = 5.dp)) {
                            Text(r.autor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("★".repeat(r.estrellas), color = Dorado, fontSize = 12.sp)
                            Text(r.texto, fontSize = 12.sp, color = Gris)
                        }
                    }
                }
            }
        }
    }
}
