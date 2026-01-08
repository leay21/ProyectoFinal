package com.example.proyectofinal

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView // Importante importar este SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectofinal.databinding.ActivityDirectorioBinding

class DirectorioActivity : BaseActivity() {

    private lateinit var binding: ActivityDirectorioBinding
    private lateinit var adapter: InstitucionAdapter

    // Lista completa de datos REALES
    private val listaCompleta = obtenerDatosReales()

    // Variables para controlar el estado actual de los filtros
    private var categoriaActual = "Todas"
    private var textoBusqueda = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDirectorioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFiltros()
    }

    private fun setupRecyclerView() {
        adapter = InstitucionAdapter(listaCompleta)
        binding.rvInstituciones.layoutManager = LinearLayoutManager(this)
        binding.rvInstituciones.adapter = adapter
    }

    private fun setupFiltros() {
        // 1. Configurar Spinner
        val categorias = listOf("Todas", "Seguridad", "Salud", "Servicios Públicos", "Legal", "Mujer")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categorias)
        binding.spinnerFiltro.adapter = spinnerAdapter

        binding.spinnerFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                categoriaActual = categorias[position]
                aplicarFiltrosCombinados()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 2. Configurar Buscador
        binding.svBuscador.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                textoBusqueda = newText ?: ""
                aplicarFiltrosCombinados()
                return true
            }
        })
    }

    // Lógica centralizada para filtrar
    private fun aplicarFiltrosCombinados() {
        val listaFiltrada = listaCompleta.filter { item ->
            // Condición 1: Categoría coincide (o es "Todas")
            val coincideCategoria = (categoriaActual == "Todas" || item.categoria == categoriaActual)

            // Condición 2: Nombre o Dirección contienen el texto buscado
            val coincideTexto = item.nombre.contains(textoBusqueda, ignoreCase = true) ||
                    item.direccion.contains(textoBusqueda, ignoreCase = true)

            coincideCategoria && coincideTexto
        }
        adapter.actualizarLista(listaFiltrada)
    }

    // Datos REALES verificados (CDMX)
    private fun obtenerDatosReales(): List<Institucion> {
        return listOf(
            // --- SEGURIDAD ---
            Institucion("Locatel CDMX", "Seguridad", "Servicio Telefónico y Digital", "56581111", "https://locatel.cdmx.gob.mx", "24 horas, los 365 días"),
            Institucion("Cruz Roja Mexicana (Polanco)", "Seguridad", "Juan Vives 200, Polanco I Secc, Miguel Hidalgo", "5553951111", "https://www.cruzrojamexicana.org.mx", "24 horas (Urgencias)"),
            Institucion("Bomberos CDMX (Estación Central)", "Seguridad", "Av. Fray Servando Teresa de Mier, Merced Balbuena", "5557683700", "https://bomberos.cdmx.gob.mx", "24 horas"),
            Institucion("Secretaría de Seguridad Ciudadana (SSC)", "Seguridad", "Liverpool 136, Juárez, Cuauhtémoc", "5552425100", "https://www.ssc.cdmx.gob.mx", "Atención ciudadana 24 hrs"),
            Institucion("Protección Civil CDMX", "Seguridad", "Av. Patriotismo 671, Mixcoac, Benito Juárez", "5556832222", "https://www.proteccioncivil.cdmx.gob.mx", "Lunes a Viernes 9:00 - 18:00"),

            // --- SALUD ---
            Institucion("Hospital General de México", "Salud", "Dr. Balmis 148, Doctores, Cuauhtémoc", "5527892000", "https://www.hgm.salud.gob.mx", "24 horas (Urgencias)"),
            Institucion("IMSS (Atención Ciudadana)", "Salud", "Paseo de la Reforma 476, Juárez", "8006232323", "https://www.imss.gob.mx", "Lunes a Viernes 8:00 - 20:00"),
            Institucion("Hospital Pediátrico Azcapotzalco", "Salud", "Av. Azcapotzalco 731, Centro de Azcapotzalco", "5555610981", "https://www.salud.cdmx.gob.mx", "24 horas"),
            Institucion("Centro de Salud T-III Mixcoac", "Salud", "Rembrandt 32, Mixcoac, Benito Juárez", "5555633728", "https://www.salud.cdmx.gob.mx", "Lunes a Domingo 8:00 - 20:00"),
            Institucion("Instituto Nacional de Nutrición", "Salud", "Vasco de Quiroga 15, Belisario Domínguez Secc 16, Tlalpan", "5554870900", "https://www.incmnsz.mx", "24 horas (Urgencias)"),

            // --- SERVICIOS PÚBLICOS ---
            Institucion("SACMEX (Sistema de Aguas)", "Servicios Públicos", "Río de la Plata 48, Cuauhtémoc", "5556543210", "https://www.sacmex.cdmx.gob.mx", "Lunes a Viernes 8:00 - 15:00"),
            Institucion("CFE (Comisión Federal de Electricidad)", "Servicios Públicos", "Paseo de la Reforma 164, Juárez", "071", "https://www.cfe.mx", "24 horas (Atención telefónica)"),
            Institucion("Tesorería CDMX", "Servicios Públicos", "Dr. Lavista 144, Doctores, Cuauhtémoc", "5557169150", "https://www.finanzas.cdmx.gob.mx", "Lunes a Viernes 9:00 - 15:00"),
            Institucion("Agencia de Gestión Urbana (072)", "Servicios Públicos", "Reportes de Servicios Urbanos", "072", "https://311locatel.cdmx.gob.mx", "24 horas"),
            Institucion("Control Canino (Antirrábico)", "Servicios Públicos", "Calle 10 s/n, Tolteca, Álvaro Obregón", "5552767700", "https://agatan.cdmx.gob.mx", "Lunes a Viernes 8:00 - 14:00"),

            // --- LEGAL ---
            Institucion("PROFECO", "Legal", "Av. José Vasconcelos 208, Condesa, Cuauhtémoc", "5555688722", "https://www.gob.mx/profeco", "Lunes a Viernes 9:00 - 15:00"),
            Institucion("CONDUSEF", "Legal", "Insurgentes Sur 762, Del Valle, Benito Juárez", "5553400999", "https://www.condusef.gob.mx", "Lunes a Viernes 8:30 - 16:00"),
            Institucion("Comisión Nacional de Derechos Humanos (CNDH)", "Legal", "Periférico Sur 3469, San Jerónimo Lídice", "5556818125", "https://www.cndh.org.mx", "Lunes a Viernes 9:00 - 18:00"),
            Institucion("Fiscalía General de Justicia CDMX", "Legal", "Gral. Gabriel Hernández 56, Doctores", "5552009000", "https://www.fgjcdmx.gob.mx", "24 horas"),
            Institucion("Defensoría Pública CDMX", "Legal", "Xocongo 131, Tránsito, Cuauhtémoc", "5551341000", "https://dgpj.cdmx.gob.mx", "Lunes a Viernes 9:00 - 15:00"),

            // --- MUJER ---
            Institucion("Secretaría de las Mujeres CDMX", "Mujer", "Av. Morelos 20, Centro Histórico", "5555122836", "https://www.semujeres.cdmx.gob.mx", "Lunes a Viernes 9:00 - 18:00"),
            Institucion("Línea Mujeres (SOS Mujeres)", "Mujer", "Marcación rápida *765", "765", "https://www.semujeres.cdmx.gob.mx", "24 horas"),
            Institucion("Centro de Justicia para las Mujeres (Azcapotzalco)", "Mujer", "Av. San Pablo Xalpa 396, San Martin Xochinahuac", "5553468991", "https://www.fgjcdmx.gob.mx", "24 horas"),
            Institucion("Consejo Ciudadano (Línea Mujer)", "Mujer", "Amberes 54, Juárez, Cuauhtémoc", "5555335533", "https://www.consejociudadanomx.org", "24 horas"),
            Institucion("Fiscalía de Feminicidios", "Mujer", "Gral. Gabriel Hernández 56, Doctores", "5552009287", "https://www.fgjcdmx.gob.mx", "24 horas")
        )
    }
}