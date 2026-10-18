package com.example.proyectofinal.ui.screens.auth

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.proyectofinal.data.Dorado
import com.example.proyectofinal.data.FondoOscuro
import com.example.proyectofinal.data.Oscuro
import com.example.proyectofinal.data.Sesion
import com.example.proyectofinal.data.guardarSesion
import com.example.proyectofinal.navigation.Pantallas
import com.example.proyectofinal.ui.screens.CampoTexto
import com.example.proyectofinal.ui.screens.EncabezadoAcceso
import com.example.proyectofinal.ui.screens.SelectorAcceso
import com.example.proyectofinal.viewmodel.BarberViewModel

@Composable
fun LoginScreen(navController: NavController, vm: BarberViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
            .imePadding()
    ) {
        EncabezadoAcceso("Iniciar sesión")

        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SelectorAcceso(
                registroSeleccionado = false,
                onLogin = { },
                onRegistro = { navController.navigate("registro") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            CampoTexto(
                etiqueta = "Correo electrónico",
                valor = correo,
                onCambio = { correo = it },
                placeholder = "ejemplo@gmail.com",
                teclado = KeyboardType.Email
            )

            CampoTexto(
                etiqueta = "Contraseña",
                valor = contrasena,
                onCambio = { contrasena = it },
                placeholder = "Tu contraseña",
                esContrasena = true
            )

            if (error.isNotEmpty()) {
                Text(error, color = Color(0xFFE57373))
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    if (correo.isBlank() || contrasena.isBlank()) {
                        error = "Completa todos los campos"
                    } else {
                        // La consulta a la BD es suspend: se ejecuta dentro de una corrutina
                        scope.launch {
                            if (Sesion.iniciarSesion(context, correo, contrasena)) {
                                navController.navigate(Pantallas.Home.ruta) {
                                    popUpTo(Pantallas.Login.ruta) { inclusive = true }
                                }
                            } else {
                                error = "Correo o contraseña incorrectos"
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
                Text("Iniciar sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun PantallaLogin(navController: NavController) {
    LoginScreen(navController)
}
