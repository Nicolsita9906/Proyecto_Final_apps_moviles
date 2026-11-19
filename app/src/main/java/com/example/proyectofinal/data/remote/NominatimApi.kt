package com.example.proyectofinal.data.remote

import com.example.proyectofinal.data.model.DireccionResponse
import com.example.proyectofinal.data.model.LugarResponse
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

    /**
     * "search" = buscador: se envía un texto ("Calle 21", "Parque Fundadores") y la API
     * responde con una LISTA de lugares. viewbox + bounded=1 limitan la búsqueda a Armenia.
     */
    @GET("search")
    suspend fun buscarLugares(
        @Query("q") texto: String,
        @Query("format") format: String = "jsonv2",
        @Query("viewbox") zona: String = "-75.7500,4.6000,-75.6100,4.4700",   // oeste,norte,este,sur
        @Query("bounded") limitar: Int = 1,
        @Query("countrycodes") pais: String = "co",
        @Query("addressdetails") detalles: Int = 1,
        @Query("limit") limite: Int = 10,
        @Query("accept-language") idioma: String = "es"
    ): List<LugarResponse>
}
