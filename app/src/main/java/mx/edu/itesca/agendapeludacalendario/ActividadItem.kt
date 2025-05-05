package mx.edu.itesca.agendapeludacalendario

data class ActividadItem(
    var id: String? = null,
    var titulo: String? = null,
    var actividad: String? = null,
    var fecha: String? = null,
    var horaInicio: String? = null,
    var horaFinal: String? = null,
    var notas: String? = null,
    var nombresMascotas: List<String>? = null
)
