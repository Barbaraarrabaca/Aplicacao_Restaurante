package com.example.comidasaborosa  // Pacote base da aplicação

import android.content.Intent  // Para operações de intents entre ecrãs
import android.os.Bundle  // Para passagem de parâmetros
import android.util.Log  // Para registo de logs
import android.widget.Button  // Componente de botão
import android.widget.EditText  // Campo de edição de texto
import android.widget.TextView  // Componente de texto
import android.widget.Toast  // Para mensagens temporárias
import androidx.appcompat.app.AppCompatActivity  // Classe base para atividades
import com.example.comidasaborosa.retrofit.RetrofitInitializer  // Inicializador Retrofit
import com.example.comidasaborosa.retrofit.service.SheetyLoginResponse  // Modelo de resposta de login
import com.example.comidasaborosa.retrofit.service.SheetyUserCreatedResponse  // Modelo de resposta de registo
import com.example.comidasaborosa.retrofit.service.UserCreateRequest  // Modelo de pedido de criação de utilizador
import com.example.comidasaborosa.retrofit.service.UserCreateWrapper  // Wrapper para o pedido
import com.example.comidasaborosa.util.ValidationUtils  // Utilitários de validação
import com.google.gson.Gson  // Para serialização JSON
import retrofit2.Call  // Chamada Retrofit
import retrofit2.Callback  // Callback para chamadas assíncronas
import retrofit2.Response  // Resposta da rede

