package cl.duoc.kaiju.model

// Los tres resultados posibles al intentar iniciar sesión
enum class ResultadoLogin {
    CORRECTO,
    CREDENCIALES_INVALIDAS,
    SIN_CONEXION
}