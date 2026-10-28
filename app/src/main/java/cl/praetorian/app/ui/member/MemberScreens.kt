package cl.praetorian.app.ui.member

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.praetorian.app.data.GymClass
import cl.praetorian.app.data.Member
import cl.praetorian.app.data.Workout
import cl.praetorian.app.ui.GymViewModel
import cl.praetorian.app.ui.components.AppShell
import cl.praetorian.app.ui.components.MembershipBadge
import cl.praetorian.app.ui.components.NumberField
import cl.praetorian.app.ui.components.SectionTitle
import cl.praetorian.app.ui.components.StatCard
import cl.praetorian.app.ui.components.Tab
import cl.praetorian.app.ui.kg
import cl.praetorian.app.ui.label
import cl.praetorian.app.ui.pretty
import cl.praetorian.app.ui.theme.Crimson
import cl.praetorian.app.ui.theme.Gold
import cl.praetorian.app.ui.theme.Ink
import cl.praetorian.app.ui.theme.Marble
import cl.praetorian.app.ui.theme.Success
import java.time.LocalDate

private val memberTabs = listOf(
    Tab("Inicio", Icons.Filled.Home),
    Tab("Clases", Icons.Filled.DateRange),
    Tab("Progreso", Icons.Filled.Star),
)

@Composable
fun MemberShell(vm: GymViewModel, memberId: String) {
    val members by vm.members.collectAsState()
    val classes by vm.classes.collectAsState()
    val workouts by vm.workouts.collectAsState()
    val member = members.firstOrNull { it.id == memberId } ?: return
    var tab by rememberSaveable { mutableIntStateOf(0) }

    AppShell(
        title = "Hola, ${member.firstName}",
        tabs = memberTabs,
        selected = tab,
        onSelect = { tab = it },
        message = vm.message,
        onMessageShown = vm::messageShown,
        onLogout = vm::logout,
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (tab) {
            0 -> MemberHome(member, onCheckIn = { vm.checkIn(member.id) }, modifier = modifier)
            1 -> MemberClasses(classes, member.id, onToggle = { vm.toggleEnrollment(it, member.id) }, modifier = modifier)
            else -> MemberProgress(
                workouts.filter { it.memberId == member.id },
                onAdd = { exercise, sets, reps, kg -> vm.addWorkout(member.id, exercise, sets, reps, kg) },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun MemberHome(member: Member, onCheckIn: () -> Unit, modifier: Modifier) {
    val today = LocalDate.now()
    val thisMonth = member.checkIns.count { it.month == today.month && it.year == today.year }
    val checkedInToday = today in member.checkIns

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { MembershipCard(member) }
        item {
            Button(
                onClick = onCheckIn,
                enabled = !checkedInToday && member.isActive,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (checkedInToday) "Ingreso de hoy registrado" else "Registrar ingreso al gimnasio")
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Ingresos este mes", "$thisMonth", Modifier.weight(1f))
                StatCard("Ingresos totales", "${member.checkIns.size}", Modifier.weight(1f), accent = Success)
            }
        }
        item { SectionTitle("Últimos ingresos") }
        if (member.checkIns.isEmpty()) {
            item { Text("Aún no registras ingresos. ¡Hoy es un buen día para partir!") }
        }
        items(member.checkIns.take(7)) { date ->
            ListItem(
                headlineContent = { Text(if (date == today) "Hoy" else date.pretty()) },
                leadingContent = { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Success) },
            )
        }
    }
}

@Composable
private fun MembershipCard(member: Member) {
    val days = member.daysLeft()
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Crimson, Ink)))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "PLAN ${member.plan.label.uppercase()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                MembershipBadge(member)
            }
            Spacer(Modifier.height(8.dp))
            Text(member.name, style = MaterialTheme.typography.headlineSmall, color = Marble)
            Text(
                if (days >= 0) "Vigente hasta el ${member.membershipEnds.pretty()}"
                else "Venció el ${member.membershipEnds.pretty()}",
                style = MaterialTheme.typography.bodyMedium,
                color = Marble.copy(alpha = 0.8f),
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { days.coerceIn(0L, 30L) / 30f },
                modifier = Modifier.fillMaxWidth(),
                color = Gold,
                trackColor = Marble.copy(alpha = 0.2f),
            )
            Spacer(Modifier.height(8.dp))
            Text("N° de socio: ${member.id.uppercase()}", style = MaterialTheme.typography.labelMedium, color = Marble)
        }
    }
}

