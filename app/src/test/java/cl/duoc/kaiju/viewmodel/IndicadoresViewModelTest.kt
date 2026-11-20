package cl.duoc.kaiju.viewmodel

import cl.duoc.kaiju.data.remote.IndicadorDto
import cl.duoc.kaiju.data.remote.IndicadoresResponse
import cl.duoc.kaiju.model.ResultadoApi
import cl.duoc.kaiju.repository.IndicadoresRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

private val indicadoresDePrueba = IndicadoresResponse(
    dolar = IndicadorDto(valor = 950.5, fecha = "2026-11-20T03:00:00.000Z"),
    euro = IndicadorDto(valor = 1100.0),
    uf = IndicadorDto(valor = 39000.0),
    utm = IndicadorDto(valor = 68000.0)
)

@OptIn(ExperimentalCoroutinesApi::class)
class IndicadoresViewModelTest : StringSpec({

    // viewModelScope usa el hilo principal de Android, que no existe en las pruebas
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    "al crearse carga los indicadores y los deja en el estado" {
        val repository = mockk<IndicadoresRepository>()
        coEvery { repository.obtener() } returns ResultadoApi.Exito(indicadoresDePrueba)

        val viewModel = IndicadoresViewModel(repository)

        val estado = viewModel.state.value
        estado.cargando shouldBe false
        estado.dolar shouldBe 950.5
        estado.euro shouldBe 1100.0
        estado.uf shouldBe 39000.0
        estado.utm shouldBe 68000.0
        estado.fecha shouldBe "2026-11-20" // solo la parte de la fecha
        estado.mensaje shouldBe null
    }

    "si el servicio falla muestra el mensaje y no deja valores" {
        val repository = mockk<IndicadoresRepository>()
        coEvery { repository.obtener() } returns ResultadoApi.Error("No se pudieron cargar los indicadores")

        val viewModel = IndicadoresViewModel(repository)

        val estado = viewModel.state.value
        estado.cargando shouldBe false
        estado.mensaje shouldBe "No se pudieron cargar los indicadores"
        estado.dolar shouldBe null
    }

    "al volver a cargar después de un error muestra los valores y borra el mensaje" {
        val repository = mockk<IndicadoresRepository>()
        val respuestas = listOf<ResultadoApi<IndicadoresResponse>>(
            ResultadoApi.Error("Sin conexión"),
            ResultadoApi.Exito(indicadoresDePrueba)
        )
        coEvery { repository.obtener() } returnsMany respuestas
        val viewModel = IndicadoresViewModel(repository)
        viewModel.state.value.mensaje shouldBe "Sin conexión"

        viewModel.cargar()

        viewModel.state.value.mensaje shouldBe null
        viewModel.state.value.dolar shouldBe 950.5
    }
})