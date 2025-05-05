package mx.edu.itesca.agendapeludacalendario

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class activity_add_event : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_event)

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val mascotasContainer = findViewById<LinearLayout>(R.id.mascotasContainer)

        val userId = auth.currentUser?.uid

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

        val tipoActividad = findViewById<Spinner>(R.id.tipo_actividad)
        val seleccionActividades = resources.getStringArray(R.array.actividades)
        if (tipoActividad != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, seleccionActividades
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            tipoActividad.adapter = adapter
        }
        tipoActividad.setSelection(0)

        val horaInicioEditText: EditText = findViewById(R.id.horaInicio)
        val horaFinalEditText: EditText = findViewById(R.id.horaFinal)

        horaInicioEditText.setOnClickListener {
            mostrarTimePicker(horaInicioEditText)
        }

        horaFinalEditText.setOnClickListener {
            mostrarTimePicker(horaFinalEditText)
        }

        val spinnerRepeticion = findViewById<Spinner>(R.id.spinner_repeticion)
        val opcionesRepeticion = listOf("No repetir", "Diaria", "Semanal", "Mensual")
        val adapterRepeticion = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcionesRepeticion)
        adapterRepeticion.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRepeticion.adapter = adapterRepeticion

        val buttonCancelEvent: Button = findViewById(R.id.btnCancelEvent)
        buttonCancelEvent.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        val buttonAddEvent: Button = findViewById(R.id.btnAddEvent)
        buttonAddEvent.setOnClickListener {
            val tituloActividad = findViewById<EditText>(R.id.tituloActividad)
            val tipoActividad = findViewById<Spinner>(R.id.tipo_actividad)
            val horaInicioEditText = findViewById<EditText>(R.id.horaInicio)
            val horaFinalEditText = findViewById<EditText>(R.id.horaFinal)
            val spinnerRepeticion = findViewById<Spinner>(R.id.spinner_repeticion)
            val mascotasContainer = findViewById<LinearLayout>(R.id.mascotasContainer)
            val notasEditText = findViewById<EditText>(R.id.notas)

            val titulo = tituloActividad.text.toString()
            val actividad = tipoActividad.selectedItem.toString()
            val horaInicio = horaInicioEditText.text.toString()
            val horaFinal = horaFinalEditText.text.toString()
            val repeticion = spinnerRepeticion.selectedItem.toString()
            val notas = notasEditText.text.toString()

            val userId = auth.currentUser?.uid

            if (userId == null) {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Recolectar mascotas seleccionadas
            val mascotasSeleccionadas = mutableListOf<String>()
            for (i in 0 until mascotasContainer.childCount) {
                val view = mascotasContainer.getChildAt(i)
                if (view is CheckBox && view.isChecked) {
                    mascotasSeleccionadas.add(view.text.toString())
                }
            }

            if (mascotasSeleccionadas.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos una mascota", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fechaMillis = intent.getLongExtra("fechaSeleccionada", System.currentTimeMillis())
            val fechaInicial = Date(fechaMillis)
            val fechas = if (repeticion == "No repetir") {
                listOf(fechaInicial)
            } else {
                generarFechasRepetidas(fechaInicial, repeticion, 10)
            }

            for (fecha in fechas) {
                val actividadToDo = ActividadItem(
                    titulo = titulo,
                    actividad = actividad,
                    fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fecha),
                    horaInicio = horaInicio,
                    horaFinal = horaFinal,
                    notas = notas,
                    nombresMascotas = mascotasSeleccionadas
                )

                val actividadRef = db.collection("usuarios")
                    .document(userId)
                    .collection("actividades")
                    .document()

                actividadToDo.id = actividadRef.id

                actividadRef.set(actividadToDo)
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al registrar", Toast.LENGTH_SHORT).show()
                    }
            }

            Toast.makeText(this, "Actividades guardadas", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, CalendarActivity::class.java))
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

    fun generarFechasRepetidas(fechaInicial: Date, tipoRepeticion: String, cantidad: Int): List<Date> {
        val calendario = Calendar.getInstance()
        calendario.time = fechaInicial
        val fechas = mutableListOf<Date>()

        repeat(cantidad) {
            fechas.add(calendario.time)

            when (tipoRepeticion) {
                "Diaria" -> calendario.add(Calendar.DAY_OF_YEAR, 1)
                "Semanal" -> calendario.add(Calendar.WEEK_OF_YEAR, 1)
                "Mensual" -> calendario.add(Calendar.MONTH, 1)
            }
        }

        return fechas
    }
}