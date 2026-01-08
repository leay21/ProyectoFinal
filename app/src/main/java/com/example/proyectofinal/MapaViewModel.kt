package com.example.proyectofinal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class MapaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    // Room retorna LiveData directo, se actualiza solo si hay cambios
    val reportes: LiveData<List<Reporte>> = db.reporteDao().obtenerTodos()

    fun cargarReportes() {
        // En Room con LiveData no es necesario llamar esto manualmente,
        // pero lo dejamos vacío por si tu Activity lo llama.
    }
}