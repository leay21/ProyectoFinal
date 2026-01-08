package com.example.proyectofinal

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class ReporteViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _estadoEnvio = MutableLiveData<EstadoResult>()
    val estadoEnvio: LiveData<EstadoResult> get() = _estadoEnvio

    fun enviarReporte(reporte: Reporte, imagenUri: Uri?) {
        _estadoEnvio.value = EstadoResult.Cargando

        val reporteId = UUID.randomUUID().toString()
        val reporteFinal = reporte.copy(id = reporteId)

        if (imagenUri != null) {
            // 1. Subir imagen primero
            val ref = storage.reference.child("evidencias/$reporteId.jpg")
            ref.putFile(imagenUri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        // 2. Guardar datos con URL de imagen
                        guardarEnFirestore(reporteFinal.copy(imagenUrl = uri.toString()))
                    }
                }
                .addOnFailureListener {
                    _estadoEnvio.value = EstadoResult.Error("Error al subir imagen")
                }
        } else {
            // Guardar sin imagen
            guardarEnFirestore(reporteFinal)
        }
    }

    private fun guardarEnFirestore(reporte: Reporte) {
        db.collection("reportes").document(reporte.id).set(reporte)
            .addOnSuccessListener {
                _estadoEnvio.value = EstadoResult.Exito
            }
            .addOnFailureListener {
                _estadoEnvio.value = EstadoResult.Error(it.message ?: "Error desconocido")
            }
    }

    sealed class EstadoResult {
        object Cargando : EstadoResult()
        object Exito : EstadoResult()
        data class Error(val mensaje: String) : EstadoResult()
    }
}