package com.example.proyectofinal.ui.components.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.proyectofinal.data.Beige
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.Dorado
import com.example.proyectofinal.data.Gris
import com.example.proyectofinal.data.Oscuro

private val Esquema = lightColorScheme(
    primary = Dorado,
    onPrimary = Oscuro,
    background = Crema,
    onBackground = Oscuro,
    surface = Color.White,
    onSurface = Oscuro,
    surfaceVariant = Beige,
    onSurfaceVariant = Gris,
    error = Color(0xFFB5453A)
)

@Composable
fun ProyectoFinalTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Esquema, content = content)
}

@Composable
fun BarberGoTheme(content: @Composable () -> Unit) = ProyectoFinalTheme(content)
