package com.example.comidasaborosa.retrofit.service

import com.example.comidasaborosa.model.Menu
import retrofit2.Call
import retrofit2.http.GET

interface NoteService {
    @GET("API/getNotes.php")
    fun list(): Call<List<Menu>>
}