package com.example.proyectofinal.ui.screens.barbero

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.data.Beige
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.Dorado
import com.example.proyectofinal.data.Gris
import com.example.proyectofinal.data.Oscuro
import com.example.proyectofinal.viewmodel.BarberoViewModel
import com.example.proyectofinal.viewmodel.BarberoViewModel.HorarioDia

private val NOMBRES_DIA = listOf("", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

/**
 * Mockup "Horarios de atención": un interruptor por día (abierto/cerrado).
 * Tocar las horas de un día abierto permite cambiarlas. Estos horarios son los que
 * usa la reserva del cliente para calcular los días y horas disponibles.
 */
@Composable
fun BarberoHorariosScreen(nav: NavController, vm: BarberoViewModel) {
    val context = LocalContext.current
    val barberia by vm.barberia.collectAsState()
    val horariosBd by vm.horarios.collectAsState()

    // Copia editable de los 7 días; se vuelve a cargar cuando cambian los datos de la BD
    val dias = remember(horariosBd) {
        mutableStateListOf<HorarioDia>().apply {
            for (d in 1..7) {
                val fila = horariosBd.firstOrNull { it.diaSemana == d }
                add(
                    HorarioDia(
                        diaSemana = d,
                        abierto = fila != null && !fila.cerrado,
                        apertura = fila?.horaApertura ?: "09:00",
                        cierre = fila?.horaCierre ?: "19:00"
                    )
                )
            }
        }
    }
    var editando by remember { mutableStateOf<Int?>(null) }     // índice del día en edición

    Scaffold(
        containerColor = Crema,
        topBar = { CabeceraBarbero("Horarios") },
        bottomBar = {
            Column {
                if (barberia != null) {
                    BotonGuardarBarbero("Guardar horario") {
                        vm.guardarHorarios(dias.toList()) { error ->
                            Toast.makeText(context, error ?: "Horario guardado", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                NavegacionBarbero(PestanaBarbero.BARBERIA, nav)
            }
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(horizontal = 20.dp)) {
            if (barberia == null) {
                item { SinBarberia(nav) }
                return@LazyColumn
            }
            itemsIndexed(dias) { i, d ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(NOMBRES_DIA[d.diaSemana], fontSize = 16.sp, color = Oscuro, modifier = Modifier.weight(1f))
                    Text(
                        if (d.abierto) "${d.apertura.trimStart('0')} - ${d.cierre.trimStart('0')}" else "Cerrado",
                        color = Gris,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .weight(1f)
                            .then(if (d.abierto) Modifier.clickable { editando = i } else Modifier)
                            .padding(vertical = 6.dp)
                    )
                    Switch(
                        checked = d.abierto,
                        onCheckedChange = { dias[i] = d.copy(abierto = it) },
                        colors = colorSwitch()
                    )
                }
                if (i < dias.lastIndex) HorizontalDivider(color = Beige)
            }
            item {
                Text(
                    "Toca las horas de un día para cambiarlas.",
                    color = Gris, fontSize = 12.sp, modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }

    editando?.let { i ->
        DialogoHoras(
            dia = dias[i],
            onCerrar = { editando = null },
            onGuardar = { apertura, cierre ->
                dias[i] = dias[i].copy(apertura = apertura, cierre = cierre)
                editando = null
            }
        )
    }
}

@Composable
private fun DialogoHoras(dia: HorarioDia, onCerrar: () -> Unit, onGuardar: (String, String) -> Unit) {
    var apertura by remember { mutableStateOf(dia.apertura) }
    var cierre by remember { mutableStateOf(dia.cierre) }
    var error by remember { mutableStateOf<String?>(null) }
    val formato = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = Crema,
        title = { Text(NOMBRES_DIA[dia.diaSemana], fontWeight = FontWeight.Bold) },
        text = {
            Column {
                CampoDialogo("Apertura (HH:mm)", apertura, KeyboardType.Number) { apertura = formatearHora(it) }
                CampoDialogo("Cierre (HH:mm)", cierre, KeyboardType.Number) { cierre = formatearHora(it) }
                error?.let { Text(it, color = Color(0xFFB5453A), fontSize = 13.sp) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                error = when {
                    !formato.matches(apertura) || !formato.matches(cierre) -> "Usa el formato HH:mm (ej. 09:00)"
                    apertura >= cierre -> "El cierre debe ser después de la apertura"
                    else -> null
                }
                if (error == null) onGuardar(apertura, cierre)
            }) { Text("Aceptar", color = Dorado, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar", color = Gris) } }
    )
}

/** Deja escribir solo dígitos y pone los ":" solos: "0930" -> "09:30". */
private fun formatearHora(texto: String): String {
    val digitos = texto.filter { it.isDigit() }.take(4)
    return if (digitos.length <= 2) digitos else digitos.substring(0, 2) + ":" + digitos.substring(2)
}
