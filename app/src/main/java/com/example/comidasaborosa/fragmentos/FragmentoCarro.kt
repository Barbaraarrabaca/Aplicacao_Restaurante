package com.example.comidasaborosa.fragmentos
// Importa as bibliotecas e classes necessárias
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.comidasaborosa.CarrinhoManager
import com.example.comidasaborosa.R
import com.example.comidasaborosa.util.UserPreferences

// Declaração da classe do fragmento do carrinho
class FragmentoCarro : Fragment() {
    // Declaração de variáveis para os componentes da UI
    private lateinit var recyclerView: RecyclerView
    private lateinit var textTotal: TextView
    private lateinit var btnFinalizar: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView

    // Método que cria a view do fragmento
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla o layout XML do fragmento
        val view = inflater.inflate(R.layout.fragmento_carro, container, false)
        // Configura as views após a inflação
        setupViews(view)
        return view
    }
    // Método para inicializar os componentes da UI
    private fun setupViews(view: View) {
        // Associa as variáveis aos elementos do layout
        recyclerView = view.findViewById(R.id.recyclerViewCarrinho)
        textTotal = view.findViewById(R.id.textTotal)
        btnFinalizar = view.findViewById(R.id.btnFinalizar)
        progressBar = view.findViewById(R.id.progressBar)
        emptyView = view.findViewById(R.id.emptyView)
        // Define o layout manager para o RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        btnFinalizar.setOnClickListener { finalizarCompra() }
    }
    // Chamado após a criação da view
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Inicializa as preferências do utilizador
        initUserPreferences()
        // Verifica login e carrega o carrinho
        verificarLoginECarregarCarrinho()
    }
    // Chamado quando o fragmento retoma
    override fun onResume() {
        super.onResume()
        // Atualiza o carrinho sempre que o fragmento é retomado
        verificarLoginECarregarCarrinho()
    }
    // Inicializa as preferências do utilizador
    private fun initUserPreferences() {
        context?.let {
            // Verifica se já foi inicializado
            if (!UserPreferences.isInitialized()) {
                UserPreferences.init(it)
            }
        }
    }
    // Verifica o estado de login do utilizador
    private fun verificarLoginECarregarCarrinho() {
        if (!UserPreferences.isLoggedIn()) {
            // Se não estiver autenticado, mostra mensagem
            showEmptyState("Faça login para aceder ao carrinho")
        } else {
            // Se autenticado, carrega o carrinho
            carregarCarrinhoDoServidor()
        }
    }
    // Carrega o carrinho do servidor
    private fun carregarCarrinhoDoServidor() {
        showLoading(true)
        // Chama o gestor do carrinho
        CarrinhoManager.carregarCarrinho { success ->
            activity?.runOnUiThread {
                showLoading(false)
                if (success) {
                    atualizarCarrinho()
                } else {
                    showErrorState()
                }
            }
        }
    }
    // Atualiza a exibição do carrinho
    private fun atualizarCarrinho() {
        // Obtém os itens do carrinho
        val itens = CarrinhoManager.getItens()
        if (itens.isEmpty()) {
            // Mostra estado vazio se não houver itens
            showEmptyState("Carrinho vazio")
        } else {
            // Mostra os itens do carrinho
            showCartContents(itens)
        }
    }

    // Controla a exibição do indicador de carregamento
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
        btnFinalizar.isEnabled = !show
    }

    // Mostra estado vazio com mensagem personalizada
    private fun showEmptyState(message: String) {
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
        emptyView.text = message
        btnFinalizar.isEnabled = false
        textTotal.text = "Total: €0,00"
    }
    // Mostra estado de erro
    private fun showErrorState() {
        Toast.makeText(context, "Erro ao carregar carrinho", Toast.LENGTH_SHORT).show()
        emptyView.visibility = View.VISIBLE
        emptyView.text = "Erro ao carregar carrinho. Tente novamente."
    }
    // Exibe os itens do carrinho
    private fun showCartContents(items: List<CarrinhoManager.CarrinhoItemWithId>) {
        recyclerView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        btnFinalizar.isEnabled = true
        // Define o adapter para o RecyclerView
        recyclerView.adapter = CarrinhoAdapter(items)
        // Atualiza o total
        updateTotal()
    }
    // Atualiza o valor total do carrinho
    private fun updateTotal() {
        val total = CarrinhoManager.calcularTotal()
        textTotal.text = "Total: €%.2f".format(total)
    }

    // Inicia o processo de finalização da compra
    private fun finalizarCompra() {
        if (CarrinhoManager.getItens().isEmpty()) return

        AlertDialog.Builder(requireContext())
            .setTitle("Finalizar Compra")
            .setMessage("Deseja realmente finalizar a compra?")
            .setPositiveButton("Sim") { _, _ -> processarCompra() }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    // Processa o pedido de compra
    private fun processarCompra() {
        showLoading(true)
        CarrinhoManager.finalizarPedido { success ->
            activity?.runOnUiThread {
                showLoading(false)
                val message = if (success) "Pedido enviado com sucesso!"
                else "Erro ao enviar pedido. Tente novamente."

                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                atualizarCarrinho()
            }
        }
    }

    // Adapter para o RecyclerView do carrinho
    inner class CarrinhoAdapter(
        private val items: List<CarrinhoManager.CarrinhoItemWithId>
    ) : RecyclerView.Adapter<CarrinhoViewHolder>() {
        // Cria novas views
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            CarrinhoViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_carrinho, parent, false)
            )
        // Liga os dados à view
        override fun onBindViewHolder(holder: CarrinhoViewHolder, position: Int) {
            holder.bind(items[position])
        }
        // Retorna o número de itens
        override fun getItemCount() = items.size
    }
    // ViewHolder para os itens do carrinho
    inner class CarrinhoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Referências aos elementos da UI
        private val textNome: TextView = itemView.findViewById(R.id.textNome)
        private val textPreco: TextView = itemView.findViewById(R.id.textPreco)
        private val textQuantidade: TextView = itemView.findViewById(R.id.textQuantidadeValue)
        private val btnRemover: Button = itemView.findViewById(R.id.btnRemover)
        private val btnMais: Button = itemView.findViewById(R.id.btnMais)
        private val btnMenos: Button = itemView.findViewById(R.id.btnMenos)

        // Associa dados ao ViewHolder
        fun bind(item: CarrinhoManager.CarrinhoItemWithId) {
            textNome.text = item.menu.nome ?: "Item sem nome"
            updatePriceDisplay(item)
            textQuantidade.text = item.quantidade.toString()

            setupQuantityButtons(item)
            setupRemoveButton(item)
        }
        // Atualiza o preço do item
        private fun updatePriceDisplay(item: CarrinhoManager.CarrinhoItemWithId) {
            val totalItem = (item.menu.preco ?: 0.0) * item.quantidade
            textPreco.text = "€%.2f".format(totalItem)
        }
        // Configura botões de quantidade
        private fun setupQuantityButtons(item: CarrinhoManager.CarrinhoItemWithId) {
            btnMais.setOnClickListener { changeQuantity(item, item.quantidade + 1) }
            btnMenos.setOnClickListener {
                if (item.quantidade > 1) changeQuantity(item, item.quantidade - 1)
            }
        }
        // Altera a quantidade de um item
        private fun changeQuantity(item: CarrinhoManager.CarrinhoItemWithId, newQuantity: Int) {
            CarrinhoManager.atualizarQuantidade(item, newQuantity) { success ->
                activity?.runOnUiThread {
                    if (success) {
                        item.quantidade = newQuantity
                        textQuantidade.text = newQuantity.toString()
                        updatePriceDisplay(item)
                        updateTotal()
                    }
                }
            }
        }
        // Configura botão de remoção
        private fun setupRemoveButton(item: CarrinhoManager.CarrinhoItemWithId) {
            btnRemover.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Remover Item")
                    .setMessage("Confirmar remoção de ${item.menu.nome}?")
                    .setPositiveButton("Sim") { _, _ -> removeItem(item) }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
        // Remove um item do carrinho
        private fun removeItem(item: CarrinhoManager.CarrinhoItemWithId) {
            showLoading(true)
            CarrinhoManager.removerItem(item.menu) { success ->
                activity?.runOnUiThread {
                    showLoading(false)
                    if (success) {
                        Toast.makeText(context, "Item removido", Toast.LENGTH_SHORT).show()
                        atualizarCarrinho()
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance() = FragmentoCarro()
    }
}
