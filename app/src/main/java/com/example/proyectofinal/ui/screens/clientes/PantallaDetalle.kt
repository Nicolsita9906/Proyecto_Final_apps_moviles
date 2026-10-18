package com.example.proyectofinal.ui.screens.clientes


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.proyectofinal.data.Barberia
import com.example.proyectofinal.data.Beige
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.Dorado
import com.example.proyectofinal.data.Gris
import com.example.proyectofinal.data.Oscuro
import com.example.proyectofinal.data.Servicio
import com.example.proyectofinal.data.listaServicios
import com.example.proyectofinal.ui.screens.BarraSuperior
import com.example.proyectofinal.ui.screens.BotonDorado
import com.example.proyectofinal.ui.screens.IconoCaja
import com.example.proyectofinal.ui.screens.TarjetaBlanca
import com.example.proyectofinal.ui.screens.subtituloBarbero
import com.example.proyectofinal.navigation.Pantallas
import com.example.proyectofinal.viewmodel.BarberViewModel

// ---------------------------------------------------------
// PANTALLA 2: DETALLE DE BARBERÍA
// ---------------------------------------------------------
@Composable
fun PantallaDetalle(navController: NavController, barberia: Barberia, vm: BarberViewModel) {
    val barberos by vm.barberos.collectAsState()
    var servicioSeleccionado by remember { mutableStateOf(0) }
    var tabSeleccionada by remember { mutableStateOf("Servicios") }

    Scaffold(
        containerColor = Crema,
        topBar = { BarraSuperior("Detalle de barbería", navController) },
        bottomBar = {
            BotonDorado("Reservar cita") {
                // Pasa la barbería y el servicio elegidos a la reserva
                vm.limpiarSeleccion()
                vm.elegirBarberia(barberia.id)
                vm.elegirServicio(listaServicios[servicioSeleccionado])
                navController.navigate(Pantallas.ServicioBarbero.ruta)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Información de la barbería
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(barberia.nombre, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                            Text("★ ${barberia.rating} (120) · Armenia, Quindío", color = Gris)
                        }
                        Box(
                            modifier = Modifier
                                .background(Dorado, RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (barberia.abierto) "ABIERTO" else "CERRADO",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${barberia.direccion} · ${barberia.horario}", color = Gris)
                }
            }

            // Pestañas
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Servicios", "Barberos", "Reseñas").forEach { nombre ->
                        val seleccionada = tabSeleccionada == nombre
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (seleccionada) Dorado else Beige,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { tabSeleccionada = nombre }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                nombre,
                                color = if (seleccionada) Oscuro else Gris,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Contenido de la pestaña
            if (tabSeleccionada == "Servicios") {
                itemsIndexed(listaServicios) { indice, servicio ->
                    ItemServicio(
                        servicio = servicio,
                        seleccionado = indice == servicioSeleccionado,
                        onClick = { servicioSeleccionado = indice }
                    )
                }
            } else if (tabSeleccionada == "Barberos") {
                items(barberos.filter { it.id != 0 }) { b ->
                    TarjetaBlanca {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconoCaja { Icon(Icons.Default.Person, null, tint = Dorado) }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(b.nombre, fontWeight = FontWeight.Bold)
                                Text(subtituloBarbero(b), color = Gris, fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                items(vm.resenas) { r ->
                    TarjetaBlanca {
                        Text(r.autor, fontWeight = FontWeight.Bold)
                        Text("★".repeat(r.estrellas), color = Dorado, fontSize = 12.sp)
                        Text(r.texto, color = Gris, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}


@Composable
fun ItemServicio(
    servicio: Servicio,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = if (seleccionado) {
            BorderStroke(2.dp, Dorado)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Beige,
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        id = servicio.icon
                    ),
                    contentDescription = servicio.nombre,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = servicio.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Text(
                    text = servicio.duracion,
                    color = Gris
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = servicio.precio,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}