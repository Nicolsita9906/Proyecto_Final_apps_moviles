package com.example.proyectofinal.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Objeto único que gestiona la conexión con la API (como en la guía):
 *  - URL base
 *  - convertidor de JSON (Gson)
 *  - instancia reutilizable (by lazy: se crea la primera vez que se usa)
 *
 * Diferencia con PokeAPI: Nominatim exige que cada petición diga qué aplicación la hace
 * (cabecera User-Agent). Por eso se agrega un OkHttpClient con esa cabecera.
 */
object RetrofitInstance {

    private const val URL_BASE = "https://nominatim.openstreetmap.org/"

    private val cliente: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { cadena ->
                val peticion = cadena.request().newBuilder()
                    .header("User-Agent", "BarberGo-Android/1.0 (proyecto academico)")
                    .build()
                cadena.proceed(peticion)
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val api: NominatimApi by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimApi::class.java)
    }
}
