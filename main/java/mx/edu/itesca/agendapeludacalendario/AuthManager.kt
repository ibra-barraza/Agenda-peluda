package mx.edu.itesca.agendapeludacalendario
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthManager(private val context: Context) {
    private val auth: FirebaseAuth = Firebase.auth
    private lateinit var googleSignInClient: GoogleSignInClient

    init {
        configureGoogleSignIn()
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>): GoogleSignInAccount? {
        return try {
            task.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            null
        }
    }

    fun getGoogleSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun signInWithGoogleCredential(idToken: String, onComplete: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
    fun signOut() {
        // Cierra sesión en Firebase
        Firebase.auth.signOut()

        // Cierra sesión en Google Sign-In
        GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut().addOnCompleteListener {
            // Opcional: Revoca permisos para forzar selección de cuenta
            GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).revokeAccess()
        }
    }
}