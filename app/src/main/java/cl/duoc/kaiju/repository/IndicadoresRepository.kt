package cl.duoc.kaiju.repository

import cl.duoc.kaiju.data.remote.IndicadoresResponse
import cl.duoc.kaiju.data.remote.IndicadoresService
import cl.duoc.kaiju.data.remote.RetrofitExterno
import cl.duoc.kaiju.model.ResultadoApi

// El servicio llega por el constructor ("enchufe"): en las pruebas se reemplaza por uno falso
class IndicadoresRepository(private val api: IndicadoresService = RetrofitExterno.api) {

    suspend fun obtener(): ResultadoApi<IndicadoresResponse> {
        return try {
            val respuesta = api.obtenerIndicadores()
            val cuerpo = respuesta.body()
            if (respuesta.isSuccessful && cuerpo != null) {
                ResultadoApi.Exito(cuerpo)
            } else {
                ResultadoApi.Error("El servicio de indicadores no respondió (${respuesta.code()})")
            }
        } catch (e: Exception) {
            ResultadoApi.Error("No se pudieron cargar los indicadores. Revisa tu conexión a internet")
        }
    }
}