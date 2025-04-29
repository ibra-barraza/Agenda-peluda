package mx.edu.itesca.agendapeludacalendario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MascotaAdapter(
    private val mascotas: List<Mascota>,
    private val onDeleteClick: (Mascota) -> Unit,
    private val onEditClick: (Mascota) -> Unit
) : RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder>() {

    class MascotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvDetalles: TextView = itemView.findViewById(R.id.tvDetalles)
        val btnEliminar: Button = itemView.findViewById(R.id.btnEliminar)
        val btnActualizar: Button = itemView.findViewById(R.id.btnActualizar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rv_mascota, parent, false)
        return MascotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MascotaViewHolder, position: Int) {
        val mascota = mascotas[position]
        holder.tvNombre.text = mascota.nombre
        holder.tvDetalles.text = "Especie: ${mascota.especie} \nEdad: ${mascota.edad} años | Peso: ${mascota.peso_kg} kg \nSexo: ${mascota.sexo}"

        holder.btnEliminar.setOnClickListener { onDeleteClick(mascota) }
        holder.btnActualizar.setOnClickListener { onEditClick(mascota) }
    }

    override fun getItemCount(): Int = mascotas.size
}
