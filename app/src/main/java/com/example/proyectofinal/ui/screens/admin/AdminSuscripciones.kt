package com.example.proyectofinal.ui.screens.admin

import android.widget.Toast
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
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
fun AdminSuscripcionesScreen(
    navController: NavController,
    vm: BarberViewModel = viewModel()
) {
    val context = LocalContext.current
    val suscripciones by vm.todasLasSuscripcionesAdmin.collectAsState()
    val usuarios by vm.todosLosUsuarios.collectAsState()
    var estadoFiltro by remember { mutableStateOf("TODAS") }

    val suscripcionesFiltradas = suscripciones.filter { s ->
        if (estadoFiltro == "TODAS") true
        else s.estado.uppercase() == estadoFiltro.uppercase()
    }

    Scaffold(
        topBar = { EncabezadoAdmin("Gestión de Suscripciones", navController) },
        bottomBar = { NavegacionInferiorAdmin(PestanaAdmin.SUSCRIPCIONES, navController) },
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
                ChipEstadoSuscripcion("Todas", estadoFiltro == "TODAS") { estadoFiltro = "TODAS" }
                ChipEstadoSuscripcion("Activas", estadoFiltro == "ACTIVA") { estadoFiltro = "ACTIVA" }
                ChipEstadoSuscripcion("Por Vencer", estadoFiltro == "POR_VENCER") { estadoFiltro = "POR_VENCER" }
                ChipEstadoSuscripcion("Vencidas", estadoFiltro == "VENCIDA") { estadoFiltro = "VENCIDA" }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${suscripcionesFiltradas.size} suscripción(es)",
                fontSize = 12.sp,
                color = Gris
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de suscripciones
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(suscripcionesFiltradas) { s ->
                    val barbero = usuarios.firstOrNull { it.idUsuario == s.idBarbero }
                    val nombreBarbero = barbero?.nombre ?: "Barbero #${s.idBarbero}"

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
                                        text = nombreBarbero,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Crema
                                    )
                                    Text(
                                        text = "Vence: ${s.fechaVencimiento}",
                                        fontSize = 12.sp,
                                        color = Gris
                                    )
                                }
                                BadgeEstadoAdmin(s.estado)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { vm.cambiarEstadoSuscripcion(s.idSuscripcion, "ACTIVA") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Verde,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Activar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { vm.cambiarEstadoSuscripcion(s.idSuscripcion, "VENCIDA") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Rojo,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Vencer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "Recordatorio enviado a $nombreBarbero", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Enviar recordatorio de pago", fontSize = 11.sp, color = Dorado)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipEstadoSuscripcion(
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
