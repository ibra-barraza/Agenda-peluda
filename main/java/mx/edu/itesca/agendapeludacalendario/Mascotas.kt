package mx.edu.itesca.agendapeludacalendario

data class Mascotas(
    var id: String="",
    var nombre: String="",
    var peso: String="",
    var Sexo: String="",
    var años: String="",
    var tipo: String=""
)

{
    fun toMap(): Map<String,String>{
        return mapOf(
            "Nombre" to nombre,
            "Macho/Hembra" to Sexo,
            "Edad" to años,
            "Tipo" to tipo,
            "Peso" to peso
        )
    }
}