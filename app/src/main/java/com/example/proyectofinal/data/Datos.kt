package com.example.proyectofinal.data

import androidx.compose.ui.graphics.Color
import com.example.proyectofinal.R


val Oscuro = Color(0xFF1D1B17)
val Dorado = Color(0xFFD79B4B)
val Crema = Color(0xFFF2EBDF)
val Beige = Color(0xFFEDD7B5)
val Gris = Color(0xFF8A8372)
val FondoOscuro = Color(0xFF2A2621)


data class Servicio(
    val nombre: String,
    val duracion: String,
    val precio: String,
    val icon: Int
)

data class Barberia(
    val id: Int,
    val nombre: String,
    val rating: String,
    val distancia: String,
    val abierto: Boolean,
    val direccion: String,
    val horario: String
)

data class Barbero(
    val id: Int,
    val nombre: String,
    val especialidad: String,
    val calificacion: Double
)

data class Cita(
    val id: Int,
    val servicio: Servicio,
    val barbero: Barbero,
    val fecha: String,
    val hora: String,
    val estado: String // "Confirmada", "Completada", "Cancelada"
)

data class Horario(val texto: String, val completa: String, val disponible: Boolean)

data class Dia(val letra: String, val numero: Int)

data class Resena(
    val autor: String,
    val estrellas: Int,
    val texto: String
)