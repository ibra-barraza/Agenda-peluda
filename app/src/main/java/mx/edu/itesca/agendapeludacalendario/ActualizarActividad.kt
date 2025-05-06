package mx.edu.itesca.agendapeludacalendario

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

class ActualizarActividad : AppCompatActivity() {

    private lateinit var actividadId: String

    private lateinit var updateButton: Button
    private lateinit var cancelButton: Button

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actualizar_actividad)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser?.uid

        val tituloActividad = findViewById<EditText>(R.id.tituloActividad)
        val tipoActividad = findViewById<Spinner>(R.id.tipo_actividad)
        val horaInicioEditText = findViewById<EditText>(R.id.horaInicio)
        val horaFinalEditText = findViewById<EditText>(R.id.horaFinal)
        val mascotasContainer = findViewById<LinearLayout>(R.id.mascotasContainer)
        val notasEditText = findViewById<EditText>(R.id.notas)
        updateButton = findViewById(R.id.btnUpdateEvent)
        cancelButton = findViewById(R.id.btnCancelEvent)

        actividadId = intent.getStringExtra("actividadId") ?: ""
        val titulo = intent.getStringExtra("titulo") ?: ""
        val tipo = intent.getStringExtra("tipo") ?: ""
        val horaInicio = intent.getStringExtra("horaInicio") ?: ""
        val horaFinal = intent.getStringExtra("horaFinal") ?: ""
        val notas = intent.getStringExtra("notas") ?: ""

        tituloActividad.setText(titulo)
        horaInicioEditText.setText(horaInicio)
        horaFinalEditText.setText(horaFinal)
        notasEditText.setText(notas)

        if (userId != null) {
            db.collection("usuarios")
                .document(userId)
                .collection("mascotas")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val nombreMascota = document.getString("nombre") ?: continue
                        val checkBox = CheckBox(this).apply {
                            text = nombreMascota
                            setTextColor(resources.getColor(android.R.color.black))
                            textSize = 16f
                        }
                        mascotasContainer.addView(checkBox)
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error al cargar mascotas", Toast.LENGTH_SHORT).show()
                }
        }

        val seleccionActividades = resources.getStringArray(R.array.actividades)
        if (tipoActividad != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, seleccionActividades
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            tipoActividad.adapter = adapter
        }
        val indiceSeleccionado = seleccionActividades.indexOf(tipo)
        if (indiceSeleccionado >= 0) {
            tipoActividad.setSelection(indiceSeleccionado)
        } else {
            tipoActividad.setSelection(0)
        }

        horaInicioEditText.setOnClickListener {
            mostrarTimePicker(horaInicioEditText)
        }

        horaFinalEditText.setOnClickListener {
            mostrarTimePicker(horaFinalEditText)
        }

        updateButton.setOnClickListener {
            actualizarActividad()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun actualizarActividad() {
        val userId = auth.currentUser?.uid ?: return

        val tituloActividad = findViewById<EditText>(R.id.tituloActividad).text.toString()
        val tipoActividad = findViewById<Spinner>(R.id.tipo_actividad).selectedItem.toString()
        val horaInicioEditText = findViewById<EditText>(R.id.horaInicio).text.toString()
        val horaFinalEditText = findViewById<EditText>(R.id.horaFinal).text.toString()
        val mascotasContainer = findViewById<LinearLayout>(R.id.mascotasContainer)
        val notasEditText = findViewById<EditText>(R.id.notas).text.toString()

        val mascotasSeleccionadas = mutableListOf<String>()
        for (i in 0 until mascotasContainer.childCount) {
            val view = mascotasContainer.getChildAt(i)
            if (view is CheckBox && view.isChecked) {
                mascotasSeleccionadas.add(view.text.toString())
            }
        }

        val actividadActualizada = hashMapOf(
            "titulo" to tituloActividad,
            "actividad" to tipoActividad,
            "horaInicio" to horaInicioEditText,
            "horaFinal" to horaFinalEditText,
            "nombresMascotas" to mascotasSeleccionadas,
            "notas" to notasEditText,
        )

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupModo)
        val checkedId = radioGroup.checkedRadioButtonId
        val currentFecha = intent.getStringExtra("fecha") ?: ""
        val currentCreatedAt = intent.getLongExtra("createdAt", 0)

        when (checkedId) {
            R.id.rbIndividual -> {
                db.collection("usuarios").document(userId)
                    .collection("actividades").document(actividadId)
                    .update(actividadActualizada as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Actividad actualizada correctamente", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
            }

            R.id.rbSiguientes -> {
                db.collection("usuarios").document(userId)
                    .collection("actividades")
                    .whereEqualTo("createdAt", currentCreatedAt)
                    .get()
                    .addOnSuccessListener { documents ->
                        val batch = db.batch()
                        for (doc in documents) {
                            val fechaActividad = doc.getString("fecha") ?: continue
                            if (esFechaMayor(fechaActividad, currentFecha)) {
                                val ref = doc.reference
                                batch.update(ref, actividadActualizada as Map<String, Any>)
                            }
                        }
                        batch.commit().addOnSuccessListener {
                            Toast.makeText(this, "Actividades siguientes actualizadas", Toast.LENGTH_SHORT).show()
                            finish()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                        }
                    }
            }

            R.id.rbTodas -> {
                db.collection("usuarios").document(userId)
                    .collection("actividades")
                    .whereEqualTo("createdAt", currentCreatedAt)
                    .get()
                    .addOnSuccessListener { documents ->
                        val batch = db.batch()
                        for (doc in documents) {
                            val ref = doc.reference
                            batch.update(ref, actividadActualizada as Map<String, Any>)
                        }
                        batch.commit().addOnSuccessListener {
                            Toast.makeText(this, "Todas las actividades actualizadas", Toast.LENGTH_SHORT).show()
                            finish()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun mostrarTimePicker(editText: EditText) {
        val calendario = Calendar.getInstance()
        val hora = calendario.get(Calendar.HOUR_OF_DAY)
        val minuto = calendario.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(this, { _, hourOfDay, minute ->
            val horaFormateada = String.format("%02d:%02d", hourOfDay, minute)
            editText.setText(horaFormateada)
        }, hora, minuto, true)

        timePicker.show()
    }

    private fun esFechaMayor(fecha1: String, fecha2: String): Boolean {
        val formato = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val date1 = formato.parse(fecha1)
            val date2 = formato.parse(fecha2)
            date1.after(date2) || date1.equals(date2)
        } catch (e: Exception) {
            Log.e("FechaParseError", "Error: ${e.message}")
            false
        }
    }
}