package com.example.proyectofinal

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.databinding.ItemInstitucionBinding

class InstitucionAdapter(private var lista: List<Institucion>) :
    RecyclerView.Adapter<InstitucionAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemInstitucionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInstitucionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.binding.tvNombre.text = item.nombre
        holder.binding.tvCategoria.text = item.categoria
        holder.binding.tvDireccion.text = item.direccion

        // Asignamos el Horario (Si está vacío, ocultamos el campo para que se vea limpio)
        if (item.horario.isNotEmpty()) {
            holder.binding.tvHorario.text = "Horario: ${item.horario}"
            holder.binding.tvHorario.visibility = View.VISIBLE
        } else {
            holder.binding.tvHorario.visibility = View.GONE
        }

        holder.binding.btnLlamar.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${item.telefono}")
            holder.itemView.context.startActivity(intent)
        }

        holder.binding.btnWeb.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(item.urlWeb)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<Institucion>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}