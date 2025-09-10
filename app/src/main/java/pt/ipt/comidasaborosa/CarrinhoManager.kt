package pt.ipt.comidasaborosa

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pt.ipt.comidasaborosa.model.Menu
import pt.ipt.comidasaborosa.retrofit.RetrofitHelper
import pt.ipt.comidasaborosa.retrofit.service.CarrinhoCreateItem
import pt.ipt.comidasaborosa.retrofit.service.CarrinhoUpdateItem
import pt.ipt.comidasaborosa.retrofit.service.PedidoCreate
import pt.ipt.comidasaborosa.retrofit.service.PedidoCreateRequest
import pt.ipt.comidasaborosa.retrofit.service.SheetyCarrinhoListResponse
import pt.ipt.comidasaborosa.retrofit.service.SheetyCarrinhoRequest
import pt.ipt.comidasaborosa.retrofit.service.SheetyCarrinhoResponse
import pt.ipt.comidasaborosa.retrofit.service.SheetyCarrinhoUpdateRequest
import pt.ipt.comidasaborosa.retrofit.service.SheetyPedidoResponse
import pt.ipt.comidasaborosa.retrofit.service.SheetyService
import pt.ipt.comidasaborosa.util.UserPreferences
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

// Objeto que gere o carrinho de compras globalmente
object CarrinhoManager {

    // ==================== DATA CLASSES ====================

    // Estrutura de dados para itens do carrinho (com ID, item do menu e quantidade
    data class CarrinhoItemWithId(
        val id: Int, // ID único do item no carrinho
        val menu: Menu,   // Item do menu associado
        var quantidade: Int = 1 // Quantidade (valor mutável)
    )

    // Estrutura para itens de pedido (serialização)
    data class ItemPedido(
        val itemId: Int, // ID do item do menu
        val nome: String, // Nome do item
        val preco: Double, // Preço unitário
        val quantidade: Int  // Quantidade
    )

    // ==================== PROPRIEDADES PRIVADAS ====================

    // Lista mutável para guardar os itens do carrinho com identificador único
    private val itensCarrinho = mutableListOf<CarrinhoItemWithId>()
    // Serviço Retrofit para comunicação com a API
    private val sheetyService = RetrofitHelper.getInstance().create(SheetyService::class.java)
    // Instância Gson para serialização/desserialização JSON
    private val gson = Gson()
    // Handler para executar operações na thread principal
    private val handler = Handler(Looper.getMainLooper())

    // ==================== PROPRIEDADES PÚBLICAS (LIVEDATA) ====================

    // LiveData para notificar alterações no carrinho
    private val _carrinhoChanged = MutableLiveData<Boolean>()
    val carrinhoChanged: LiveData<Boolean> = _carrinhoChanged

    // ==================== MÉTODOS PÚBLICOS ====================

    // Carrega os itens do carrinho para o utilizador autenticado
    fun carregarCarrinho(onComplete: (Boolean) -> Unit) {
        // Obtém o ID do utilizador das preferências
        val userId = UserPreferences.getUserId().takeIf { it != -1 } ?: run {
            onComplete(false)
            return
        }
        // Requisição à API para obter todos os itens do carrinho
        sheetyService.getAllCarrinhoItems().enqueue(object : Callback<SheetyCarrinhoListResponse> {
            override fun onResponse(
                call: Call<SheetyCarrinhoListResponse>,
                response: Response<SheetyCarrinhoListResponse>
            ) {
                if (response.isSuccessful) {
                    // Limpa a lista local
                    itensCarrinho.clear()
                    response.body()?.carrinho
                        // Filtra itens pelo ID do utilizador
                        ?.filter { it.userId == userId }
                        ?.forEach { item ->
                            // Converte para objeto Menu
                            val menu = Menu(
                                id = item.itemId,
                                nome = item.nome,
                                preco = item.preco,
                                descricao = "",
                                categoria = "",
                                imagem = ""
                            )
                            // Adiciona à lista local
                            itensCarrinho.add(
                                CarrinhoItemWithId(
                                    id = item.id,
                                    menu = menu,
                                    quantidade = item.quantidade
                                )
                            )
                        }

                    notifyChange()
                    onComplete(true)
                } else {
                    Log.e("CarrinhoManager", "Erro ao carregar: ${response.code()}")
                    onComplete(false)
                }
            }

            override fun onFailure(call: Call<SheetyCarrinhoListResponse>, t: Throwable) {
                Log.e("CarrinhoManager", "Falha de rede: ${t.message}")
                onComplete(false)
            }
        })
    }

