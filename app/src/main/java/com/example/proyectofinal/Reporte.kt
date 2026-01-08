package com.example.proyectofinal

data class Reporte(
    var id: String = "",
    val categoria: String = "",
    val descripcion: String = "",
    val detallesExtra: Map<String, String> = emptyMap(),
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fotoBase64: String = "", // CAMBIO: Ahora guardamos el texto de la imagen
    val alias: String = "Anónimo",
    val fecha: Long = System.currentTimeMillis()
)