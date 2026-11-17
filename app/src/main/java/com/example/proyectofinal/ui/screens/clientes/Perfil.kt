package com.example.proyectofinal.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
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
import com.example.proyectofinal.data.Sesion
import com.example.proyectofinal.data.cerrarSesion
import com.example.proyectofinal.data.obtenerNombre
import com.example.proyectofinal.data.obtenerSesion
import com.example.proyectofinal.navigation.Pantallas
import com.example.proyectofinal.data.*
import com.example.proyectofinal.viewmodel.BarberViewModel

/** Pantalla 6 · Mi perfil. Nombre y correo salen de SharedPreferences. */
@Composable
fun PerfilScreen(nav: NavController, vm: BarberViewModel) {
    val context = LocalContext.current
    val nombre = obtenerNombre(context).ifBlank { "Cliente" }
    val correo = obtenerSesion(context) ?: ""

    // TODO FUTURO: estas opciones aún no tienen pantalla
    val opciones: List<Pair<ImageVector, String>> = listOf(
        Icons.Default.Person to "Editar datos personales",
        Icons.Default.LocationOn to "Direcciones guardadas",
        Icons.Default.Star to "Barberías favoritas",
        Icons.Default.Notifications to "Notificaciones",
        Icons.Default.Info to "Ayuda y soporte"
    )

    Scaffold(
        containerColor = FondoPerfil,
        topBar = { CabeceraOscura("Mi perfil") },
        bottomBar = { BarraInferior(nav, Pantallas.Perfil.ruta, oscura = true) }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(horizontal = 24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Box(
                Modifier.size(64.dp).clip(RoundedCornerShape(14.dp)).background(Oscuro),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Person, null, tint = Ambar, modifier = Modifier.size(32.dp)) }
            Spacer(Modifier.height(10.dp))
            Text(nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            if ("@" in correo) Text(correo, color = Color(0xFFB5AB99), fontSize = 12.sp)
            Spacer(Modifier.height(24.dp))

            opciones.forEach { (icono, texto) ->
                FilaPerfil(icono, texto) {
                    Toast.makeText(context, "Próximamente", Toast.LENGTH_SHORT).show()
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Cerrar sesión", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                modifier = Modifier.clickable {
                    Sesion.cerrarSesion()
                    cerrarSesion(context)
                    vm.limpiarSeleccion()
                    nav.navigate(Pantallas.Login.ruta) { popUpTo(0) { inclusive = true } }
                }.padding(12.dp)
            )
        }
    }
}

@Composable
private fun FilaPerfil(icono: ImageVector, texto: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(12.dp))
        Text(texto, color = Color.White, fontSize = 13.sp)
    }
}
