package cl.duoc.kaiju.repository

import cl.duoc.kaiju.model.ResultadoLogin

class AuthRepository {

    // Credenciales de prueba (solo para el MVP académico).
    // Cuando exista el backend, aquí se llamará a la API con Retrofit.
    private val usuarioDemo = "admin"
    private val claveDemo = "kaiju123"

    suspend fun iniciarSesion(usuario: String, clave: String): ResultadoLogin {
        return if (usuario == usuarioDemo && clave == claveDemo) {
            ResultadoLogin.CORRECTO
        } else {
            ResultadoLogin.CREDENCIALES_INVALIDAS
        }
    }
}