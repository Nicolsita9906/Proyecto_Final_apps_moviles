package com.example.proyectofinal.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.Dorado
import com.example.proyectofinal.data.FondoOscuro
import com.example.proyectofinal.data.Gris
import com.example.proyectofinal.data.Oscuro
import com.example.proyectofinal.data.model.Rol
import com.example.proyectofinal.ui.screens.Rojo
import com.example.proyectofinal.ui.screens.Verde
import com.example.proyectofinal.viewmodel.BarberViewModel

@Composable
fun AdminUsuariosScreen(
    navController: NavController,
    vm: BarberViewModel = viewModel()
) {
    val usuarios by vm.todosLosUsuarios.collectAsState()
    var busqueda by remember { mutableStateOf("") }
    var rolFiltro by remember { mutableStateOf("TODOS") }

    val usuariosFiltrados = usuarios.filter { u ->
        val coincideTexto = u.nombre.contains(busqueda, ignoreCase = true) || u.correo.contains(busqueda, ignoreCase = true)
        val coincideRol = if (rolFiltro == "TODOS") true else u.rol.equals(rolFiltro, ignoreCase = true)
        coincideTexto && coincideRol
    }

    Scaffold(
        topBar = { EncabezadoAdmin("Gestión de Usuarios", navController) },
        bottomBar = { NavegacionInferiorAdmin(PestanaAdmin.USUARIOS, navController) },
        containerColor = FondoOscuro
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Buscador
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("Buscar por nombre o correo...", color = Gris, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Dorado,
                    unfocusedBorderColor = Gris,
                    focusedTextColor = Crema,
                    unfocusedTextColor = Crema,
                    focusedContainerColor = Oscuro,
                    unfocusedContainerColor = Oscuro
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filtro por Rol
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FiltroChipAdmin("Todos", rolFiltro == "TODOS") { rolFiltro = "TODOS" }
                FiltroChipAdmin("Clientes", rolFiltro == Rol.CLIENTE) { rolFiltro = Rol.CLIENTE }
                FiltroChipAdmin("Barberos", rolFiltro == Rol.BARBERO) { rolFiltro = Rol.BARBERO }
                FiltroChipAdmin("Admins", rolFiltro == Rol.ADMINISTRADOR) { rolFiltro = Rol.ADMINISTRADOR }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${usuariosFiltrados.size} usuario(s) encontrado(s)",
                fontSize = 12.sp,
                color = Gris
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de usuarios
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(usuariosFiltrados) { u ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Oscuro),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = u.nombre,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Crema
                                    )
                                    Text(
                                        text = u.correo,
                                        fontSize = 12.sp,
                                        color = Gris
                                    )
                                    if (!u.telefono.isNullX()) {
                                        Text(
                                            text = "Tel: ${u.telefono}",
                                            fontSize = 11.sp,
                                            color = Gris
                                        )
                                    }
                                }
                                BadgeEstadoAdmin(u.estado)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rol: ${u.rol}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Dorado
                                )

                                // Botón para alternar estado (Bloquear / Activar)
                                if (u.rol != Rol.ADMINISTRADOR) {
                                    val esActivo = u.estado.uppercase() == "ACTIVO"
                                    Button(
                                        onClick = {
                                            val nuevoEstado = if (esActivo) "BLOQUEADO" else "ACTIVO"
                                            vm.cambiarEstadoUsuario(u, nuevoEstado)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (esActivo) Rojo else Verde,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (esActivo) "Bloquear" else "Activar",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String?.isNullX(): Boolean = this.isNullOrBlank()

@Composable
private fun FiltroChipAdmin(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (seleccionado) Dorado else Oscuro,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = texto,
            fontSize = 11.sp,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
            color = if (seleccionado) Oscuro else Crema
        )
    }
}
