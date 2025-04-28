package mx.edu.itesca.agendapeludacalendario

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class activity_add_event : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_event)

        val buttonCancelEvent: Button = findViewById(R.id.btnCancelEvent)

        buttonCancelEvent.setOnClickListener {
            val intent: Intent = Intent(
                this, CalendarActivity::class.java
            )
            startActivity(intent)
        }

        val buttonAddEvent: Button = findViewById(R.id.btnAddEvent)

        buttonAddEvent.setOnClickListener {
            val intent: Intent = Intent(
                this, CalendarActivity::class.java
            )
            startActivity(intent)
        }

        val buttonNuevaMascota: Button = findViewById(R.id.btnNuevaMascota)

        buttonNuevaMascota.setOnClickListener {
            val intent: Intent = Intent(
                this, RegisterMascota::class.java
            )
            startActivity(intent)
        }
    }
}