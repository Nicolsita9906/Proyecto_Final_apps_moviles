package com.example.proyectofinal.data

import com.example.proyectofinal.data.mapper.hora12
import com.example.proyectofinal.data.model.BloqueoHorarioEntity
import com.example.proyectofinal.data.model.CitaEntity
import com.example.proyectofinal.data.model.HorarioAtencionEntity
import java.util.Calendar

/**
 * Calcula los días y las horas que el cliente puede reservar, a partir de:
 *  - horario_atencion de la barbería (qué días abre y de qué hora a qué hora),
 *  - las citas activas del barbero ese día (no se puede cruzar con otra),
 *  - bloqueo_horario de la barbería (descansos, festivos, ausencias),
 *  - la hora actual (no se reserva en el pasado).
 *
 * Reemplaza a los días/horas fijos de DatosEstaticos.
 */
object Disponibilidad {

    /** Cada cuántos minutos empieza una franja (9:00, 9:30, 10:00...). */
    const val INTERVALO_MIN = 30

    /** Cuántos días abiertos se muestran en el selector. */
    const val DIAS_A_MOSTRAR = 6

    /** Hasta cuántos días hacia adelante se buscan días abiertos. */
    private const val DIAS_A_BUSCAR = 21

    private val LETRAS = listOf("", "L", "M", "M", "J", "V", "S", "D")
    private val MESES = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    // ------------------------------- Fechas -------------------------------

    /** 1 = lunes ... 7 = domingo (igual que horario_atencion.dia_semana). */
    fun diaSemana(cal: Calendar): Int {
        val d = cal.get(Calendar.DAY_OF_WEEK)
        return if (d == Calendar.SUNDAY) 7 else d - 1
    }

    fun iso(cal: Calendar): String =
        "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))

    fun hoyIso(): String = iso(Calendar.getInstance())

    /** "2026-11-16" -> Calendar a las 00:00 de ese día. */
    fun calendarDe(iso: String): Calendar? {
        val p = iso.split("-").mapNotNull { it.toIntOrNull() }
        if (p.size != 3) return null
        return Calendar.getInstance().apply {
            clear()
            set(p[0], p[1] - 1, p[2])
        }
    }

    /** "Noviembre" o "Noviembre - Diciembre" según los días que se muestran. */
    fun textoMes(dias: List<Dia>): String {
        val meses = dias.mapNotNull { calendarDe(it.iso)?.get(Calendar.MONTH) }.distinct()
        return meses.joinToString(" - ") { MESES[it] }
    }

    /** "HH:mm" -> minutos desde la medianoche. */
    fun minutos(hora: String): Int {
        val p = hora.split(":")
        return (p.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (p.getOrNull(1)?.toIntOrNull() ?: 0)
    }

    fun hhmm(minutos: Int): String = "%02d:%02d".format(minutos / 60, minutos % 60)

    // ------------------------------- Días -------------------------------

    /** Los próximos días (desde hoy) en los que la barbería abre. */
    fun proximosDias(
        horarios: List<HorarioAtencionEntity>,
        desde: Calendar = Calendar.getInstance()
    ): List<Dia> {
        val abiertos = horarios
            .filter { !it.cerrado && it.horaApertura != null && it.horaCierre != null }
            .map { it.diaSemana }
            .toSet()
        if (abiertos.isEmpty()) return emptyList()

        val cal = desde.clone() as Calendar
        val dias = mutableListOf<Dia>()
        repeat(DIAS_A_BUSCAR) {
            val ds = diaSemana(cal)
            if (ds in abiertos) dias += Dia(LETRAS[ds], cal.get(Calendar.DAY_OF_MONTH), iso(cal))
            if (dias.size == DIAS_A_MOSTRAR) return dias
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return dias
    }

    // ------------------------------- Horas -------------------------------

    /**
     * Franjas del día con su disponibilidad.
     * @param duracionMin duración del servicio elegido (la franja debe caber antes del cierre).
     * @param citas citas activas que compiten por la franja (del barbero, o "sin preferencia").
     * @param excluirCita id de la cita que se está modificando (no choca consigo misma).
     */
    fun horarios(
        dia: Dia,
        horariosBarberia: List<HorarioAtencionEntity>,
        duracionMin: Int,
        citas: List<CitaEntity>,
        bloqueos: List<BloqueoHorarioEntity>,
        excluirCita: Int? = null,
        ahora: Calendar = Calendar.getInstance()
    ): List<Horario> {
        val fechaCal = calendarDe(dia.iso) ?: return emptyList()
        val hoy = horariosBarberia.firstOrNull { it.diaSemana == diaSemana(fechaCal) } ?: return emptyList()
        val apertura = hoy.horaApertura
        val cierre = hoy.horaCierre
        if (hoy.cerrado || apertura == null || cierre == null) return emptyList()

        val duracion = if (duracionMin > 0) duracionMin else INTERVALO_MIN
        val ocupadas = citas.filter { it.idCita != excluirCita }
        val esHoy = dia.iso == iso(ahora)
        val minutoActual = ahora.get(Calendar.HOUR_OF_DAY) * 60 + ahora.get(Calendar.MINUTE)
        val inicioDiaMs = fechaCal.timeInMillis

        val franjas = mutableListOf<Horario>()
        var inicio = minutos(apertura)
        val fin = minutos(cierre)
        while (inicio + duracion <= fin) {
            val ini = hhmm(inicio)
            val finFranja = hhmm(inicio + duracion)
            val iniMs = inicioDiaMs + inicio * 60_000L
            val finMs = inicioDiaMs + (inicio + duracion) * 60_000L

            val pasada = esHoy && inicio <= minutoActual
            val choca = ocupadas.any { it.horaInicio < finFranja && it.horaFin > ini }
            val bloqueada = bloqueos.any { it.fechaInicio < finMs && it.fechaFin > iniMs }

            val texto = hora12(ini)
            franjas += Horario(texto = texto, completa = texto, disponible = !pasada && !choca && !bloqueada)
            inicio += INTERVALO_MIN
        }
        return franjas
    }
}
