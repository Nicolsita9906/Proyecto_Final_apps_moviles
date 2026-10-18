package com.example.proyectofinal.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyectofinal.ui.screens.DetalleCitaScreen
import com.example.proyectofinal.ui.screens.FechaHoraScreen
import com.example.proyectofinal.ui.screens.MisCitasScreen
import com.example.proyectofinal.ui.screens.PerfilScreen
import com.example.proyectofinal.ui.screens.ServicioBarberoScreen
import com.example.proyectofinal.ui.screens.SplashScreen
import com.example.proyectofinal.ui.screens.auth.PantallaLogin
import com.example.proyectofinal.ui.screens.auth.PantallaRegistro
import com.example.proyectofinal.ui.screens.clientes.PantallaConfirmar
import com.example.proyectofinal.ui.screens.clientes.PantallaDetalle
import com.example.proyectofinal.ui.screens.clientes.PantallaInicio
import com.example.proyectofinal.viewmodel.BarberViewModel

/**
 * Flujo completo:
 *  Login / Registro -> (Splash, solo tras iniciar sesión o registrarse) -> Inicio
 *  Inicio -> Detalle de barbería -> Servicio y barbero -> Fecha y hora -> Confirmar -> Mis citas
 *  Mis citas -> Detalle de cita (Modificar / Cancelar);  Perfil; barra inferior en Inicio, Citas y Perfil.
 *
 *  Con sesión guardada (SharedPreferences) la app abre directo en Inicio, sin splash.
 */
@Composable
fun AppNav(inicio: String, vm: BarberViewModel = viewModel()) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = inicio) {

        // ---------- Acceso ----------
        composable(Pantallas.Login.ruta) {
            LaunchedEffect(Unit) { vm.splashPendiente = true }   // el próximo ingreso muestra el splash
            PantallaLogin(nav)
        }
        composable(Pantallas.Registro.ruta) {
            LaunchedEffect(Unit) { vm.splashPendiente = true }
            PantallaRegistro(nav)
        }

        // ---------- Inicio (con splash solo después de iniciar sesión / registrarse) ----------
        composable(Pantallas.Home.ruta) {
            if (vm.splashPendiente) {
                // La sesión ya se guardó en Login/Registro; aquí solo se termina el splash
                SplashScreen { vm.splashPendiente = false }
            } else {
                PantallaInicio(nav)
            }
        }

        // ---------- Reserva ----------
        composable(
            Pantallas.Detalle.ruta,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt("id") ?: 0
            val barberia = vm.barberias.firstOrNull { it.id == id } ?: vm.barberias.first()
            PantallaDetalle(nav, barberia, vm)
        }
        composable(Pantallas.ServicioBarbero.ruta) { ServicioBarberoScreen(nav, vm) }
        composable(Pantallas.FechaHora.ruta) { FechaHoraScreen(nav, vm) }
        composable(Pantallas.Confirmar.ruta) { PantallaConfirmar(nav, vm) }

        // ---------- Citas y perfil ----------
        composable(Pantallas.MisCitas.ruta) { MisCitasScreen(nav, vm) }
        composable(
            Pantallas.DetalleCita.ruta,
            arguments = listOf(navArgument("citaId") { type = NavType.IntType })
        ) { entry ->
            DetalleCitaScreen(nav, vm, entry.arguments?.getInt("citaId") ?: -1)
        }
        composable(Pantallas.Perfil.ruta) { PerfilScreen(nav, vm) }
    }
}
