package cl.duoc.kaiju.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.kaiju.model.LoginErrores
import cl.duoc.kaiju.model.LoginUiState
import cl.duoc.kaiju.model.ResultadoLogin
import cl.duoc.kaiju.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// El repository llega por el constructor ("enchufe"): así en las pruebas se puede usar uno falso
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onUsuarioChanged(valor: String) {
        _state.update {
            it.copy(usuario = valor, errores = it.errores.copy(usuario = null, credenciales = null))
        }
    }

    fun onClaveChanged(valor: String) {
        _state.update {
            it.copy(clave = valor, errores = it.errores.copy(clave = null, credenciales = null))
        }
    }

    fun iniciarSesion(onExito: () -> Unit) {
        val s = _state.value

        // 1. Validar que los campos tengan formato correcto
        val errores = LoginErrores(
            usuario = if (s.usuario.isBlank()) "El correo es requerido" else null,
            clave = when {
                s.clave.isBlank() -> "La contraseña es requerida"
                s.clave.length < 4 -> "Debe tener al menos 4 caracteres"
                else -> null
            }
        )
        if (errores.usuario != null || errores.clave != null) {
            _state.update { it.copy(errores = errores) }
            return
        }

        // 2. Preguntarle al repository si las credenciales son correctas
        viewModelScope.launch {
            when (authRepository.iniciarSesion(s.usuario.trim(), s.clave)) {
                ResultadoLogin.CORRECTO -> {
                    // 3. Todo bien: limpia el formulario y avisa
                    _state.value = LoginUiState()
                    onExito()
                }

                ResultadoLogin.CREDENCIALES_INVALIDAS -> _state.update {
                    it.copy(errores = LoginErrores(credenciales = "Usuario o contraseña incorrectos"))
                }

                ResultadoLogin.SIN_CONEXION -> _state.update {
                    it.copy(errores = LoginErrores(credenciales = "No se pudo conectar con el servidor"))
                }
            }
        }
    }

    // ---------- Fábrica: arma el ViewModel real ----------
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LoginViewModel(AuthRepository())
            }
        }
    }
}