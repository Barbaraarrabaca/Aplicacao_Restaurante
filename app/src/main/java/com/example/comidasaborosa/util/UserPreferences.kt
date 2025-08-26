package com.example.comidasaborosa.util

import android.content.Context
import android.content.SharedPreferences

object UserPreferences {
    // Nome do ficheiro de preferências
    private const val PREFS_NAME = "user_prefs"
    // Chave para armazenar ID do utilizador
    private const val KEY_USER_ID = "user_id"
    // Chave para armazenar nome do utilizador
    private const val KEY_USER_NAME = "user_name"
    // Chave para armazenar email do utilizador
    private const val KEY_USER_EMAIL = "user_email"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        // Verifica se as preferências ainda não foram inicializadas
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }
    // Verifica se o objeto já foi inicializado
    fun isInitialized(): Boolean = prefs != null

    //Armazena os dados do utilizador após login bem-sucedido
    fun saveUser(userId: Int, userName: String, userEmail: String) {
        // Usa o editor de preferências
        prefs?.edit()?.apply {
            // Insere o ID do utilizador
            putInt(KEY_USER_ID, userId)
            // Insere o nome do utilizador
            putString(KEY_USER_NAME, userName)
            // Insere o email do utilizador
            putString(KEY_USER_EMAIL, userEmail)
            // Aplica as alterações de forma assíncrona
            apply()
        }
    }
    //Recuperam os dados armazenados do utilizador
    // Obtém o ID do utilizador (retorna -1 se não existir)
    fun getUserId(): Int = prefs?.getInt(KEY_USER_ID, -1) ?: -1

    //Limpa todos os dados do utilizador (usado no logout)
    fun clearUser() {
        prefs?.edit()?.clear()?.apply()
    }
    // Verifica se existe um utilizador com sessão iniciada
    fun isLoggedIn(): Boolean = isInitialized() && getUserId() != -1
}
