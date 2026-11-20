package cl.duoc.kaiju.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Cada función corresponde a un endpoint del backend usuarios-api
interface ApiService {

    @POST("api/auth/login")
    suspend fun iniciarSesion(@Body datos: LoginRequest): Response<UsuarioResponse>

    @GET("api/usuarios")
    suspend fun listarUsuarios(): Response<List<UsuarioResponse>>

    @POST("api/usuarios")
    suspend fun crearUsuario(@Body datos: UsuarioCrearRequest): Response<UsuarioResponse>

    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(
        @Path("id") id: Long,
        @Body datos: UsuarioActualizarRequest
    ): Response<UsuarioResponse>

    @DELETE("api/usuarios/{id}")
    suspend fun eliminarUsuario(@Path("id") id: Long): Response<Unit>
}