package com.example.comidasaborosa

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val fazerRegistoTextView: TextView = findViewById(R.id.fazer_registo)
        fazerRegistoTextView.setOnClickListener {
            // Inicia a atividade de registo
            val intent = Intent(this, Registo::class.java)
            startActivity(intent)
    }
}
}