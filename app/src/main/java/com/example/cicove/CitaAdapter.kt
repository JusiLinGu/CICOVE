package com.example.cicove.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.cicove.DB.CitaConMascota
import com.example.cicove.R

class CitaAdapter(
    private val listaCitas: List<CitaConMascota>,
    private val onItemClick: (CitaConMascota) -> Unit
) : RecyclerView.Adapter<CitaAdapter.CitaViewHolder>() {

    class CitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFotoMascota: ImageView = itemView.findViewById(R.id.imgFotoMascota)
        val tvNombreMascota: TextView = itemView.findViewById(R.id.tvNombreMascota)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvHora: TextView = itemView.findViewById(R.id.tvHora)
        val tvMotivo: TextView = itemView.findViewById(R.id.tvMotivo)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
        val tvObservaciones: TextView = itemView.findViewById(R.id.tvObservaciones)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = listaCitas[position]

        holder.imgFotoMascota.setImageResource(R.drawable.icon_foto)
        holder.tvNombreMascota.text = cita.nombreMascota
        holder.tvFecha.text = "Fecha: ${cita.fecha}"
        holder.tvHora.text = "Hora: ${cita.hora}"
        holder.tvMotivo.text = "Motivo: ${cita.motivoConsulta}"
        holder.tvEstado.text = "Estado: ${cita.estadoCita}"
        holder.tvObservaciones.text = "Observaciones: ${cita.observaciones ?: "Sin observaciones"}"

        if (!cita.foto.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(cita.foto)
                holder.imgFotoMascota.setImageURI(uri)
            } catch (e: Exception) {
                holder.imgFotoMascota.setImageResource(R.drawable.icon_foto)
            }
        } else {
            holder.imgFotoMascota.setImageResource(R.drawable.icon_foto)
        }

        holder.itemView.setOnClickListener {
            if (cita.estadoCita == "Pendiente") {
                onItemClick(cita)
            } else {
                // Toast.makeText(holder.itemView.context, "Solo puedes seleccionar citas pendientes", Toast.LENGTH_SHORT).show()
            }
            onItemClick(cita)
        }
    }

    override fun getItemCount(): Int = listaCitas.size
}

