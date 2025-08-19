package com.example.comidasaborosa.retrofit

// Importa a classe principal do Retrofit
import retrofit2.Retrofit
// Importa o conversor GSON para serialização
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {
    // URL base da API
    private const val BASE_URL = "https://api.sheety.co/2e0d44beddc7cf3e39dc567d307645f2/dam/"

    // Método público para obter uma instância configurada do Retrofit
    fun getInstance(): Retrofit {
        return Retrofit.Builder()// Inicia o construtor do Retrofit
            .baseUrl(BASE_URL) // Define a URL base das requisições
            .addConverterFactory(GsonConverterFactory.create()) // Adiciona conversor GSON
            .build() // Constrói o objeto Retrofit final
    }
}
