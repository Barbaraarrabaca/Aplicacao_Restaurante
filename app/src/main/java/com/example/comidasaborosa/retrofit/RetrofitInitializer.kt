package com.example.comidasaborosa.retrofit

import com.example.comidasaborosa.retrofit.service.MenuService
import com.example.comidasaborosa.retrofit.service.SheetyService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitInitializer {

    private val gson: Gson = GsonBuilder().setLenient().create()

    // Adicionar logging interceptor
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Para a API do menu
    private val retrofitMenu = Retrofit.Builder()
        .baseUrl("http://ram.ipt.pt/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()

    // Para a API do Sheety
    private val retrofitSheety = Retrofit.Builder()
        .baseUrl("https://api.sheety.co/2e0d44beddc7cf3e39dc567d307645f2/dam/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()

    fun sheetyService(): SheetyService = retrofitSheety.create(SheetyService::class.java)
    fun menuService(): MenuService = retrofitSheety.create(MenuService::class.java)
}
