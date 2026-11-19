package com.example.proyectofinal.ui.screens.barbero

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.Dorado
import com.example.proyectofinal.data.Gris
import com.example.proyectofinal.data.Oscuro
import com.example.proyectofinal.ui.navigation.Pantallas
import com.example.proyectofinal.ui.screens.AmbarPendiente
import com.example.proyectofinal.ui.screens.CajaIcono
import com.example.proyectofinal.ui.screens.CampoTexto
import com.example.proyectofinal.ui.screens.Insignia
import com.example.proyectofinal.ui.screens.Rojo
import com.example.proyectofinal.ui.screens.Verde
import com.example.proyectofinal.viewmodel.BarberoViewModel

/**
 * Mockup "Mi barbería": datos de la barbería del barbero.
 * Si aún no tiene una, el mismo formulario la registra (queda pendiente de aprobación).
 */
@Composable
fun MiBarberiaScreen(nav: NavController, vm: BarberoViewModel) {
    val context = LocalContext.current
    val barberia by vm.barberia.collectAsState()
    val ciudad by vm.ciudad.collectAsState()
    val misBarberias by vm.misBarberias.collectAsState()

    // El formulario se rellena de nuevo cada vez que cambia la barbería (o carga su ciudad)
    val clave = barberia?.idBarberia to ciudad?.idCiudad
    var nombre by remember(clave) { mutableStateOf(barberia?.nombre.orEmpty()) }
    var ciudadTexto by remember(clave) {
        mutableStateOf(ciudad?.let { "${it.nombre}, ${it.departamento}" } ?: "Armenia, Quindío")
    }
    var direccion by remember(clave) { mutableStateOf(barberia?.direccion.orEmpty()) }
    var descripcion by remember(clave) { mutableStateOf(barberia?.descripcion.orEmpty()) }
    var guardando by remember { mutableStateOf(false) }

    val nueva = barberia == null

    Scaffold(
        containerColor = Crema,
        topBar = { CabeceraBarbero(if (nueva) "Registrar barbería" else "Mi barbería") },
        bottomBar = {
            Column {
                BotonGuardarBarbero(if (nueva) "Registrar barbería" else "Guardar cambios", enabled = !guardando) {
                    guardando = true
                    vm.guardarBarberia(nombre, ciudadTexto, direccion, descripcion) { error ->
                        guardando = false
                        val ok = if (nueva) "Barbería registrada. Queda pendiente de aprobación." else "Cambios guardados"
                        Toast.makeText(context, error ?: ok, Toast.LENGTH_LONG).show()
                    }
                }
                NavegacionBarbero(PestanaBarbero.BARBERIA, nav)
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .imePadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // Si el barbero tiene varias barberías, se elige cuál administrar
            if (misBarberias.size > 1) {
                Text("Tus barberías", color = Gris, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    misBarberias.forEach { b ->
                        val sel = b.idBarberia == barberia?.idBarberia
                        Text(
                            b.nombre, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            color = if (sel) Oscuro else Gris,
                            modifier = Modifier
                                .background(if (sel) Dorado else CajaIcono, RoundedCornerShape(50))
                                .clickable { vm.elegirBarberia(b.idBarberia) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Banner con tijeras (como en el mockup)
            Box(
                Modifier.fillMaxWidth().height(120.dp).background(CajaIcono, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.ContentCut, contentDescription = null, tint = Dorado, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(12.dp))

            barberia?.let { b ->
                val (texto, color) = when (b.estadoAprobacion) {
                    "APROBADA" -> "Aprobada" to Verde
                    "PENDIENTE" -> "Pendiente de aprobación" to AmbarPendiente
                    else -> b.estadoAprobacion.lowercase().replaceFirstChar { it.uppercase() } to Rojo
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Estado: ", color = Gris, fontSize = 13.sp)
                    Insignia(texto, color)
                }
                Spacer(Modifier.height(12.dp))
            }

            CampoTexto("Nombre", nombre, { nombre = it }, "Ej. Barbería el corte")
            CampoTexto("Ciudad", ciudadTexto, { ciudadTexto = it }, "Ciudad, Departamento")
            CampoTexto("Dirección", direccion, { direccion = it }, "Ej. Cra 14 #10-25")
            CampoTexto("Descripción", descripcion, { descripcion = it }, "Cuéntales a tus clientes sobre tu barbería")

            if (!nueva) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BotonPildora("Servicios y precios", relleno = false, modifier = Modifier.weight(1f)) {
                        nav.navigate(Pantallas.BarberoServicios.ruta)
                    }
                    BotonPildora("Horarios", relleno = false, modifier = Modifier.weight(1f)) {
                        nav.navigate(Pantallas.BarberoHorarios.ruta)
                    }
                }
                Spacer(Modifier.height(10.dp))
                BotonPildora(
                    if (barberia?.latitud == null) "Ubicar en el mapa" else "Cambiar ubicación en el mapa",
                    relleno = false, modifier = Modifier.fillMaxWidth()
                ) { nav.navigate(Pantallas.Mapa.ruta) }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
