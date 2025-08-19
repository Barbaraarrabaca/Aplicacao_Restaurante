package com.example.comidasaborosa.fragmentos

// Importa a classe Bundle para passar dados entre componentes
import android.os.Bundle
// Importa a classe Log para registo de mensagens de depuração
import android.util.Log
// Importa o LayoutInflater para transformar layouts XML em Views
import android.view.LayoutInflater
// Importa a classe base View para componentes de interface
import android.view.View
// Importa ViewGroup como contentor de outros views
import android.view.ViewGroup
// Importa o componente Button para botões clicáveis
import android.widget.Button
// Importa ImageView para exibição de imagens
import android.widget.ImageView
// Importa TextView para exibição de texto
import android.widget.TextView
// Importa Toast para mostrar mensagens temporárias
import android.widget.Toast
// Importa a classe base Fragment do AndroidX
import androidx.fragment.app.Fragment
// Importa LinearLayoutManager para organizar itens linearmente em RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
// Importa RecyclerView para listas eficientes
import androidx.recyclerview.widget.RecyclerView
// Importa a biblioteca Glide para carregamento de imagens
import com.bumptech.glide.Glide
// Importa o gestor personalizado do carrinho de compras
import com.example.comidasaborosa.CarrinhoManager
// Importa os recursos da aplicação (IDs de layouts, drawables, etc.)
import com.example.comidasaborosa.R
// Importa o modelo de dados Menu
import com.example.comidasaborosa.model.Menu
// Importa o helper de configuração do Retrofit
import com.example.comidasaborosa.retrofit.RetrofitHelper
// Importa o modelo de resposta da API Sheety
import com.example.comidasaborosa.retrofit.service.SheetyResponse
// Importa a interface de serviço para chamadas à API Sheety
import com.example.comidasaborosa.retrofit.service.SheetyService
// Importa Call do Retrofit para operações assíncronas
import retrofit2.Call
// Importa Callback para tratamento de respostas da API
import retrofit2.Callback
// Importa Response para manipular respostas HTTP
import retrofit2.Response

// Classe que representa o fragmento do menu
class FragmentoMenu : Fragment() {
    // Declaração de componentes da UI
    private lateinit var recyclerView: RecyclerView
    // Serviço para comunicação com a API Sheety
    private lateinit var sheetyService: SheetyService
    // Lista mutável para armazenar itens do menu
    private var menuList = mutableListOf<Menu>()
    // Adaptador para o RecyclerView
    private var menuAdapter: MenuAdapter? = null

