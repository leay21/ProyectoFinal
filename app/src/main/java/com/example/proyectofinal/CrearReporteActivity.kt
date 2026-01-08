package com.example.proyectofinal
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.example.proyectofinal.Reporte.databinding.ActivityCrearReporteBinding // Asegúrate de activar ViewBinding


class CrearReporteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearReporteBinding
    private val viewModel: ReporteViewModel by viewModels()

    private var imagenUriSeleccionada: Uri? = null
    private var latitudActual: Double = 0.0
    private var longitudActual: Double = 0.0

    // Categorías obligatorias
    private val categorias = listOf(
        "Servicios Públicos", "Robo o Asalto", "Corrupción u Omisión",
        "Violencia de Género", "Narcomenudeo", "Reporte General"
    )

    // Launcher para obtener imagen
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imagenUriSeleccionada = it
            binding.ivPreview.setImageURI(it)
            binding.ivPreview.visibility = android.view.View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearReporteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner()
        setupBotones()
        observarViewModel()
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categorias)
        binding.spinnerCategoria.adapter = adapter
    }

    private fun setupBotones() {
        binding.btnFoto.setOnClickListener {
            getContent.launch("image/*") // Abre galería
        }

        binding.btnObtenerUbicacion.setOnClickListener {
            obtenerUbicacion()
        }

        binding.btnEnviar.setOnClickListener {
            enviarDatos()
        }
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
                binding.tvUbicacion.text = "Ubicación: $latitudActual, $longitudActual"
            } else {
                Toast.makeText(this, "Enciende el GPS e intenta de nuevo", Toast.LENGTH_SHORT).show()
            }
        }
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
        binding.progressBar.visibility = android.view.View.VISIBLE
        viewModel.enviarReporte(nuevoReporte, imagenUriSeleccionada)
    }

    private fun observarViewModel() {
        viewModel.estadoEnvio.observe(this) { estado ->
            when (estado) {
                is ReporteViewModel.EstadoResult.Exito -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, "Reporte enviado correctamente", Toast.LENGTH_LONG).show()
                    finish() // Cierra la actividad
                }
                is ReporteViewModel.EstadoResult.Error -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnEnviar.isEnabled = true
                    Toast.makeText(this, "Error: ${estado.mensaje}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }
}