package mx.edu.itesca.agendapeludacalendario

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActualizarMascotaActivity : AppCompatActivity() {

    private lateinit var nombreEditText: EditText
    private lateinit var especieSpinner: Spinner
    private lateinit var edadEditText: EditText
    private lateinit var pesoEditText: EditText
    private lateinit var generoRadioGroup: RadioGroup
    private lateinit var machoRadioButton: RadioButton
    private lateinit var hembraRadioButton: RadioButton
    private lateinit var updateButton: Button
    private lateinit var cancelButton: Button

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var mascotaId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actualizar_mascota_activty)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        nombreEditText = findViewById(R.id.nombre_mascota)
        especieSpinner = findViewById(R.id.tipo_mascota)
        edadEditText = findViewById(R.id.edad_mascota)
        pesoEditText = findViewById(R.id.peso_mascota)
        generoRadioGroup = findViewById(R.id.genero_mascota)
        machoRadioButton = findViewById(R.id.macho)
        hembraRadioButton = findViewById(R.id.hembra)
        updateButton = findViewById(R.id.updateButton)
        cancelButton = findViewById(R.id.cancelButton)

        mascotaId = intent.getStringExtra("mascota_id") ?: ""
        val nombre = intent.getStringExtra("nombre") ?: ""
        val especie = intent.getStringExtra("especie") ?: ""
        val edad = intent.getIntExtra("edad", 0)
        val peso = intent.getDoubleExtra("peso", 0.0)
        val genero = intent.getStringExtra("sexo") ?: ""

        nombreEditText.setText(nombre)
        edadEditText.setText(edad.toString())
        pesoEditText.setText(peso.toString())
        if (genero == "Macho") machoRadioButton.isChecked = true
        else if (genero == "Hembra") hembraRadioButton.isChecked = true

        val especies = resources.getStringArray(R.array.especies)
        if (especieSpinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, especies
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            especieSpinner.adapter = adapter
        }
        val indiceSeleccionado = especies.indexOf(especie)
        if (indiceSeleccionado >= 0) {
            especieSpinner.setSelection(indiceSeleccionado)
        } else {
            especieSpinner.setSelection(0)
        }

        updateButton.setOnClickListener {
            actualizarMascota()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun actualizarMascota() {
        val nombre = nombreEditText.text.toString()
        val especie = especieSpinner.selectedItem.toString()
        val edad = edadEditText.text.toString().toIntOrNull() ?: 0
        val peso = pesoEditText.text.toString().toDoubleOrNull() ?: 0.0
        val genero = if (machoRadioButton.isChecked) "Macho" else "Hembra"

        val mascotaActualizada = hashMapOf(
            "nombre" to nombre,
            "especie" to especie,
            "edad" to edad,
            "peso_kg" to peso,
            "sexo" to genero
        )

        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(userId)
            .collection("mascotas").document(mascotaId)
            .update(mascotaActualizada as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Mascota actualizada correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
    }
}