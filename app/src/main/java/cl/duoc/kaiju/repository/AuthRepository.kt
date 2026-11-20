package cl.duoc.kaiju.repository

import cl.duoc.kaiju.data.remote.ApiService
import cl.duoc.kaiju.data.remote.LoginRequest
import cl.duoc.kaiju.data.remote.RetrofitInstance
import cl.duoc.kaiju.model.ResultadoLogin

// El ApiService llega por el constructor ("enchufe"): en las pruebas se reemplaza por uno falso
class AuthRepository(private val api: ApiService = RetrofitInstance.api) {

    suspend fun iniciarSesion(correo: String, clave: String): ResultadoLogin {
        return try {
            val respuesta = api.iniciarSesion(LoginRequest(correo, clave))
            when {
                respuesta.isSuccessful -> ResultadoLogin.CORRECTO
                respuesta.code() == 401 -> ResultadoLogin.CREDENCIALES_INVALIDAS
                else -> ResultadoLogin.SIN_CONEXION // el servidor respondió con otro error
            }
        } catch (e: Exception) {
            // Sin internet, servidor apagado o tiempo de espera agotado
            ResultadoLogin.SIN_CONEXION
        }
    }
}