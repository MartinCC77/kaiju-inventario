package cl.duoc.kaiju.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.kaiju.data.remote.UsuarioActualizarRequest
import cl.duoc.kaiju.data.remote.UsuarioCrearRequest
import cl.duoc.kaiju.data.remote.UsuarioResponse
import cl.duoc.kaiju.model.ResultadoApi
import cl.duoc.kaiju.model.UsuarioErrores
import cl.duoc.kaiju.model.UsuarioUiState
import cl.duoc.kaiju.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// El repository llega por el constructor ("enchufe"): así en las pruebas se puede usar uno falso
class UsuarioViewModel(
    private val repository: UsuarioRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UsuarioUiState())
    val state: StateFlow<UsuarioUiState> = _state.asStateFlow()

    // Al abrir la pantalla se carga la lista desde el servidor
    init {
        cargarUsuarios()
    }

    // ---------- LEER ----------
    fun cargarUsuarios() {
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, mensaje = null) }
            when (val resultado = repository.listar()) {
                is ResultadoApi.Exito -> _state.update {
                    it.copy(usuarios = resultado.datos, cargando = false)
                }

                is ResultadoApi.Error -> _state.update {
                    it.copy(cargando = false, mensaje = resultado.mensaje)
                }
            }
        }
    }

    // ---------- Formulario: abrir y cerrar ----------
    fun abrirFormularioNuevo() {
        _state.update {
            it.copy(
                formularioVisible = true, editandoId = null,
                nombre = "", correo = "", contrasena = "", rol = "USUARIO",
                errores = UsuarioErrores()
            )
        }
    }

    fun abrirFormularioEditar(usuario: UsuarioResponse) {
        _state.update {
            it.copy(
                formularioVisible = true, editandoId = usuario.id,
                nombre = usuario.nombre, correo = usuario.correo,
                contrasena = "", rol = usuario.rol, // la contraseña actual no se puede ver
                errores = UsuarioErrores()
            )
        }
    }

    fun cerrarFormulario() {
        _state.update { it.copy(formularioVisible = false, errores = UsuarioErrores()) }
    }

    // ---------- Formulario: eventos al escribir ----------
    fun onNombreChanged(valor: String) {
        _state.update {
            it.copy(nombre = valor, errores = it.errores.copy(nombre = null, general = null))
        }
    }

    fun onCorreoChanged(valor: String) {
        _state.update {
            it.copy(correo = valor, errores = it.errores.copy(correo = null, general = null))
        }
    }

    fun onContrasenaChanged(valor: String) {
        _state.update {
            it.copy(contrasena = valor, errores = it.errores.copy(contrasena = null, general = null))
        }
    }

    fun onRolChanged(valor: String) {
        _state.update { it.copy(rol = valor) }
    }

    // ---------- Validación ----------
    private fun validar(): Boolean {
        val s = _state.value
        val creando = s.editandoId == null

        val errores = UsuarioErrores(
            nombre = if (s.nombre.isBlank()) "El nombre es requerido" else null,
            correo = when {
                s.correo.isBlank() -> "El correo es requerido"
                !CORREO_VALIDO.matches(s.correo.trim()) -> "El correo no tiene un formato válido"
                else -> null
            },
            // Al crear la contraseña es obligatoria; al editar es opcional
            contrasena = when {
                creando && s.contrasena.isBlank() -> "La contraseña es requerida"
                s.contrasena.isNotBlank() && s.contrasena.length < 6 -> "Debe tener al menos 6 caracteres"
                else -> null
            }
        )

        _state.update { it.copy(errores = errores) }
        return errores == UsuarioErrores()
    }

    // ---------- CREAR y ACTUALIZAR ----------
    fun guardar() {
        if (!validar()) return
        val s = _state.value

        viewModelScope.launch {
            _state.update { it.copy(guardando = true) }

            val resultado = if (s.editandoId == null) {
                repository.crear(
                    UsuarioCrearRequest(s.nombre.trim(), s.correo.trim(), s.contrasena, s.rol)
                )
            } else {
                repository.actualizar(
                    s.editandoId,
                    UsuarioActualizarRequest(
                        nombre = s.nombre.trim(),
                        correo = s.correo.trim(),
                        contrasena = s.contrasena.ifBlank { null }, // vacía = no cambia
                        rol = s.rol
                    )
                )
            }

            when (resultado) {
                is ResultadoApi.Exito -> {
                    _state.update { it.copy(guardando = false, formularioVisible = false) }
                    cargarUsuarios()
                }

                is ResultadoApi.Error -> _state.update {
                    it.copy(guardando = false, errores = it.errores.copy(general = resultado.mensaje))
                }
            }
        }
    }

    // ---------- ELIMINAR (con confirmación) ----------
    fun pedirEliminar(usuario: UsuarioResponse) {
        _state.update { it.copy(usuarioAEliminar = usuario) }
    }

    fun cancelarEliminar() {
        _state.update { it.copy(usuarioAEliminar = null) }
    }

    fun confirmarEliminar() {
        val usuario = _state.value.usuarioAEliminar ?: return

        viewModelScope.launch {
            when (val resultado = repository.eliminar(usuario.id)) {
                is ResultadoApi.Exito -> {
                    _state.update { it.copy(usuarioAEliminar = null) }
                    cargarUsuarios()
                }

                is ResultadoApi.Error -> _state.update {
                    it.copy(usuarioAEliminar = null, mensaje = resultado.mensaje)
                }
            }
        }
    }

    // ---------- Fábrica: arma el ViewModel real ----------
    companion object {
        private val CORREO_VALIDO = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                UsuarioViewModel(UsuarioRepository())
            }
        }
    }
}