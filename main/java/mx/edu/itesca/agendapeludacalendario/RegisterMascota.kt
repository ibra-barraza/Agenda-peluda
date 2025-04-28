package mx.edu.itesca.agendapeludacalendario

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider


class RegisterMascota : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MascotasAdap
    private lateinit var viewModel: MascotasViewM

    var MascotasEdit = Mascotas()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        viewModel = ViewModelProvider(this)[MascotasViewM::class.java]

        viewModel.listaMascotas.observe(this) { Mascotas ->
            setupRecyclerView(Mascotas)
        }

        binding.btnAgregarTarea.setOnClickListener {
            val Mascotas = Mascotas(
                titulo = binding.etTitulo.text.toString(),
                descripcion = binding.etDescripcion.text.toString()
            )

            viewModel.agregarmascotas(Mascotas)

            binding.etTitulo.setText("")
            binding.etDescripcion.setText("")
        }

        binding.btnActualizarTarea.setOnClickListener {
            MascadotaEdit.nombre = ""
            MascadotaEdit.tipo = ""

            MascadotaEdit.nombre = binding.nombre_mascota.text.toString()
            MascadotaEdit.tipo = binding.tipo_mascota.text.toString()

            viewModel.actualizarTareas(MascotasEdit)
        }

    }

    fun setupRecyclerView(listaMascotas: List<Mascotas>) {
        adapter = MascotasAdap(ListaMascotas, ::borrarTarea, ::ActualizarMascota)
        binding.ListNombre.adapter = adapter
    }

    fun borrarTarea(id: String) {
        viewModel.borrar mascotas(id)
    }

    fun actualizarTarea(Mascota: Mascotas) {
        MascotasEdit = Mascota

        binding.nombre_mascota.setText(MascotasEdit.nombre)
        binding.tipo_mascota.setText(MascotasEdit.tipo)
    }
}