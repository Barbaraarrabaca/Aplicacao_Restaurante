package pt.ipt.comidasaborosa.fragmentos

import android.app.AlertDialog  // Para criação de diálogos de alerta
import android.os.Bundle        // Para passagem de dados entre componentes
import android.view.LayoutInflater // Para "inflar" layouts XML em objetos View
import android.view.View        // Componente base de interface gráfica
import android.view.ViewGroup   // Container para views
import android.widget.ScrollView // View que permite scroll de conteúdo
import android.widget.TextView   // Para exibição de texto
import androidx.fragment.app.Fragment // Classe base para fragmentos
import pt.ipt.comidasaborosa.R // Recursos do projeto

// Fragmento para exibir perfil do utilizador
class FragmentoPerfil : Fragment() {

    // Cria a view do fragmento
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Converte o layout XML correspondente ao fragmento numa View
        return inflater.inflate(R.layout.fragmento_perfil, container, false)
    }
    // Chamado após a criação da view
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lista de bibliotecas/dependências do projeto
        val libraries = listOf(
            "org.osmdroid:osmdroid-android:6.1.18",
            "androidx.preference:preference-ktx:1.2.1",
            "com.google.android.gms:play-services-location:21.0.1",
            "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0",
            "com.github.bumptech.glide:glide:4.12.0",
            "androidx.cardview:cardview:1.0.0",
            "com.squareup.retrofit2:retrofit:2.11.0",
            "com.squareup.retrofit2:converter-gson:2.9.0",
            "androidx.lifecycle:lifecycle-runtime-ktx:2.6.1",
            "io.ktor:ktor-client-content-negotiation:2.0.0",
            "io.ktor:ktor-client-core:2.0.0",
            "io.ktor:ktor-client-cio:2.0.0",
            "io.ktor:ktor-client-json:2.0.0",
            "io.ktor:ktor-client-serialization:2.0.0",
            "com.squareup.okhttp3:okhttp:4.9.1",
            "com.google.code.gson:gson:2.8.8",
            "androidx.core:core-ktx:1.9.0",
            "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0",
            "androidx.appcompat:appcompat:1.6.1",
            "com.google.android.material:material:1.11.0",
            "androidx.constraintlayout:constraintlayout:2.1.4",
            "com.android.volley:volley:1.2.1",
            "androidx.privacysandbox.tools:tools-core:1.0.0-alpha09",
            "com.squareup.okhttp3:logging-interceptor:4.11.0"
        )

        // Formata a lista de bibliotecas para exibição
        val librariesText = "Bibliotecas usadas no projeto:\n" + libraries.joinToString(separator = "\n- ", prefix = "- ")

        // Define ação de clique em qualquer área do fragmento
        view.setOnClickListener {
            // Cria ScrollView para permitir scroll do conteúdo
            val scrollView = ScrollView(requireContext())
            // Cria TextView para mostrar as bibliotecas
            val textView = TextView(requireContext())
            textView.text = librariesText
            textView.setPadding(32, 32, 32, 32) // Define espaçamento interno (pixels)
            scrollView.addView(textView) // Adiciona o TextView ao ScrollView

            // Constrói e exibe diálogo de alerta
            AlertDialog.Builder(requireContext())
                // Título do diálogo
                .setTitle("Bibliotecas do Projeto")
                .setView(scrollView)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
    // Companion object para criação de instâncias do fragmento
    companion object {
        fun newInstance() = FragmentoPerfil()
    }
}
