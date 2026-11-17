package com.example.proyectofinal.ui.screens.barbero

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.data.Beige
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.Dorado
import com.example.proyectofinal.data.Gris
import com.example.proyectofinal.data.Oscuro
import com.example.proyectofinal.data.mapper.precioTexto
import com.example.proyectofinal.data.model.ServicioEntity
import com.example.proyectofinal.ui.screens.Insignia
import com.example.proyectofinal.ui.screens.TarjetaBlanca
import com.example.proyectofinal.viewmodel.BarberoViewModel

/** Mockup "Servicios y precios": lista + "Añadir"; tocar un servicio lo edita. */
@Composable
fun BarberoServiciosScreen(nav: NavController, vm: BarberoViewModel) {
    val barberia by vm.barberia.collectAsState()
    val servicios by vm.servicios.collectAsState()

    // null = diálogo cerrado; Nuevo = añadir; Editar = editar ese servicio
    var dialogo by remember { mutableStateOf<DialogoServicio?>(null) }

    Scaffold(
        containerColor = Crema,
        topBar = {
            CabeceraBarbero("Servicios") {
                if (barberia != null) BotonCabecera("+ Añadir") { dialogo = DialogoServicio.Nuevo }
            }
        },
        bottomBar = { NavegacionBarbero(PestanaBarbero.BARBERIA, nav) }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            if (barberia == null) {
                item { SinBarberia(nav) }
                return@LazyColumn
            }
            item { Spacer(Modifier.height(12.dp)) }
            if (servicios.isEmpty()) {
                item { Text("Aún no tienes servicios. Toca \"+ Añadir\".", color = Gris, fontSize = 14.sp) }
            }
            items(servicios, key = { it.idServicio }) { s ->
                TarjetaBlanca(Modifier.padding(vertical = 5.dp), onClick = { dialogo = DialogoServicio.Editar(s) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                s.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                                color = if (s.activo) Oscuro else Gris
                            )
                            Text("${s.duracionMinutos} min", color = Gris, fontSize = 13.sp)
                        }
                        if (!s.activo) {
                            Insignia("Oculto", Gris)
                            Spacer(Modifier.padding(4.dp))
                        }
                        Text(precioTexto(s.precio), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }

    dialogo?.let { d ->
        DialogoEditarServicio(
            servicio = (d as? DialogoServicio.Editar)?.servicio,
            vm = vm,
            onCerrar = { dialogo = null }
        )
    }
}

private sealed class DialogoServicio {
    object Nuevo : DialogoServicio()
    data class Editar(val servicio: ServicioEntity) : DialogoServicio()
}

@Composable
private fun DialogoEditarServicio(servicio: ServicioEntity?, vm: BarberoViewModel, onCerrar: () -> Unit) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf(servicio?.nombre.orEmpty()) }
    var duracion by remember { mutableStateOf(servicio?.duracionMinutos?.toString().orEmpty()) }
    var precio by remember { mutableStateOf(servicio?.precio?.toInt()?.toString().orEmpty()) }
    var activo by remember { mutableStateOf(servicio?.activo ?: true) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = Crema,
        title = { Text(if (servicio == null) "Nuevo servicio" else "Editar servicio", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                CampoDialogo("Nombre", nombre) { nombre = it }
                CampoDialogo("Duración (minutos)", duracion, KeyboardType.Number) { v -> duracion = v.filter { it.isDigit() } }
                CampoDialogo("Precio (COP)", precio, KeyboardType.Number) { v -> precio = v.filter { it.isDigit() } }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Visible para clientes", modifier = Modifier.weight(1f), fontSize = 14.sp)
                    Switch(checked = activo, onCheckedChange = { activo = it }, colors = colorSwitch())
                }
                error?.let { Text(it, color = Color(0xFFB5453A), fontSize = 13.sp) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                vm.guardarServicio(
                    servicio, nombre, duracion.toIntOrNull() ?: 0, precio.toDoubleOrNull() ?: 0.0, activo
                ) { e ->
                    if (e == null) {
                        Toast.makeText(context, "Servicio guardado", Toast.LENGTH_SHORT).show()
                        onCerrar()
                    } else {
                        error = e
                    }
                }
            }) { Text("Guardar", color = Dorado, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar", color = Gris) } }
    )
}

@Composable
fun CampoDialogo(
    etiqueta: String,
    valor: String,
    teclado: KeyboardType = KeyboardType.Text,
    onCambio: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Dorado,
            unfocusedBorderColor = Beige,
            focusedLabelColor = Dorado
        ),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    )
}

@Composable
fun colorSwitch() = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = Dorado,
    uncheckedThumbColor = Color.White,
    uncheckedTrackColor = Beige,
    uncheckedBorderColor = Beige
)
