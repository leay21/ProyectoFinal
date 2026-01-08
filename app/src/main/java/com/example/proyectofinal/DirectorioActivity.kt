package com.example.proyectofinal

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectofinal.databinding.ActivityDirectorioBinding

class DirectorioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDirectorioBinding
    private lateinit var adapter: InstitucionAdapter

    // Lista completa de datos
    private val listaCompleta = obtenerDatosDummy()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDirectorioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFiltro()
    }

    private fun setupRecyclerView() {
        adapter = InstitucionAdapter(listaCompleta)
        binding.rvInstituciones.layoutManager = LinearLayoutManager(this)
        binding.rvInstituciones.adapter = adapter
    }

    private fun setupFiltro() {
        // Categorías disponibles + "Todas"
        val categorias = listOf("Todas", "Seguridad", "Salud", "Servicios Públicos", "Legal", "Mujer")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categorias)
        binding.spinnerFiltro.adapter = spinnerAdapter

        binding.spinnerFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val categoriaSeleccionada = categorias[position]
                filtrarLista(categoriaSeleccionada)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun filtrarLista(categoria: String) {
        if (categoria == "Todas") {
            adapter.actualizarLista(listaCompleta)
        } else {
            val listaFiltrada = listaCompleta.filter { it.categoria == categoria }
            adapter.actualizarLista(listaFiltrada)
        }
    }

    // Generador de 30 instituciones de ejemplo
    private fun obtenerDatosDummy(): List<Institucion> {
        val lista = mutableListOf<Institucion>()

        // Seguridad
        lista.add(Institucion("Policía Municipal", "Seguridad", "Centro Civico S/N", "911", "https://www.gob.mx/911"))
        lista.add(Institucion("Bomberos Central", "Seguridad", "Av. Industrias 123", "5551234567", "https://bomberos.cdmx.gob.mx"))
        lista.add(Institucion("Protección Civil", "Seguridad", "Calle 5 de Mayo #40", "5559876543", "https://www.proteccioncivil.gob.mx"))
        lista.add(Institucion("Guardia Nacional", "Seguridad", "Base Militar Zona 1", "088", "https://www.gob.mx/guardianacional"))
        lista.add(Institucion("Fiscalía General", "Seguridad", "Av. Reforma 200", "8000085400", "https://www.fgr.org.mx"))

        // Salud
        lista.add(Institucion("Cruz Roja Mexicana", "Salud", "Juan Vives 200", "5553951111", "https://www.cruzrojamexicana.org.mx"))
        lista.add(Institucion("IMSS Clínica 1", "Salud", "Calzada Vallejo", "8006232323", "https://www.imss.gob.mx"))
        lista.add(Institucion("ISSSTE Hospital General", "Salud", "Av. Universidad", "5551409617", "https://www.gob.mx/issste"))
        lista.add(Institucion("Hospital General", "Salud", "Dr. Balmis 148", "5527892000", "https://www.hgm.salud.gob.mx"))
        lista.add(Institucion("Centro de Salud Comunitario", "Salud", "Calle Tulipanes 45", "5555555555", "https://www.gob.mx/salud"))

        // Servicios Públicos
        lista.add(Institucion("Comisión de Agua (Potable)", "Servicios Públicos", "Rio Churubusco", "5556543210", "https://www.sacmex.cdmx.gob.mx"))
        lista.add(Institucion("Comisión Federal de Electricidad", "Servicios Públicos", "Reforma 164", "071", "https://www.cfe.mx"))
        lista.add(Institucion("Limpia y Transporte", "Servicios Públicos", "Eje 5 Sur", "5557299300", "https://www.obras.cdmx.gob.mx"))
        lista.add(Institucion("Alumbrado Público", "Servicios Públicos", "Av. Tláhuac", "072", "https://www.cdmx.gob.mx"))
        lista.add(Institucion("Control Animal", "Servicios Públicos", "Calle 10", "5556789012", "https://www.agatan.cdmx.gob.mx"))

        // Legal / DDHH
        lista.add(Institucion("Comisión Derechos Humanos", "Legal", "Av. Universidad 1449", "5552295600", "https://cdhcm.org.mx"))
        lista.add(Institucion("Defensoría Pública", "Legal", "Bucareli 26", "8002242426", "https://www.ifecom.cjf.gob.mx"))
        lista.add(Institucion("Profeco", "Legal", "Av. Jose Vasconcelos", "5555688722", "https://www.gob.mx/profeco"))
        lista.add(Institucion("Condusef", "Legal", "Insurgentes Sur 762", "5553400999", "https://www.condusef.gob.mx"))
        lista.add(Institucion("INAI (Transparencia)", "Legal", "Insurgentes Sur 3211", "8008354324", "https://home.inai.org.mx"))

        // Mujer / Género
        lista.add(Institucion("Instituto de las Mujeres", "Mujer", "Morelos 20", "5555122836", "https://www.gob.mx/inmujeres"))
        lista.add(Institucion("Línea Mujeres (Locatel)", "Mujer", "N/A", "5556581111", "https://locatel.cdmx.gob.mx"))
        lista.add(Institucion("Centro de Justicia para Mujeres", "Mujer", "Azcapotzalco", "5553468991", "https://www.fgjcdmx.gob.mx"))
        lista.add(Institucion("Red Nacional de Refugios", "Mujer", "Secreto", "8008224460", "https://rednacionalderefugios.org.mx"))
        lista.add(Institucion("Fiscalía de Feminicidios", "Mujer", "General Gabriel Hernández 56", "5552009000", "https://www.fgjcdmx.gob.mx"))

        // Rellenar hasta 30... (Repetiremos algunos genéricos para cumplir el requisito)
        for (i in 1..5) {
            lista.add(Institucion("Módulo de Atención Ciudadana $i", "Servicios Públicos", "Plaza Central", "555000000$i", "https://www.gob.mx"))
        }

        return lista
    }
}