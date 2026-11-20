package cl.duoc.kaiju.repository

import cl.duoc.kaiju.data.remote.IndicadorDto
import cl.duoc.kaiju.data.remote.IndicadoresResponse
import cl.duoc.kaiju.data.remote.IndicadoresService
import cl.duoc.kaiju.model.ResultadoApi
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import retrofit2.Response

class IndicadoresRepositoryTest : StringSpec({

    "devuelve los indicadores cuando el servicio responde bien" {
        val servicio = mockk<IndicadoresService>()
        val datos = IndicadoresResponse(dolar = IndicadorDto(valor = 950.5))
        coEvery { servicio.obtenerIndicadores() } returns Response.success(datos)

        val resultado = IndicadoresRepository(servicio).obtener()

        resultado shouldBe ResultadoApi.Exito(datos)
    }

    "si el servicio responde con error muestra el código" {
        val servicio = mockk<IndicadoresService>()
        val respuesta = mockk<Response<IndicadoresResponse>>()
        every { respuesta.isSuccessful } returns false
        every { respuesta.body() } returns null
        every { respuesta.code() } returns 503
        coEvery { servicio.obtenerIndicadores() } returns respuesta

        val resultado = IndicadoresRepository(servicio).obtener()

        resultado shouldBe ResultadoApi.Error("El servicio de indicadores no respondió (503)")
    }

    "si el servicio responde sin datos devuelve un error" {
        val servicio = mockk<IndicadoresService>()
        coEvery { servicio.obtenerIndicadores() } returns Response.success<IndicadoresResponse>(null)

        val resultado = IndicadoresRepository(servicio).obtener()

        resultado shouldBe ResultadoApi.Error("El servicio de indicadores no respondió (200)")
    }

    "si no hay internet devuelve el mensaje de conexión" {
        val servicio = mockk<IndicadoresService>()
        coEvery { servicio.obtenerIndicadores() } throws IOException("sin red")

        val resultado = IndicadoresRepository(servicio).obtener()

        resultado shouldBe
                ResultadoApi.Error("No se pudieron cargar los indicadores. Revisa tu conexión a internet")
    }
})