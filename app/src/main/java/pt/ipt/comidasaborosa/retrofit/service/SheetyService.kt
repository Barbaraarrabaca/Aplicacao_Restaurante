package pt.ipt.comidasaborosa.retrofit.service

import pt.ipt.comidasaborosa.model.Menu
import retrofit2.Call
import retrofit2.http.*
// Define uma classe de resposta para dados do menu
data class SheetyResponse(
    val menu: List<Menu> // Lista de itens do menu
)

// Classe para criação de utilizador (sem ID)
data class UserCreateRequest(
    val nome: String, // Nome do novo utilizador
    val email: String, // Email do novo utilizador
    val senha: String // Senha do novo utilizador

)

// Request para criar utilizador (sem ID)
data class UserCreateWrapper(
    val utilizador: UserCreateRequest // Contém dados do novo utilizador
)
// Define uma classe de resposta para dados de utilizador
data class UserResponse(
    val id: Int,
    val nome: String,
    val email: String,
    val senha: String
)

// Resposta após criação de utilizador
data class SheetyUserCreatedResponse(
    val utilizador: UserResponse // Dados do utilizador criado
)

// Resposta da API para operações de login
data class SheetyLoginResponse(
    val utilizador: List<UserResponse> // Lista de utilizadores
)

// Representa um item no carrinho de compras
data class CarrinhoItem(
    val id: Int = 0, // ID do item
    val userId: Int, // ID do utilizador dono do carrinho
    val itemId: Int,  // ID do item do menu
    val nome: String,
    val preco: Double,
    val quantidade: Int = 1
)
// Resposta para operações de carrinho
data class SheetyCarrinhoResponse(
    val carrinho: CarrinhoItem
)
// Resposta para listagem de carrinho
data class SheetyCarrinhoListResponse(
    val carrinho: List<CarrinhoItem>
)
// Versão para criação de itens no carrinho (sem ID)
data class CarrinhoCreateItem(
    val userId: Int,
    val itemId: Int,
    val nome: String,
    val preco: Double,
    val quantidade: Int = 1
)
// Adicionar item ao carrinho
data class SheetyCarrinhoRequest(
    val carrinho: CarrinhoCreateItem // Item a ser criado
)
// Dados para atualização de item no carrinho
data class CarrinhoUpdateItem(
    val quantidade: Int // Nova quantidade
)

//  Atualizar item no carrinho
data class SheetyCarrinhoUpdateRequest(
    val carrinho: CarrinhoUpdateItem  // Atualização de quantidade
)

// Classes para Pedidos
// Criação de pedidos
data class PedidoCreate(
    val userId: Int,
    val dataHora: String,
    val total: Double,
    val status: String = "pendente",
    val itens: String
)

data class PedidoCreateRequest(
    val pedido: PedidoCreate
)

// Representa um pedido finalizado
data class Pedido(
    val id: Int = 0,
    val userId: Int,
    val dataHora: String,
    val total: Double,
    val status: String = "pendente",
    val itens: String
)

data class SheetyPedidoResponse(
    val pedido: Pedido
)

// Interface que define os endpoints da API
interface SheetyService {
    // Endpoints do Menu
    // Obtém todos os itens do menu
    @GET("menu")
    fun getAllMenuItems(): Call<SheetyResponse>

    // Endpoints do Utilizador
    // Obtém todos os utilizadores
    @GET("utilizador")
    fun getAllUsers(): Call<SheetyLoginResponse>
    // Cria um novo utilizador
    @POST("utilizador")
    fun createUser(@Body user: UserCreateWrapper): Call<SheetyUserCreatedResponse>

    // Endpoints do Carrinho
    // Obtém todos os itens do carrinho
    @GET("carrinho")
    fun getAllCarrinhoItems(): Call<SheetyCarrinhoListResponse>
    // Adiciona item ao carrinho
    @POST("carrinho")
    fun addItemToCarrinho(@Body request: SheetyCarrinhoRequest): Call<SheetyCarrinhoResponse>
    // Atualiza item no carrinho
    @PUT("carrinho/{id}")
    fun updateCarrinhoItem(
        @Path("id") id: Int,
        @Body request: SheetyCarrinhoUpdateRequest
    ): Call<SheetyCarrinhoResponse>
    // Remove item do carrinho
    @DELETE("carrinho/{id}")
    fun removeItemFromCarrinho(@Path("id") id: Int): Call<Void>

    // Endpoints dos Pedidos
    // Cria um novo pedido
    @POST("pedidos")
    fun criarPedido(@Body request: PedidoCreateRequest): Call<SheetyPedidoResponse>
}

