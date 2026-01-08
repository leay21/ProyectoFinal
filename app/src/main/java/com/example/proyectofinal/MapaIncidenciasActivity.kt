package com.example.proyectofinal

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectofinal.databinding.ActivityMapaIncidenciasBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

class MapaIncidenciasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapaIncidenciasBinding
    private val viewModel: MapaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Configuración OBLIGATORIA para OSMdroid (User Agent)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        binding = ActivityMapaIncidenciasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMapa()

        // 2. Cargar datos
        viewModel.cargarReportes()

        // 3. Observar cambios y pintar
        viewModel.reportes.observe(this) { listaReportes ->
            pintarMarcadores(listaReportes)
            calcularZonasDeCalor(listaReportes)
        }
    }

    private fun setupMapa() {
        binding.mapview.setTileSource(TileSourceFactory.MAPNIK) // Estilo visual del mapa
        binding.mapview.setMultiTouchControls(true)

        // Centrar mapa inicialmente (Ejemplo: CDMX, o usa tu GPS actual)
        val startPoint = GeoPoint(19.4326, -99.1332)
        binding.mapview.controller.setZoom(12.0)
        binding.mapview.controller.setCenter(startPoint)
    }

    private fun pintarMarcadores(reportes: List<Reporte>) {
        // Limpiamos overlays anteriores para no duplicar
        binding.mapview.overlays.clear()

        for (repo in reportes) {
            if (repo.latitud != 0.0 && repo.longitud != 0.0) {
                val marker = Marker(binding.mapview)
                marker.position = GeoPoint(repo.latitud, repo.longitud)
                marker.title = repo.categoria
                marker.snippet = "${repo.descripcion}\nAlias: ${repo.alias}"

                // Icono personalizado según categoría (opcional)
                // marker.icon = getDrawable(R.drawable.ic_alerta)

                // Acción al tocar el marcador
                marker.setOnMarkerClickListener { m, map ->
                    m.showInfoWindow()
                    // Aquí podrías abrir un Dialog o Activity con la FOTO y detalles completos
                    true
                }

                binding.mapview.overlays.add(marker)
            }
        }
        binding.mapview.invalidate() // Refrescar mapa
    }

    /**
     * LÓGICA DE SEMÁFORO (SIMULADA)
     * Como no tenemos polígonos de alcaldías definidos, crearemos un círculo
     * alrededor del centro del mapa que cambia de color según la cantidad de reportes visibles.
     * * Nota: En una app real, usarías GeoJSON para pintar las alcaldías exactas.
     */
    private fun calcularZonasDeCalor(reportes: List<Reporte>) {
        // Ejemplo simplificado: Crear un radio de análisis de 5km alrededor del centro
        val centro = binding.mapview.mapCenter as GeoPoint
        val radioMetros = 5000.0

        // Contar reportes en esa zona (lógica básica de distancia)
        var contador = 0
        for (repo in reportes) {
            val puntoRepo = GeoPoint(repo.latitud, repo.longitud)
            if (centro.distanceToAsDouble(puntoRepo) <= radioMetros) {
                contador++
            }
        }

        // Definir color del semáforo
        val colorZona = when {
            contador > 10 -> Color.parseColor("#40FF0000") // Rojo semi-transparente (Alta)
            contador > 5 -> Color.parseColor("#40FFFF00")  // Amarillo (Media)
            else -> Color.parseColor("#4000FF00")          // Verde (Baja)
        }

        dibujarCirculoRiesgo(centro, radioMetros, colorZona)
    }

    private fun dibujarCirculoRiesgo(centro: GeoPoint, radio: Double, colorARGB: Int) {
        val circulo = Polygon()
        circulo.points = Polygon.pointsAsCircle(centro, radio)
        circulo.fillPaint.color = colorARGB
        circulo.outlinePaint.color = Color.TRANSPARENT // Sin borde
        circulo.title = "Zona de Densidad"

        // Agregamos el círculo al mapa (primero para que quede debajo de los marcadores)
        binding.mapview.overlays.add(0, circulo)
        binding.mapview.invalidate()
    }

    override fun onResume() {
        super.onResume()
        binding.mapview.onResume() // Necesario para osmdroid
    }

    override fun onPause() {
        super.onPause()
        binding.mapview.onPause() // Necesario para osmdroid
    }
}