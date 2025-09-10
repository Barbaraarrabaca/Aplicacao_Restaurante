package pt.ipt.comidasaborosa.util

object ValidationUtils {
    //Valida endereços de email
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() &&
               android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
               email.length <= 100 // Limite máximo (100 caracteres)
    }
    //Valida passwords
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6 && // Mínimo 6 caracteres
               password.length <= 50 && // Máximo 50 caracteres
               !password.contains(" ") // Sem espaços
    }

    fun sanitizeInput(input: String): String {
        return input.trim()
            .replace("'", "") // Remove aspas simples
            .replace("\"", "") // Remove aspas duplas
            .replace(";", "") // Remove ponto e vírgula
            .replace("--", "") // Remove comentários SQL
            .replace("<", "") // Remove tags HTML
            .replace(">", "")
            .replace("script", "", ignoreCase = true) // Remove tentativas de script injection
    }
    //Valida nomes de utilizador
    fun isValidName(name: String): Boolean {
        return name.isNotEmpty() &&
               name.length >= 2 &&
               name.length <= 100 &&
               name.matches(Regex("^[a-zA-ZÀ-ÿ\\s]+$")) // Apenas letras e espaços
    }
}
