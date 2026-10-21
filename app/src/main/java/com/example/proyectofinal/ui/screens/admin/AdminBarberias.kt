package com.example.proyectofinal.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.Dorado
import com.example.proyectofinal.data.FondoOscuro
import com.example.proyectofinal.data.Gris
import com.example.proyectofinal.data.Oscuro
import com.example.proyectofinal.ui.screens.Rojo
import com.example.proyectofinal.ui.screens.Verde
import com.example.proyectofinal.viewmodel.BarberViewModel

@Composable
fun AdminBarberiasScreen(
    navController: NavController,
    vm: BarberViewModel = viewModel()
) {
    val barberias by vm.todasLasBarberiasAdmin.collectAsState()
    var estadoFiltro by remember { mutableStateOf("TODAS") }

    val barberiasFiltradas = barberias.filter { b ->
        if (estadoFiltro == "TODAS") true
        else b.estadoAprobacion.equals(estadoFiltro, ignoreCase = true)
    }

    Scaffold(
        topBar = { EncabezadoAdmin("Gestión de Barberías", navController) },
        bottomBar = { NavegacionInferiorAdmin(PestanaAdmin.BARBERIAS, navController) },
        containerColor = FondoOscuro
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Filtro por Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChipEstadoBarberia("Todas", estadoFiltro == "TODAS") { estadoFiltro = "TODAS" }
                ChipEstadoBarberia("Aprobadas", estadoFiltro == "APROBADA") { estadoFiltro = "APROBADA" }
                ChipEstadoBarberia("Pendientes", estadoFiltro == "PENDIENTE") { estadoFiltro = "PENDIENTE" }
                ChipEstadoBarberia("Suspendidas", estadoFiltro == "SUSPENDIDA") { estadoFiltro = "SUSPENDIDA" }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${barberiasFiltradas.size} barbería(s) en total",
                fontSize = 12.sp,
                color = Gris
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de barberías
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(barberiasFiltradas) { b ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Oscuro),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = b.nombre,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Crema
                                    )
                                    Text(
                                        text = b.direccion,
                                        fontSize = 12.sp,
                                        color = Gris
                                    )
                                }
                                BadgeEstadoAdmin(b.estadoAprobacion)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Botones de acción según el estado
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (b.estadoAprobacion.uppercase() != "APROBADA") {
                                    Button(
                                        onClick = { vm.cambiarEstadoBarberia(b, "APROBADA") },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Verde,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Aprobar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (b.estadoAprobacion.uppercase() != "SUSPENDIDA") {
                                    Button(
                                        onClick = { vm.cambiarEstadoBarberia(b, "SUSPENDIDA") },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Rojo,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Suspender", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipEstadoBarberia(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (seleccionado) Dorado else Oscuro,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = texto,
            fontSize = 11.sp,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
            color = if (seleccionado) Oscuro else Crema
        )
    }
}
