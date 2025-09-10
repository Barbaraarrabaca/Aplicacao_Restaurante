package pt.ipt.comidasaborosa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import pt.ipt.comidasaborosa.util.UserPreferences
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

// Classe principal que define a atividade inicial
class MainActivity : AppCompatActivity() {

    // Método chamado durante a criação da atividade
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define o layout XML associado a esta atividade
        setContentView(R.layout.activity_main)

        // Inicializa as preferências do utilizador com o contexto da aplicação
        UserPreferences.init(applicationContext)
        // Obtém referências aos elementos de UI do layout
        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        val viewPager2: ViewPager2 = findViewById(R.id.view_pager2)

        // Configura o ViewPager2 para manter 3 páginas adjacentes em memória
        viewPager2.offscreenPageLimit = 3
        // Define o adaptador responsável pela gestão dos fragments
        viewPager2.adapter = ViewPagerAdapter(this)
        // Sincroniza o TabLayout com o ViewPager2 e configura cada aba
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            when(position) {
                0 -> { // Primeira aba
                    tab.text = "Perfil"
                    tab.setIcon(R.drawable.baseline_person_perfil)
                }
                1 -> {  // Segunda aba
                    tab.text = "Menu"
                    tab.setIcon(R.drawable.baseline_ementa)
                }
                2 -> {  // Terceira aba
                    tab.text = "Carro"
                    tab.setIcon(R.drawable.baseline_pedidos)
                }
                3 -> { // Quarta aba
                    tab.text = "Localização"
                    tab.setIcon(R.drawable.baseline_localizacao)
                }
            }
        }.attach() // Aplica a ligação entre os componentes


        // Verifica se o utilizador está autenticado
        if (UserPreferences.isLoggedIn()) {
            // Carrega o carrinho de compras
            CarrinhoManager.carregarCarrinho { }
        }
    }
}
