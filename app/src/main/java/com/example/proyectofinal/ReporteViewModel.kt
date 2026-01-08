package com.example.proyectofinal

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.util.UUID

// Cambiamos a AndroidViewModel para tener acceso al "context" y procesar la imagen
class ReporteViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val context = application.applicationContext

    private val _estadoEnvio = MutableLiveData<EstadoResult>()
    val estadoEnvio: LiveData<EstadoResult> get() = _estadoEnvio

    fun enviarReporte(reporte: Reporte, imagenUri: Uri?) {
        _estadoEnvio.value = EstadoResult.Cargando

        val reporteId = UUID.randomUUID().toString()
        var stringBase64 = ""

        // Lógica de conversión de imagen
        if (imagenUri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(imagenUri)
                val bitmapOriginal = BitmapFactory.decodeStream(inputStream)

                // Comprimir imagen (Importante para no superar 1MB de Firestore)
                stringBase64 = convertirBitmapABase64(bitmapOriginal)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Creamos el reporte final con el ID y la imagen en texto
        val reporteFinal = reporte.copy(
            id = reporteId,
            fotoBase64 = stringBase64
        )

        // Guardar directamente en Firestore (sin Storage)
        db.collection("reportes").document(reporteId).set(reporteFinal)
            .addOnSuccessListener {
                _estadoEnvio.value = EstadoResult.Exito
            }
            .addOnFailureListener {
                _estadoEnvio.value = EstadoResult.Error(it.message ?: "Error desconocido")
            }
    }

    private fun convertirBitmapABase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Comprimimos a JPEG con calidad 50% para reducir tamaño
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