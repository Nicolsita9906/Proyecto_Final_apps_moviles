package com.example.proyectofinal.data.database

import android.content.Context
import androidx.room.Room

/**
 * Singleton: una sola conexión a la base de datos en toda la app (patrón de la guía).
 * Al crearse se cargan los datos iniciales (SemillaBD).
 */
object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "barbergo_db"
            )
                .addCallback(SemillaBD)
                // SOLO PARA DESARROLLO: si cambias la versión, Room borra la base y la crea de nuevo
                // (se pierden los datos). Antes de entregar a usuarios reales hay que escribir una
                // Migration en lugar de esto.
                .fallbackToDestructiveMigration(true)
                .build()
            INSTANCE = instance
            instance
        }
    }
}
