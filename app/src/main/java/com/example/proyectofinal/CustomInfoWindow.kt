package com.example.proyectofinal

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomInfoWindow(mapView: MapView) : InfoWindow(R.layout.layout_custom_infowindow, mapView) {

    override fun onOpen(item: Any?) {
        // Obtenemos referencias a las vistas del layout XML
        val tvTitulo = mView.findViewById<TextView>(R.id.tvTituloBubble)
        val tvFecha = mView.findViewById<TextView>(R.id.tvFechaBubble)
        val tvDesc = mView.findViewById<TextView>(R.id.tvDescripcionBubble)
        val ivFoto = mView.findViewById<ImageView>(R.id.ivEvidenciaBubble)

        // Casteamos el item a Marker
        val marker = item as Marker

        // Recuperamos el objeto Reporte que guardamos en el marcador
        val reporte = marker.relatedObject as? Reporte

        if (reporte != null) {
            tvTitulo.text = "${reporte.categoria} (${reporte.alias})"
            tvDesc.text = reporte.descripcion

            // Formatear Fecha
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvFecha.text = "Fecha: ${sdf.format(Date(reporte.fecha))}"

            // Decodificar Imagen Base64 (si existe)
            if (reporte.fotoBase64.isNotEmpty()) {
                try {
                    val decodedString = Base64.decode(reporte.fotoBase64, Base64.DEFAULT)
                    val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    ivFoto.setImageBitmap(decodedByte)
                    ivFoto.visibility = View.VISIBLE
                } catch (e: Exception) {
                    ivFoto.visibility = View.GONE
                }
            } else {
                ivFoto.visibility = View.GONE
            }
        }
    }

    override fun onClose() {
        // No necesitamos limpiar nada específico
    }
}