package cl.duoc.kaiju.data.remote

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

// Un indicador de mindicador.cl (por ejemplo, el dólar). Todo es opcional por si el servicio omite algo
@Keep
data class IndicadorDto(
    val codigo: String? = null,
    val nombre: String? = null,
    @SerializedName("unidad_medida") val unidadMedida: String? = null,
    val fecha: String? = null,
    val valor: Double? = null
)

// Respuesta de https://mindicador.cl/api. Solo leemos los indicadores que usa la app
@Keep
data class IndicadoresResponse(
    val uf: IndicadorDto? = null,
    val dolar: IndicadorDto? = null,
    val euro: IndicadorDto? = null,
    val utm: IndicadorDto? = null
)