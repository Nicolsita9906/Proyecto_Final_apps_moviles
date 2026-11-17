package com.example.proyectofinal.ui.screens.barbero

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.Disponibilidad
import com.example.proyectofinal.data.Oscuro
import com.example.proyectofinal.data.mapper.precioTexto
import com.example.proyectofinal.ui.screens.BotonPrimario
import com.example.proyectofinal.ui.screens.FondoPerfil
import com.example.proyectofinal.ui.screens.Insignia
import com.example.proyectofinal.ui.screens.Verde
import com.example.proyectofinal.viewmodel.BarberoViewModel
import java.util.Calendar

private val MESES_LARGOS = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)
private val MESES_CORTOS = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
private val TextoClaro = Color(0xFFB5AB99)

/** Mockup "Mi suscripción" (fondo oscuro): estado del plan, renovar e historial de pagos. */
@Composable
fun BarberoSuscripcionScreen(nav: NavController, vm: BarberoViewModel) {
    val context = LocalContext.current
    val suscripcion by vm.suscripcion.collectAsState()
    val estado by vm.estadoSuscripcion.collectAsState()
    val pagos by vm.pagos.collectAsState()
    var guardando by remember { mutableStateOf(false) }

    val (textoEstado, colorEstado) = estadoSuscripcionTexto(estado)
    val vencimiento = suscripcion?.fechaVencimiento?.let { iso ->
        Disponibilidad.calendarDe(iso)?.let { "${it.get(Calendar.DAY_OF_MONTH)} de ${MESES_LARGOS[it.get(Calendar.MONTH)]}" }
    }

    Scaffold(
        containerColor = FondoPerfil,
        topBar = { CabeceraBarbero("Mi suscripción", onVolver = { nav.popBackStack() }) }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            item {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Oscuro),
                    border = BorderStroke(1.5.dp, colorEstado)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Plan mensual", color = Crema, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Insignia(textoEstado, colorEstado)
                        }
                        Text(
                            when {
                                vencimiento == null -> "${precioTexto(BarberoViewModel.PRECIO_PLAN_MENSUAL)} al mes"
                                estado == "VENCIDA" -> "Venció el $vencimiento"
                                else -> "Vence el $vencimiento"
                            },
                            color = TextoClaro, fontSize = 14.sp
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Si tu suscripción vence, no podrás consultar ni gestionar tus citas hasta renovar.",
                    color = TextoClaro, fontSize = 14.sp
                )
                Spacer(Modifier.height(16.dp))
                BotonPrimario(
                    if (suscripcion == null) "Activar suscripción" else "Renovar suscripción",
                    enabled = !guardando
                ) {
                    guardando = true
                    vm.renovarSuscripcion { error ->
                        guardando = false
                        Toast.makeText(
                            context,
                            error ?: "Pago de ${precioTexto(BarberoViewModel.PRECIO_PLAN_MENSUAL)} registrado. ¡Suscripción renovada!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("Historial de pagos", color = TextoClaro, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                if (pagos.isEmpty()) Text("Aún no hay pagos registrados", color = TextoClaro, fontSize = 13.sp)
            }
            items(pagos, key = { it.idPago }) { p ->
                val fecha = Calendar.getInstance().apply { timeInMillis = p.fechaPago }
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${MESES_CORTOS[fecha.get(Calendar.MONTH)]} ${fecha.get(Calendar.DAY_OF_MONTH)}",
                        color = TextoClaro, fontSize = 15.sp, modifier = Modifier.weight(1f)
                    )
                    Text(precioTexto(p.monto), color = Crema, fontSize = 17.sp, modifier = Modifier.weight(1f))
                    Insignia("Pagado", Verde)
                }
            }
        }
    }
}
