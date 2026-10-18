package com.example.proyectofinal.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.data.Beige
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.Dia
import com.example.proyectofinal.data.Dorado
import com.example.proyectofinal.data.Gris
import com.example.proyectofinal.data.Horario
import com.example.proyectofinal.data.Oscuro
import com.example.proyectofinal.navigation.Pantallas
import java.util.Locale

// ============================================================
//  COMPONENTES COMPARTIDOS (versión unificada)
//  Une los componentes de acceso (login/registro) con los de
//  las pantallas de cliente de BarberGo.
// ============================================================

// ---------- Colores extra (los que no existían en data) ----------
// "Ambar" es el mismo color que "Dorado": se deja un alias para no tocar las otras pantallas.
val Ambar = Dorado
val CajaIcono = Color(0xFFF0DDBF)
val Verde = Color(0xFF3E7A55)
val AmbarPendiente = Color(0xFFC98A2B)
val Rojo = Color(0xFFB5453A)
val FondoPerfil = Color(0xFF2A2622)

// ---------- Utilidades ----------
fun formatoPrecio(precio: Int): String =
    "$" + String.format(Locale("es", "CO"), "%,d", precio)

fun colorEstado(estado: String): Color = when (estado) {
    "Confirmada" -> Verde
    "Pendiente" -> AmbarPendiente
    "Cancelada" -> Rojo
    else -> Gris
}

// ---------- Cabeceras ----------

/** Barra superior con flecha para volver (pantallas de acceso y otras vistas). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(titulo: String, navController: NavController) {
    TopAppBar(
        title = { Text(titulo, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Text("←", fontSize = 24.sp)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Oscuro,
            titleContentColor = Crema,
            navigationIconContentColor = Crema
        )
    )
}

/** Cabecera oscura con título y subtítulo opcional (pantallas de cliente, como en el Figma). */
@Composable
fun CabeceraOscura(titulo: String, subtitulo: String? = null) {
    Column(
        Modifier.fillMaxWidth().background(Oscuro).padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(titulo, color = Crema, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (subtitulo != null) {
            Text(subtitulo, color = Color(0xFFB5AB99), fontSize = 12.sp)
        }
    }
}

/** Encabezado grande de las pantallas de acceso (login / registro). */
@Composable
fun EncabezadoAcceso(titulo: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Oscuro)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(titulo, color = Crema, fontSize = 26.sp, fontWeight = FontWeight.Bold)
    }
}

// ---------- Botones ----------

/** Botón dorado/ámbar. Úsalo dentro de una pantalla o de una barra inferior. */
@Composable
fun BotonPrimario(
    texto: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Dorado,
            contentColor = Oscuro,
            disabledContainerColor = Dorado.copy(alpha = 0.4f),
            disabledContentColor = Oscuro.copy(alpha = 0.5f)
        )
    ) { Text(texto, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
}

/** Botón dorado ya envuelto en una barra inferior crema (para bottomBar de un Scaffold). */
@Composable
fun BotonDorado(texto: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Crema)
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        BotonPrimario(texto, onClick = onClick)
    }
}

// ---------- Tarjetas y etiquetas ----------

@Composable
fun Insignia(texto: String, fondo: Color, contenido: Color = Color.White) {
    Box(
        Modifier.clip(RoundedCornerShape(50)).background(fondo)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(texto, color = contenido, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TarjetaBlanca(
    modifier: Modifier = Modifier,
    seleccionada: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val base = modifier.fillMaxWidth()
    Card(
        modifier = if (onClick != null) base.clickable { onClick() } else base,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = if (seleccionada) BorderStroke(1.5.dp, Dorado) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(12.dp), content = content)
    }
}

@Composable
fun IconoCaja(content: @Composable () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(CajaIcono),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
fun EtiquetaSeccion(texto: String) {
    Text(
        texto, color = Gris, fontSize = 12.sp,
        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
    )
}

// ---------- Pestañas ----------

/** Pestañas tipo "pastilla" (Servicios/Barberos/Reseñas y Próximas/Historial). */
@Composable
fun Pestanas(opciones: List<String>, seleccion: Int, onSel: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opciones.forEachIndexed { i, texto ->
            val activa = i == seleccion
            Box(
                Modifier.weight(1f).height(38.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (activa) Dorado else Beige)
                    .clickable { onSel(i) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    texto, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    color = if (activa) Oscuro else Gris
                )
            }
        }
    }
}

/** Selector Iniciar sesión / Registrarse (sobre fondo oscuro). */
@Composable
fun SelectorAcceso(
    registroSeleccionado: Boolean,
    onLogin: () -> Unit,
    onRegistro: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PestanaAcceso("Iniciar sesión", !registroSeleccionado, onLogin, Modifier.weight(1f))
        PestanaAcceso("Registrarse", registroSeleccionado, onRegistro, Modifier.weight(1f))
    }
}

@Composable
fun PestanaAcceso(
    texto: String,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                if (seleccionada) Dorado else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            color = if (seleccionada) Oscuro else Crema,
            fontWeight = FontWeight.Bold
        )
    }
}

