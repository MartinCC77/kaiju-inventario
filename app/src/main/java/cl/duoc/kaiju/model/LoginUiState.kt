package cl.duoc.kaiju.model

data class LoginErrores(
    val usuario: String? = null,
    val clave: String? = null,
    val credenciales: String? = null // error general: usuario o clave incorrectos
)

data class LoginUiState(
    val usuario: String = "",
    val clave: String = "",
    val errores: LoginErrores = LoginErrores()
)