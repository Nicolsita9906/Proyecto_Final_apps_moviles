package com.example.proyectofinal.viewmodel

import com.example.proyectofinal.data.model.DireccionResponse

/**
 * Todos los estados por los que pasa la consulta de la dirección (guía "Manejo de estados UiState"):
 *  Idle    -> aún no se ha tocado el mapa
 *  Loading -> esperando la respuesta de la API
 *  Success -> la API devolvió la dirección
 *  Error   -> sin internet, la API falló, etc.
 */
sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val data: DireccionResponse) : UiState()
    data class Error(val message: String) : UiState()
}
