package pt.ipt.comidasaborosa
// Importa a classe base Fragment do AndroidX
import androidx.fragment.app.Fragment
// Importa a classe FragmentActivity para gestão de fragments
import androidx.fragment.app.FragmentActivity
// Importa o adaptador base para ViewPager2
import androidx.viewpager2.adapter.FragmentStateAdapter
// Importa os fragments personalizados da aplicação
import pt.ipt.comidasaborosa.fragmentos.FragmentoCarro
import pt.ipt.comidasaborosa.fragmentos.FragmentoLocalizacao
import pt.ipt.comidasaborosa.fragmentos.FragmentoMenu
import pt.ipt.comidasaborosa.fragmentos.FragmentoPerfil

// Declara a classe adaptadora para ViewPager2, estendendo FragmentStateAdapter
class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    // Sobrescreve o método que retorna o número total de tabs/fragments
    override fun getItemCount(): Int = 4 // Existem 4 fragments fixos

    // Sobrescreve o método que cria fragments para cada posição
    override fun createFragment(position: Int): Fragment {
        // Usa uma expressão 'when' para selecionar o fragment conforme a posição
        return when(position) {
            0 -> FragmentoPerfil.newInstance() // Primeiro tab: Perfil
            1 -> FragmentoMenu.newInstance() // Segundo tab: Menu
            2 -> FragmentoCarro.newInstance()  // Terceiro tab: Carrinho
            3 -> FragmentoLocalizacao.newInstance()// Quarto tab: Localização
            else -> FragmentoPerfil.newInstance() // Fallback para Perfil
        }
    }
}