// Declaração da atividade de registo
class Registo : AppCompatActivity() {
    // Declaração de componentes de interface
    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registo) // Define o layout da atividade

        // Inicialização dos componentes de interface com os elementos do layout
        editTextName = findViewById(R.id.inputUserName)
        editTextEmail = findViewById(R.id.inputEmail)
        editTextPassword = findViewById(R.id.inputPassword)
        editTextConfirmPassword = findViewById(R.id.confirmPassword)
        btnRegister = findViewById(R.id.btnRegisto)

        // Configuração do listener do botão de registo
        btnRegister.setOnClickListener {
            // Obtenção dos valores dos campos
            val name = editTextName.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val confirmPassword = editTextConfirmPassword.text.toString().trim()

            // Validação dos inputs antes de prosseguir
            if (validateAllInputs(name, email, password, confirmPassword)) {
                // Desativa o botão durante o processamento
                btnRegister.isEnabled = false
                btnRegister.text = "A registar..."

                // Verifica se o email já existe antes de registar
                checkEmailAndRegister(name, email, password)
            }
        }

        // Configuração do link para a atividade de login
        val fazerLoginTextView: TextView = findViewById(R.id.conta)
        fazerLoginTextView.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
    // Função para validar todos os campos de input
    private fun validateAllInputs(name: String, email: String, password: String, confirmPassword: String): Boolean {
        // Limpa erros anteriores
        editTextName.error = null
        editTextEmail.error = null
        editTextPassword.error = null
        editTextConfirmPassword.error = null

        var isValid = true

        // Validação do nome
        when {
            !ValidationUtils.isValidName(name) -> {
                editTextName.error = when {
                    name.isEmpty() -> "Nome é obrigatório"
                    name.length < 2 -> "Nome deve ter pelo menos 2 caracteres"
                    name.length > 100 -> "Nome demasiado longo"
                    else -> "Nome deve conter apenas letras"
                }
                editTextName.requestFocus()
                isValid = false
            }
        }

        // Validação do email
        when {
            !ValidationUtils.isValidEmail(email) -> {
                editTextEmail.error = when {
                    email.isEmpty() -> "Email é obrigatório"
                    email.length > 100 -> "Email demasiado longo"
                    else -> "Email inválido"
                }
                if (isValid) editTextEmail.requestFocus()
                isValid = false
            }
        }

        // Validação da senha
        when {
            !ValidationUtils.isValidPassword(password) -> {
                editTextPassword.error = when {
                    password.isEmpty() -> "Senha é obrigatória"
                    password.length < 6 -> "Senha deve ter pelo menos 6 caracteres"
                    password.length > 50 -> "Senha demasiado longa"
                    password.contains(" ") -> "Senha não pode conter espaços"
                    else -> "Senha inválida"
                }
                if (isValid) editTextPassword.requestFocus()
                isValid = false
            }
        }

        // Validação da confirmação de senha
        when {
            confirmPassword.isEmpty() -> {
                editTextConfirmPassword.error = "Confirmação de senha é obrigatória"
                if (isValid) editTextConfirmPassword.requestFocus()
                isValid = false
            }
            confirmPassword != password -> {
                editTextConfirmPassword.error = "As senhas não coincidem"
                if (isValid) editTextConfirmPassword.requestFocus()
                isValid = false
            }
        }

        return isValid  // Retorna o estado global da validação
    }
    // Verifica se o email já está registado antes de criar novo utilizador
    private fun checkEmailAndRegister(name: String, email: String, password: String) {
        // Obtém lista de utilizadores existentes
        val call = RetrofitInitializer().sheetyService().getAllUsers()

        call.enqueue(object : Callback<SheetyLoginResponse> {
            override fun onResponse(call: Call<SheetyLoginResponse>, response: Response<SheetyLoginResponse>) {
                if (response.isSuccessful) {
                    val users = response.body()?.utilizador ?: emptyList()
                    // Verifica duplicação de email
                    val emailExists = users.any { it.email.equals(email, ignoreCase = true) }
                    // Reativa botão e mostra erro
                    if (emailExists) {
                        btnRegister.isEnabled = true
                        btnRegister.text = getString(R.string.btn_registo)
                        editTextEmail.error = "Este email já está registado"
                        editTextEmail.requestFocus()
                    } else {
                        // Prossegue com o registo
                        performRegistration(name, email, password)
                    }
                } else {
                    // Em caso de falha na chamada, tenta registar mesmo assim
                    performRegistration(name, email, password)
                }
            }

            override fun onFailure(call: Call<SheetyLoginResponse>, t: Throwable) {

                performRegistration(name, email, password)
            }
        })
    }
    // Executa o registo na API
    private fun performRegistration(name: String, email: String, password: String) {
        // Sanitiza inputs para prevenir problemas
        val sanitizedName = ValidationUtils.sanitizeInput(name)
        val sanitizedEmail = ValidationUtils.sanitizeInput(email)
        val sanitizedPassword = ValidationUtils.sanitizeInput(password)

        // Cria objeto de pedido
        val userCreateRequest = UserCreateRequest(
            nome = sanitizedName,
            email = sanitizedEmail,
            senha = sanitizedPassword
        )

        val request = UserCreateWrapper(userCreateRequest)


        Log.d("Registo", "Enviando registo para Sheety...")

        Log.d("Registo", "Dados JSON: ${Gson().toJson(request)}")

        val call = RetrofitInitializer().sheetyService().createUser(request)

        call.enqueue(object : Callback<SheetyUserCreatedResponse> {
            override fun onResponse(call: Call<SheetyUserCreatedResponse>, response: Response<SheetyUserCreatedResponse>) {
                // Reativa o botão independentemente do resultado
                btnRegister.isEnabled = true
                btnRegister.text = getString(R.string.btn_registo)
                // Logs detalhados
                Log.d("Registo", "Resposta recebida - Código: ${response.code()}")
                Log.d("Registo", "Headers: ${response.headers()}")
                // Tratamento de diferentes códigos de resposta
                when {
                    response.isSuccessful -> {
                        Log.d("Registo", "Registo bem-sucedido!")
                        val userResponse = response.body()
                        Log.d("Registo", "Dados retornados: $userResponse")
                        // Feedback ao utilizador
                        Toast.makeText(this@Registo, "Registo realizado com sucesso!", Toast.LENGTH_LONG).show()

                        // Limpa os campos
                        editTextName.text.clear()
                        editTextEmail.text.clear()
                        editTextPassword.text.clear()
                        editTextConfirmPassword.text.clear()

                        // Navega para o login após 1.5 segundos
                        btnRegister.postDelayed({
                            val intent = Intent(this@Registo, Login::class.java)
                            startActivity(intent)
                            finish()
                        }, 1500)
                    }
                    response.code() == 400 -> {
                        val errorBody = response.errorBody()?.string()
                        Log.e("Registo", "Erro 400 - Body: $errorBody")
                        Toast.makeText(
                            this@Registo,
                            "Erro no formato dos dados. Verifique os campos.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    response.code() == 401 || response.code() == 403 -> {
                        Toast.makeText(
                            this@Registo,
                            "Erro de autenticação. Verifique a configuração da API.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    response.code() == 409 -> {
                        editTextEmail.error = "Este email já está registado"
                        editTextEmail.requestFocus()
                    }
                    response.code() == 500 -> {
                        Toast.makeText(
                            this@Registo,
                            "Erro no servidor. Tente novamente mais tarde.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        val errorBody = response.errorBody()?.string()
                        Log.e("Registo", "Erro ${response.code()} - Body: $errorBody")
                        Toast.makeText(
                            this@Registo,
                            "Erro no registo: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<SheetyUserCreatedResponse>, t: Throwable) {
                // Reativa o botão em caso de falha
                btnRegister.isEnabled = true
                btnRegister.text = getString(R.string.btn_registo)

                Log.e("Registo", "Falha na conexão", t)

                val errorMessage = when {
                    t.message?.contains("timeout", true) == true ->
                        "Tempo de conexão esgotado. Verifique sua internet."
                    t.message?.contains("connection", true) == true ->
                        "Sem conexão com a internet."
                    else -> "Falha na conexão: ${t.localizedMessage}"
                }

                Toast.makeText(this@Registo, errorMessage, Toast.LENGTH_LONG).show()
            }
        })
    }
}
