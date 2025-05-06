package mx.edu.itesca.agendapeludacalendario

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class DeleteActividad : AppCompatActivity() {

    private lateinit var btnEliminar: Button
    private lateinit var btnCancelar: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_actividad)

        btnEliminar = findViewById(R.id.btnDoDeletion)
        btnCancelar = findViewById(R.id.btnCancelDeletion)

        val actividadId = intent.getStringExtra("actividadId") ?: ""
        val currentFecha = intent.getStringExtra("fecha") ?: ""
        val currentCreatedAt = intent.getLongExtra("createdAt", 0)

        val userId = auth.currentUser?.uid ?: return

        btnCancelar.setOnClickListener {
            finish()
        }

        btnEliminar.setOnClickListener {
            val radioGroup = findViewById<RadioGroup>(R.id.radioGroupModoDel)
            val checkedId = radioGroup.checkedRadioButtonId

            when (checkedId) {
                R.id.rbDelIndividual -> {
                    db.collection("usuarios").document(userId)
                        .collection("actividades").document(actividadId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Actividad eliminada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                        }
                }

                R.id.rbDelSiguientes -> {
                    db.collection("usuarios").document(userId)
                        .collection("actividades")
                        .whereEqualTo("createdAt", currentCreatedAt)
                        .get()
                        .addOnSuccessListener { documents ->
                            val batch = db.batch()
                            for (doc in documents) {
                                val fechaActividad = doc.getString("fecha") ?: continue
                                if (esFechaMayor(fechaActividad, currentFecha)) {
                                    batch.delete(doc.reference)
                                }
                            }
                            batch.commit().addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Actividades siguientes eliminadas",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }.addOnFailureListener {
                                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                            }
                        }
                }

                R.id.rbDelTodas -> {
                    db.collection("usuarios").document(userId)
                        .collection("actividades")
                        .whereEqualTo("createdAt", currentCreatedAt)
                        .get()
                        .addOnSuccessListener { documents ->
                            val batch = db.batch()
                            for (doc in documents) {
                                batch.delete(doc.reference)
                            }
                            batch.commit().addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Todas las actividades eliminadas",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }.addOnFailureListener {
                                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
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