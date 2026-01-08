package com.example.proyectofinal

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.UUID

class ReporteViewModel(application: Application) : AndroidViewModel(application) {

    // Inicializamos la BD local
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.reporteDao()
    private val context = application.applicationContext

    private val _estadoEnvio = MutableLiveData<EstadoResult>()
    val estadoEnvio: LiveData<EstadoResult> get() = _estadoEnvio

    fun enviarReporte(reporte: Reporte, imagenUri: Uri?) {
        _estadoEnvio.value = EstadoResult.Cargando

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val reporteId = UUID.randomUUID().toString()
                var stringBase64 = ""

                // Procesar Imagen
                if (imagenUri != null) {
                    try {
                        val inputStream = context.contentResolver.openInputStream(imagenUri)
                        val bitmapOriginal = BitmapFactory.decodeStream(inputStream)
                        stringBase64 = convertirBitmapABase64(bitmapOriginal)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Generar Jitter (Desplazamiento leve para no encimar marcadores)
                val jitterLat = (Math.random() - 0.5) * 0.0002
                val jitterLng = (Math.random() - 0.5) * 0.0002

                val latFinal = if(reporte.latitud != 0.0) reporte.latitud + jitterLat else 0.0
                val longFinal = if(reporte.longitud != 0.0) reporte.longitud + jitterLng else 0.0

                val reporteFinal = reporte.copy(
                    id = reporteId,
                    fotoBase64 = stringBase64,
                    latitud = latFinal,
                    longitud = longFinal
                )

                // GUARDAR EN ROOM (Local)
                dao.insertar(reporteFinal)

                // Responder a la UI
                _estadoEnvio.postValue(EstadoResult.Exito)

            } catch (e: Exception) {
                _estadoEnvio.postValue(EstadoResult.Error("Error al guardar localmente: ${e.message}"))
            }
        }
    }

    private fun convertirBitmapABase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    sealed class EstadoResult {
        object Cargando : EstadoResult()
        object Exito : EstadoResult()
        data class Error(val mensaje: String) : EstadoResult()
    }
}