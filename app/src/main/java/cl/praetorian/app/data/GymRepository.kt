package cl.praetorian.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

const val DEMO_PASSWORD = "1234"

/**
 * Fuente de datos de la app. Por ahora guarda todo en memoria con datos de ejemplo;
 * más adelante se puede reemplazar por Room (local) o Firebase (nube) sin tocar las pantallas.
 */
class GymRepository {

    private val today = LocalDate.now()

    private val staff = listOf(Staff("s1", "Valentina Rojas", "staff@praetorian.cl"))

    private val _members = MutableStateFlow(
        listOf(
            Member(
                "m1", "Diego Soto", "socio@praetorian.cl", Plan.GLADIADOR, today.plusDays(18),
                listOf(today.minusDays(1), today.minusDays(3), today.minusDays(5), today.minusDays(8)),
            ),
            Member("m2", "Camila Pérez", "camila@correo.cl", Plan.LEGION, today.plusDays(45), listOf(today)),
            Member("m3", "Matías Fuentes", "matias@correo.cl", Plan.BASICO, today.minusDays(3)),
            Member("m4", "Fernanda Muñoz", "fernanda@correo.cl", Plan.GLADIADOR, today.plusDays(5), listOf(today, today.minusDays(2))),
            Member("m5", "Benjamín Torres", "benja@correo.cl", Plan.BASICO, today.plusDays(25)),
        )
    )
    val members: StateFlow<List<Member>> = _members.asStateFlow()

    private val _classes = MutableStateFlow(
        listOf(
            GymClass("c1", "Funcional", "Valentina Rojas", DayOfWeek.MONDAY, "07:00", 15, setOf("m2", "m4")),
            GymClass("c2", "Cross Training", "Ignacio Vera", DayOfWeek.MONDAY, "19:00", 12, setOf("m1")),
            GymClass("c3", "Boxeo", "Ignacio Vera", DayOfWeek.TUESDAY, "20:00", 10, setOf("m2", "m5")),
            GymClass("c4", "Yoga", "Antonia Silva", DayOfWeek.WEDNESDAY, "08:00", 12),
            GymClass("c5", "Spinning", "Antonia Silva", DayOfWeek.THURSDAY, "19:30", 20, setOf("m4")),
            GymClass("c6", "HIIT", "Valentina Rojas", DayOfWeek.FRIDAY, "18:00", 2, setOf("m2", "m4")),
            GymClass("c7", "Open Box", "Ignacio Vera", DayOfWeek.SATURDAY, "10:00", 25),
        )
    )
    val classes: StateFlow<List<GymClass>> = _classes.asStateFlow()

    private val _workouts = MutableStateFlow(
        listOf(
            Workout("w1", "m1", today.minusDays(1), "Sentadilla", 4, 8, 80.0),
            Workout("w2", "m1", today.minusDays(3), "Press banca", 4, 8, 60.0),
            Workout("w3", "m1", today.minusDays(5), "Sentadilla", 4, 6, 85.0),
            Workout("w4", "m1", today.minusDays(8), "Peso muerto", 3, 5, 110.0),
        )
    )
    val workouts: StateFlow<List<Workout>> = _workouts.asStateFlow()

    fun login(email: String, password: String): Session? {
        if (password != DEMO_PASSWORD) return null
        val normalized = email.trim().lowercase()
        staff.firstOrNull { it.email == normalized }?.let { return Session.AsStaff(it) }
        return _members.value.firstOrNull { it.email == normalized }?.let { Session.AsMember(it.id) }
    }

    /** Registra el ingreso del socio hoy. Devuelve un mensaje para mostrar al usuario. */
    fun checkIn(memberId: String): String {
        val member = findMember(memberId) ?: return "Socio no encontrado"
        val now = LocalDate.now()
        if (!member.isActive) return "Membresía vencida: hay que renovar para ingresar"
        if (now in member.checkIns) return "${member.firstName} ya registró su ingreso hoy"
        updateMember(memberId) { it.copy(checkIns = listOf(now) + it.checkIns) }
        return "¡Ingreso registrado! A entrenar, ${member.firstName}"
    }

    /** Reserva la clase si el socio no estaba inscrito, o cancela la reserva si ya lo estaba. */
    fun toggleEnrollment(classId: String, memberId: String): String {
        val gymClass = _classes.value.firstOrNull { it.id == classId } ?: return "Clase no encontrada"
        val member = findMember(memberId) ?: return "Socio no encontrado"

        if (memberId in gymClass.enrolled) {
            updateClass(classId) { it.copy(enrolled = it.enrolled - memberId) }
            return "Reserva cancelada: ${gymClass.name}"
        }
        if (!member.isActive) return "Necesitas una membresía activa para reservar"
        if (gymClass.spotsLeft <= 0) return "La clase ${gymClass.name} está llena"
        val booked = _classes.value.count { memberId in it.enrolled }
        if (booked >= member.plan.classesPerWeek) {
            return "Tu plan ${member.plan.label} permite ${member.plan.classesPerWeek} clases por semana"
        }
        updateClass(classId) { it.copy(enrolled = it.enrolled + memberId) }
        return "Reservaste ${gymClass.name} a las ${gymClass.time}"
    }

    fun addWorkout(memberId: String, exercise: String, sets: Int, reps: Int, weightKg: Double) {
        val workout = Workout(UUID.randomUUID().toString(), memberId, LocalDate.now(), exercise, sets, reps, weightKg)
        _workouts.update { listOf(workout) + it }
    }

    /** Extiende la membresía un mes desde hoy o desde su vencimiento, lo que sea más tarde. */
    fun renewMembership(memberId: String): String {
        val member = findMember(memberId) ?: return "Socio no encontrado"
        val now = LocalDate.now()
        val from = if (member.membershipEnds.isAfter(now)) member.membershipEnds else now
        updateMember(memberId) { it.copy(membershipEnds = from.plusMonths(1)) }
        return "Membresía de ${member.firstName} renovada por 1 mes"
    }

    /** Agrega un socio nuevo con un mes de membresía. Devuelve un error o null si todo salió bien. */
    fun addMember(name: String, email: String, plan: Plan): String? {
        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()
        if (cleanName.isEmpty()) return "Falta el nombre"
        if (!cleanEmail.contains('@')) return "El correo no es válido"
        if (_members.value.any { it.email == cleanEmail }) return "Ya existe un socio con ese correo"
        val member = Member(
            id = "m" + UUID.randomUUID().toString().take(6),
            name = cleanName,
            email = cleanEmail,
            plan = plan,
            membershipEnds = LocalDate.now().plusMonths(1),
        )
        _members.update { it + member }
        return null
    }

    private fun findMember(id: String) = _members.value.firstOrNull { it.id == id }

    private fun updateMember(id: String, transform: (Member) -> Member) =
        _members.update { list -> list.map { if (it.id == id) transform(it) else it } }

    private fun updateClass(id: String, transform: (GymClass) -> GymClass) =
        _classes.update { list -> list.map { if (it.id == id) transform(it) else it } }
}
