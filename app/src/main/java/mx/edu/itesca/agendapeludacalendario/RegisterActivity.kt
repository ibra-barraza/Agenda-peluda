package mx.edu.itesca.agendapeludacalendario

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth
        val email: EditText = findViewById(R.id.etEmail)
        val password: EditText = findViewById(R.id.etPassword)
        val confirmPassword: EditText = findViewById(R.id.etCPassword)
        val errorTv: TextView = findViewById(R.id.tvError)
        val button: Button = findViewById(R.id.btnRegistrarse)

        val btnRegresar: Button = findViewById(R.id.btnRegresar)
        btnRegresar.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        button.setOnClickListener {
            if (email.text.isEmpty() || password.text.isEmpty() || confirmPassword.text.isEmpty()) {
                errorTv.text = "Todos los campos deben de ser llenados"
                errorTv.visibility = View.VISIBLE
            } else if (!password.text.toString().equals(confirmPassword.text.toString())) {
                errorTv.text = "Las contraseñas no coinciden"
                errorTv.visibility = View.VISIBLE
            } else {
                errorTv.visibility = View.INVISIBLE
                signIn(email.text.toString(), password.text.toString())
            }
        }

        errorTv.visibility = View.INVISIBLE
    }

    private fun signIn(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val uid = it.uid
                        val db = FirebaseFirestore.getInstance()
                        val userRef = db.collection("usuarios").document(uid)

                        // Verificamos si ya existe (opcional si es registro nuevo)
                        userRef.get().addOnSuccessListener { document ->
                            if (!document.exists()) {
                                val userData = hashMapOf(
                                    "uid" to user.uid,
                                    "username" to email.substringBefore("@"),
                                    "correo" to email,
                                    "createdAt" to FieldValue.serverTimestamp()
                                )

                                userRef.set(userData)
                                    .addOnSuccessListener {
                                        Log.d("Firestore", "Usuario guardado en Firestore")
                                        val intent = Intent(this, CalendarActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firestore", "Error al guardar usuario", e)
                                        Toast.makeText(this, "Error al guardar usuario", Toast.LENGTH_LONG).show()
                                    }
                            }
                        }
                    }
                } else {
                    Log.w("ERROR", "signInWithEmail:failure", task.exception)
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthWeakPasswordException -> "La contraseña debe tener al menos 6 caracteres."
                        is FirebaseAuthInvalidCredentialsException -> "El correo electrónico no es válido."
                        is FirebaseAuthUserCollisionException -> "Este correo ya está registrado."
                        else -> "El registro falló: ${task.exception?.message}"
                    }
                    Toast.makeText(baseContext, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

}