// ---------- Campo de texto ----------

@Composable
fun CampoTexto(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    placeholder: String,
    teclado: KeyboardType = KeyboardType.Text,
    esContrasena: Boolean = false
) {
    Text(etiqueta, color = Gris, fontSize = 14.sp)
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        placeholder = { Text(placeholder, color = Gris) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        visualTransformation = if (esContrasena) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedTextColor = Oscuro,
            unfocusedTextColor = Oscuro,
            focusedBorderColor = Dorado,
            unfocusedBorderColor = Beige
        ),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(12.dp))
}

// ---------- Selectores de fecha y hora ----------

@Composable
fun DiaChip(dia: Dia, seleccionado: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.padding(horizontal = 3.dp).clip(RoundedCornerShape(10.dp))
            .background(if (seleccionado) Dorado else Color.White)
            .border(1.dp, if (seleccionado) Dorado else Beige, RoundedCornerShape(10.dp))
            .clickable { onClick() }.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(dia.letra, fontSize = 11.sp, color = if (seleccionado) Oscuro else Gris)
        Text(dia.numero.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Oscuro)
    }
}

@Composable
fun HoraChip(h: Horario, seleccionada: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val fondo = when {
        !h.disponible -> Beige
        seleccionada -> Dorado
        else -> Color.White
    }
    Box(
        modifier.padding(4.dp).height(38.dp).clip(RoundedCornerShape(10.dp)).background(fondo)
            .border(1.dp, if (h.disponible) Beige else Color.Transparent, RoundedCornerShape(10.dp))
            .then(if (h.disponible) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            h.texto, fontSize = 13.sp,
            fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal,
            color = if (h.disponible) Oscuro else Gris.copy(alpha = 0.6f),
            textDecoration = if (!h.disponible) TextDecoration.LineThrough else null
        )
    }
}

// ---------- Barra inferior ----------

/** Barra inferior manual: Inicio · Buscar · Citas · Perfil. */
@Composable
fun BarraInferior(nav: NavController, actual: String, oscura: Boolean = false) {
    val ctx = LocalContext.current
    Row(
        Modifier.fillMaxWidth().background(if (oscura) Oscuro else Color.White).padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ItemNav("Inicio", actual == Pantallas.Home.ruta, oscura) { irA(nav, Pantallas.Home.ruta) }
        ItemNav("Buscar", false, oscura) {
            // TODO FUTURO: pantalla de búsqueda (no está en los mockups)
            Toast.makeText(ctx, "Próximamente", Toast.LENGTH_SHORT).show()
        }
        ItemNav("Citas", actual == Pantallas.MisCitas.ruta, oscura) { irA(nav, Pantallas.MisCitas.ruta) }
        ItemNav("Perfil", actual == Pantallas.Perfil.ruta, oscura) { irA(nav, Pantallas.Perfil.ruta) }
    }
}

@Composable
private fun ItemNav(texto: String, activo: Boolean, oscura: Boolean, onClick: () -> Unit) {
    Text(
        texto, fontSize = 12.sp,
        fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
        color = if (activo) Dorado else if (oscura) Color(0xFFB5AB99) else Gris,
        modifier = Modifier.clickable { onClick() }.padding(horizontal = 12.dp)
    )
}

private fun irA(nav: NavController, ruta: String) {
    nav.navigate(ruta) {
        popUpTo(Pantallas.Home.ruta)
        launchSingleTop = true
    }
}