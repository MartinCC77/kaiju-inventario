package cl.duoc.kaiju.repository

import cl.duoc.kaiju.data.remote.ApiService
import cl.duoc.kaiju.data.remote.ErrorApi
import cl.duoc.kaiju.data.remote.RetrofitInstance
import cl.duoc.kaiju.data.remote.UsuarioActualizarRequest
import cl.duoc.kaiju.data.remote.UsuarioCrearRequest
import cl.duoc.kaiju.data.remote.UsuarioResponse
import cl.duoc.kaiju.model.ResultadoApi
import com.google.gson.Gson
import retrofit2.Response

// El ApiService llega por el constructor ("enchufe"): en las pruebas se reemplaza por uno falso
class UsuarioRepository(private val api: ApiService = RetrofitInstance.api) {

    suspend fun listar(): ResultadoApi<List<UsuarioResponse>> = llamar { api.listarUsuarios() }

    suspend fun crear(datos: UsuarioCrearRequest): ResultadoApi<UsuarioResponse> =
        llamar { api.crearUsuario(datos) }

    suspend fun actualizar(id: Long, datos: UsuarioActualizarRequest): ResultadoApi<UsuarioResponse> =
        llamar { api.actualizarUsuario(id, datos) }

    suspend fun eliminar(id: Long): ResultadoApi<Unit> = llamar { api.eliminarUsuario(id) }

    // Ejecuta una petición y la convierte en Exito o Error
    @Suppress("UNCHECKED_CAST")
    private suspend fun <T> llamar(peticion: suspend () -> Response<T>): ResultadoApi<T> {
        return try {
            val respuesta = peticion()
            if (respuesta.isSuccessful) {
                ResultadoApi.Exito(respuesta.body() as T)
            } else {
                ResultadoApi.Error(leerMensaje(respuesta))
            }
        } catch (e: Exception) {
            // Sin internet, servidor apagado o tiempo de espera agotado
            ResultadoApi.Error("No se pudo conectar con el servidor")
        }
    }

    // El servidor responde los errores como {"mensaje": "..."}
    private fun leerMensaje(respuesta: Response<*>): String {
        return try {
            val cuerpo = respuesta.errorBody()?.string()
            Gson().fromJson(cuerpo, ErrorApi::class.java)?.mensaje
                ?: "Error del servidor (${respuesta.code()})"
        } catch (e: Exception) {
            "Error del servidor (${respuesta.code()})"
        }
    }
}