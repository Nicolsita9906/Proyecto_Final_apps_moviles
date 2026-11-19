package com.example.proyectofinal.data.model

/**
 * Respuesta de la API de Nominatim (OpenStreetMap) al pedir la dirección de un punto del mapa:
 *   https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=4.5339&lon=-75.6811
 *
 * El JSON es grande; solo se modela lo que se usa (igual que PokemonResponse en la guía).
 * Los nombres coinciden con los del JSON para que Gson los convierta solo.
 */
data class DireccionResponse(
    val display_name: String?,
    val address: Direccion?
) {
    /** Texto corto para el formulario: "Carrera 14 #10-25" o, si no hay calle, el inicio de display_name. */
    fun textoCorto(): String {
        val calle = address?.road
        if (!calle.isNullOrBlank()) {
            val numero = address.house_number
            return if (numero.isNullOrBlank()) calle else "$calle #$numero"
        }
        return display_name.orEmpty().split(",").take(2).joinToString(",").trim()
    }

    /** Barrio, si la API lo trae (se muestra como ayuda debajo de la dirección). */
    fun barrio(): String? = address?.neighbourhood ?: address?.suburb

    /**
     * Ciudad del punto ("Armenia" si la API no la trae).
     * En Colombia "city" llega como "Perímetro Urbano Armenia"; el nombre limpio viene en "county".
     */
    fun ciudad(): String =
        address?.county
            ?: address?.city?.removePrefix("Perímetro Urbano ")
            ?: address?.town
            ?: address?.village
            ?: "Armenia"

    fun departamento(): String = address?.state ?: "Quindío"
}

/** Objeto interno "address" del JSON. Todos opcionales: depende de qué tan detallado esté el mapa. */
data class Direccion(
    val road: String?,
    val house_number: String?,
    val neighbourhood: String?,
    val suburb: String?,
    val city: String?,
    val town: String?,
    val village: String?,
    val county: String?,
    val state: String?
)
