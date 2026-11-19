package com.example.proyectofinal.data.remote

import com.example.proyectofinal.data.model.DireccionResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Define CÓMO se hace la petición a la API (como PokemonApi en la guía).
 *
 * "reverse" = geocodificación inversa: se envían las coordenadas del punto que se tocó
 * en el mapa y la API responde con la dirección (calle, barrio, ciudad...).
 *
 *  @GET    -> endpoint (se suma a la URL base de RetrofitInstance)
 *  @Query  -> agrega ?lat=...&lon=... a la URL
 *  suspend -> se ejecuta en una corrutina, sin bloquear la interfaz
 */
interface NominatimApi {

    @GET("reverse")
    suspend fun getDireccion(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "jsonv2",
        @Query("zoom") zoom: Int = 18,                 // 18 = nivel de calle / edificio
        @Query("addressdetails") detalles: Int = 1,
        @Query("accept-language") idioma: String = "es"
    ): DireccionResponse
}