    // Adiciona um item ao carrinho (se existir, incrementa quantidade)
    fun adicionarItem(item: Menu, onComplete: (Boolean) -> Unit = {}) {
        val userId = UserPreferences.getUserId()
        if (userId == -1) {
            onComplete(false)
            return
        }
        // Verifica se o item já existe no carrinho
        itensCarrinho.find { it.menu.id == item.id }?.let {
            atualizarQuantidade(it, it.quantidade + 1, onComplete)
        } ?: criarNovoItem(item, onComplete) // Cria novo item se não existir
    }

    // Remove um item do carrinho
    fun removerItem(item: Menu, onComplete: (Boolean) -> Unit = {}) {
        itensCarrinho.find { it.menu.id == item.id }?.let {
            // Remove pelo ID único do carrinho
            removerItemPorId(it.id, onComplete)
        } ?: onComplete(false)
    }

    // Atualiza a quantidade de um item no carrinho
    fun atualizarQuantidade(item: CarrinhoItemWithId, novaQuantidade: Int, onComplete: (Boolean) -> Unit) {
        if (novaQuantidade < 1) {
            // Se quantidade < 1, remove o item
            removerItemPorId(item.id, onComplete)
            return
        }
        // Prepara pedido de atualização
        val updateRequest = SheetyCarrinhoUpdateRequest(CarrinhoUpdateItem(novaQuantidade))
        sheetyService.updateCarrinhoItem(item.id, updateRequest).enqueue(object : Callback<SheetyCarrinhoResponse> {
            override fun onResponse(call: Call<SheetyCarrinhoResponse>, response: Response<SheetyCarrinhoResponse>) {
                if (response.isSuccessful) {
                    // Atualiza localmente e notifica
                    item.quantidade = novaQuantidade
                    notifyChange()
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }

            override fun onFailure(call: Call<SheetyCarrinhoResponse>, t: Throwable) {
                Log.e("CarrinhoManager", "Erro atualizar quantidade: ${t.message}")
                onComplete(false)
            }
        })
    }

    // Finaliza o pedido (cria pedido e limpa carrinho)
    fun finalizarPedido(onComplete: (Boolean) -> Unit) {
        val userId = UserPreferences.getUserId().takeIf { it != -1 } ?: run {
            onComplete(false)
            return
        }
        // Verifica se há itens
        if (itensCarrinho.isEmpty()) {
            onComplete(false)
            return
        }
        // Inicia processo de criação do pedido
        criarPedido(userId, onComplete)
    }

    // Retorna cópia da lista de itens
    fun getItens(): List<CarrinhoItemWithId> = itensCarrinho.toList()

    // Calcula o valor total do carrinho
    fun calcularTotal(): Double = itensCarrinho.sumOf { (it.menu.preco ?: 0.0) * it.quantidade }

    // Obtém a quantidade de um item específico pelo ID do menu
    fun getQuantidadeItem(menuId: Int): Int =
        itensCarrinho.find { it.menu.id == menuId }?.quantidade ?: 0

    // ==================== MÉTODOS PRIVADOS ====================

    // Notifica os utilizadores sobre mudanças no carrinho
    private fun notifyChange() {
        _carrinhoChanged.postValue(true)
    }

    // Cria um novo item no carrinho (servidor e local)
    private fun criarNovoItem(item: Menu, onComplete: (Boolean) -> Unit) {
        val carrinhoItem = CarrinhoCreateItem(
            userId = UserPreferences.getUserId(),
            itemId = item.id ?: 0,
            nome = item.nome ?: "",
            preco = item.preco ?: 0.0,
            quantidade = 1
        )
        // Requisição para adicionar item à API
        sheetyService.addItemToCarrinho(SheetyCarrinhoRequest(carrinhoItem)).enqueue(object : Callback<SheetyCarrinhoResponse> {
            override fun onResponse(call: Call<SheetyCarrinhoResponse>, response: Response<SheetyCarrinhoResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        // Adiciona localmente com o ID retornado
                        itensCarrinho.add(CarrinhoItemWithId(it.carrinho.id, item, it.carrinho.quantidade))
                        notifyChange() // Notifica mudanças após adicionar item
                    }
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }

            override fun onFailure(call: Call<SheetyCarrinhoResponse>, t: Throwable) {
                Log.e("CarrinhoManager", "Erro ao adicionar item: ${t.message}")
                onComplete(false)
            }
        })
    }

