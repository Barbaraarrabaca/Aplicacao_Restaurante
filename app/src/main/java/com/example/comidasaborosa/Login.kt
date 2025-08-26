// Define o pacote da aplicação
package com.example.comidasaborosa

// Importações necessárias para o funcionamento da atividade
import android.content.Intent // Para iniciar novas atividades/screens
import android.os.Bundle // Para passar dados entre activities
import android.util.Log // Para registar mensagens no logcat (depuração)
import android.widget.Button // Componente de botão clicável
import android.widget.EditText // Campo de texto editável para inputs
import android.widget.TextView // Componente para exibição de texto
import android.widget.Toast // Para mostrar mensagens temporárias ao utilizador
import androidx.appcompat.app.AppCompatActivity  // Classe base para atividades com Action Bar
import com.example.comidasaborosa.retrofit.RetrofitHelper  // Inicializador da API Retrofit
import com.example.comidasaborosa.retrofit.service.SheetyLoginResponse // Modelo de resposta da API
import com.example.comidasaborosa.retrofit.service.UserResponse // Modelo de dados do utilizador
import com.example.comidasaborosa.util.UserPreferences // Gestor de preferências locais
import com.example.comidasaborosa.util.ValidationUtils // Utilitário de validação de dados
import retrofit2.Call // Representa uma chamada à API
import retrofit2.Callback // Callback para tratamento assíncrono
import retrofit2.Response // Resposta da chamada à API
import java.io.IOException // Exceção de I/O (rede/ficheiros)
import java.net.SocketTimeoutException // Exceção de timeout de conexão

// Classe principal da atividade de Login
class Login : AppCompatActivity() {  // Herda de AppCompatActivity (compatibilidade)

    // Declaração de variáveis para os componentes da interface
    private lateinit var editTextEmail: EditText // Campo para inserção do email
    private lateinit var editTextPassword: EditText // Campo para inserção da senha
    private lateinit var btnLogin: Button // Botão para submeter o login

    // Variáveis para controlo de tentativas de login
    private var loginAttempts = 0  // Contador de tentativas falhadas
    private var lastAttemptTime = 0L  // Timestamp da última tentativa (milissegundos)
    private val MAX_ATTEMPTS = 3  // Limite máximo de tentativas antes de bloqueio
    private val LOCKOUT_TIME = 5 * 60 * 1000L  // 5 minutos de bloqueio em milissegundos

    // Método chamado quando a atividade é criada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define o layout da atividade
        setContentView(R.layout.activity_login)

        // Inicializa as preferências do utilizador
        if (!UserPreferences.isInitialized()) {
            UserPreferences.init(this)
        }

        // Limpa a sessão do utilizador ao iniciar
        clearUserSession()

        // Associa os componentes do layout às variáveis
        editTextEmail = findViewById(R.id.inputEmail)
        editTextPassword = findViewById(R.id.inputPassword)
        btnLogin = findViewById(R.id.btnLogin)

