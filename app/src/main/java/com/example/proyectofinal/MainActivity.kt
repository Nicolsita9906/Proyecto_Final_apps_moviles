package com.example.proyectofinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.obtenerRol
import com.example.proyectofinal.data.obtenerSesion
import com.example.proyectofinal.ui.navigation.AppNav
import com.example.proyectofinal.ui.navigation.Pantallas
import com.example.proyectofinal.ui.navigation.inicioPorRol
import com.example.proyectofinal.ui.components.theme.ProyectoFinalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sesion = obtenerSesion(this)
        val rol = obtenerRol(this)
        val inicio = if (sesion != null) {
            inicioPorRol(rol)
        } else {
            Pantallas.Login.ruta
        }

        setContent {
            ProyectoFinalTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Crema) {
                    AppNav(inicio)
                }
            }
        }
    }
}