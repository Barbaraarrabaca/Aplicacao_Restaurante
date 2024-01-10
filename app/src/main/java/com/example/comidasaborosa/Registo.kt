package com.example.comidasaborosa

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class Registo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registo)

        // Configura um listener para o texto "Já tem uma conta? Faça o login"
        val fazerLoginTextView: TextView = findViewById(R.id.conta)

        fazerLoginTextView.setOnClickListener {
            // Inicia a atividade de login
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
    }
