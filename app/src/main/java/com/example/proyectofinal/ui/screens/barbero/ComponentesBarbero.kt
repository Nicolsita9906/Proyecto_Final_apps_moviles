package com.example.proyectofinal.ui.screens.barbero

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.Dorado
import com.example.proyectofinal.data.Gris
import com.example.proyectofinal.data.Oscuro
import com.example.proyectofinal.data.mapper.fechaCorta
import com.example.proyectofinal.data.mapper.hora12
import com.example.proyectofinal.data.mapper.precioTexto
import com.example.proyectofinal.data.model.CitaRecibida
import com.example.proyectofinal.data.model.EstadoCitaDb
import com.example.proyectofinal.data.model.EstadoSuscripcion
import com.example.proyectofinal.navigation.Pantallas
import com.example.proyectofinal.ui.screens.AmbarPendiente
import com.example.proyectofinal.ui.screens.BotonPrimario
import com.example.proyectofinal.ui.screens.CajaIcono
import com.example.proyectofinal.ui.screens.IconoCaja
import com.example.proyectofinal.ui.screens.Insignia
import com.example.proyectofinal.ui.screens.Rojo
import com.example.proyectofinal.ui.screens.TarjetaBlanca
import com.example.proyectofinal.ui.screens.Verde

// ============================================================
//  COMPONENTES DEL ROL BARBERO (mockups "Barbero")
// ============================================================

/**
 * Cabecera oscura del barbero. Con onVolver muestra flecha atrás;
 * accion = contenido a la derecha (insignia "Activa", botón "+ Añadir", etc.).
 */
@Composable
fun CabeceraBarbero(
    titulo: String,
    onVolver: (() -> Unit)? = null,
    accion: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Oscuro)
            .statusBarsPadding()
            .padding(horizontal = if (onVolver != null) 4.dp else 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onVolver != null) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Crema)
            }
        }
        Text(
            titulo, color = Crema, fontSize = 20.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (accion != null) {
            Row(Modifier.padding(end = if (onVolver != null) 16.dp else 0.dp), content = accion)
        }
    }
}

