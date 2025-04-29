package mx.edu.itesca.agendapeludacalendario

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MascotasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MascotaAdapter
    private val mascotaList = mutableListOf<Mascota>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onResume() {
        super.onResume()
        obtenerMascotas()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mascotas)

        recyclerView = findViewById(R.id.rvMascotas)

        adapter = MascotaAdapter(mascotaList,
            onDeleteClick = { mascota -> eliminarMascota(mascota) },
            onEditClick = { mascota -> irActualizarMascota(mascota) }
        )

        recyclerView.adapter = adapter

        obtenerMascotas()
    }

    private fun obtenerMascotas() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("usuarios")
            .document(userId)
            .collection("mascotas")
            .get()
            .addOnSuccessListener { documents ->
                mascotaList.clear()
                for (doc in documents) {
                    val mascota = doc.toObject(Mascota::class.java)
                    mascota.id = doc.id
                    mascotaList.add(mascota)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarMascota(mascota: Mascota) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("usuarios")
            .document(userId)
            .collection("mascotas")
            .document(mascota.id!!)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Mascota eliminada", Toast.LENGTH_SHORT).show()
                obtenerMascotas()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun irActualizarMascota(mascota: Mascota) {
        val intent = Intent(this, ActualizarMascotaActivity::class.java)
        intent.putExtra("mascota_id", mascota.id)
        intent.putExtra("nombre", mascota.nombre)
        intent.putExtra("especie", mascota.especie)
        intent.putExtra("edad", mascota.edad)
        intent.putExtra("sexo", mascota.sexo)
        intent.putExtra("peso", mascota.peso_kg)
        startActivity(intent)
    }
}
