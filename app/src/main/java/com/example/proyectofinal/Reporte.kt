package com.example.proyectofinal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_reportes")
data class Reporte(
    @PrimaryKey
    val id: String = "", // UUID generado
    val categoria: String = "",
    val descripcion: String = "",
    val infoAdicional: String = "", // Simplificamos el Map a un String directo
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fotoBase64: String = "", // Texto largo
    val alias: String = "Anónimo",
    val fecha: Long = System.currentTimeMillis()
)