package com.example.myfirstapp.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.myfirstapp.R

class OtraVentana() : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.otra_ventana)
        val context = this

        val ritmo_cardiaco: Button = findViewById(R.id.rc)
        val velocimetro: Button = findViewById(R.id.vel)

        ritmo_cardiaco.setOnClickListener {
            val intent = Intent(this@OtraVentana, HearthRate::class.java)
            startActivity(intent)
        }

        velocimetro.setOnClickListener {
            val intent = Intent(this@OtraVentana, Veloccimeter::class.java)
            startActivity(intent)
        }
    }
}
