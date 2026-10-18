package com.example.proyectofinal.data.mapper

import com.example.proyectofinal.R
import com.example.proyectofinal.data.Barberia
import com.example.proyectofinal.data.Barbero
import com.example.proyectofinal.data.Cita
import com.example.proyectofinal.data.DatosEstaticos
import com.example.proyectofinal.data.Servicio
import com.example.proyectofinal.data.model.BarberiaResumen
import com.example.proyectofinal.data.model.CitaDetalle
import com.example.proyectofinal.data.model.HorarioAtencionEntity
import com.example.proyectofinal.data.model.ServicioEntity
import com.example.proyectofinal.data.model.UsuarioEntity
import java.util.Calendar
import java.util.Locale

/**
 * Convierte lo que devuelve Room (formato de la base de datos) al formato que ya
 * usan las pantallas (Servicio, Barberia, Barbero, Cita). Así la interfaz no cambia.
 */

private const val ANIO_DEMO = 2026
private val MESES = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
private val DIAS_CORTOS = listOf("", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

/** Opción fija de la app (no existe en la BD): cita sin barbero asignado. */
val SIN_PREFERENCIA = Barbero(0, "Sin preferencia", "Cualquier barbero disponible", 0.0)

// ------------------------------- Formatos -------------------------------

fun precioTexto(precio: Double): String =
    "$" + String.format(Locale("es", "CO"), "%,d", precio.toInt())

/** "2026-09-10" -> "10 sep" */
fun fechaCorta(iso: String): String {
    val partes = iso.split("-")
    if (partes.size != 3) return iso
    val mes = partes[1].toIntOrNull()?.let { MESES.getOrNull(it - 1) } ?: partes[1]
    return "${partes[2].toIntOrNull() ?: partes[2]} $mes"
}

/** Día del mes elegido en la app -> "2026-09-10" (el mes sale de DatosEstaticos.MES_CORTO). */
fun fechaIso(dia: Int): String {
    val mes = MESES.indexOf(DatosEstaticos.MES_CORTO) + 1
    return "%04d-%02d-%02d".format(ANIO_DEMO, if (mes > 0) mes else 1, dia)
}

/** "14:30" -> "2:30 PM" */
fun hora12(hora24: String): String {
    val p = hora24.split(":")
    val h = p.getOrNull(0)?.toIntOrNull() ?: return hora24
    val m = p.getOrNull(1) ?: "00"
    val sufijo = if (h < 12) "AM" else "PM"
    val h12 = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
    }
    return "$h12:$m $sufijo"
}

/** "2:30 PM" -> "14:30" */
fun hora24(hora12: String): String {
    val partes = hora12.trim().split(" ")
    val hm = partes[0].split(":")
    var h = hm[0].toIntOrNull() ?: return hora12
    val m = hm.getOrNull(1) ?: "00"
    val esPm = partes.getOrNull(1).equals("PM", ignoreCase = true)
    if (esPm && h < 12) h += 12
    if (!esPm && h == 12) h = 0
    return "%02d:%s".format(h, m)
}

/** "10:30" + 45 min -> "11:15" */
fun sumarMinutos(hora24: String, minutos: Int): String {
    val p = hora24.split(":")
    val total = (p[0].toInt() * 60 + p[1].toInt() + minutos) % (24 * 60)
    return "%02d:%02d".format(total / 60, total % 60)
}

private fun estadoTexto(estado: String): String =
    estado.lowercase().replaceFirstChar { it.uppercase() }   // "CONFIRMADA" -> "Confirmada"

// ------------------------------- Servicio / Barbero -------------------------------

fun ServicioEntity.aServicio(): Servicio = Servicio(
    nombre = nombre,
    duracion = "$duracionMinutos min",
    precio = precioTexto(precio),
    icon = R.drawable.andrsrxn__scissors,
    id = idServicio,
    duracionMin = duracionMinutos,
    precioValor = precio
)

/**
 * La tabla `usuario` no tiene especialidad ni calificación del barbero.
 * Mientras no existan, se muestra un texto fijo.
 * TODO: si el equipo agrega esas columnas, leerlas aquí.
 */
fun UsuarioEntity.aBarbero(): Barbero = Barbero(
    id = idUsuario,
    nombre = nombre,
    especialidad = "Barbero profesional",
    calificacion = 0.0
)

// ------------------------------- Barbería (rating, horario, abierto) -------------------------------

fun BarberiaResumen.aBarberia(horarios: List<HorarioAtencionEntity>): Barberia = Barberia(
    id = idBarberia,
    nombre = nombre,
    rating = if (promedio != null) String.format(Locale.US, "%.1f", promedio) else "Nuevo",
    // TODO: la distancia real necesita el GPS del celular + barberia.latitud/longitud.
    // Mientras tanto se muestra la ciudad.
    distancia = ciudad,
    abierto = estaAbierto(horarios),
    direccion = direccion,
    horario = textoHorario(horarios)
)

/** Ej.: "Lun-Sáb 9:00-19:00" (usa el primer y último día abierto y las horas del primero). */
fun textoHorario(filas: List<HorarioAtencionEntity>): String {
    val abiertos = filas
        .filter { !it.cerrado && it.horaApertura != null && it.horaCierre != null }
        .sortedBy { it.diaSemana }
    if (abiertos.isEmpty()) return "Cerrado"
    val primero = abiertos.first()
    val ultimo = abiertos.last()
    val rango =
        if (primero.diaSemana == ultimo.diaSemana) DIAS_CORTOS[primero.diaSemana]
        else "${DIAS_CORTOS[primero.diaSemana]}-${DIAS_CORTOS[ultimo.diaSemana]}"
    return "$rango ${primero.horaApertura!!.trimStart('0')}-${primero.horaCierre!!.trimStart('0')}"
}

/** true si AHORA cae dentro del horario de hoy. */
fun estaAbierto(filas: List<HorarioAtencionEntity>): Boolean {
    val cal = Calendar.getInstance()
    val d = cal.get(Calendar.DAY_OF_WEEK)
    val dia = if (d == Calendar.SUNDAY) 7 else d - 1          // 1 = lunes ... 7 = domingo
    val ahora = "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    val hoy = filas.firstOrNull { it.diaSemana == dia } ?: return false
    val apertura = hoy.horaApertura
    val cierre = hoy.horaCierre
    return !hoy.cerrado && apertura != null && cierre != null && ahora >= apertura && ahora < cierre
}

// ------------------------------- Cita -------------------------------

fun CitaDetalle.aCita(): Cita = Cita(
    id = idCita,
    servicio = Servicio(
        nombre = servicioNombre,
        duracion = "$duracionMinutos min",
        precio = precioTexto(valorTotal),
        icon = R.drawable.andrsrxn__scissors,
        id = idServicio,
        duracionMin = duracionMinutos,
        precioValor = valorTotal
    ),
    barbero = if (idBarbero == null) SIN_PREFERENCIA
    else Barbero(idBarbero, barberoNombre ?: "Barbero", "Barbero profesional", 0.0),
    fecha = fechaCorta(fecha),
    hora = hora12(horaInicio),
    estado = estadoTexto(estado)
)
