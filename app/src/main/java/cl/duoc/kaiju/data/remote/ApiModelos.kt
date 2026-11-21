package cl.duoc.kaiju.data.remote

import androidx.annotation.Keep

// Datos que la app envía y recibe del backend (los nombres coinciden con el JSON del servidor)
// @Keep evita que el optimizador (R8) cambie los nombres de los campos en el APK de release

@Keep
data class LoginRequest(val correo: String, val contrasena: String)

@Keep
data class UsuarioResponse(
    val id: Long,
    val nombre: String,
    val correo: String,
    val rol: String
)

@Keep
data class UsuarioCrearRequest(
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val rol: String
)

// contrasena y rol son opcionales: si van null no se envían y el servidor conserva los actuales
@Keep
data class UsuarioActualizarRequest(
    val nombre: String,
    val correo: String,
    val contrasena: String? = null,
    val rol: String? = null
)

// Formato de los errores del servidor: {"mensaje": "..."}
@Keep
data class ErrorApi(val mensaje: String)