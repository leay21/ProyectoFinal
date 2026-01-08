package com.example.proyectofinal

data class Institucion(
    val nombre: String,
    val categoria: String,
    val direccion: String,
    val telefono: String,
    val urlWeb: String,
    val horario: String // Nuevo campo
)