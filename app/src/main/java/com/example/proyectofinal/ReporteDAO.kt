package com.example.proyectofinal

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReporteDao {
    // Guardar reporte
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(reporte: Reporte)

    // Leer todos los reportes
    @Query("SELECT * FROM tabla_reportes ORDER BY fecha DESC")
    fun obtenerTodos(): LiveData<List<Reporte>>
}