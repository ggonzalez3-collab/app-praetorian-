package cl.praetorian.app.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class Plan(val label: String, val monthlyPriceClp: Int, val classesPerWeek: Int) {
    BASICO("Básico", 19_990, 2),
    GLADIADOR("Gladiador", 29_990, 4),
    LEGION("Legión", 39_990, Int.MAX_VALUE),
}

data class Member(
    val id: String,
    val name: String,
    val email: String,
    val plan: Plan,
    val membershipEnds: LocalDate,
    // Fechas en que el socio ingresó al gimnasio, la más reciente primero
    val checkIns: List<LocalDate> = emptyList(),
) {
    val firstName: String get() = name.substringBefore(' ')
    val isActive: Boolean get() = !membershipEnds.isBefore(LocalDate.now())
    fun daysLeft(): Long = ChronoUnit.DAYS.between(LocalDate.now(), membershipEnds)
}

data class Staff(val id: String, val name: String, val email: String)

data class GymClass(
    val id: String,
    val name: String,
    val coach: String,
    val day: DayOfWeek,
    val time: String,
    val capacity: Int,
    val enrolled: Set<String> = emptySet(),
) {
    val spotsLeft: Int get() = capacity - enrolled.size
}

data class Workout(
    val id: String,
    val memberId: String,
    val date: LocalDate,
    val exercise: String,
    val sets: Int,
    val reps: Int,
    val weightKg: Double,
)

/** Quién inició sesión: un socio o alguien del equipo. */
sealed interface Session {
    data class AsMember(val memberId: String) : Session
    data class AsStaff(val staff: Staff) : Session
}
