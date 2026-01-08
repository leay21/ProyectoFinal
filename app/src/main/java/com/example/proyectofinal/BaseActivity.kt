package com.example.proyectofinal

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Cargar preferencia ANTES del super.onCreate y setContentView
        val prefs: SharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val temaGuardado = prefs.getString("tema", "guinda")

        if (temaGuardado == "azul") {
            setTheme(R.style.Theme_ProyectoFinal_Azul)
        } else {
            setTheme(R.style.Theme_ProyectoFinal_Guinda)
        }

        super.onCreate(savedInstanceState)
    }

    // Función auxiliar para guardar y cambiar
    protected fun cambiarTema(nuevoTema: String) {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        prefs.edit().putString("tema", nuevoTema).apply()
        recreate() // Recarga la actividad actual
    }
}