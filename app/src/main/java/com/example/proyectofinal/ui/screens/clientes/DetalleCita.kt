package com.example.proyectofinal.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.data.Beige
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.Dorado
import com.example.proyectofinal.data.Gris
import com.example.proyectofinal.navigation.Pantallas
import com.example.proyectofinal.viewmodel.BarberViewModel

/** Pantalla 5 · Detalle de la cita (ruta con parámetro citaId). */
@Composable
fun DetalleCitaScreen(nav: NavController, vm: BarberViewModel, citaId: Int) {
    val citas by vm.citas.collectAsState()
    val cita = citas.firstOrNull { it.id == citaId }
    val context = LocalContext.current
    var dialogo by remember { mutableStateOf(false) }
    var dialogoEliminar by remember { mutableStateOf(false) }
    val activa = cita != null && cita.estado in setOf("Confirmada", "Pendiente")
    // Solo las canceladas o rechazadas se pueden borrar del historial
    val eliminable = cita != null && cita.estado in setOf("Cancelada", "Rechazada")

    Scaffold(
        containerColor = Crema,
        topBar = { CabeceraOscura("Detalle de la cita") },
        bottomBar = {
            if (activa) {
                Row(
                    Modifier.background(Crema).padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            vm.editarCita(citaId)
                            nav.navigate(Pantallas.ServicioBarbero.ruta)
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Dorado),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Dorado)
                    ) { Text("Modificar", fontWeight = FontWeight.Bold) }
                    Button(
                        onClick = { dialogo = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Rojo, contentColor = Color.White)
                    ) { Text("Cancelar", fontWeight = FontWeight.Bold) }
                }
            } else if (eliminable) {
                Box(Modifier.background(Crema).padding(16.dp).fillMaxWidth()) {
                    Button(
                        onClick = { dialogoEliminar = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Rojo, contentColor = Color.White)
                    ) { Text("Eliminar del historial", fontWeight = FontWeight.Bold) }
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            if (cita == null) {
                Text("No se encontró la cita")
            } else {
                TarjetaBlanca {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(vm.barberiaDe(cita.id), fontWeight = FontWeight.Bold, fontSize = 15.sp,
                            modifier = Modifier.weight(1f))
                        Insignia(cita.estado, colorEstado(cita.estado))
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Beige)
                    Linea("Servicio", cita.servicio.nombre)
                    Linea("Barbero", cita.barbero.nombre)
                    Linea("Fecha", "${cita.fecha} · ${cita.hora}")
                    Linea("Precio", cita.servicio.precio)
                }
                Spacer(Modifier.height(10.dp))
                TarjetaBlanca {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Gris, modifier = Modifier.size(16.dp))
                        Text(" ${vm.direccionDe(cita.id)}", fontSize = 12.sp, color = Gris)
                    }
                }
            }
        }
    }

    if (dialogo) {
        AlertDialog(
            onDismissRequest = { dialogo = false },
            title = { Text("Cancelar cita") },
            text = { Text("¿Seguro que deseas cancelar esta cita?") },
            confirmButton = {
                TextButton(onClick = { vm.cancelarCita(citaId); dialogo = false }) {
                    Text("Sí, cancelar", color = Rojo)
                }
            },
            dismissButton = { TextButton(onClick = { dialogo = false }) { Text("No") } }
        )
    }

    if (dialogoEliminar) {
        AlertDialog(
            onDismissRequest = { dialogoEliminar = false },
            title = { Text("Eliminar cita") },
            text = { Text("La cita se borrará de tu historial y no se podrá recuperar.") },
            confirmButton = {
                TextButton(onClick = {
                    dialogoEliminar = false
                    vm.eliminarCita(citaId) { error ->
                        if (error == null) {
                            Toast.makeText(context, "Cita eliminada", Toast.LENGTH_SHORT).show()
                            nav.popBackStack()          // vuelve a "Mis citas"
                        } else {
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    }
                }) { Text("Eliminar", color = Rojo) }
            },
            dismissButton = { TextButton(onClick = { dialogoEliminar = false }) { Text("No") } }
        )
    }
}

@Composable
private fun Linea(titulo: String, valor: String) {
    Text("$titulo: $valor", fontSize = 12.sp, color = Gris, modifier = Modifier.padding(vertical = 2.dp))
}
