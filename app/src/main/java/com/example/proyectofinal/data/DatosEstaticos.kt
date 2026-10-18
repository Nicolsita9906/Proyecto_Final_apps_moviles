package com.example.proyectofinal.data

import com.example.proyectofinal.R

/**
 * ============================================================
 *  FUENTE ÚNICA DE DATOS (ESTÁTICOS POR AHORA)
 * ============================================================
 *  Usa tus modelos: Servicio, Barberia, Barbero, Cita, Dia, Horario, Resena.
 *  Para agregar persistencia (Room / SQLite / API / MySQL) se reemplazan
 *  estas listas por consultas en BarberViewModel ("TODO PERSISTENCIA").
 *
 *   barberia         -> tabla `barberia`
 *   servicios        -> tabla `servicio`
 *   barberos         -> tabla `barbero`
 *   resenas          -> tabla `resena`
 *   dias / horarios  -> tabla `disponibilidad` (barbero_id, fecha, hora, ocupada)
 *   citasIniciales   -> tabla `cita`
 *   barberiaPorCita  -> columna `barberia_id` de `cita` (el modelo Cita no la trae)
 *   sesión           -> SharedPreferences (SesionPrefs.kt); usuarios -> tabla `usuario`
 * ============================================================
 */
object DatosEstaticos {

    // TODO PERSISTENCIA: SELECT * FROM barberia
    // Nota: el id coincide con la posición en la lista (se busca por id).
    val barberias = listOf(
        Barberia(0, "Barbería el corte", "4.8", "1.2 km", true, "Cra 14 #10-25", "Lun-Sáb 9:00-19:00"),
        Barberia(1, "Talento urbano", "4.6", "2.5 km", true, "Cl 20 #14-30", "Lun-Sáb 10:00-20:00"),
        Barberia(2, "Estilo clásico", "4.9", "3.1 km", false, "Av. Bolívar #5-12", "Mar-Dom 9:00-18:00")
    )

    // TODO PERSISTENCIA: SELECT * FROM servicio WHERE barberia_id = ?
    // TODO DISEÑO: cambia el ícono de cada servicio por el R.drawable.* que corresponda
    val servicios = listOf(
        Servicio("Corte clásico", "30 min", "\$25.000", R.drawable.andrsrxn__scissors),
        Servicio("Arreglo de barba", "20 min", "\$15.000", R.drawable.andrsrxn__scissors),
        Servicio("Corte + barba", "45 min", "\$35.000", R.drawable.andrsrxn__scissors)
    )

    // TODO PERSISTENCIA: SELECT * FROM barbero WHERE barberia_id = ?
    // `especialidad` guarda aquí la disponibilidad ("Disponible hoy") para igualar el mockup.
    // id 0 = "Sin preferencia" (opción fija de la app, no va en la BD).
    val barberos = listOf(
        Barbero(1, "Andrés M.", "Disponible hoy", 4.9),
        Barbero(2, "Luis P.", "Disponible mañana", 4.7),
        Barbero(0, "Sin preferencia", "Cualquier barbero disponible", 0.0)
    )

    // TODO PERSISTENCIA: SELECT * FROM resena WHERE barberia_id = ?
    val resenas = listOf(
        Resena("Camila R.", 5, "Excelente atención, el corte quedó perfecto."),
        Resena("Juan D.", 4, "Muy buen ambiente y puntuales.")
    )

    // TODO PERSISTENCIA: SELECT fecha FROM disponibilidad WHERE barbero_id = ? AND ocupada = 0
    const val MES = "Septiembre"
    const val MES_CORTO = "sep"
    val dias = listOf(
        Dia("L", 8), Dia("M", 9), Dia("M", 10),
        Dia("J", 11), Dia("V", 12), Dia("S", 13)
    )

    // TODO PERSISTENCIA: SELECT hora, ocupada FROM disponibilidad WHERE barbero_id = ? AND fecha = ?
    val horarios = listOf(
        Horario("9:00", "9:00 AM", true),
        Horario("9:30", "9:30 AM", false),
        Horario("10:00", "10:00 AM", true),
        Horario("10:30", "10:30 AM", true),
        Horario("11:00", "11:00 AM", true),
        Horario("11:30", "11:30 AM", false),
        Horario("2:00", "2:00 PM", true),
        Horario("2:30", "2:30 PM", true),
        Horario("3:00", "3:00 PM", true)
    )

    // TODO PERSISTENCIA: SELECT * FROM cita WHERE usuario_id = ?
    val citasIniciales = listOf(
        Cita(1, servicios[2], barberos[0], "10 sep", "10:30 AM", "Confirmada"),
        Cita(2, servicios[0], barberos[2], "14 sep", "4:00 PM", "Pendiente"),
        Cita(3, servicios[0], barberos[1], "20 ago", "4:00 PM", "Completada"),
        Cita(4, servicios[1], barberos[2], "2 ago", "11:00 AM", "Cancelada")
    )

    // TODO PERSISTENCIA: JOIN cita -> barberia (nombre de la barbería de cada cita)
    val barberiaPorCita = mapOf(
        1 to "Barbería el corte",
        2 to "Talento urbano",
        3 to "Barbería el corte",
        4 to "Talento urbano"
    )

    // TODO PERSISTENCIA: dirección de la barbería de la cita
    const val DIRECCION_CITA = "Cra 14 #10-25, Armenia, Quindío"
}

// Alias con los nombres que usan las pantallas de cliente
val listaBarberias: List<Barberia> = DatosEstaticos.barberias
val listaServicios: List<Servicio> = DatosEstaticos.servicios
