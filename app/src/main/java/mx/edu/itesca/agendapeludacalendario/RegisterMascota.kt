package mx.edu.itesca.agendapeludacalendario

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterMascota : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_mascota)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nombreMascota = findViewById<EditText>(R.id.nombre_mascota)
        val edadMascota = findViewById<EditText>(R.id.edad_mascota)
        val pesoMascota = findViewById<EditText>(R.id.peso_mascota)
        val tipoMascota = findViewById<Spinner>(R.id.tipo_mascota)
        val generoMascota = findViewById<RadioGroup>(R.id.genero_mascota)

        val especies = resources.getStringArray(R.array.especies)
        if (tipoMascota != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, especies
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            tipoMascota.adapter = adapter
        }

        tipoMascota.setSelection(0)

        val addButton = findViewById<Button>(R.id.addButton)
        addButton.setOnClickListener {
            val nombre = nombreMascota.text.toString().trim()
            val edad = edadMascota.text.toString().toIntOrNull() ?: 0
            val peso = pesoMascota.text.toString().toDoubleOrNull() ?: 0.0
            val especie = tipoMascota.selectedItem.toString()
            val sexo = when (generoMascota.checkedRadioButtonId) {
                R.id.macho -> "Macho"
                R.id.hembra -> "Hembra"
                else -> ""
            }

            val mascota = Mascota(
                nombre = nombre,
                especie = especie,
                edad = edad,
                sexo = sexo,
                peso_kg = peso
            )

            val userId = auth.currentUser?.uid
            if (userId != null) {
                val mascotaRef = db.collection("usuarios")
                    .document(userId)
                    .collection("mascotas")
                    .document()

                mascota.id = mascotaRef.id

                mascotaRef.set(mascota)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Mascota registrada", Toast.LENGTH_SHORT).show()
                        finish() // Cierra la pantalla de registro
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al registrar", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            }
        }

        val cancelButton = findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            finish()
        }
    }

}