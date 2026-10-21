package com.example.proyectofinal.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.Dorado
import com.example.proyectofinal.data.FondoOscuro
import com.example.proyectofinal.data.Gris
import com.example.proyectofinal.data.Oscuro
import com.example.proyectofinal.data.cerrarSesion
import com.example.proyectofinal.navigation.Pantallas
import com.example.proyectofinal.ui.screens.AmbarPendiente
import com.example.proyectofinal.ui.screens.Rojo
import com.example.proyectofinal.ui.screens.Verde

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncabezadoAdmin(
    titulo: String,
    navController: NavController
) {
    val context = LocalContext.current
    TopAppBar(
        title = {
            Column {
                Text(
                    text = titulo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Crema
                )
                Text(
                    text = "Panel de Administración",
                    fontSize = 12.sp,
                    color = Dorado
                )
            }
        },
        actions = {
            IconButton(
                onClick = {
                    cerrarSesion(context)
                    navController.navigate(Pantallas.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                Text("🚪", fontSize = 20.sp)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Oscuro,
            titleContentColor = Crema
        )
    )
}

@Composable
fun TarjetaMetrica(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier,
    colorValor: Color = Crema
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Oscuro),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = valor,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorValor
            )
            Text(
                text = titulo,
                fontSize = 12.sp,
                color = Gris
            )
        }
    }
}

@Composable
fun BadgeEstadoAdmin(estado: String) {
    val (colorFondo, texto) = when (estado.uppercase()) {
        "ACTIVO", "APROBADA", "ACTIVA" -> Verde to estado.lowercase().replaceFirstChar { it.uppercase() }
        "POR_VENCER", "PENDIENTE" -> AmbarPendiente to if (estado.uppercase() == "POR_VENCER") "Por vencer" else "Pendiente"
        "BLOQUEADO", "SUSPENDIDA", "VENCIDA", "RECHAZADA" -> Rojo to estado.lowercase().replaceFirstChar { it.uppercase() }
        else -> Gris to estado
    }

    Box(
        modifier = Modifier
            .background(color = colorFondo, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = texto,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

enum class PestanaAdmin { PANEL, USUARIOS, BARBERIAS, SUSCRIPCIONES }

@Composable
fun NavegacionInferiorAdmin(
    pestanaActual: PestanaAdmin,
    navController: NavController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Oscuro)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ElementoNavAdmin("📊", "Panel", pestanaActual == PestanaAdmin.PANEL) {
            if (pestanaActual != PestanaAdmin.PANEL) {
                navController.navigate(Pantallas.AdminPanel.ruta) {
                    popUpTo(Pantallas.AdminPanel.ruta) { inclusive = true }
                }
            }
        }
        ElementoNavAdmin("👥", "Usuarios", pestanaActual == PestanaAdmin.USUARIOS) {
            if (pestanaActual != PestanaAdmin.USUARIOS) {
                navController.navigate(Pantallas.AdminUsuarios.ruta) {
                    popUpTo(Pantallas.AdminPanel.ruta)
                }
            }
        }
        ElementoNavAdmin("💈", "Barberías", pestanaActual == PestanaAdmin.BARBERIAS) {
            if (pestanaActual != PestanaAdmin.BARBERIAS) {
                navController.navigate(Pantallas.AdminBarberias.ruta) {
                    popUpTo(Pantallas.AdminPanel.ruta)
                }
            }
        }
        ElementoNavAdmin("💳", "Suscrip.", pestanaActual == PestanaAdmin.SUSCRIPCIONES) {
            if (pestanaActual != PestanaAdmin.SUSCRIPCIONES) {
                navController.navigate(Pantallas.AdminSuscripciones.ruta) {
                    popUpTo(Pantallas.AdminPanel.ruta)
                }
            }
        }
    }
}

@Composable
private fun ElementoNavAdmin(
    icono: String,
    etiqueta: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(icono, fontSize = 18.sp)
        Text(
            text = etiqueta,
            fontSize = 10.sp,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
            color = if (seleccionado) Dorado else Gris
        )
    }
}
