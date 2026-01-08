package com.example.proyectofinal

data class Institucion(
    val nombre: String,
    val categoria: String, // Ej: "Seguridad", "Salud", "Servicios"
    val direccion: String,
    val telefono: String,
    val urlWeb: String
)
