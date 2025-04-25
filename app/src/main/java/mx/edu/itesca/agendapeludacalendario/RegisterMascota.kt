package mx.edu.itesca.agendapeludacalendario

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class RegisterMascota : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_mascota)

        val buttonCancel: Button = findViewById(R.id.cancelButton)

        buttonCancel.setOnClickListener {
            val intent: Intent = Intent(
                this, activity_add_event::class.java
            )
            startActivity(intent)
        }

        val buttonAdd: Button = findViewById(R.id.addButton)

        buttonAdd.setOnClickListener {
            val intent: Intent = Intent(
                this, activity_add_event::class.java
            )
            startActivity(intent)
        }
    }
}