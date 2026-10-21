package com.example.proyectofinal.data.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Datos iniciales (catálogo): se insertan cuando la base está vacía (ver onOpen).
 * Reemplaza a las listas de DatosEstaticos (barberías, servicios, barberos, reseñas).
 *
 * Los usuarios de ejemplo (barberos y reseñistas) no pueden iniciar sesión:
 * su contrasena_hash es un texto que ningún SHA-256 produce.
 *
 * Si cambias estas semillas o las entidades: sube `version` en AppDatabase (la base se recrea
 * y se vuelve a sembrar sola) o desinstala la app del emulador.
 */
object SemillaBD : RoomDatabase.Callback() {

    // Se ejecuta cada vez que se abre la base. Si está vacía (primera vez, o Room la recreó por un
    // cambio de versión) se carga el catálogo; si ya tiene datos no hace nada.
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        val vacia = db.query("SELECT COUNT(*) FROM ciudad").use { it.moveToFirst() && it.getInt(0) == 0 }
        if (vacia) {
            db.beginTransaction()
            try {
                sembrar(db)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    private fun sembrar(db: SupportSQLiteDatabase) {
        val ahora = System.currentTimeMillis()
        val sinAcceso = "sin_acceso"

        // ---- ciudad ----
        db.execSQL("INSERT INTO ciudad (id_ciudad, nombre, departamento) VALUES (1, 'Armenia', 'Quindío')")

        // ---- usuario (barberos, clientes y administrador de ejemplo) ----
        // Hash SHA-256 de "admin123": 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
        db.execSQL(
            """
            INSERT INTO usuario (id_usuario, nombre, correo, telefono, contrasena_hash, foto, rol, estado, fecha_registro) VALUES
              (1, 'Andrés M.', 'andres@barbergo.com', NULL, '$sinAcceso', NULL, 'BARBERO', 'ACTIVO', $ahora),
              (2, 'Luis P.',   'luis@barbergo.com',   NULL, '$sinAcceso', NULL, 'BARBERO', 'ACTIVO', $ahora),
              (3, 'Camila R.', 'camila@ejemplo.com',  NULL, '$sinAcceso', NULL, 'CLIENTE', 'ACTIVO', $ahora),
              (4, 'Juan D.',   'juan@ejemplo.com',    NULL, '$sinAcceso', NULL, 'CLIENTE', 'ACTIVO', $ahora),
              (5, 'Admin BarberGo', 'admin@barbergo.com', '3000000000', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', NULL, 'ADMINISTRADOR', 'ACTIVO', $ahora)
            """.trimIndent()
        )

        // ---- barberia ----
        db.execSQL(
            """
            INSERT INTO barberia (id_barberia, id_propietario, id_ciudad, nombre, direccion, descripcion, telefono, foto,
                                  latitud, longitud, estado_aprobacion, horas_limite_cancelacion, fecha_registro) VALUES
              (1, 1, 1, 'Barbería el corte', 'Cra 14 #10-25',    NULL, NULL, NULL, NULL, NULL, 'APROBADA', 2, $ahora),
              (2, 2, 1, 'Talento urbano',    'Cl 20 #14-30',     NULL, NULL, NULL, NULL, NULL, 'APROBADA', 2, $ahora),
              (3, 1, 1, 'Estilo clásico',    'Av. Bolívar #5-12', NULL, NULL, NULL, NULL, NULL, 'APROBADA', 2, $ahora)
            """.trimIndent()
        )

        // ---- horario_atencion (1 = lunes ... 7 = domingo) ----
        // Barbería 1: Lun-Sáb 09:00-19:00 | Barbería 2: Lun-Sáb 10:00-20:00 | Barbería 3: Mar-Dom 09:00-18:00
        val horarios = listOf(
            Triple(1, "09:00", "19:00") to (1..6),
            Triple(2, "10:00", "20:00") to (1..6),
            Triple(3, "09:00", "18:00") to (2..7)
        )
        for ((datos, diasAbiertos) in horarios) {
            val (idBarberia, apertura, cierre) = datos
            for (dia in 1..7) {
                if (dia in diasAbiertos) {
                    db.execSQL(
                        "INSERT INTO horario_atencion (id_barberia, dia_semana, hora_apertura, hora_cierre, cerrado) " +
                            "VALUES ($idBarberia, $dia, '$apertura', '$cierre', 0)"
                    )
                } else {
                    db.execSQL(
                        "INSERT INTO horario_atencion (id_barberia, dia_semana, hora_apertura, hora_cierre, cerrado) " +
                            "VALUES ($idBarberia, $dia, NULL, NULL, 1)"
                    )
                }
            }
        }

        // ---- servicio (los mismos 3 en cada barbería) ----
        for (idBarberia in 1..3) {
            db.execSQL(
                """
                INSERT INTO servicio (id_barberia, nombre, descripcion, duracion_minutos, precio, activo) VALUES
                  ($idBarberia, 'Corte clásico',    NULL, 30, 25000, 1),
                  ($idBarberia, 'Arreglo de barba', NULL, 20, 15000, 1),
                  ($idBarberia, 'Corte + barba',    NULL, 45, 35000, 1)
                """.trimIndent()
            )
        }

        // ---- resena ----
        db.execSQL(
            """
            INSERT INTO resena (id_cliente, id_barberia, calificacion, comentario, fecha) VALUES
              (3, 1, 5, 'Excelente atención, el corte quedó perfecto.', $ahora),
              (4, 1, 4, 'Muy buen ambiente y puntuales.', $ahora),
              (3, 2, 5, 'Excelente atención, el corte quedó perfecto.', $ahora),
              (4, 2, 4, 'Muy buen ambiente y puntuales.', $ahora),
              (3, 3, 5, 'Excelente atención, el corte quedó perfecto.', $ahora),
              (4, 3, 5, 'Muy buen ambiente y puntuales.', $ahora)
            """.trimIndent()
        )

        // ---- suscripcion y pago_suscripcion (datos de EJEMPLO para los 2 barberos) ----
        db.execSQL(
            """
            INSERT INTO suscripcion (id_suscripcion, id_barbero, fecha_inicio, fecha_vencimiento, estado) VALUES
              (1, 1, '2026-10-01', '2026-11-01', 'ACTIVA'),
              (2, 2, '2026-10-01', '2026-11-01', 'ACTIVA')
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO pago_suscripcion (id_suscripcion, monto, fecha_pago, metodo_pago, referencia) VALUES
              (1, 30000, $ahora, 'PSE', 'REF-0001'),
              (2, 30000, $ahora, 'PSE', 'REF-0002')
            """.trimIndent()
        )
    }
}