    // Método chamado para criar a view do fragmento
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla o layout do fragmento
        val view = inflater.inflate(R.layout.fragmento_menu, container, false)
        // Inicializa os componentes visuais
        initViews(view)
        // Configura o Retrofit para chamadas à API
        setupRetrofit()
        // Carrega os itens do menu da API
        loadMenuItems()
        return view
    }
    // Chamado após a view ser criada
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //  mudanças no carrinho - atualiza a UI
        CarrinhoManager.carrinhoChanged.observe(viewLifecycleOwner) { _ ->
            updateVisibleItems()
        }

        // Carrega o carrinho guardado
        CarrinhoManager.carregarCarrinho { success ->
            if (success) {
                // Atualiza a UI na thread principal
                activity?.runOnUiThread {
                    menuAdapter?.notifyDataSetChanged()
                }
            }
        }
    }
    // Inicializa os componentes da UI
    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewMenu)
        // Define um layout linear para o RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Otimização para tamanho fixo
        recyclerView.setHasFixedSize(true)
    }
    // Configura o cliente Retrofit
    private fun setupRetrofit() {
        val retrofit = RetrofitHelper.getInstance()
        sheetyService = retrofit.create(SheetyService::class.java)
    }
    // Carrega os itens do menu da API
    private fun loadMenuItems() {
        sheetyService.getAllMenuItems().enqueue(object : Callback<SheetyResponse> {
            // Chamada em caso de resposta bem-sucedida
            override fun onResponse(call: Call<SheetyResponse>, response: Response<SheetyResponse>) {
                handleMenuResponse(response)
            }
            // Chamada em caso de falha na rede
            override fun onFailure(call: Call<SheetyResponse>, t: Throwable) {
                handleMenuError(t)
            }
        })
    }
    // Processa a resposta da API
    private fun handleMenuResponse(response: Response<SheetyResponse>) {
        if (response.isSuccessful) {
            // Atualiza a lista de itens do menu
            response.body()?.menu?.let { items ->
                menuList.clear()
                menuList.addAll(items)
                // Cria e define o adaptador
                menuAdapter = MenuAdapter(menuList)
                recyclerView.adapter = menuAdapter
            }
        } else {
            // Regista erro no log
            Log.e("API_ERROR", "Código: ${response.code()} - ${response.errorBody()?.string()}")
            showError("Erro ao carregar o menu")
        }
    }
    // Trata erros de rede
    private fun handleMenuError(t: Throwable) {
        Log.e("NETWORK_ERROR", "Falha na rede", t)
        showError("Erro de conexão: ${t.message}")
    }
    // Mostra mensagem de erro em Toast
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // Atualiza os itens visíveis no RecyclerView
    private fun updateVisibleItems() {
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
        val firstVisible = layoutManager.findFirstVisibleItemPosition()
        val lastVisible = layoutManager.findLastVisibleItemPosition()

        if (firstVisible != RecyclerView.NO_POSITION && lastVisible != RecyclerView.NO_POSITION) {
            menuAdapter?.notifyItemRangeChanged(firstVisible, lastVisible - firstVisible + 1)
        }
    }
    // Adaptador para o RecyclerView
    inner class MenuAdapter(private val items: List<Menu>) : RecyclerView.Adapter<MenuViewHolder>() {
        // Cria novas views
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_menu, parent, false)
            return MenuViewHolder(view)
        }
        // Liga os dados à view
        override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
            holder.bind(items[position])
        }
        // Retorna o número total de itens
        override fun getItemCount() = items.size
    }
    // ViewHolder para cada item do menu
    inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Referências aos componentes da UI
        private val textNome: TextView = itemView.findViewById(R.id.textNome)
        private val textDescricao: TextView = itemView.findViewById(R.id.textDescricao)
        private val textPreco: TextView = itemView.findViewById(R.id.textPreco)
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewItem)
        private val btnAdicionar: Button = itemView.findViewById(R.id.btnAdicionar)

        // Associa os dados do item à UI
        fun bind(menuItem: Menu) {
            setupTextViews(menuItem)
            loadImage(menuItem.imagem)
            setupAddButton(menuItem)
        }
        // Define os textos nos TextViews
        private fun setupTextViews(menuItem: Menu) {
            textNome.text = menuItem.nome ?: "Item sem nome"
            textDescricao.text = menuItem.descricao ?: "Descrição não disponível"
            textPreco.text = "€${String.format("%.2f", menuItem.preco ?: 0.0)}"
        }
        // Carrega a imagem usando Glide
        private fun loadImage(imageUrl: String?) {
            imageUrl?.let { url ->
                Glide.with(requireContext())
                    .load(fixImageUrl(url))
                    .placeholder(R.drawable.placeholder) // Imagem durante carregamento
                    .error(R.drawable.error) // Imagem em caso de erro
                    .into(imageView)
            }
        }
        // Corrige formatação da URL
        private fun fixImageUrl(url: String): String {
            return url.replace("\\", "/")
        }
        // Configura o botão de adicionar
        private fun setupAddButton(menuItem: Menu) {
            updateButtonText(menuItem.id)
            btnAdicionar.setOnClickListener {
                adicionarAoCarrinho(menuItem)
            }
        }
        // Atualiza o texto do botão conforme quantidade no carrinho
        private fun updateButtonText(menuId: Int?) {
            val quantidade = CarrinhoManager.getQuantidadeItem(menuId ?: 0)
            btnAdicionar.text = if (quantidade > 0) {
                "Adicionar mais ($quantidade)"
            } else {
                "Adicionar"
            }
        }
    }
    // Adiciona item ao carrinho
    private fun adicionarAoCarrinho(item: Menu) {
        CarrinhoManager.adicionarItem(item) { success ->
            activity?.runOnUiThread {
                if (success) {
                    showSuccessMessage(item)

                } else {
                    showError("Falha ao adicionar item")
                }
            }
        }
    }
    // Mostra mensagem de sucesso
    private fun showSuccessMessage(item: Menu) {
        val quantidade = CarrinhoManager.getQuantidadeItem(item.id ?: 0)
        Toast.makeText(
            requireContext(),
            "${item.nome} (x$quantidade) no carrinho!",
            Toast.LENGTH_SHORT
        ).show()
    }
    // Quando o fragmento retoma
    override fun onResume() {
        super.onResume()
        // Recarrega o carrinho
        CarrinhoManager.carregarCarrinho { success ->
            if (success) {
                activity?.runOnUiThread {
                    menuAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    companion object {
        fun newInstance() = FragmentoMenu()
    }
}
