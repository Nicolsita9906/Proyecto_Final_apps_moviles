package com.example.proyectofinal.ui.screens.clientes


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.proyectofinal.R
import com.example.proyectofinal.data.Barberia
import com.example.proyectofinal.data.Beige
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.Dorado
import com.example.proyectofinal.data.Gris
import com.example.proyectofinal.data.Oscuro
import com.example.proyectofinal.data.Sesion
import com.example.proyectofinal.data.cerrarSesion
import com.example.proyectofinal.data.listaBarberias
import com.example.proyectofinal.data.obtenerNombre
import com.example.proyectofinal.navigation.Pantallas
import com.example.proyectofinal.ui.screens.BarraInferior


@Composable
fun PantallaInicio(navController: NavController) {
    val context = LocalContext.current
    Scaffold(
        containerColor = Crema,
        bottomBar = { BarraInferior(navController, Pantallas.Home.ruta) },
        topBar = {

            val usuario = Sesion.usuarioActual

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Oscuro)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Hola,", color = Gris)
                    Text(
                        (usuario?.nombre ?: obtenerNombre(context)).substringBefore(" "),
                        color = Crema,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = {
                        Sesion.cerrarSesion()
                        cerrarSesion(context)
                        navController.navigate(Pantallas.Login.ruta) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Salir", color = Dorado, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Beige)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Encuentra tu barbería ideal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Busca por ciudad, servicio o calificación",
                            color = Gris
                        )
                    }
                }
            }


            item {
                Column {
                    Text(
                        text = "Accesos rápidos",
                        color = Gris
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        AccesoRapido(R.drawable.search, "Buscar") {
                            Toast.makeText(context, "Próximamente", Toast.LENGTH_SHORT).show()
                        }
                        AccesoRapido(R.drawable.calendar, "Mis citas") {
                            navController.navigate(Pantallas.MisCitas.ruta)
                        }
                        AccesoRapido(R.drawable.user, "Perfil") {
                            navController.navigate(Pantallas.Perfil.ruta)
                        }
                        AccesoRapido(R.drawable.favorite, "Favoritos") {
                            Toast.makeText(context, "Próximamente", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }


            item {
                Text("Cerca de ti", color = Gris)
            }

            items(listaBarberias) { barberia ->
                ItemBarberia(
                    barberia = barberia,
                    onClick = { navController.navigate(Pantallas.Detalle.crear(barberia.id)) }
                )
            }
        }
    }
}

@Composable
fun AccesoRapido(img: Int, texto: String, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    Beige,
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = img),
                contentDescription = texto,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = texto,
            color = Gris,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ItemBarberia(barberia: Barberia, onClick: () -> Unit) {
    val estado = if (barberia.abierto) "ABIERTO" else "CERRADO"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp), // Un pequeño padding para separar filas
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp) // Tamaño de la caja (como en tu imagen)
                .background(Beige, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // ✅ AQUÍ ESTÁ LA CORRECCIÓN
            Image(
                painter = painterResource(id = R.drawable.andrsrxn__scissors),
                contentDescription = "Tijeras",
                modifier = Modifier.size(32.dp) // Tamaño del ícono dentro de la caja
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(barberia.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("★ ${barberia.rating} · ${barberia.distancia} · $estado", color = Gris)
        }
    }
}