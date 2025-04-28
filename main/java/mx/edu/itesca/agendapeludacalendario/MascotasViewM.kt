package mx.edu.itesca.agendapeludacalendario
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MascotasViewM: ViewModel() {
    private val db = Firebase.firestore;

    private var _listaMascotas = MutableLiveData<List<RegisterMascota>>(emptyList())
    val listaTareas: LiveData<List<RegisterMascota>> = _listaMascotas

    init {
        obtenerMascotas()
    }

    fun obtenerMascotas() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resultado = db.collection("Mascotas").get().await()

                val mascota = resultado.documents.mapNotNull {
                    it.toObject(RegisterMascota::class.java)
                }
                _listaMascotas.postValue(mascota)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun agregarmascotas(mascota: RegisterMascota) {
        tarea.id = UUID.randomUUID().toString()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("Mascota").document(mascota.id).set(mascota).await()
                _listaMascotas.postValue(_listaMascotas.value?.plus(mascota))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun actualizarTareas(mascota: RegisterMascota) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("Mascota").document(mascota.id).update(mascota.toMap()).await()
                _listaMascotas.postValue(_listaMascotas.value?.map { if (it.id == RegisterMascota.id) mascota else it })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun CancelarMascota(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("Mascota").document(id).delete().await()
                _listaMascotas.postValue(_listaMascotas.value?.filter { it.id != id })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}