package com.example.proyectofinal

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectofinal.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón para ir a Crear Reporte
        binding.btnIrReporte.setOnClickListener {
            val intent = Intent(this, CrearReporteActivity::class.java)
            startActivity(intent)
        }

        // Botón para ir al Mapa
        binding.btnIrMapa.setOnClickListener {
            val intent = Intent(this, MapaIncidenciasActivity::class.java)
            startActivity(intent)
        }

        binding.btnIrDirectorio.setOnClickListener {
            val intent = Intent(this, DirectorioActivity::class.java)
            startActivity(intent)
        }
        binding.btnCambiarTema.setOnClickListener {
            // Leemos el tema actual y cambiamos al otro
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val actual = prefs.getString("tema", "guinda")

            if (actual == "guinda") {
                cambiarTema("azul") // Función heredada de BaseActivity
            } else {
                cambiarTema("guinda")
            }
        }
    }
}