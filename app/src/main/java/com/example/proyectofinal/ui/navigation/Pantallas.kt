package com.example.proyectofinal.navigation

sealed class Pantallas(val ruta: String) {
    object Login : Pantallas("login")
    object Registro : Pantallas("registro")
    object Splash : Pantallas("splash")                 // (el splash se muestra dentro de Home; ver AppNav)
    object Home : Pantallas("inicio")                   // PantallaInicio (lista de barberías)
    object Detalle : Pantallas("detalle/{id}") {        // PantallaDetalle (detalle de la barbería)
        fun crear(id: Int) = "detalle/$id"
    }
    object ServicioBarbero : Pantallas("servicio_barbero")
    object FechaHora : Pantallas("fecha_hora")
    object Confirmar : Pantallas("confirmar")           // PantallaConfirmar
    object MisCitas : Pantallas("mis_citas")
    object DetalleCita : Pantallas("detalle_cita/{citaId}") {
        fun crear(id: Int) = "detalle_cita/$id"
    }
    object Perfil : Pantallas("perfil")

    // ---------- Administrador ----------
    object AdminPanel : Pantallas("admin_panel")
    object AdminUsuarios : Pantallas("admin_usuarios")
    object AdminBarberias : Pantallas("admin_barberias")
    object AdminSuscripciones : Pantallas("admin_suscripciones")
}