        // Configura o clique no botão de login
        btnLogin.setOnClickListener {
            // Obtém os valores dos campos
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            // Verifica se pode tentar login e valida os inputs
            if (canAttemptLogin() && validateInputs(email, password)) {
                loginUser(email, password)
            }
        }
        // Configura o clique no texto "Fazer Registo"
        val fazerRegistoTextView: TextView = findViewById(R.id.fazer_registo)
        fazerRegistoTextView.setOnClickListener {
            // Inicia a atividade de registo
            val intent = Intent(this, Registo::class.java)
            startActivity(intent)
        }
        // Configura o clique no texto "Esqueceu Password"
        val esqPasswordTextView: TextView = findViewById(R.id.esqPassword)
        esqPasswordTextView.setOnClickListener {
            // Mostra mensagem de funcionalidade em desenvolvimento
            Toast.makeText(this, "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
    }
    // Limpa os dados do utilizador armazenados
    private fun clearUserSession() {
        UserPreferences.clearUser()
    }
    // Verifica se é permitido tentar login (controlo de tentativas)
    private fun canAttemptLogin(): Boolean {
        val currentTime = System.currentTimeMillis()
        // Verifica se excedeu o limite de tentativas
        if (loginAttempts >= MAX_ATTEMPTS) {
            val timePassed = currentTime - lastAttemptTime
            // Verifica se ainda está no período de bloqueio
            if (timePassed < LOCKOUT_TIME) {
                val remainingTime = (LOCKOUT_TIME - timePassed) / 1000
                showError("Demasiadas tentativas. Aguarde $remainingTime segundos")
                return false
            } else {
                // Reseta o contador após o período de bloqueio
                loginAttempts = 0
            }
        }

        return true
    }
    // Valida os campos de email e password
    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        // Validação do email
        if (!ValidationUtils.isValidEmail(email)) {
            editTextEmail.error = when {
                email.isEmpty() -> "Email é obrigatório"
                email.length > 100 -> "Email demasiado longo"
                else -> "Formato de email inválido"
            }
            isValid = false
        }

        // Validação da password
        if (!ValidationUtils.isValidPassword(password)) {
            editTextPassword.error = when {
                password.isEmpty() -> "Senha é obrigatória"
                password.length < 6 -> "Senha deve ter no mínimo 6 caracteres"
                password.length > 50 -> "Senha demasiado longa"
                password.contains(" ") -> "Senha não pode conter espaços"
                else -> "Senha inválida"
            }
            isValid = false
        }

        return isValid
    }
    // Processa o login do utilizador
    private fun loginUser(email: String, password: String) {
        // Desativa o botão durante o processo
        btnLogin.isEnabled = false
        btnLogin.text = "A verificar..."

        // Sanitiza os inputs
        val sanitizedEmail = ValidationUtils.sanitizeInput(email)
        val sanitizedPassword = ValidationUtils.sanitizeInput(password)

        // Prepara a chamada à API
        val call = RetrofitHelper.sheetyService().getAllUsers()

        // Logs para depuração
        Log.d("Login", "Tentando fazer login com email: $sanitizedEmail")
        Log.d("Login", "URL: ${call.request().url}")
        Log.d("Login", "Headers: ${call.request().headers}")
        Log.d("Login", "Method: ${call.request().method}")
        // Executa a chamada à API de forma assíncrona
        call.enqueue(object : Callback<SheetyLoginResponse> {
            override fun onResponse(call: Call<SheetyLoginResponse>, response: Response<SheetyLoginResponse>) {
                // Reativa o botão após resposta
                btnLogin.isEnabled = true
                btnLogin.text = getString(R.string.btn_login)
                // Logs da resposta
                Log.d("Login", "Resposta recebida - Código: ${response.code()}")
                Log.d("Login", "Headers da resposta: ${response.headers()}")
                // Processa a resposta conforme o status code
                when {
                    response.isSuccessful -> {
                        val loginResponse = response.body()
                        Log.d("Login", "Número de utilizadores encontrados: ${loginResponse?.utilizador?.size ?: 0}")

                        if (loginResponse != null && loginResponse.utilizador.isNotEmpty()) {
                            // Procura o utilizador com as credenciais
                            val user = loginResponse.utilizador.find { user ->
                                user.email.equals(sanitizedEmail, ignoreCase = true) &&
                                        user.senha == sanitizedPassword
                            }

                            if (user != null) {
                                // Login bem-sucedido
                                loginAttempts = 0

                                Toast.makeText(this@Login, "Login bem-sucedido!", Toast.LENGTH_LONG).show()

                                // Armazena os dados do utilizador
                                saveTemporaryUserData(user)

                                // Limpa os campos
                                editTextEmail.text.clear()
                                editTextPassword.text.clear()

                                // Navega para a MainActivity após 1 segundo
                                btnLogin.postDelayed({
                                    val intent = Intent(this@Login, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }, 1000)
                            } else {
                                // Credenciais inválidas
                                loginAttempts++
                                lastAttemptTime = System.currentTimeMillis()
                                showError("Email ou senha incorretos")
                                Log.d("Login", "Utilizador não encontrado com essas credenciais")
                            }
                        } else {
                            // Lista de utilizadores vazia
                            showError("Nenhum utilizador registado no sistema")
                            Log.w("Login", "Lista de utilizadores vazia")
                        }
                    }
                    // Tratamento de erros específicos
                    response.code() == 400 -> {
                        val errorBody = response.errorBody()?.string()
                        Log.e("Login", "Erro 400 - Bad Request. Body: $errorBody")
                        showError("Erro na requisição. Verifique a configuração da API.")
                    }
                    response.code() == 401 || response.code() == 403 -> {
                        Log.e("Login", "Erro de autenticação: ${response.code()}")
                        showError("Erro de autenticação. Verifique a configuração da API.")
                    }
                    response.code() == 404 -> {
                        Log.e("Login", "Endpoint não encontrado")
                        showError("Serviço não encontrado. Verifique a URL da API.")
                    }
                    response.code() == 429 -> {
                        Log.e("Login", "Rate limit excedido")
                        showError("Demasiadas tentativas. Tente novamente mais tarde")
                    }
                    response.code() == 500 -> {
                        Log.e("Login", "Erro interno do servidor")
                        showError("Erro no servidor. Tente novamente mais tarde")
                    }
                    else -> {
                        // Outros erros
                        val errorBody = response.errorBody()?.string()
                        Log.e("Login", "Erro ${response.code()} - Body: $errorBody")
                        showError("Erro no servidor: ${response.code()}")
                    }
                }
            }
            // Tratamento de falhas na rede
            override fun onFailure(call: Call<SheetyLoginResponse>, t: Throwable) {
                btnLogin.isEnabled = true
                btnLogin.text = getString(R.string.btn_login)

                Log.e("Login", "Falha na conexão: ${t.message}", t)
                // Mensagens de erro específicas
                val errorMessage = when {
                    t is SocketTimeoutException -> "Tempo de conexão esgotado"
                    t is IOException -> {
                        if (t.message?.contains("failed to connect", true) == true) {
                            "Não foi possível conectar ao servidor"
                        } else {
                            "Sem conexão com a internet"
                        }
                    }
                    else -> "Erro de conexão: ${t.localizedMessage}"
                }

                showError(errorMessage)
            }
        })
    }
    // Mostra uma mensagem de erro
    private fun showError(message: String) {
        Toast.makeText(this@Login, message, Toast.LENGTH_LONG).show()
    }
    // Armazena os dados do utilizador nas preferências
    private fun saveTemporaryUserData(user: UserResponse) {

        UserPreferences.saveUser(user.id, user.nome, user.email)
        Log.d("Login", "Dados do utilizador salvos: ID=${user.id}, Nome=${user.nome}")
    }
    // Limpa os campos quando a atividade é destruída
    override fun onDestroy() {
        super.onDestroy()

        editTextEmail.text.clear()
        editTextPassword.text.clear()
    }
}
