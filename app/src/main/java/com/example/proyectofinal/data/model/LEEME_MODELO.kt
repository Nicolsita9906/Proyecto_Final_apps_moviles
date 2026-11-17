package com.example.proyectofinal.data.model

/**
 * ============================================================================
 *  ENTIDADES ROOM = tablas del script MySQL de BarberGo (barbergo.sql)
 * ============================================================================
 *  Los nombres de tabla y de columna son los MISMOS del script, para que la
 *  base local (SQLite/Room) y la del servidor (MySQL) hablen igual.
 *
 *  Las 13 tablas del script están aquí:
 *    ciudad, usuario, barberia, foto_barberia, servicio, horario_atencion,
 *    bloqueo_horario, cita, suscripcion, pago_suscripcion, resena, favorito,
 *    notificacion.
 *  Cada una tiene su DAO (un archivo por tabla, ej.: CitaDao.kt).
 *
 *  Diferencias con el script (por limitaciones de SQLite/Room):
 *   - ENUM de MySQL -> TEXT con los mismos valores en MAYÚSCULAS.
 *   - DATE/TIME/DATETIME -> TEXT ("2026-09-10", "10:30") o Long (fecha_registro).
 *   - DECIMAL -> Double.
 *   - Los CHECK y la columna generada `slot_activo` no se pueden declarar en
 *     Room; se validan en código / quedan para el backend.
 *   - cita.id_barbero acepta NULL = "Sin preferencia" (la app lo ofrece; en el
 *     script es NOT NULL, hay que acordarlo con el backend).
 * ============================================================================
 */

// Este archivo solo documenta el paquete: cada tabla del script es una clase XxxEntity en esta carpeta.
