package cl.praetorian.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.praetorian.app.data.GymRepository
import cl.praetorian.app.data.Plan
import cl.praetorian.app.data.Session

/** Conecta las pantallas con el repositorio y guarda la sesión y los avisos (snackbar). */
class GymViewModel : ViewModel() {

    private val repository = GymRepository()

    val members = repository.members
    val classes = repository.classes
    val workouts = repository.workouts

    var session by mutableStateOf<Session?>(null)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    fun login(email: String, password: String): Boolean {
        session = repository.login(email, password)
        return session != null
    }

    fun logout() {
        session = null
    }

    fun checkIn(memberId: String) {
        message = repository.checkIn(memberId)
    }

    fun toggleEnrollment(classId: String, memberId: String) {
        message = repository.toggleEnrollment(classId, memberId)
    }

    fun addWorkout(memberId: String, exercise: String, sets: Int, reps: Int, weightKg: Double) {
        repository.addWorkout(memberId, exercise, sets, reps, weightKg)
        message = "Entrenamiento guardado"
    }

    fun renew(memberId: String) {
        message = repository.renewMembership(memberId)
    }

    fun addMember(name: String, email: String, plan: Plan): Boolean {
        val error = repository.addMember(name, email, plan)
        message = error ?: "Socio ${name.trim()} agregado"
        return error == null
    }

    fun messageShown() {
        message = null
    }
}
