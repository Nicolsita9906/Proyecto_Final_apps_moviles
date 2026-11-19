package com.example.proyectofinal.data.model

/**
 * Un resultado del buscador de Nominatim:
 *   https://nominatim.openstreetmap.org/search?q=Calle 21&format=jsonv2&...
 *
 * La API devuelve una LISTA de estos objetos (JSON array -> List<LugarResponse>).
 * Ojo: lat y lon llegan como texto ("4.5339"), por eso son String y se convierten abajo.
 */
data class LugarResponse(
    val place_id: Long,
    val name: String?,
    val display_name: String,
    val lat: String,
    val lon: String,
    val address: Direccion?
) {
    val latitud: Double get() = lat.toDoubleOrNull() ?: 0.0
    val longitud: Double get() = lon.toDoubleOrNull() ?: 0.0

    /** Línea principal de la lista: el nombre del lugar o la calle. */
    fun titulo(): String =
        name?.takeIf { it.isNotBlank() }
            ?: address?.road
            ?: display_name.substringBefore(",")

    /** Línea secundaria: barrio y comuna (sin repetir el título ni "Colombia"). */
    fun detalle(): String =
        display_name.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() && it != titulo() && it != "Colombia" }
            .take(3)
            .joinToString(", ")
}