@Composable
private fun MemberClasses(classes: List<GymClass>, memberId: String, onToggle: (String) -> Unit, modifier: Modifier) {
    val byDay = classes.sortedWith(compareBy<GymClass>({ it.day }, { it.time })).groupBy { it.day }
    val booked = classes.count { memberId in it.enrolled }

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                "Tienes $booked clase(s) reservada(s) esta semana",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        byDay.forEach { (day, dayClasses) ->
            item(key = day.name) { SectionTitle(day.label()) }
            items(dayClasses, key = { it.id }) { gymClass ->
                ClassCard(gymClass, enrolled = memberId in gymClass.enrolled, onToggle = { onToggle(gymClass.id) })
            }
        }
    }
}

@Composable
private fun ClassCard(gymClass: GymClass, enrolled: Boolean, onToggle: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(gymClass.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${gymClass.time} · ${gymClass.coach}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    if (gymClass.spotsLeft > 0) "${gymClass.spotsLeft} cupos disponibles" else "Sin cupos",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (gymClass.spotsLeft > 0) Success else MaterialTheme.colorScheme.error,
                )
            }
            if (enrolled) {
                OutlinedButton(onClick = onToggle) { Text("Cancelar") }
            } else {
                Button(onClick = onToggle, enabled = gymClass.spotsLeft > 0) { Text("Reservar") }
            }
        }
    }
}

@Composable
private fun MemberProgress(
    workouts: List<Workout>,
    onAdd: (exercise: String, sets: Int, reps: Int, kg: Double) -> Unit,
    modifier: Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }
    // Récord personal = el mayor peso levantado en cada ejercicio
    val records = workouts.groupBy { it.exercise }
        .mapValues { (_, list) -> list.maxOf { it.weightKg } }
        .entries.sortedByDescending { it.value }

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
        ) {
            item { SectionTitle("Récords personales") }
            if (records.isEmpty()) {
                item { Text("Registra tu primer entrenamiento para ver tus récords.") }
            }
            items(records, key = { "pr-" + it.key }) { (exercise, best) ->
                ListItem(
                    headlineContent = { Text(exercise) },
                    leadingContent = { Icon(Icons.Filled.Star, contentDescription = null, tint = Gold) },
                    trailingContent = { Text("${best.kg()} kg", color = Gold, fontWeight = FontWeight.Bold) },
                )
            }
            item { SectionTitle("Historial") }
            items(workouts.sortedByDescending { it.date }, key = { it.id }) { workout ->
                ListItem(
                    headlineContent = { Text(workout.exercise) },
                    supportingContent = { Text("${workout.sets} × ${workout.reps} · ${workout.weightKg.kg()} kg") },
                    trailingContent = { Text(workout.date.pretty()) },
                )
            }
        }
        ExtendedFloatingActionButton(
            onClick = { showDialog = true },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Registrar") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    if (showDialog) {
        AddWorkoutDialog(
            onDismiss = { showDialog = false },
            onSave = { exercise, sets, reps, kg ->
                onAdd(exercise, sets, reps, kg)
                showDialog = false
            },
        )
    }
}

@Composable
private fun AddWorkoutDialog(onDismiss: () -> Unit, onSave: (String, Int, Int, Double) -> Unit) {
    var exercise by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("4") }
    var reps by remember { mutableStateOf("10") }
    var weight by remember { mutableStateOf("") }

    val setsValue = sets.toIntOrNull()?.takeIf { it > 0 }
    val repsValue = reps.toIntOrNull()?.takeIf { it > 0 }
    val weightValue = weight.replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 }
    val valid = exercise.isNotBlank() && setsValue != null && repsValue != null && weightValue != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo entrenamiento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = exercise,
                    onValueChange = { exercise = it },
                    label = { Text("Ejercicio") },
                    singleLine = true,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField("Series", sets, { sets = it }, Modifier.weight(1f))
                    NumberField("Reps", reps, { reps = it }, Modifier.weight(1f))
                }
                NumberField("Peso (kg)", weight, { weight = it }, decimal = true)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(exercise.trim(), setsValue!!, repsValue!!, weightValue!!) },
                enabled = valid,
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
