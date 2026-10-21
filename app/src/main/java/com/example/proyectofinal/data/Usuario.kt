package com.example.proyectofinal.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.proyectofinal.data.model.Rol
import com.example.proyectofinal.data.model.UsuarioEntity
import com.example.proyectofinal.data.repository.BarberGoRepository
import java.security.MessageDigest

data class Usuario(
    val nombre: String,
    val correo: String,
    val telefono: String,
    val contrasena: String
)

/**
 * Registro e inicio de sesión contra la tabla `usuario` (a través de BarberGoRepository).
 * (La sesión activa —quién está logueado— se guarda aparte en SharedPreferences, abajo.)
 */
object Sesion {

    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set

    /** La contraseña no se guarda en texto plano: se guarda su hash SHA-256 (columna contrasena_hash). */
    private fun hash(texto: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(texto.toByteArray())
            .joinToString("") { "%02x".format(it) }

    /** Crea el usuario (rol CLIENTE) y deja la sesión iniciada. false si el correo ya existe. */
    suspend fun registrar(context: Context, usuario: Usuario): Boolean {
        val repo = BarberGoRepository.obtener(context)
        val correo = usuario.correo.trim()
        if (repo.buscarUsuarioPorCorreo(correo) != null) return false

        val id = repo.registrarUsuario(
            UsuarioEntity(
                nombre = usuario.nombre.trim(),
                correo = correo,
                telefono = usuario.telefono,
                contrasenaHash = hash(usuario.contrasena),
                rol = Rol.CLIENTE
            )
        ).toInt()

        usuarioActual = usuario.copy(correo = correo, contrasena = "")
        guardarSesion(context, correo, usuario.nombre.trim(), id, Rol.CLIENTE)
        return true
    }

    /** true si el correo existe, está ACTIVO y la contraseña coincide. */
    suspend fun iniciarSesion(context: Context, correo: String, contrasena: String): Boolean {
        val repo = BarberGoRepository.obtener(context)
        val correoLimpio = correo.trim()
        var u = repo.buscarUsuarioPorCorreo(correoLimpio)

        // Si es la cuenta admin de ejemplo y aún no existe en la base de datos, se crea dinámicamente
        if (u == null && correoLimpio.equals("admin@barbergo.com", ignoreCase = true) && contrasena == "admin123") {
            val idAdmin = repo.registrarUsuario(
                UsuarioEntity(
                    nombre = "Admin BarberGo",
                    correo = "admin@barbergo.com",
                    telefono = "3000000000",
                    contrasenaHash = hash("admin123"),
                    rol = Rol.ADMINISTRADOR,
                    estado = "ACTIVO"
                )
            ).toInt()
            u = repo.usuarioPorId(idAdmin)
        }

        val ok = u != null && u.estado == "ACTIVO" && u.contrasenaHash == hash(contrasena)
        if (u != null && ok) {
            usuarioActual = Usuario(u.nombre, u.correo, u.telefono ?: "", "")
            guardarSesion(context, u.correo, u.nombre, u.idUsuario, u.rol)
        } else {
            usuarioActual = null
        }
        return ok
    }

    fun cerrarSesion() {
        usuarioActual = null
    }
}

// ---------------------------------------------------------------------------
//  Sesión activa en SharedPreferences (archivo "sesion_prefs")
// ---------------------------------------------------------------------------

fun obtenerSesion(context: Context): String? {
    val prefs = context.getSharedPreferences("sesion_prefs", Context.MODE_PRIVATE)
    return prefs.getString("correo", null)
}

fun obtenerNombre(context: Context): String {
    val prefs = context.getSharedPreferences("sesion_prefs", Context.MODE_PRIVATE)
    return prefs.getString("nombre", "") ?: ""
}

/** id_usuario del cliente logueado (-1 si no hay sesión). Sirve para pedir sus citas a la BD. */
fun obtenerIdUsuario(context: Context): Int {
    val prefs = context.getSharedPreferences("sesion_prefs", Context.MODE_PRIVATE)
    return prefs.getInt("id_usuario", -1)
}

/** Rol del usuario logueado ("CLIENTE", "BARBERO", "ADMINISTRADOR"). Default "CLIENTE". */
fun obtenerRol(context: Context): String {
    val prefs = context.getSharedPreferences("sesion_prefs", Context.MODE_PRIVATE)
    return prefs.getString("rol", Rol.CLIENTE) ?: Rol.CLIENTE
}

/** Si no se pasa idUsuario o rol, se conservan los que ya estaban guardados. */
fun guardarSesion(
    context: Context,
    correo: String,
    nombre: String,
    idUsuario: Int = obtenerIdUsuario(context),
    rol: String = obtenerRol(context)
) {
    val prefs = context.getSharedPreferences("sesion_prefs", Context.MODE_PRIVATE)
    prefs.edit()
        .putString("correo", correo)
        .putString("nombre", nombre)
        .putInt("id_usuario", idUsuario)
        .putString("rol", rol)
        .apply()
}

fun cerrarSesion(context: Context) {
    val prefs = context.getSharedPreferences("sesion_prefs", Context.MODE_PRIVATE)
    prefs.edit().clear().apply()
    Sesion.cerrarSesion()
}
