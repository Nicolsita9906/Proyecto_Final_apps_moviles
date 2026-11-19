package com.example.proyectofinal.viewmodel

/**
 * Todos los estados por los que pasa una consulta a la API (guía "Manejo de estados UiState"):
 *  Idle    -> aún no se ha consultado
 *  Loading -> esperando la respuesta de la API
 *  Success -> la API respondió; `data` trae el resultado
 *  Error   -> sin internet, la API falló, etc.
 *
 * Es genérico (<T>) para reutilizarlo con distintas respuestas:
 *  UiState<DireccionResponse>     -> dirección del punto tocado en el mapa
 *  UiState<List<LugarResponse>>   -> lista de lugares que devuelve el buscador
 */
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