/** Botón pequeño dorado de la cabecera ("+ Añadir"). */
@Composable
fun BotonCabecera(texto: String, onClick: () -> Unit) {
    Text(
        texto, color = Oscuro, fontSize = 13.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(Dorado, RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

// ---------- Barra inferior: Panel · Citas · Barbería · Perfil ----------

enum class PestanaBarbero { PANEL, CITAS, BARBERIA, PERFIL }

@Composable
fun NavegacionBarbero(actual: PestanaBarbero, nav: NavController, oscura: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (oscura) Oscuro else Color.White)
            .navigationBarsPadding()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ItemNavBarbero("Panel", actual == PestanaBarbero.PANEL, oscura) {
            nav.navigate(Pantallas.BarberoPanel.ruta) {
                popUpTo(Pantallas.BarberoPanel.ruta) { inclusive = true }
            }
        }
        ItemNavBarbero("Citas", actual == PestanaBarbero.CITAS, oscura) { irA(nav, Pantallas.BarberoCitas.ruta) }
        ItemNavBarbero("Barbería", actual == PestanaBarbero.BARBERIA, oscura) {
            irA(nav, Pantallas.BarberoBarberia.ruta)
        }
        ItemNavBarbero("Perfil", actual == PestanaBarbero.PERFIL, oscura) { irA(nav, Pantallas.BarberoPerfil.ruta) }
    }
}

@Composable
private fun ItemNavBarbero(texto: String, activo: Boolean, oscura: Boolean, onClick: () -> Unit) {
    Text(
        texto, fontSize = 12.sp,
        fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
        color = if (activo) Dorado else if (oscura) Color(0xFFB5AB99) else Gris,
        modifier = Modifier.clickable { onClick() }.padding(horizontal = 12.dp)
    )
}

private fun irA(nav: NavController, ruta: String) {
    nav.navigate(ruta) {
        popUpTo(Pantallas.BarberoPanel.ruta)
        launchSingleTop = true
    }
}

/** Botón dorado sobre la barra inferior (pantallas con "Guardar"). */
@Composable
fun BotonGuardarBarbero(texto: String, enabled: Boolean = true, onClick: () -> Unit) {
    Column(Modifier.background(Crema).padding(horizontal = 16.dp, vertical = 10.dp)) {
        BotonPrimario(texto, enabled = enabled, onClick = onClick)
    }
}

// ---------- Accesos (íconos en caja) ----------

@Composable
fun AccesoBarbero(icono: ImageVector, texto: String, onClick: () -> Unit) {
    Column(
        Modifier.clickable { onClick() }.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            Modifier.size(56.dp).background(CajaIcono, RoundedCornerShape(16.dp)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icono, contentDescription = texto, tint = Dorado, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(texto, color = Gris, fontSize = 12.sp)
    }
}

// ---------- Estados ----------

/** Texto y color de la insignia de una cita vista por el barbero. */
fun estadoCitaBarbero(estado: String): Pair<String, Color> = when (estado) {
    EstadoCitaDb.CONFIRMADA -> "Confirmada" to Verde
    EstadoCitaDb.PENDIENTE -> "Por confirmar" to AmbarPendiente
    EstadoCitaDb.CANCELADA -> "Cancelada" to Rojo
    EstadoCitaDb.RECHAZADA -> "Rechazada" to Rojo
    EstadoCitaDb.COMPLETADA -> "Completada" to Gris
    else -> estado to Gris
}

/** Texto y color de la insignia de suscripción (null = sin suscripción). */
fun estadoSuscripcionTexto(estado: String?): Pair<String, Color> = when (estado) {
    EstadoSuscripcion.ACTIVA -> "Activa" to Verde
    EstadoSuscripcion.POR_VENCER -> "Por vencer" to AmbarPendiente
    EstadoSuscripcion.VENCIDA -> "Vencida" to Rojo
    else -> "Sin suscripción" to Gris
}

/** true si el barbero puede consultar y gestionar sus citas (mockup "Mi suscripción"). */
fun suscripcionVigente(estado: String?): Boolean =
    estado == EstadoSuscripcion.ACTIVA || estado == EstadoSuscripcion.POR_VENCER

// ---------- Tarjetas ----------

/** Tarjeta de cita recibida. Con acciones de aceptar/rechazar si está pendiente. */
@Composable
fun TarjetaCitaBarbero(
    cita: CitaRecibida,
    mostrarFecha: Boolean = false,
    onAceptar: (() -> Unit)? = null,
    onRechazar: (() -> Unit)? = null,
    onCompletar: (() -> Unit)? = null
) {
    val (texto, color) = estadoCitaBarbero(cita.estado)
    TarjetaBlanca(Modifier.padding(vertical = 5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                cita.clienteNombre, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Insignia(texto, color)
        }
        Spacer(Modifier.height(4.dp))
        val fecha = if (mostrarFecha) "${fechaCorta(cita.fecha)} · " else ""
        val precio = if (cita.estado == EstadoCitaDb.CANCELADA || cita.estado == EstadoCitaDb.RECHAZADA) ""
        else " · ${precioTexto(cita.valorTotal)}"
        Text("$fecha${hora12(cita.horaInicio)} · ${cita.servicioNombre}$precio", fontSize = 13.sp, color = Gris)
        if (cita.idBarbero == null) {
            Text("Sin preferencia de barbero · ${cita.barberiaNombre}", fontSize = 12.sp, color = Gris)
        }

        if (cita.estado == EstadoCitaDb.PENDIENTE && onAceptar != null && onRechazar != null) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotonPildora("Aceptar", relleno = true, modifier = Modifier.weight(1f), onClick = onAceptar)
                BotonPildora("Rechazar", relleno = false, modifier = Modifier.weight(1f), onClick = onRechazar)
            }
        } else if (cita.estado == EstadoCitaDb.CONFIRMADA && onCompletar != null) {
            Spacer(Modifier.height(10.dp))
            BotonPildora("Marcar como atendida", relleno = false, modifier = Modifier.fillMaxWidth(), onClick = onCompletar)
        }
    }
}

@Composable
fun BotonPildora(texto: String, relleno: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val forma = RoundedCornerShape(12.dp)
    Text(
        texto,
        color = if (relleno) Oscuro else Dorado,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(forma)
            .background(if (relleno) Dorado else Color.White)
            .border(1.5.dp, Dorado, forma)
            .clickable { onClick() }
            .padding(vertical = 10.dp)
    )
}

/** Fila de persona con ícono (Panel -> "Próximas citas"). */
@Composable
fun FilaPersona(icono: ImageVector, titulo: String, subtitulo: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        IconoCaja { Icon(icono, null, tint = Dorado) }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(subtitulo, color = Gris, fontSize = 13.sp)
        }
    }
}

/** Aviso cuando la suscripción está vencida: bloquea la gestión de citas. */
@Composable
fun AvisoSuscripcion(estado: String?, nav: NavController) {
    TarjetaBlanca(Modifier.padding(vertical = 8.dp)) {
        Text(
            if (estado == null) "Activa tu suscripción" else "Tu suscripción está vencida",
            fontWeight = FontWeight.Bold, fontSize = 15.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Mientras no tengas una suscripción vigente no podrás consultar ni gestionar tus citas.",
            color = Gris, fontSize = 13.sp
        )
        Spacer(Modifier.height(10.dp))
        BotonPrimario(if (estado == null) "Activar suscripción" else "Renovar suscripción") {
            nav.navigate(Pantallas.BarberoSuscripcion.ruta)
        }
    }
}

/** Mensaje para las pantallas que necesitan una barbería registrada. */
@Composable
fun SinBarberia(nav: NavController) {
    TarjetaBlanca(Modifier.padding(16.dp)) {
        Text("Aún no tienes una barbería registrada", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(Modifier.height(4.dp))
        Text("Regístrala para poder configurar servicios y horarios.", color = Gris, fontSize = 13.sp)
        Spacer(Modifier.height(10.dp))
        BotonPrimario("Registrar mi barbería") { irA(nav, Pantallas.BarberoBarberia.ruta) }
    }
}
