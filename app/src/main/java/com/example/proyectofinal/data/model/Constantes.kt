package com.example.proyectofinal.data.model


// ---------------------------------------------------------------------------
//  Valores de los ENUM del script MySQL (en SQLite se guardan como texto)
// ---------------------------------------------------------------------------

object Rol {
    const val CLIENTE = "CLIENTE"
    const val BARBERO = "BARBERO"
    const val ADMINISTRADOR = "ADMINISTRADOR"
}

object EstadoCitaDb {
    const val PENDIENTE = "PENDIENTE"
    const val CONFIRMADA = "CONFIRMADA"
    const val RECHAZADA = "RECHAZADA"
    const val CANCELADA = "CANCELADA"
    const val COMPLETADA = "COMPLETADA"
}

object EstadoSuscripcion {
    const val ACTIVA = "ACTIVA"
    const val POR_VENCER = "POR_VENCER"
    const val VENCIDA = "VENCIDA"
}

object TipoNotificacion {
    const val SUSCRIPCION_POR_VENCER = "SUSCRIPCION_POR_VENCER"
    const val SUSCRIPCION_VENCIDA = "SUSCRIPCION_VENCIDA"
    const val CITA = "CITA"
    const val GENERAL = "GENERAL"
}
