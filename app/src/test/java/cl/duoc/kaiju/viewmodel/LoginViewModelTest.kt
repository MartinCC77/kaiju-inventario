package cl.duoc.kaiju.viewmodel

import cl.duoc.kaiju.model.LoginUiState
import cl.duoc.kaiju.model.ResultadoLogin
import cl.duoc.kaiju.repository.AuthRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

// Escribe el correo y la contraseña en el formulario
private fun LoginViewModel.escribirCredenciales(correo: String, clave: String) {
    onUsuarioChanged(correo)
    onClaveChanged(clave)
}

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest : StringSpec({

    // viewModelScope usa el hilo principal de Android, que no existe en las pruebas
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    "con los campos vacíos muestra los errores y no consulta al servidor" {
        val repository = mockk<AuthRepository>()
        val viewModel = LoginViewModel(repository)
        var entro = false

        viewModel.iniciarSesion { entro = true }

        val errores = viewModel.state.value.errores
        errores.usuario shouldBe "El correo es requerido"
        errores.clave shouldBe "La contraseña es requerida"
        entro shouldBe false
        coVerify(exactly = 0) { repository.iniciarSesion(any(), any()) }
    }

    "una contraseña de menos de 4 caracteres muestra el error de largo" {
        val repository = mockk<AuthRepository>()
        val viewModel = LoginViewModel(repository)
        viewModel.escribirCredenciales("admin@kaiju.cl", "abc")

        viewModel.iniciarSesion { }

        viewModel.state.value.errores.clave shouldBe "Debe tener al menos 4 caracteres"
        coVerify(exactly = 0) { repository.iniciarSesion(any(), any()) }
    }

    "con credenciales correctas avisa el éxito y limpia el formulario" {
        val repository = mockk<AuthRepository>()
        coEvery { repository.iniciarSesion("admin@kaiju.cl", "kaiju123") } returns ResultadoLogin.CORRECTO
        val viewModel = LoginViewModel(repository)
        viewModel.escribirCredenciales("admin@kaiju.cl", "kaiju123")
        var entro = false

        viewModel.iniciarSesion { entro = true }

        entro shouldBe true
        viewModel.state.value shouldBe LoginUiState()
    }

    "con credenciales incorrectas muestra el error general y no entra" {
        val repository = mockk<AuthRepository>()
        coEvery { repository.iniciarSesion(any(), any()) } returns ResultadoLogin.CREDENCIALES_INVALIDAS
        val viewModel = LoginViewModel(repository)
        viewModel.escribirCredenciales("admin@kaiju.cl", "otraclave")
        var entro = false

        viewModel.iniciarSesion { entro = true }

        viewModel.state.value.errores.credenciales shouldBe "Usuario o contraseña incorrectos"
        entro shouldBe false
    }

    "si no hay conexión con el servidor muestra el aviso y no entra" {
        val repository = mockk<AuthRepository>()
        coEvery { repository.iniciarSesion(any(), any()) } returns ResultadoLogin.SIN_CONEXION
        val viewModel = LoginViewModel(repository)
        viewModel.escribirCredenciales("admin@kaiju.cl", "kaiju123")
        var entro = false

        viewModel.iniciarSesion { entro = true }

        viewModel.state.value.errores.credenciales shouldBe "No se pudo conectar con el servidor"
        entro shouldBe false
    }

    "envía el correo sin espacios sobrantes" {
        val repository = mockk<AuthRepository>()
        coEvery { repository.iniciarSesion(any(), any()) } returns ResultadoLogin.CORRECTO
        val viewModel = LoginViewModel(repository)
        viewModel.escribirCredenciales("  admin@kaiju.cl  ", "kaiju123")

        viewModel.iniciarSesion { }

        coVerify(exactly = 1) { repository.iniciarSesion("admin@kaiju.cl", "kaiju123") }
    }

    "al escribir de nuevo desaparece el error general" {
        val repository = mockk<AuthRepository>()
        coEvery { repository.iniciarSesion(any(), any()) } returns ResultadoLogin.CREDENCIALES_INVALIDAS
        val viewModel = LoginViewModel(repository)
        viewModel.escribirCredenciales("admin@kaiju.cl", "otraclave")
        viewModel.iniciarSesion { }
        viewModel.state.value.errores.credenciales shouldBe "Usuario o contraseña incorrectos"

        viewModel.onClaveChanged("kaiju123")

        viewModel.state.value.errores.credenciales shouldBe null
    }

    "al escribir en el correo desaparece su error" {
        val repository = mockk<AuthRepository>()
        val viewModel = LoginViewModel(repository)
        viewModel.iniciarSesion { }
        viewModel.state.value.errores.usuario shouldBe "El correo es requerido"

        viewModel.onUsuarioChanged("a")

        viewModel.state.value.errores.usuario shouldBe null
    }
})