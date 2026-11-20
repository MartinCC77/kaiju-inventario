package cl.duoc.kaiju.viewmodel

import cl.duoc.kaiju.data.remote.UsuarioActualizarRequest
import cl.duoc.kaiju.data.remote.UsuarioCrearRequest
import cl.duoc.kaiju.data.remote.UsuarioResponse
import cl.duoc.kaiju.model.ResultadoApi
import cl.duoc.kaiju.repository.UsuarioRepository
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

private val ana = UsuarioResponse(id = 1, nombre = "Ana", correo = "ana@kaiju.cl", rol = "USUARIO")

// Repository falso que ya tiene un usuario en la lista
private fun crearRepositorioUsuarios(): UsuarioRepository {
    val repository = mockk<UsuarioRepository>()
    coEvery { repository.listar() } returns ResultadoApi.Exito(listOf(ana))
    return repository
}

// Llena el formulario de usuario
private fun UsuarioViewModel.llenarFormulario(nombre: String, correo: String, clave: String) {
    onNombreChanged(nombre)
    onCorreoChanged(correo)
    onContrasenaChanged(clave)
}

@OptIn(ExperimentalCoroutinesApi::class)
class UsuarioViewModelTest : StringSpec({

    // viewModelScope usa el hilo principal de Android, que no existe en las pruebas
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    "al crearse carga la lista de usuarios desde el servidor" {
        val viewModel = UsuarioViewModel(crearRepositorioUsuarios())

        viewModel.state.value.usuarios shouldBe listOf(ana)
        viewModel.state.value.cargando shouldBe false
        viewModel.state.value.mensaje shouldBe null
    }

    "si la carga falla muestra el mensaje de error" {
        val repository = mockk<UsuarioRepository>()
        coEvery { repository.listar() } returns ResultadoApi.Error("No se pudo conectar con el servidor")

        val viewModel = UsuarioViewModel(repository)

        viewModel.state.value.mensaje shouldBe "No se pudo conectar con el servidor"
        viewModel.state.value.cargando shouldBe false
        viewModel.state.value.usuarios shouldBe emptyList<UsuarioResponse>()
    }

    "abrir el formulario nuevo lo deja vacío y en modo creación" {
        val viewModel = UsuarioViewModel(crearRepositorioUsuarios())

        viewModel.abrirFormularioNuevo()

        val estado = viewModel.state.value
        estado.formularioVisible shouldBe true
        estado.editandoId shouldBe null
        estado.nombre shouldBe ""
        estado.correo shouldBe ""
        estado.rol shouldBe "USUARIO"
    }

    "abrir el formulario de edición carga los datos del usuario sin la contraseña" {
        val viewModel = UsuarioViewModel(crearRepositorioUsuarios())

        viewModel.abrirFormularioEditar(ana)

        val estado = viewModel.state.value
        estado.formularioVisible shouldBe true
        estado.editandoId shouldBe 1L
        estado.nombre shouldBe "Ana"
        estado.correo shouldBe "ana@kaiju.cl"
        estado.rol shouldBe "USUARIO"
        estado.contrasena shouldBe ""
    }

    "cerrar el formulario lo oculta" {
        val viewModel = UsuarioViewModel(crearRepositorioUsuarios())
        viewModel.abrirFormularioNuevo()

        viewModel.cerrarFormulario()

        viewModel.state.value.formularioVisible shouldBe false
    }

    "cambiar el rol actualiza el formulario" {
        val viewModel = UsuarioViewModel(crearRepositorioUsuarios())

        viewModel.onRolChanged("ADMIN")

        viewModel.state.value.rol shouldBe "ADMIN"
    }

    "con el formulario vacío muestra los errores y no llama al servidor" {
        val repository = crearRepositorioUsuarios()
        val viewModel = UsuarioViewModel(repository)
        viewModel.abrirFormularioNuevo()

        viewModel.guardar()

        val errores = viewModel.state.value.errores
        errores.nombre shouldBe "El nombre es requerido"
        errores.correo shouldBe "El correo es requerido"
        errores.contrasena shouldBe "La contraseña es requerida"
        coVerify(exactly = 0) { repository.crear(any()) }
    }

    "un correo con formato incorrecto muestra el error" {
        val repository = crearRepositorioUsuarios()
        val viewModel = UsuarioViewModel(repository)
        viewModel.abrirFormularioNuevo()
        viewModel.llenarFormulario("Ana", "no-es-correo", "123456")

        viewModel.guardar()

        viewModel.state.value.errores.correo shouldBe "El correo no tiene un formato válido"
        coVerify(exactly = 0) { repository.crear(any()) }
    }

    "una contraseña de menos de 6 caracteres muestra el error" {
        val repository = crearRepositorioUsuarios()
        val viewModel = UsuarioViewModel(repository)
        viewModel.abrirFormularioNuevo()
        viewModel.llenarFormulario("Ana", "ana@kaiju.cl", "123")

        viewModel.guardar()

        viewModel.state.value.errores.contrasena shouldBe "Debe tener al menos 6 caracteres"
        coVerify(exactly = 0) { repository.crear(any()) }
    }

    "al escribir en un campo con error, el error de ese campo desaparece" {
        val viewModel = UsuarioViewModel(crearRepositorioUsuarios())
        viewModel.abrirFormularioNuevo()
        viewModel.guardar()
        viewModel.state.value.errores.nombre shouldBe "El nombre es requerido"

        viewModel.onNombreChanged("A")

        viewModel.state.value.errores.nombre shouldBe null
    }

    "crear un usuario válido lo envía sin espacios, cierra el formulario y recarga la lista" {
        val repository = crearRepositorioUsuarios()
        coEvery { repository.crear(any()) } returns ResultadoApi.Exito(ana)
        val viewModel = UsuarioViewModel(repository)
        viewModel.abrirFormularioNuevo()
        viewModel.llenarFormulario(" Ana ", " ana@kaiju.cl ", "123456")

        viewModel.guardar()

        coVerify(exactly = 1) {
            repository.crear(UsuarioCrearRequest("Ana", "ana@kaiju.cl", "123456", "USUARIO"))
        }
        viewModel.state.value.formularioVisible shouldBe false
        viewModel.state.value.guardando shouldBe false
        coVerify(exactly = 2) { repository.listar() } // una al crearse y otra después de guardar
    }

    "si el servidor rechaza el guardado, muestra su mensaje y deja el formulario abierto" {
        val repository = crearRepositorioUsuarios()
        coEvery { repository.crear(any()) } returns ResultadoApi.Error("Ya existe un usuario con ese correo")
        val viewModel = UsuarioViewModel(repository)
        viewModel.abrirFormularioNuevo()
        viewModel.llenarFormulario("Ana", "ana@kaiju.cl", "123456")

        viewModel.guardar()

        viewModel.state.value.errores.general shouldBe "Ya existe un usuario con ese correo"
        viewModel.state.value.formularioVisible shouldBe true
        viewModel.state.value.guardando shouldBe false
    }

    "editar sin escribir contraseña no la envía" {
        val repository = crearRepositorioUsuarios()
        coEvery { repository.actualizar(any(), any()) } returns ResultadoApi.Exito(ana)
        val viewModel = UsuarioViewModel(repository)
        viewModel.abrirFormularioEditar(ana)

        viewModel.guardar()

        coVerify(exactly = 1) {
            repository.actualizar(1L, UsuarioActualizarRequest("Ana", "ana@kaiju.cl", null, "USUARIO"))
        }
        viewModel.state.value.formularioVisible shouldBe false
    }

    "editar escribiendo una contraseña nueva la envía" {
        val repository = crearRepositorioUsuarios()
        coEvery { repository.actualizar(any(), any()) } returns ResultadoApi.Exito(ana)
        val viewModel = UsuarioViewModel(repository)
        viewModel.abrirFormularioEditar(ana)
        viewModel.onContrasenaChanged("nueva123")

        viewModel.guardar()

        coVerify(exactly = 1) {
            repository.actualizar(1L, UsuarioActualizarRequest("Ana", "ana@kaiju.cl", "nueva123", "USUARIO"))
        }
    }

    "pedir eliminar guarda el usuario y cancelar lo quita" {
        val viewModel = UsuarioViewModel(crearRepositorioUsuarios())

        viewModel.pedirEliminar(ana)
        viewModel.state.value.usuarioAEliminar shouldBe ana

        viewModel.cancelarEliminar()
        viewModel.state.value.usuarioAEliminar shouldBe null
    }

    "confirmar eliminar borra el usuario y recarga la lista" {
        val repository = crearRepositorioUsuarios()
        coEvery { repository.eliminar(1L) } returns ResultadoApi.Exito(Unit)
        val viewModel = UsuarioViewModel(repository)
        viewModel.pedirEliminar(ana)

        viewModel.confirmarEliminar()

        coVerify(exactly = 1) { repository.eliminar(1L) }
        viewModel.state.value.usuarioAEliminar shouldBe null
        coVerify(exactly = 2) { repository.listar() }
    }

    "si eliminar falla muestra el mensaje de error" {
        val repository = crearRepositorioUsuarios()
        coEvery { repository.eliminar(1L) } returns ResultadoApi.Error("Usuario no encontrado")
        val viewModel = UsuarioViewModel(repository)
        viewModel.pedirEliminar(ana)

        viewModel.confirmarEliminar()

        viewModel.state.value.mensaje shouldBe "Usuario no encontrado"
        viewModel.state.value.usuarioAEliminar shouldBe null
    }

    "confirmar eliminar sin un usuario elegido no llama al servidor" {
        val repository = crearRepositorioUsuarios()
        val viewModel = UsuarioViewModel(repository)

        viewModel.confirmarEliminar()

        coVerify(exactly = 0) { repository.eliminar(any()) }
    }
})