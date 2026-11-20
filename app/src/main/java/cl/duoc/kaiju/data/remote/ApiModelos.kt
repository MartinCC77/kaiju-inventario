package cl.duoc.kaiju.data.remote

// Datos que la app envía y recibe del backend (los nombres coinciden con el JSON del servidor)

data class LoginRequest(val correo: String, val contrasena: String)

data class UsuarioResponse(
    val id: Long,
    val nombre: String,
    val correo: String,
    val rol: String
)

data class UsuarioCrearRequest(
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val rol: String
)

// contrasena y rol son opcionales: si van null no se envían y el servidor conserva los actuales
data class UsuarioActualizarRequest(
    val nombre: String,
    val correo: String,
    val contrasena: String? = null,
    val rol: String? = null
)

// Formato de los errores del servidor: {"mensaje": "..."}
data class ErrorApi(val mensaje: String)