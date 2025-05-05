package mx.edu.itesca.agendapeludacalendario

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class CalendarActivity : AppCompatActivity() {
    private lateinit var calendarView: CalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ActividadAdapter
    private val actividadList = mutableListOf<ActividadItem>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var fechaSeleccionada: String = ""

    override fun onResume() {
        super.onResume()
        fechaSeleccionada = obtenerFechaFormateada(calendarView.date)
        obtenerActividades(fechaSeleccionada)
    }

    @SuppressLint("MissingInflatedId", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calendar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Botón de cerrar sesión
        val logoutButton: Button = findViewById(R.id.btnLogout)
        logoutButton.setOnClickListener {
            // Cierra sesión de Firebase
            Firebase.auth.signOut()

            // Cierra sesión de Google
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut().addOnCompleteListener {
                // Revoca acceso para forzar selección de cuenta
                googleSignInClient.revokeAccess()

                // Redirige al MainActivity
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
        }

        calendarView = findViewById(R.id.calendarView)
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            fechaSeleccionada = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
            obtenerActividades(fechaSeleccionada)
        }

        recyclerView = findViewById(R.id.rvActividades)

        adapter = ActividadAdapter(actividadList,
            onDeleteClick = { actividad -> eliminarActividad(actividad, fechaSeleccionada) },
            onEditClick = { actividad -> irActualizarActividad(actividad) }
        )

        recyclerView.adapter = adapter

        fechaSeleccionada = obtenerFechaFormateada(calendarView.date)
        obtenerActividades(fechaSeleccionada)

        val buttonActividad: Button = findViewById(R.id.addButton)
        buttonActividad.setOnClickListener {
            val fechaSeleccionada = calendarView.date

            val intent = Intent(this, activity_add_event::class.java)
            intent.putExtra("fechaSeleccionada", fechaSeleccionada)
            startActivity(intent)
        }

        val buttonMascotas: Button = findViewById(R.id.viewMascotasButton)
        buttonMascotas.setOnClickListener {
            startActivity(Intent(this, MascotasActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        // Verificar si el usuario está autenticado
        if (auth.currentUser == null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun obtenerActividades(fecha: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("usuarios")
            .document(userId)
            .collection("actividades")
            .whereEqualTo("fecha", fecha)
            .get()
            .addOnSuccessListener { documents ->
                actividadList.clear()
                for (doc in documents) {
                    try {
                        val actividad = doc.toObject(ActividadItem::class.java)
                        actividad.id = doc.id
                        actividadList.add(actividad)
                    } catch (e: Exception) {
                        Log.e("CalendarActivity", "${fecha} Error parsing document: ${e.message}")
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarActividad(actividad: ActividadItem, fecha: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("usuarios")
            .document(userId)
            .collection("actividades")
            .document(actividad.id!!)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Actividad eliminada", Toast.LENGTH_SHORT).show()
                obtenerActividades(fecha)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun irActualizarActividad(actividad: ActividadItem) {
        val intent = Intent(this, ActualizarActividad::class.java)
        intent.putExtra("actividadId", actividad.id)
        intent.putExtra("titulo", actividad.titulo)
        intent.putExtra("tipo", actividad.actividad)
        intent.putExtra("horaInicio", actividad.horaInicio)
        intent.putExtra("horaFinal", actividad.horaFinal)
        intent.putExtra("notas", actividad.notas)
        startActivity(intent)
    }

    private fun obtenerFechaFormateada(timeInMillis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format("%02d/%02d/%04d", day, month, year)
    }

}
