package com.example.proyectofinal

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.proyectofinal.databinding.ActivityCrearReporteBinding
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class CrearReporteActivity : BaseActivity() {

    private lateinit var binding: ActivityCrearReporteBinding
    private val viewModel: ReporteViewModel by viewModels()

    private var imagenUriSeleccionada: Uri? = null
    private var latitudActual: Double = 0.0
    private var longitudActual: Double = 0.0

    // Marcador visual para el mapa pequeño
    private var markerUbicacion: Marker? = null

    // Categorías obligatorias
    private val categorias = listOf(
        "Servicios Públicos", "Robo o Asalto", "Corrupción u Omisión",
        "Violencia de Género", "Narcomenudeo", "Reporte General"
    )

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imagenUriSeleccionada = it
            binding.ivPreview.setImageURI(it)
            binding.ivPreview.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración OSM
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid_prefs", MODE_PRIVATE))

        binding = ActivityCrearReporteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- LÓGICA DE IMAGEN FULL SCREEN ---
        binding.ivPreview.setOnClickListener {
            // Solo abrir si hay imagen
            if (binding.ivPreview.drawable != null) {
                binding.ivFullPreview.setImageDrawable(binding.ivPreview.drawable)
                binding.overlayLayout.visibility = View.VISIBLE
            }
        }

        binding.overlayLayout.setOnClickListener {
            binding.overlayLayout.visibility = View.GONE
        }
        // ------------------------------------

        setupSpinner()
        setupBotones()
        setupMapaSeleccion()
        centrarMapaEnUbicacionActual()
        observarViewModel()
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categorias)
        binding.spinnerCategoria.adapter = adapter

        binding.spinnerCategoria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                actualizarFormulario(categorias[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun actualizarFormulario(categoria: String) {
        binding.etDetalleExtra.text.clear()
        when (categoria) {
            "Servicios Públicos" -> {
                binding.etDetalleExtra.visibility = View.VISIBLE
                binding.etDetalleExtra.hint = "Especifique: Baches, Luminarias o Fuga de agua"
            }
            "Robo o Asalto" -> {
                binding.etDetalleExtra.visibility = View.VISIBLE
                binding.etDetalleExtra.hint = "¿Qué objetos fueron sustraídos?"
            }
            "Corrupción u Omisión" -> {
                binding.etDetalleExtra.visibility = View.VISIBLE
                binding.etDetalleExtra.hint = "Nombre de la dependencia o servidor público"
            }
            "Violencia de Género" -> {
                binding.etDetalleExtra.visibility = View.VISIBLE
                binding.etDetalleExtra.hint = "Relación con el agresor"
            }
            "Narcomenudeo" -> {
                binding.etDetalleExtra.visibility = View.VISIBLE
                binding.etDetalleExtra.hint = "Descripción de personas o vehículos"
            }
            "Reporte General" -> {
                binding.etDetalleExtra.visibility = View.VISIBLE
                binding.etDetalleExtra.hint = "Información adicional relevante"
            }
        }
    }

    private fun setupMapaSeleccion() {
        binding.mapPickLocation.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapPickLocation.setMultiTouchControls(true)
        binding.mapPickLocation.controller.setZoom(15.0)
        // Centro default (CDMX)
        binding.mapPickLocation.controller.setCenter(GeoPoint(19.4326, -99.1332))

        val receiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                latitudActual = p.latitude
                longitudActual = p.longitude
                actualizarTextoUbicacion()
                colocarMarcadorVisual(p)
                return true
            }
            override fun longPressHelper(p: GeoPoint?): Boolean = false
        }

        binding.mapPickLocation.overlays.add(MapEventsOverlay(receiver))
    }

    // NUEVA FUNCIÓN: Centra el mapa automáticamente al inicio
    private fun centrarMapaEnUbicacionActual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val punto = GeoPoint(location.latitude, location.longitude)
                    binding.mapPickLocation.controller.animateTo(punto)
                    binding.mapPickLocation.controller.setZoom(16.0)
                }
            }
        }
    }

    private fun setupBotones() {
        binding.btnFoto.setOnClickListener { getContent.launch("image/*") }

        binding.btnObtenerUbicacion.setOnClickListener { obtenerUbicacion() }

        binding.btnEnviar.setOnClickListener { enviarDatos() }
    }

    private fun obtenerUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                latitudActual = location.latitude
                longitudActual = location.longitude

                // Actualizamos texto y mapa visualmente
                actualizarTextoUbicacion()
                val punto = GeoPoint(latitudActual, longitudActual)
                colocarMarcadorVisual(punto)

                // Movemos el mapa ahí también
                binding.mapPickLocation.controller.animateTo(punto)
                binding.mapPickLocation.controller.setZoom(17.0)

            } else {
                Toast.makeText(this, "Enciende el GPS e intenta de nuevo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun colocarMarcadorVisual(p: GeoPoint) {
        if (markerUbicacion != null) {
            binding.mapPickLocation.overlays.remove(markerUbicacion)
        }
        markerUbicacion = Marker(binding.mapPickLocation)
        markerUbicacion?.position = p
        markerUbicacion?.title = "Ubicación Seleccionada"
        markerUbicacion?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        binding.mapPickLocation.overlays.add(markerUbicacion)
        binding.mapPickLocation.invalidate()
    }

    private fun actualizarTextoUbicacion() {
        val lat = String.format("%.5f", latitudActual)
        val lon = String.format("%.5f", longitudActual)
        binding.tvUbicacion.text = "Ubicación: $lat, $lon"
    }

    private fun enviarDatos() {
        val categoria = binding.spinnerCategoria.selectedItem.toString()
        val descripcion = binding.etDescripcion.text.toString()
        val extra = binding.etDetalleExtra.text.toString()
        val alias = binding.etAlias.text.toString().ifEmpty { "Anónimo" }

        if (descripcion.isEmpty() || latitudActual == 0.0) {
            Toast.makeText(this, "Descripción y Ubicación son obligatorias", Toast.LENGTH_SHORT).show()
            return
        }

        val detallesMap = mapOf("info_adicional" to extra)

        val nuevoReporte = Reporte(
            categoria = categoria,
            descripcion = descripcion,
            detallesExtra = detallesMap,
            latitud = latitudActual,
            longitud = longitudActual,
            alias = alias
        )

        binding.btnEnviar.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        viewModel.enviarReporte(nuevoReporte, imagenUriSeleccionada)
    }

    private fun observarViewModel() {
        viewModel.estadoEnvio.observe(this) { estado ->
            when (estado) {
                is ReporteViewModel.EstadoResult.Exito -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Reporte enviado correctamente", Toast.LENGTH_LONG).show()
                    finish()
                }
                is ReporteViewModel.EstadoResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnEnviar.isEnabled = true
                    Toast.makeText(this, "Error: ${estado.mensaje}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }
}