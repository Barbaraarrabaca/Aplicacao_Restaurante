package com.example.comidasaborosa.model

import com.google.gson.annotations.SerializedName

// Classe de modelo que representa um utilizador
data class User(
    // Mapeia o campo JSON "nome" para a propriedade 'name'
    @SerializedName("nome") val name: String,
    // Mapeia o campo JSON "email" para a propriedade 'email'
    @SerializedName("email") val email: String,
    // Mapeia o campo JSON "senha" para a propriedade 'password'
    @SerializedName("senha") val password: String
)