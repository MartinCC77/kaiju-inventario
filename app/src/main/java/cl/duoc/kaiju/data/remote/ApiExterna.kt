package cl.duoc.kaiju.data.remote

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// Endpoint de la API externa: GET https://mindicador.cl/api
interface IndicadoresService {

    @GET("api")
    suspend fun obtenerIndicadores(): Response<IndicadoresResponse>
}

// Retrofit propio para la API externa. Es independiente del que usa el backend de usuarios
object RetrofitExterno {

    private const val BASE_URL = "https://mindicador.cl/"

    val api: IndicadoresService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IndicadoresService::class.java)
    }
}