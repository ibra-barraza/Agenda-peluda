package mx.edu.itesca.agendapeludacalendario


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class MascotasAdap(
    var ListaMascotas: List<RegisterMascota>,
    var onBorrarClic: (String) -> Unit,
    var onActualizarClic: (RegisterMascota) -> Unit
) : RecyclerView.Adapter<MascotasAdap.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ListNombre: CardView = itemView.findViewById(R.id.ListNombre)
        val ListTipo: TextView = itemView.findViewById(R.id.ListTipo)
        val ListEdad: TextView = itemView.findViewById(R.id.LisEdad)
        val ibtnBorrar: ImageButton = itemView.findViewById(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listamascotas, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return ListaMascotas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tarea = ListaMascotas[position]

        holder.ListNombre.text = Mascotas.nombre
        holder.ListTipo.text = Mascotas.descripcion

        holder.ibtnBorrar.setOnClickListener {
            onBorrarClic(Mascotas.id)
        }

        holder.ListNombre.setOnClickListener {
            onActualizarClic(tarea)
        }
    }
}