    // Remove item do carrinho pelo ID (com tentativas)
    private fun removerItemPorId(id: Int, onComplete: (Boolean) -> Unit, tentativasRestantes: Int = 3) {
        sheetyService.removeItemFromCarrinho(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Remove localmente e notifica
                    itensCarrinho.removeIf { it.id == id }
                    notifyChange() // Notifica mudanças após remover item
                    onComplete(true)
                } else if (tentativasRestantes > 0) {
                    // Tenta novamente após atraso (recursão)
                    handler.postDelayed({ removerItemPorId(id, onComplete, tentativasRestantes - 1) }, 1000)
                } else {
                    onComplete(false)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                if (tentativasRestantes > 0) {
                    handler.postDelayed({ removerItemPorId(id, onComplete, tentativasRestantes - 1) }, 1000)
                } else {
                    onComplete(false)
                }
            }
        })
    }

    // Cria um novo pedido no servidor
    private fun criarPedido(userId: Int, onComplete: (Boolean) -> Unit) {
        val pedido = PedidoCreate(
            userId = userId,
            // Formata data atual
            dataHora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            total = calcularTotal(),
            status = "pendente",
            itens = gson.toJson(itensCarrinho.map {
                ItemPedido(it.menu.id ?: 0, it.menu.nome ?: "", it.menu.preco ?: 0.0, it.quantidade)
            })
        )
        // Envia pedido para a API
        sheetyService.criarPedido(PedidoCreateRequest(pedido)).enqueue(object : Callback<SheetyPedidoResponse> {
            override fun onResponse(call: Call<SheetyPedidoResponse>, response: Response<SheetyPedidoResponse>) {
                if (response.isSuccessful) {
                    limparCarrinhoRemoto(userId, onComplete)
                } else {
                    onComplete(false)
                }
            }

            override fun onFailure(call: Call<SheetyPedidoResponse>, t: Throwable) {
                onComplete(false)
            }
        })
    }

    // Limpa todos os itens do carrinho do utilizador após finalizar pedido
    private fun limparCarrinhoRemoto(userId: Int, onComplete: (Boolean) -> Unit) {
        sheetyService.getAllCarrinhoItems().enqueue(object : Callback<SheetyCarrinhoListResponse> {
            override fun onResponse(call: Call<SheetyCarrinhoListResponse>, response: Response<SheetyCarrinhoListResponse>) {
                response.body()?.carrinho
                    // Filtra IDs do utilizador
                    ?.filter { it.userId == userId }
                    ?.map { it.id }
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { apagarItensComTentativas(it, onComplete) }
                    ?: onComplete(true)
            }

            override fun onFailure(call: Call<SheetyCarrinhoListResponse>, t: Throwable) {
                onComplete(false)
            }
        })
    }

    // Apaga múltiplos itens com sistema de tentativas/repetição
    private fun apagarItensComTentativas(ids: List<Int>, onComplete: (Boolean) -> Unit) {
        val attempts = AtomicInteger(0)
        val remaining = AtomicInteger(ids.size)
        var tentativasRestantes = 3

        fun processarLote() {
            remaining.set(ids.size)
            ids.forEach { id ->
                // Requisição para apagar cada item
                sheetyService.removeItemFromCarrinho(id).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            itensCarrinho.removeIf { it.id == id }
                        }
                        processarResultado()
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        processarResultado()
                    }

                    private fun processarResultado() {
                        remaining.decrementAndGet()
                        if (remaining.get() == 0) {
                            val todosRemovidos = ids.all { id -> itensCarrinho.none { it.id == id } }

                            when {
                                todosRemovidos -> {
                                    notifyChange()
                                    onComplete(true)
                                }
                                tentativasRestantes > 0 -> {
                                    tentativasRestantes--
                                    attempts.incrementAndGet()
                                    handler.postDelayed(::processarLote, 2000)
                                }
                                else -> onComplete(false)
                            }
                        }
                    }
                })
            }
        }

        processarLote()
    }
}
