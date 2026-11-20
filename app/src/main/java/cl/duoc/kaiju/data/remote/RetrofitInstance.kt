package cl.duoc.kaiju.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Contiene la configuración de Retrofit (se crea una sola vez)
object RetrofitInstance {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // convierte JSON a objetos
            .build()
            .create(ApiService::class.java)
    }
}