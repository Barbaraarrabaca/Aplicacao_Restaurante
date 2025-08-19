package com.example.comidasaborosa.retrofit.service

// Importa o modelo de dados MenuList
import com.example.comidasaborosa.model.MenuList
// Importa Call para operações assíncronas
import retrofit2.Call
// Importa a anotação GET do Retrofit
import retrofit2.http.GET

interface MenuService {
    // Define um endpoint GET
    @GET("2e0d44beddc7cf3e39dc567d307645f2/dam/menu")
    // Declara função que retorna chamada assíncrona contendo MenuList
    fun getMenu(): Call<MenuList>
}