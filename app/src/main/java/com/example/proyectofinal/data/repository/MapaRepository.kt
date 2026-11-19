package com.example.proyectofinal.data.repository

import com.example.proyectofinal.data.model.DireccionResponse
import com.example.proyectofinal.data.model.LugarResponse
import com.example.proyectofinal.data.remote.NominatimApi
import com.example.proyectofinal.data.remote.RetrofitInstance

/**
 * Capa intermedia entre el ViewModel y la API (guía "Separación de responsabilidades"):
 *
 *   PantallaMapa -> MapaViewModel -> MapaRepository -> NominatimApi (Retrofit)
 *
 * El ViewModel no sabe que los datos vienen de internet ni que se usa Retrofit.
 * Se puede pasar otra API en el constructor (por ejemplo, una falsa para pruebas).
 */
class MapaRepository(private val api: NominatimApi = RetrofitInstance.api) {

    suspend fun getDireccion(lat: Double, lon: Double): DireccionResponse =
        api.getDireccion(lat, lon)

    suspend fun buscarLugares(texto: String): List<LugarResponse> =
        api.buscarLugares(texto)
}
