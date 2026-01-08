package com.example.proyectofinal

data class Reporte(
    var id: String = "",
    val categoria: String = "",
    val descripcion: String = "", // Para detalles del incidente
    val detallesExtra: Map<String, String> = emptyMap(), // Para campos específicos (ej. relación agresor, objetos robados)
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val imagenUrl: String = "",
    val alias: String = "Anónimo",
    val fecha: Long = System.currentTimeMillis()
)