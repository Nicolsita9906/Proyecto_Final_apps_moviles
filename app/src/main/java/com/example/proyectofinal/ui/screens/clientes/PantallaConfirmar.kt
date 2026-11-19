package com.example.proyectofinal.ui.screens.clientes

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.proyectofinal.data.Gris
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.proyectofinal.data.mapper.fechaCorta
import com.example.proyectofinal.data.obtenerNombre
import com.example.proyectofinal.ui.navigation.Pantallas
import com.example.proyectofinal.viewmodel.BarberViewModel
import com.example.proyectofinal.data.Sesion
import com.example.proyectofinal.ui.screens.BarraSuperior
import com.example.proyectofinal.ui.screens.BotonDorado


@Composable
fun PantallaConfirmar(navController: NavController, vm: BarberViewModel) {
    val context = LocalContext.current
    val servicio by vm.servicioSel.collectAsState()
    val barbero by vm.barberoSel.collectAsState()
    val dia by vm.diaSel.collectAsState()
    val hora by vm.horaSel.collectAsState()
    val cliente = Sesion.usuarioActual?.nombre ?: obtenerNombre(context)
    val fecha = dia?.let { d ->
        hora?.let { h -> "${fechaCorta(d.iso)} · ${h.completa}" }
    } ?: ""
    var guardando by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Crema,
        topBar = { BarraSuperior("Confirmar cita", navController) },
        bottomBar = {
            BotonDorado("Confirmar cita") {
                if (guardando) return@BotonDorado
                guardando = true
                // Crea la cita (o actualiza la que se está modificando) y va a Mis citas.
                // El ViewModel revisa en la BD que la hora siga libre antes de guardar.
                vm.confirmarCita { resultado ->
                    guardando = false
                    when (resultado) {
                        BarberViewModel.ResultadoReserva.OK -> {
                            Toast.makeText(context, "¡Cita confirmada!", Toast.LENGTH_SHORT).show()
                            navController.navigate(Pantallas.MisCitas.ruta) {
                                popUpTo(Pantallas.Home.ruta)
                            }
                        }
                        BarberViewModel.ResultadoReserva.HORARIO_OCUPADO -> {
                            Toast.makeText(
                                context,
                                "Esa hora acaba de ser reservada. Elige otra.",
                                Toast.LENGTH_LONG
                            ).show()
                            navController.popBackStack()     // vuelve a Fecha y hora
                        }
                        BarberViewModel.ResultadoReserva.FALTAN_DATOS ->
                            Toast.makeText(context, "Faltan datos de la reserva", Toast.LENGTH_SHORT).show()
                        BarberViewModel.ResultadoReserva.ERROR ->
                            Toast.makeText(context, "No se pudo guardar la cita", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    FilaDetalle("Barbería", vm.barberia.nombre)
                    HorizontalDivider(color = Beige)
                    FilaDetalle("Servicio", servicio?.nombre ?: "")
                    HorizontalDivider(color = Beige)
                    FilaDetalle("Barbero", barbero?.nombre ?: "")
                    HorizontalDivider(color = Beige)
                    FilaDetalle("Cliente", cliente)
                    HorizontalDivider(color = Beige)
                    FilaDetalle("Fecha", fecha)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Beige)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total a pagar", fontSize = 16.sp)
                        Text("Pago en el establecimiento", color = Gris, fontSize = 13.sp)
                    }
                    Text(servicio?.precio ?: "", fontWeight = FontWeight.Bold, fontSize = 26.sp)
                }
            }
        }
    }
}

@Composable
fun FilaDetalle(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, color = Gris)
        Text(valor, fontWeight = FontWeight.Bold)
    }
}
