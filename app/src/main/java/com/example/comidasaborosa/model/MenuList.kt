package com.example.comidasaborosa.model
// Importa a anotação SerializedName da biblioteca GSON
import com.google.gson.annotations.SerializedName
// Classe de modelo para representar a lista de menus no formato esperado pela API
data class MenuList(
    // Mapeia o campo JSON "menu" para a propriedade 'dam'
    // 'dam' contém uma lista de objetos Menu
    @SerializedName("menu") val dam: List<Menu>
)
// Classe de modelo que representa um item individual do menu
data class Menu(
    // Mapeia o campo JSON "id"
    @SerializedName("id") val id: Int?,
    // Mapeia o campo JSON "nome"
    @SerializedName("nome") val nome: String?,
    // Mapeia o campo JSON "descricao"
    @SerializedName("descricao") val descricao: String?,
    // Mapeia o campo JSON "preco"
    @SerializedName("preco") val preco: Double?,
    // Mapeia o campo JSON "imagem" (URL da imagem)
    @SerializedName("imagem") val imagem: String?,
    // Mapeia o campo JSON "categoria"
    @SerializedName("categoria") val categoria: String?
)