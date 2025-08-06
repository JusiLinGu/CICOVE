package com.example.cicove.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cicove.DB.Mascota
import com.example.cicove.R

class MascotaAdapter(
    private var mascotas: List<Mascota>,
    private val onItemClick: (Mascota) -> Unit
) : RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder>() {

    class MascotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMascota: ImageView = itemView.findViewById(R.id.imgMascota)
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvEspecie: TextView = itemView.findViewById(R.id.tvEspecie)
        val tvGenero: TextView = itemView.findViewById(R.id.tvGenero)
        val tvEdad: TextView = itemView.findViewById(R.id.tvEdad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mascota, parent, false)
        return MascotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MascotaViewHolder, position: Int) {
        val mascota = mascotas[position]
        holder.tvNombre.text = mascota.nombreMascota
        holder.tvEspecie.text = "Especie: ${mascota.especie}"
        holder.tvGenero.text = "Género: ${mascota.genero}"

        val edad = calcularEdadDesdeFecha(mascota.fechaNac)
        holder.tvEdad.text = "Edad: $edad años"

        //Mostrar la imagen de la mascota si existe
        if (!mascota.foto.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(mascota.foto)
                holder.imgMascota.setImageURI(uri)
            } catch (e: Exception) {
                holder.imgMascota.setImageResource(R.drawable.icon_foto) //imagen por defecto
            }
        } else {
            holder.imgMascota.setImageResource(R.drawable.icon_foto) //imagen por defecto
        }

        holder.itemView.setOnClickListener {
            onItemClick(mascota)
        }

        holder.itemView.setOnClickListener {
            onItemClick(mascota)
        }
    }

    override fun getItemCount(): Int = mascotas.size

    private fun calcularEdadDesdeFecha(fechaNac: String): Int {
        try {
            val partes = fechaNac.split("-")
            if (partes.size == 3) {
                val anio = partes[0].toInt()
                val anioActual = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                return anioActual - anio
            }
        } catch (_: Exception) { }
        return 0
    }

    fun actualizarLista(nuevaLista: List<Mascota>) {
        mascotas = nuevaLista
        notifyDataSetChanged()
    }
}
