package cl.duoc.kaiju.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.kaiju.model.IndicadoresUiState
import cl.duoc.kaiju.model.ResultadoApi
import cl.duoc.kaiju.repository.IndicadoresRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// El repository llega por el constructor ("enchufe"): así en las pruebas se puede usar uno falso
class IndicadoresViewModel(
    private val repository: IndicadoresRepository
) : ViewModel() {

    private val _state = MutableStateFlow(IndicadoresUiState())
    val state: StateFlow<IndicadoresUiState> = _state.asStateFlow()

    // Al crearse, carga los indicadores
    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, mensaje = null) }

            when (val resultado = repository.obtener()) {
                is ResultadoApi.Exito -> {
                    val datos = resultado.datos
                    _state.update {
                        it.copy(
                            cargando = false,
                            dolar = datos.dolar?.valor,
                            euro = datos.euro?.valor,
                            uf = datos.uf?.valor,
                            utm = datos.utm?.valor,
                            fecha = datos.dolar?.fecha?.take(10) // "2026-11-20T03:00..." -> "2026-11-20"
                        )
                    }
                }

                is ResultadoApi.Error -> _state.update {
                    it.copy(cargando = false, mensaje = resultado.mensaje)
                }
            }
        }
    }

    // ---------- Fábrica: arma el ViewModel real ----------
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                IndicadoresViewModel(IndicadoresRepository())
            }
        }
    }
}