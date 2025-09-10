package pt.ipt.comidasaborosa.retrofit

import pt.ipt.comidasaborosa.retrofit.service.SheetyService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitHelper {
    // URL base da API
    private const val BASE_URL = "https://api.sheety.co/2e0d44beddc7cf3e39dc567d307645f2/dam/"

    // Configuração GSON personalizada
    private val gson: Gson = GsonBuilder().setLenient().create()

    // Interceptor de logging para debugging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Cliente HTTP personalizado com timeouts e logging
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Instância única do Retrofit (padrão singleton)
    private val retrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
    }

    // Método para obter a instância do Retrofit (compatibilidade)
    fun getInstance(): Retrofit {
        return retrofitInstance
    }

    // Acesso direto ao serviço Sheety
    fun sheetyService(): SheetyService {
        return retrofitInstance.create(SheetyService::class.java)
    }
}