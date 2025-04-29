package mx.edu.itesca.agendapeludacalendario

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        setupFirebase()
        setupGoogleSignIn()
        setupUI()
    }

    private fun setupFirebase() {
        auth = Firebase.auth
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupUI() {
        val btnGoogle = findViewById<com.google.android.gms.common.SignInButton>(R.id.btnGoogle)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        // Configuración Google Sign-In
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) handleGoogleSignIn(result.data)
        }

        btnGoogle.setOnClickListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        // Login email/password (existente)
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                signInWithEmail(email, password)
            } else {
                showError("Ingrese email y contraseña")
            }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun handleGoogleSignIn(data: Intent?) {
        Log.d("DEBUG", "Inicio de handleGoogleSignInResult") // Paso 1
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            Log.d("DEBUG", "ID Token recibido: ${account.idToken != null}") // Paso 2
            account.idToken?.let { token ->
                firebaseAuthWithGoogle(token)
            } ?: showError("Token de Google nulo")
        } catch (e: ApiException) {
            Log.e("DEBUG", "ERROR GOOGLE: ${e.statusCode} - ${e.message}") // Paso 3
            showError("Error código ${e.statusCode}")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.d("DEBUG", "Iniciando autenticación Firebase") // Paso 4
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val db = Firebase.firestore

                    if (user != null) {
                        val userDocRef = db.collection("usuarios").document(user.uid)

                        userDocRef.get().addOnSuccessListener { document ->
                            if (!document.exists()) {
                                val newUser = hashMapOf(
                                    "uid" to user.uid,
                                    "username" to (user.displayName ?: ""),
                                    "correo" to (user.email ?: ""),
                                    "createdAt" to FieldValue.serverTimestamp()
                                )
                                userDocRef.set(newUser)
                                    .addOnSuccessListener {
                                        Log.d("FIRESTORE", "Usuario guardado correctamente")
                                    }
                                    .addOnFailureListener {
                                        Log.e("FIRESTORE", "Error al guardar usuario: ${it.message}")
                                    }
                            } else {
                                Log.d("FIRESTORE", "El usuario ya existe")
                            }
                            navigateToCalendar()
                        }
                    }
                } else {
                    Log.e("DEBUG", "ERROR FIREBASE: ${task.exception?.message}") // Paso 6
                    showError("Firebase error: ${task.exception?.message}")
                }
            }
      }


    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) navigateToCalendar()
                else showError("Credenciales inválidas")
            }
    }

    private fun navigateToCalendar() {
        val intent = Intent(this, CalendarActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish() // ¡Este es clave!
    }

    private fun showError(message: String) {
        findViewById<TextView>(R.id.tvError).apply {
            text = message
            visibility = View.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
        if (Firebase.auth.currentUser != null) {
            navigateToCalendar()
        }
    }
}