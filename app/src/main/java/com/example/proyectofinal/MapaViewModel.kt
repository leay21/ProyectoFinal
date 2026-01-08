package com.example.proyectofinal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class MapaViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // LiveData que contiene la lista de reportes
    private val _reportes = MutableLiveData<List<Reporte>>()
    val reportes: LiveData<List<Reporte>> get() = _reportes

    fun cargarReportes() {
        db.collection("reportes")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.toObjects(Reporte::class.java)
                _reportes.value = lista
            }
            .addOnFailureListener {
                // Manejar error si es necesario
            }
    }
}