package mx.edu.itesca.agendapeludacalendario

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class CalendarActivity : AppCompatActivity() {
    private lateinit var adapter: ArrayAdapter<String>
    private val activities = mutableListOf<String>()
    private lateinit var listView: ListView
    private lateinit var calendarView: CalendarView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calendar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

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

        val buttonActividad: Button = findViewById(R.id.addButton)
        buttonActividad.setOnClickListener {
            startActivity(Intent(this, activity_add_event::class.java))
        }

        listView = findViewById(R.id.listView)
        calendarView = findViewById(R.id.calendarView)

        adapter = ArrayAdapter(this, R.layout.activity_calendar_item, R.id.petName, activities)
        listView.adapter = adapter

        // Cargar actividades al iniciar
        val calendar = Calendar.getInstance()
        loadActivitiesForDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Listener para cambio de fecha
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            loadActivitiesForDate(year, month, dayOfMonth)
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

    private fun loadActivitiesForDate(year: Int, month: Int, dayOfMonth: Int) {
        activities.clear()
        activities.addAll(getActivitiesForDate(year, month, dayOfMonth))
        adapter.notifyDataSetChanged()
    }

    private fun getActivitiesForDate(year: Int, month: Int, dayOfMonth: Int): List<String> {
        return listOf(
            "Quevedo - Clase de Cisco",
            "Firulais - Paseo matutino",
            "Michi - Comida",
            "Firulais - Paseo vespertino",
            "Angulo - Regar la plantita",
            "Quevedo - Jugar consigo mismo"
        )
    }
}