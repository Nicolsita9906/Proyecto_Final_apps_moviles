package com.example.proyectofinal.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.proyectofinal.data.Dorado
import com.example.proyectofinal.data.FondoOscuro
import com.example.proyectofinal.data.Gris
import com.example.proyectofinal.data.Oscuro
import com.example.proyectofinal.data.Sesion
import com.example.proyectofinal.data.Usuario
import com.example.proyectofinal.data.guardarSesion
import com.example.proyectofinal.data.model.Rol
import com.example.proyectofinal.navigation.Pantallas
import com.example.proyectofinal.navigation.inicioPorRol
import com.example.proyectofinal.ui.screens.CampoTexto
import com.example.proyectofinal.ui.screens.EncabezadoAcceso
import com.example.proyectofinal.ui.screens.SelectorAcceso

@Composable
fun PantallaRegistro(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    // false = cuenta de cliente · true = cuenta de barbero ("Regístrate como barbero")
    var esBarbero by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
            .imePadding()
    ) {
        EncabezadoAcceso(if (esBarbero) "Crear cuenta de barbero" else "Crear cuenta")

        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SelectorAcceso(
                registroSeleccionado = true,
                onLogin = { navController.popBackStack() },
                onRegistro = { }
            )

            Spacer(modifier = Modifier.height(20.dp))

            CampoTexto(
                etiqueta = "Nombre completo",
                valor = nombre,
                onCambio = { nombre = it },
                placeholder = "Ej. Camila Ríos"
            )

            CampoTexto(
                etiqueta = "Correo electrónico",
                valor = correo,
                onCambio = { correo = it },
                placeholder = "ejemplo@gmail.com",
                teclado = KeyboardType.Email
            )

            CampoTexto(
                etiqueta = "Teléfono",
                valor = telefono,
                onCambio = { nuevo -> telefono = nuevo.filter { it.isDigit() } },
                placeholder = "300 000 0000",
                teclado = KeyboardType.Phone
            )

            CampoTexto(
                etiqueta = "Contraseña",
                valor = contrasena,
                onCambio = { contrasena = it },
                placeholder = "Mínimo 6 caracteres",
                esContrasena = true
            )

            CampoTexto(
                etiqueta = "Confirmar contraseña",
                valor = confirmar,
                onCambio = { confirmar = it },
                placeholder = "Repite la contraseña",
                esContrasena = true
            )

            if (error.isNotEmpty()) {
                Text(error, color = Color(0xFFE57373))
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    if (nombre.isBlank() || correo.isBlank() || telefono.isBlank() ||
                        contrasena.isBlank() || confirmar.isBlank()
                    ) {
                        error = "Completa todos los campos"
                    } else if (!correo.contains("@")) {
                        error = "El correo no es válido"
                    } else if (contrasena.length < 6) {
                        error = "La contraseña debe tener al menos 6 caracteres"
                    } else if (contrasena != confirmar) {
                        error = "Las contraseñas no coinciden"
                    } else {
                        val usuario = Usuario(
                            nombre = nombre.trim(),
                            correo = correo.trim(),
                            telefono = telefono,
                            contrasena = contrasena
                        )
                        // La consulta a la BD es suspend: se ejecuta dentro de una corrutina
                        val rol = if (esBarbero) Rol.BARBERO else Rol.CLIENTE
                        scope.launch {
                            if (Sesion.registrar(context, usuario, rol)) {
                                navController.navigate(inicioPorRol(rol)) {
                                    popUpTo(Pantallas.Login.ruta) { inclusive = true }
                                }
                            } else {
                                error = "Ese correo ya está registrado"
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Dorado,
                    contentColor = Oscuro
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Crear cuenta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "¿Eres barbero? Regístrate como barbero" (y al revés para volver a cliente)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    if (esBarbero) "¿Eres cliente? " else "¿Eres barbero? ",
                    color = Gris,
                    fontSize = 14.sp
                )
                Text(
                    if (esBarbero) "Regístrate como cliente" else "Regístrate como barbero",
                    color = Dorado,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        esBarbero = !esBarbero
                        error = ""
                    }
                )
            }
            if (esBarbero) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Después de crear la cuenta podrás registrar tu barbería y activar tu suscripción.",
                    color = Gris,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
