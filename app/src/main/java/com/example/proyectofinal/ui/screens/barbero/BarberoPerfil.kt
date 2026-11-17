package com.example.proyectofinal.ui.screens.barbero

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.data.Dorado
import com.example.proyectofinal.data.Oscuro
import com.example.proyectofinal.data.cerrarSesion
import com.example.proyectofinal.data.obtenerNombre
import com.example.proyectofinal.data.obtenerSesion
import com.example.proyectofinal.navigation.Pantallas
import com.example.proyectofinal.ui.screens.FondoPerfil

/** Perfil del barbero (pestaña "Perfil"): datos de la cuenta, atajos y cerrar sesión. */
@Composable
fun BarberoPerfilScreen(nav: NavController) {
    val context = LocalContext.current
    val nombre = obtenerNombre(context).ifBlank { "Barbero" }
    val correo = obtenerSesion(context).orEmpty()

    val opciones: List<Triple<ImageVector, String, String>> = listOf(
        Triple(Icons.Outlined.Storefront, "Mi barbería", Pantallas.BarberoBarberia.ruta),
        Triple(Icons.Outlined.ContentCut, "Servicios y precios", Pantallas.BarberoServicios.ruta),
        Triple(Icons.Outlined.Schedule, "Horarios de atención", Pantallas.BarberoHorarios.ruta),
        Triple(Icons.Outlined.CreditCard, "Mi suscripción", Pantallas.BarberoSuscripcion.ruta)
    )

    Scaffold(
        containerColor = FondoPerfil,
        topBar = { CabeceraBarbero("Mi perfil") },
        bottomBar = { NavegacionBarbero(PestanaBarbero.PERFIL, nav, oscura = true) }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(horizontal = 24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Box(
                Modifier.size(64.dp).clip(RoundedCornerShape(14.dp)).background(Oscuro),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Person, null, tint = Dorado, modifier = Modifier.size(32.dp)) }
            Spacer(Modifier.height(10.dp))
            Text(nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("Barbero · $correo", color = Color(0xFFB5AB99), fontSize = 12.sp)
            Spacer(Modifier.height(24.dp))

            opciones.forEach { (icono, texto, ruta) ->
                Row(
                    Modifier.fillMaxWidth().clickable { nav.navigate(ruta) }.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icono, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(texto, color = Color.White, fontSize = 15.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Cerrar sesión", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                modifier = Modifier.clickable {
                    cerrarSesion(context)
                    nav.navigate(Pantallas.Login.ruta) { popUpTo(0) { inclusive = true } }
                }.padding(8.dp)
            )
        }
    }
}
