package cl.duoc.kaiju.repository

import cl.duoc.kaiju.data.remote.ApiService
import cl.duoc.kaiju.data.remote.UsuarioActualizarRequest
import cl.duoc.kaiju.data.remote.UsuarioCrearRequest
import cl.duoc.kaiju.data.remote.UsuarioResponse
import cl.duoc.kaiju.model.ResultadoApi
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import okhttp3.ResponseBody
import retrofit2.Response

private val usuarioDePrueba = UsuarioResponse(id = 1, nombre = "Ana", correo = "ana@kaiju.cl", rol = "USUARIO")

// Respuesta de error falsa. El cuerpo es el texto JSON que enviaría el servidor (o null si no hay)
private fun respuestaConError(codigo: Int, cuerpo: String?): Response<UsuarioResponse> {
    val respuesta = mockk<Response<UsuarioResponse>>()
    every { respuesta.isSuccessful } returns false
    every { respuesta.code() } returns codigo
    if (cuerpo == null) {
        every { respuesta.errorBody() } returns null
    } else {
        val cuerpoFalso = mockk<ResponseBody>()
        every { cuerpoFalso.string() } returns cuerpo
        every { respuesta.errorBody() } returns cuerpoFalso
    }
    return respuesta
}

class UsuarioRepositoryTest : StringSpec({

    "listar devuelve los usuarios que entrega el servidor" {
        val api = mockk<ApiService>()
        coEvery { api.listarUsuarios() } returns Response.success(listOf(usuarioDePrueba))

        val resultado = UsuarioRepository(api).listar()

        resultado shouldBe ResultadoApi.Exito(listOf(usuarioDePrueba))
    }

    "crear devuelve el usuario creado" {
        val api = mockk<ApiService>()
        coEvery { api.crearUsuario(any()) } returns Response.success(usuarioDePrueba)

        val resultado = UsuarioRepository(api)
            .crear(UsuarioCrearRequest("Ana", "ana@kaiju.cl", "123456", "USUARIO"))

        resultado shouldBe ResultadoApi.Exito(usuarioDePrueba)
    }

    "actualizar devuelve el usuario modificado" {
        val api = mockk<ApiService>()
        coEvery { api.actualizarUsuario(any(), any()) } returns Response.success(usuarioDePrueba)

        val resultado = UsuarioRepository(api)
            .actualizar(1L, UsuarioActualizarRequest("Ana", "ana@kaiju.cl"))

        resultado shouldBe ResultadoApi.Exito(usuarioDePrueba)
    }

    "eliminar devuelve éxito cuando el servidor responde sin contenido" {
        val api = mockk<ApiService>()
        coEvery { api.eliminarUsuario(1L) } returns Response.success(Unit)

        val resultado = UsuarioRepository(api).eliminar(1L)

        resultado shouldBe ResultadoApi.Exito(Unit)
    }

    "un error del servidor muestra el mensaje que el servidor envió" {
        val api = mockk<ApiService>()
        coEvery { api.crearUsuario(any()) } returns
                respuestaConError(409, """{"mensaje":"Ya existe un usuario con ese correo"}""")

        val resultado = UsuarioRepository(api)
            .crear(UsuarioCrearRequest("Ana", "ana@kaiju.cl", "123456", "USUARIO"))

        resultado shouldBe ResultadoApi.Error("Ya existe un usuario con ese correo")
    }

    "un error sin cuerpo muestra el código del error" {
        val api = mockk<ApiService>()
        coEvery { api.listarUsuarios() } returns
                Response.success(emptyList<UsuarioResponse>()).let { respuestaConErrorLista(500) }

        val resultado = UsuarioRepository(api).listar()

        resultado shouldBe ResultadoApi.Error("Error del servidor (500)")
    }

    "un error con un cuerpo que no es JSON muestra el código del error" {
        val api = mockk<ApiService>()
        coEvery { api.crearUsuario(any()) } returns respuestaConError(502, "<html>error</html>")

        val resultado = UsuarioRepository(api)
            .crear(UsuarioCrearRequest("Ana", "ana@kaiju.cl", "123456", "USUARIO"))

        resultado shouldBe ResultadoApi.Error("Error del servidor (502)")
    }

    "si no hay red muestra el mensaje de sin conexión" {
        val api = mockk<ApiService>()
        coEvery { api.listarUsuarios() } throws IOException("sin red")

        val resultado = UsuarioRepository(api).listar()

        resultado shouldBe ResultadoApi.Error("No se pudo conectar con el servidor")
    }
})

// Respuesta de error falsa para la lista de usuarios (sin cuerpo)
private fun respuestaConErrorLista(codigo: Int): Response<List<UsuarioResponse>> {
    val respuesta = mockk<Response<List<UsuarioResponse>>>()
    every { respuesta.isSuccessful } returns false
    every { respuesta.code() } returns codigo
    every { respuesta.errorBody() } returns null
    return respuesta
}