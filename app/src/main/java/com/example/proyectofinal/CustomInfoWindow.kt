package com.example.proyectofinal

import android.graphics.Bitmap
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

// MODIFICACIÓN: Agregamos el parámetro onImageClick
class CustomInfoWindow(
    mapView: MapView,
    private val onImageClick: (Bitmap) -> Unit
) : InfoWindow(R.layout.layout_custom_infowindow, mapView) {

    override fun onOpen(item: Any?) {
        val tvTitulo = mView.findViewById<TextView>(R.id.tvTituloBubble)
        val tvFecha = mView.findViewById<TextView>(R.id.tvFechaBubble)
        val tvDesc = mView.findViewById<TextView>(R.id.tvDescripcionBubble)
        val ivFoto = mView.findViewById<ImageView>(R.id.ivEvidenciaBubble)

        val marker = item as Marker
        val reporte = marker.relatedObject as? Reporte

        if (reporte != null) {
            tvTitulo.text = "${reporte.categoria} (${reporte.alias})"
            tvDesc.text = reporte.descripcion
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvFecha.text = "Fecha: ${sdf.format(Date(reporte.fecha))}"

            if (reporte.fotoBase64.isNotEmpty()) {
                try {
                    val decodedString = Base64.decode(reporte.fotoBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

                    ivFoto.setImageBitmap(bitmap)
                    ivFoto.visibility = View.VISIBLE

                    // AQUÍ ESTÁ EL TRUCO:
                    // Asignamos el clic a la imagen y llamamos al callback
                    ivFoto.setOnClickListener {
                        onImageClick(bitmap)
                    }

                } catch (e: Exception) {
                    ivFoto.visibility = View.GONE
                }
            } else {
                ivFoto.visibility = View.GONE
            }
        }
    }

    override fun onClose() {
        // Nada
    }
}