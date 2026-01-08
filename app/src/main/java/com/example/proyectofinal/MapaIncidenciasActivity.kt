package com.example.proyectofinal

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.proyectofinal.databinding.ActivityMapaIncidenciasBinding
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

class MapaIncidenciasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapaIncidenciasBinding
    private val viewModel: MapaViewModel by viewModels()

    // Variable para el detector de toques (para cerrar ventanas)
    private lateinit var mapEventsOverlay: MapEventsOverlay

    // Guardamos la lista localmente
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

        // 1. Inicializamos el detector de toques (pero aun no lo agregamos)
        inicializarDetectorToques()

        setupMapa()

        // 2. Intentamos centrar en ubicación real
        centrarEnUbicacionActual()

        viewModel.cargarReportes()

        viewModel.reportes.observe(this) { listaReportes ->
            listaReportesLocal = listaReportes
            pintarMarcadores(listaReportes)
            calcularZonasDeCalor()
        }
    }

    private fun inicializarDetectorToques() {
        mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
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
    }

    private fun setupMapa() {
        binding.mapview.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapview.setMultiTouchControls(true)
        binding.mapview.controller.setZoom(15.0) // Zoom más cercano por defecto

        // Coordenada por defecto (CDMX) por si falla el GPS
        val startPoint = GeoPoint(19.4326, -99.1332)
        binding.mapview.controller.setCenter(startPoint)

        // Listener para movimiento/zoom
        binding.mapview.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                calcularZonasDeCalor()
                return true
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                calcularZonasDeCalor()
                return true
            }
        })
    }

    private fun centrarEnUbicacionActual() {
        // Verificar permisos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return // Si no hay permiso, se queda en la coordenada por defecto (CDMX)
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val miUbicacion = GeoPoint(location.latitude, location.longitude)
                binding.mapview.controller.animateTo(miUbicacion)
                binding.mapview.controller.setZoom(16.0) // Acercar a mi ubicación
            }
        }
    }

    private fun pintarMarcadores(reportes: List<Reporte>) {
        // CORRECCIÓN IMPORTANTE:
        // 1. Limpiamos todo
        binding.mapview.overlays.clear()

        // 2. Inmediatamente RE-AGREGAMOS el detector de toques al fondo (índice 0)
        // Esto soluciona que "no se puedan cerrar" los detalles
        binding.mapview.overlays.add(0, mapEventsOverlay)

        for (repo in reportes) {
            if (repo.latitud != 0.0 && repo.longitud != 0.0) {
                val marker = Marker(binding.mapview)
                marker.position = GeoPoint(repo.latitud, repo.longitud)
                marker.relatedObject = repo
                marker.infoWindow = CustomInfoWindow(binding.mapview)
                marker.title = repo.categoria

                marker.setOnMarkerClickListener { m, map ->
                    m.showInfoWindow()
                    map.controller.animateTo(m.position)
                    true
                }

                binding.mapview.overlays.add(marker)
            }
        }
        binding.mapview.invalidate()
    }

    private fun calcularZonasDeCalor() {
        if (listaReportesLocal.isEmpty()) return

        // Eliminar solo los polígonos de zona de calor anteriores
        for (i in binding.mapview.overlays.size - 1 downTo 0) {
            val overlay = binding.mapview.overlays[i]
            if (overlay is Polygon && overlay.title == "Zona de Densidad") {
                binding.mapview.overlays.removeAt(i)
            }
        }

        val centro = binding.mapview.mapCenter as GeoPoint
        val radioMetros = 1000.0

        var contador = 0
        for (repo in listaReportesLocal) {
            val puntoRepo = GeoPoint(repo.latitud, repo.longitud)
            if (centro.distanceToAsDouble(puntoRepo) <= radioMetros) {
                contador++
            }
        }

        val colorZona = when {
            contador > 10 -> Color.parseColor("#40FF0000")
            contador > 5 -> Color.parseColor("#40FFFF00")
            else -> Color.parseColor("#4000FF00")
        }

        val circulo = Polygon()
        circulo.points = Polygon.pointsAsCircle(centro, radioMetros)
        circulo.fillPaint.color = colorZona
        circulo.outlinePaint.color = Color.TRANSPARENT
        circulo.title = "Zona de Densidad"

        // Agregar al índice 0 para que quede DETRÁS, pero después del detector de toques si es posible
        // Ojo: Si ponemos index 0, queda abajo de todo.
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