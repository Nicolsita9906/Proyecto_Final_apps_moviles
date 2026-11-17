package com.example.proyectofinal.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.proyectofinal.navigation.Pantallas
import com.example.proyectofinal.ui.screens.Rojo
import com.example.proyectofinal.ui.screens.Verde
import com.example.proyectofinal.viewmodel.BarberViewModel

@Composable
fun AdminPanelScreen(
    navController: NavController,
    vm: BarberViewModel = viewModel()
) {
    val usuarios by vm.todosLosUsuarios.collectAsState()
    val barberias by vm.todasLasBarberiasAdmin.collectAsState()
    val suscripciones by vm.todasLasSuscripcionesAdmin.collectAsState()

    val totalUsuarios = usuarios.size
    val totalBarberias = barberias.size
    val suscripcionesActivas = suscripciones.count { it.estado.uppercase() == "ACTIVA" }
    val suscripcionesVencidas = suscripciones.count { it.estado.uppercase() == "VENCIDA" || it.estado.uppercase() == "POR_VENCER" }

    Scaffold(
        topBar = { EncabezadoAdmin("Panel General", navController) },
        bottomBar = { NavegacionInferiorAdmin(PestanaAdmin.PANEL, navController) },
        containerColor = FondoOscuro
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Resumen de la Plataforma",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Crema
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Grid de métricas generales (2x2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TarjetaMetrica(
                    titulo = "Usuarios",
                    valor = totalUsuarios.toString(),
                    modifier = Modifier.weight(1f)
                )
                TarjetaMetrica(
                    titulo = "Barberías",
                    valor = totalBarberias.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TarjetaMetrica(
                    titulo = "Suscrip. Activas",
                    valor = suscripcionesActivas.toString(),
                    modifier = Modifier.weight(1f),
                    colorValor = Verde
                )
                TarjetaMetrica(
                    titulo = "Suscrip. Vencidas",
                    valor = suscripcionesVencidas.toString(),
                    modifier = Modifier.weight(1f),
                    colorValor = Rojo
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Accesos Rápidos
            Text(
                text = "Accesos Rápidos",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Crema
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BotonAccesoRapidoAdmin("Gestionar Usuarios", Modifier.weight(1f)) {
                    navController.navigate(Pantallas.AdminUsuarios.ruta)
                }
                BotonAccesoRapidoAdmin("Gestionar Barberías", Modifier.weight(1f)) {
                    navController.navigate(Pantallas.AdminBarberias.ruta)
                }
                BotonAccesoRapidoAdmin("Suscripciones", Modifier.weight(1f)) {
                    navController.navigate(Pantallas.AdminSuscripciones.ruta)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Usuarios Recientes
            Text(
                text = "Usuarios Registrados Recientemente",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Crema
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (usuarios.isEmpty()) {
                Text("No hay usuarios registrados aún.", color = Gris, fontSize = 14.sp)
            } else {
                usuarios.take(4).forEach { u ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Oscuro),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(u.nombre, fontWeight = FontWeight.Bold, color = Crema, fontSize = 14.sp)
                                Text("${u.correo} • ${u.rol}", color = Gris, fontSize = 11.sp)
                            }
                            BadgeEstadoAdmin(u.estado)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonAccesoRapidoAdmin(
    titulo: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Oscuro),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(titulo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Dorado)
        }
    }
}
