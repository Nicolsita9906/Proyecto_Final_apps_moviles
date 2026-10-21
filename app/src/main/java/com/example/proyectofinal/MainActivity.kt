package com.example.proyectofinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.model.Rol
import com.example.proyectofinal.data.obtenerRol
import com.example.proyectofinal.data.obtenerSesion
import com.example.proyectofinal.navigation.AppNav
import com.example.proyectofinal.navigation.Pantallas
import com.example.proyectofinal.ui.components.theme.ProyectoFinalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sesion = obtenerSesion(this)
        val rol = obtenerRol(this)
        val inicio = if (sesion != null) {
            if (rol == Rol.ADMINISTRADOR) Pantallas.AdminPanel.ruta else Pantallas.Home.ruta
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