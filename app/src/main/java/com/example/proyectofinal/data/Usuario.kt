package com.example.proyectofinal.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

data class Usuario(
    val nombre: String,
    val correo: String,
    val telefono: String,
    val contrasena: String
)

object Sesion {

    // Archivo de SharedPreferences con las cuentas registradas (sobrevive al cerrar la app)
    // TODO PERSISTENCIA: reemplazar por tabla "usuario" en BD / API de autenticación
    private const val PREFS_USUARIOS = "usuarios_prefs"
    private const val KEY_LISTA = "lista"

    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set

    /** La contraseña no se guarda en texto plano: se guarda su hash SHA-256. */
    private fun hash(texto: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(texto.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private fun cargarUsuarios(context: Context): MutableList<Usuario> {
        val json = context.getSharedPreferences(PREFS_USUARIOS, Context.MODE_PRIVATE)
            .getString(KEY_LISTA, null) ?: return mutableListOf()
        val lista = mutableListOf<Usuario>()
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            lista.add(
                Usuario(
                    nombre = o.getString("nombre"),
                    correo = o.getString("correo"),
                    telefono = o.getString("telefono"),
                    contrasena = o.getString("contrasena") // ya viene con hash
                )
            )
        }
        return lista
    }

    private fun guardarUsuarios(context: Context, lista: List<Usuario>) {
        val arr = JSONArray()
        lista.forEach {
            arr.put(
                JSONObject()
                    .put("nombre", it.nombre)
                    .put("correo", it.correo)
                    .put("telefono", it.telefono)
                    .put("contrasena", it.contrasena)
            )
        }
        context.getSharedPreferences(PREFS_USUARIOS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LISTA, arr.toString()).apply()
    }

    fun registrar(context: Context, usuario: Usuario): Boolean {
        val usuarios = cargarUsuarios(context)
        val existe = usuarios.any { it.correo.equals(usuario.correo, ignoreCase = true) }
        if (existe) return false

        val conHash = usuario.copy(contrasena = hash(usuario.contrasena))
        usuarios.add(conHash)
        guardarUsuarios(context, usuarios)
        usuarioActual = conHash
        return true
    }

    fun iniciarSesion(context: Context, correo: String, contrasena: String): Boolean {
        val hashIngresado = hash(contrasena)
        val usuario = cargarUsuarios(context).find {
            it.correo.equals(correo.trim(), ignoreCase = true) && it.contrasena == hashIngresado
        }
        usuarioActual = usuario
        return usuario != null
    }

    fun cerrarSesion() {
        usuarioActual = null
    }
}

fun obtenerSesion(context: Context): String? {
    val prefs = context.getSharedPreferences("sesion_prefs", Context.MODE_PRIVATE)
    return prefs.getString("correo", null)
}

fun obtenerNombre(context: Context): String {
    val prefs = context.getSharedPreferences("sesion_prefs", Context.MODE_PRIVATE)
    return prefs.getString("nombre", "") ?: ""
}

fun guardarSesion(context: Context, correo: String, nombre: String) {
    val prefs = context.getSharedPreferences("sesion_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("correo", correo).putString("nombre", nombre).apply()
}

fun cerrarSesion(context: Context) {
    val prefs = context.getSharedPreferences("sesion_prefs", Context.MODE_PRIVATE)
    prefs.edit().clear().apply()
    Sesion.cerrarSesion()
}
