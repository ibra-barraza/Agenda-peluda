package mx.edu.itesca.agendapeludacalendario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ActividadAdapter(
    private val actividades: List<ActividadItem>,
    private val onDeleteClick: (ActividadItem) -> Unit,
    private val onEditClick: (ActividadItem) -> Unit
) : RecyclerView.Adapter<ActividadAdapter.ActividadViewHolder>() {

    class ActividadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTitulo: TextView = itemView.findViewById(R.id.titulo)
        val txtTiempo: TextView = itemView.findViewById(R.id.tiempo)
        val txtTipoAct: TextView = itemView.findViewById(R.id.tipoActividad)
        val txtNotas: TextView = itemView.findViewById(R.id.notasActividad)
        val btnEliminar: Button = itemView.findViewById(R.id.removeButton)
        val btnActualizar: Button = itemView.findViewById(R.id.editButton)
        val listaMascotas: LinearLayout = itemView.findViewById(R.id.listaMascotas)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActividadViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_calendar_item, parent, false)
        return ActividadViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActividadViewHolder, position: Int) {
        val actividad = actividades[position]
        holder.txtTitulo.text = actividad.titulo
        holder.txtTiempo.text = "${actividad.horaInicio} a ${actividad.horaFinal}"
        holder.txtTipoAct.text = actividad.actividad
        holder.txtNotas.text = actividad.notas

        holder.listaMascotas.removeAllViews()

        for (mascota in actividad.nombresMascotas!!) {
            val textView = TextView(holder.listaMascotas.context).apply {
                text = mascota
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            holder.listaMascotas.addView(textView)
        }

        holder.btnEliminar.setOnClickListener { onDeleteClick(actividad) }
        holder.btnActualizar.setOnClickListener { onEditClick(actividad) }
    }

    override fun getItemCount(): Int = actividades.size
}
