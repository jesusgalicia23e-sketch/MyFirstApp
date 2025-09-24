package com.example.myfirstapp.presentation

import android.os.Bundle;
import androidx.activity.ComponentActivity;
import com.example.myfirstapp.R;


class OtraVentana: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.otra_ventana)
    }
}