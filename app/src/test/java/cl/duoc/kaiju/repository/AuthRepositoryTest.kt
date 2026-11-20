package cl.duoc.kaiju.repository

import cl.duoc.kaiju.data.remote.ApiService
import cl.duoc.kaiju.data.remote.LoginRequest
import cl.duoc.kaiju.data.remote.UsuarioResponse
import cl.duoc.kaiju.model.ResultadoLogin
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import retrofit2.Response

// Respuesta falsa del servidor, con el código HTTP indicado
private fun respuestaDeLogin(exitosa: Boolean, codigo: Int): Response<UsuarioResponse> {
    val respuesta = mockk<Response<UsuarioResponse>>()
    every { respuesta.isSuccessful } returns exitosa
    every { respuesta.code() } returns codigo
    return respuesta
}

class AuthRepositoryTest : StringSpec({

    "un login aceptado por el servidor devuelve CORRECTO" {
        val api = mockk<ApiService>()
        coEvery { api.iniciarSesion(any()) } returns respuestaDeLogin(true, 200)

        val resultado = AuthRepository(api).iniciarSesion("admin@kaiju.cl", "kaiju123")

        resultado shouldBe ResultadoLogin.CORRECTO
    }

    "un error 401 devuelve CREDENCIALES_INVALIDAS" {
        val api = mockk<ApiService>()
        coEvery { api.iniciarSesion(any()) } returns respuestaDeLogin(false, 401)

        val resultado = AuthRepository(api).iniciarSesion("admin@kaiju.cl", "otraclave")

        resultado shouldBe ResultadoLogin.CREDENCIALES_INVALIDAS
    }

    "otro error del servidor devuelve SIN_CONEXION" {
        val api = mockk<ApiService>()
        coEvery { api.iniciarSesion(any()) } returns respuestaDeLogin(false, 500)

        val resultado = AuthRepository(api).iniciarSesion("admin@kaiju.cl", "kaiju123")

        resultado shouldBe ResultadoLogin.SIN_CONEXION
    }

    "si no hay red devuelve SIN_CONEXION sin cerrar la app" {
        val api = mockk<ApiService>()
        coEvery { api.iniciarSesion(any()) } throws IOException("sin red")

        val resultado = AuthRepository(api).iniciarSesion("admin@kaiju.cl", "kaiju123")

        resultado shouldBe ResultadoLogin.SIN_CONEXION
    }

    "envía al servidor el correo y la contraseña tal como se escribieron" {
        val api = mockk<ApiService>()
        coEvery { api.iniciarSesion(any()) } returns respuestaDeLogin(true, 200)

        AuthRepository(api).iniciarSesion("admin@kaiju.cl", "kaiju123")

        coVerify(exactly = 1) { api.iniciarSesion(LoginRequest("admin@kaiju.cl", "kaiju123")) }
    }
})