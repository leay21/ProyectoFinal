package com.example.proyectofinal

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectofinal.databinding.ActivityMapaIncidenciasBinding
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay

class MapaIncidenciasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapaIncidenciasBinding
    private val viewModel: MapaViewModel by viewModels()

    // Guardamos la lista localmente para recalcular sin volver a descargar de internet
    private var listaReportesLocal: List<Reporte> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración OSM
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid_prefs", MODE_PRIVATE)
        )

        binding = ActivityMapaIncidenciasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMapa()
        viewModel.cargarReportes()

        viewModel.reportes.observe(this) { listaReportes ->
            listaReportesLocal = listaReportes // Guardar referencia local
            pintarMarcadores(listaReportes)
            calcularZonasDeCalor() // Primer cálculo
        }
    }

    private fun setupMapa() {
        binding.mapview.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapview.setMultiTouchControls(true)
        val startPoint = GeoPoint(19.4326, -99.1332)
        binding.mapview.controller.setZoom(12.0)
        binding.mapview.controller.setCenter(startPoint)

        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                // Cierra todas las ventanas de información abiertas
                binding.mapview.overlays.forEach {
                    if (it is Marker) it.closeInfoWindow()
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        })

        // Agregamos este overlay al principio para que no bloquee los clics de los marcadores
        binding.mapview.overlays.add(0, mapEventsOverlay)

        // MEJORA 3: LISTENER PARA DETECTAR MOVIMIENTO O ZOOM
        binding.mapview.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                calcularZonasDeCalor() // Recalcular al mover
                return true
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                calcularZonasDeCalor() // Recalcular al hacer zoom
                return true
            }
        })
    }

    private fun pintarMarcadores(reportes: List<Reporte>) {
        // Limpiamos overlays (pero cuidado, esto borra también el círculo,
        // así que el orden importa: primero marcadores, luego círculo en calcularZonas)
        binding.mapview.overlays.clear()

        for (repo in reportes) {
            if (repo.latitud != 0.0 && repo.longitud != 0.0) {
                val marker = Marker(binding.mapview)
                marker.position = GeoPoint(repo.latitud, repo.longitud)

                // IMPORTANTE: Guardamos el objeto completo para usarlo en el InfoWindow
                marker.relatedObject = repo

                // Asignamos nuestra ventana personalizada (MEJORA 2)
                marker.infoWindow = CustomInfoWindow(binding.mapview)

                // Título de respaldo
                marker.title = repo.categoria

                marker.setOnMarkerClickListener { m, map ->
                    m.showInfoWindow()
                    // Centrar mapa en el marcador al tocarlo (opcional)
                    map.controller.animateTo(m.position)
                    true
                }

                binding.mapview.overlays.add(marker)
            }
        }
        binding.mapview.invalidate()
    }

    private fun calcularZonasDeCalor() {
        // Si no hay reportes, no hacemos nada
        if (listaReportesLocal.isEmpty()) return

        // 1. Eliminar círculos anteriores para no encimarlos
        // Iteramos al revés para remover seguros
        for (i in binding.mapview.overlays.size - 1 downTo 0) {
            val overlay = binding.mapview.overlays[i]
            if (overlay is Polygon && overlay.title == "Zona de Densidad") {
                binding.mapview.overlays.removeAt(i)
            }
        }

        // 2. Obtener nuevo centro y calcular
        val centro = binding.mapview.mapCenter as GeoPoint
        val radioMetros = 1000.0 // Radio de 1.5km

        var contador = 0
        for (repo in listaReportesLocal) {
            val puntoRepo = GeoPoint(repo.latitud, repo.longitud)
            if (centro.distanceToAsDouble(puntoRepo) <= radioMetros) {
                contador++
            }
        }

        val colorZona = when {
            contador > 10 -> Color.parseColor("#40FF0000") // Rojo
            contador > 5 -> Color.parseColor("#40FFFF00")  // Amarillo
            else -> Color.parseColor("#4000FF00")          // Verde
        }

        val circulo = Polygon()
        circulo.points = Polygon.pointsAsCircle(centro, radioMetros)
        circulo.fillPaint.color = colorZona
        circulo.outlinePaint.color = Color.TRANSPARENT
        circulo.title = "Zona de Densidad"

        // Agregar al índice 0 para que quede DETRÁS de los marcadores
        binding.mapview.overlays.add(0, circulo)
        binding.mapview.invalidate()
    }

    override fun onResume() {
        super.onResume()
        binding.mapview.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapview.onPause()
    }
}