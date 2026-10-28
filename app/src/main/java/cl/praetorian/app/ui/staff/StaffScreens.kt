package cl.praetorian.app.ui.staff

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.praetorian.app.data.GymClass
import cl.praetorian.app.data.Member
import cl.praetorian.app.data.Plan
import cl.praetorian.app.data.Staff
import cl.praetorian.app.ui.GymViewModel
import cl.praetorian.app.ui.components.AppShell
import cl.praetorian.app.ui.components.Avatar
import cl.praetorian.app.ui.components.MembershipBadge
import cl.praetorian.app.ui.components.SectionTitle
import cl.praetorian.app.ui.components.StatCard
import cl.praetorian.app.ui.components.Tab
import cl.praetorian.app.ui.formatClp
import cl.praetorian.app.ui.label
import cl.praetorian.app.ui.pretty
import cl.praetorian.app.ui.theme.Gold
import cl.praetorian.app.ui.theme.Success
import java.time.LocalDate

private val staffTabs = listOf(
    Tab("Panel", Icons.Filled.Home),
    Tab("Socios", Icons.Filled.Person),
    Tab("Clases", Icons.Filled.DateRange),
)

@Composable
fun StaffShell(vm: GymViewModel, staff: Staff) {
    val members by vm.members.collectAsState()
    val classes by vm.classes.collectAsState()
    var tab by rememberSaveable { mutableIntStateOf(0) }

    AppShell(
        title = "Staff · ${staff.name.substringBefore(' ')}",
        tabs = staffTabs,
        selected = tab,
        onSelect = { tab = it },
        message = vm.message,
        onMessageShown = vm::messageShown,
        onLogout = vm::logout,
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (tab) {
            0 -> StaffDashboard(members, classes, modifier)
            1 -> StaffMembers(members, onCheckIn = vm::checkIn, onRenew = vm::renew, onAdd = vm::addMember, modifier = modifier)
            else -> StaffClasses(classes, members, modifier)
        }
    }
}

@Composable
private fun StaffDashboard(members: List<Member>, classes: List<GymClass>, modifier: Modifier) {
    val today = LocalDate.now()
    val active = members.filter { it.isActive }
    val expiredCount = members.size - active.size
    val checkedInToday = members.filter { today in it.checkIns }
    val monthlyRevenue = active.sumOf { it.plan.monthlyPriceClp }
    val expiringSoon = active.filter { it.daysLeft() <= 7 }.sortedBy { it.membershipEnds }
    val todayClasses = classes.filter { it.day == today.dayOfWeek }.sortedBy { it.time }

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Socios activos", "${active.size}", Modifier.weight(1f))
                StatCard("Vencidos", "$expiredCount", Modifier.weight(1f), accent = MaterialTheme.colorScheme.error)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Ingresos hoy", "${checkedInToday.size}", Modifier.weight(1f), accent = Success)
                StatCard("Facturación mensual", formatClp(monthlyRevenue), Modifier.weight(1f))
            }
        }

        item { SectionTitle("Clases de hoy · ${today.dayOfWeek.label()}") }
        if (todayClasses.isEmpty()) item { EmptyText("No hay clases programadas hoy") }
        items(todayClasses, key = { "class-" + it.id }) { gymClass ->
            ListItem(
                headlineContent = { Text(gymClass.name) },
                supportingContent = { Text("${gymClass.time} · ${gymClass.coach}") },
                trailingContent = { Text("${gymClass.enrolled.size}/${gymClass.capacity}", color = Gold) },
            )
        }

        item { SectionTitle("Vencen esta semana") }
        if (expiringSoon.isEmpty()) item { EmptyText("Ninguna membresía vence en los próximos 7 días") }
        items(expiringSoon, key = { "exp-" + it.id }) { member ->
            ListItem(
                headlineContent = { Text(member.name) },
                supportingContent = { Text("Plan ${member.plan.label} · ${member.membershipEnds.pretty()}") },
                trailingContent = { MembershipBadge(member) },
            )
        }

        item { SectionTitle("Ingresaron hoy") }
        if (checkedInToday.isEmpty()) item { EmptyText("Todavía no ingresa nadie hoy") }
        items(checkedInToday, key = { "in-" + it.id }) { member ->
            ListItem(
                headlineContent = { Text(member.name) },
                leadingContent = { Avatar(member.name) },
            )
        }
    }
}

@Composable
private fun StaffMembers(
    members: List<Member>,
    onCheckIn: (String) -> Unit,
    onRenew: (String) -> Unit,
    onAdd: (String, String, Plan) -> Boolean,
    modifier: Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    val filtered = members
        .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true) }
        .sortedBy { it.name }

    Box(modifier.fillMaxSize()) {
        Column {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Buscar por nombre o correo") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
            LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                items(filtered, key = { it.id }) { member ->
                    ListItem(
                        modifier = Modifier.clickable { selectedId = member.id },
                        leadingContent = { Avatar(member.name) },
                        headlineContent = { Text(member.name) },
                        supportingContent = { Text("${member.plan.label} · ${member.email}") },
                        trailingContent = { MembershipBadge(member) },
                    )
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { showAdd = true },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Nuevo socio") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    selectedId?.let { id ->
        members.firstOrNull { it.id == id }?.let { member ->
            MemberActionsDialog(
                member = member,
                onDismiss = { selectedId = null },
                onCheckIn = { onCheckIn(member.id) },
                onRenew = { onRenew(member.id) },
            )
        }
    }
    if (showAdd) {
        AddMemberDialog(
            onDismiss = { showAdd = false },
            onSave = { name, email, plan -> if (onAdd(name, email, plan)) showAdd = false },
        )
    }
}

@Composable
private fun MemberActionsDialog(member: Member, onDismiss: () -> Unit, onCheckIn: () -> Unit, onRenew: () -> Unit) {
    val today = LocalDate.now()
    val thisMonth = member.checkIns.count { it.month == today.month && it.year == today.year }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(member.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Plan ${member.plan.label} · ${formatClp(member.plan.monthlyPriceClp)}/mes", Modifier.weight(1f))
                    MembershipBadge(member)
                }
                Text(member.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Vigencia: ${member.membershipEnds.pretty()}")
                Text("Ingresos este mes: $thisMonth")
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onCheckIn,
                    enabled = member.isActive && today !in member.checkIns,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Registrar ingreso") }
                OutlinedButton(onClick = onRenew, modifier = Modifier.fillMaxWidth()) {
                    Text("Renovar 1 mes (${formatClp(member.plan.monthlyPriceClp)})")
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } },
    )
}

@Composable
private fun AddMemberDialog(onDismiss: () -> Unit, onSave: (String, String, Plan) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var plan by remember { mutableStateOf(Plan.GLADIADOR) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo socio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre completo") }, singleLine = true)
                OutlinedTextField(
                    email,
                    { email = it },
                    label = { Text("Correo") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                Text("Plan", style = MaterialTheme.typography.labelLarge, color = Gold)
                Plan.entries.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(selected = plan == option, onClick = { plan = option }, role = Role.RadioButton),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = plan == option, onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text("${option.label} · ${formatClp(option.monthlyPriceClp)}")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, email, plan) }, enabled = name.isNotBlank() && email.isNotBlank()) {
                Text("Agregar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun StaffClasses(classes: List<GymClass>, members: List<Member>, modifier: Modifier) {
    val namesById = members.associate { it.id to it.firstName }
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(classes.sortedWith(compareBy<GymClass>({ it.day }, { it.time })), key = { it.id }) { gymClass ->
            val occupancy = gymClass.enrolled.size.toFloat() / gymClass.capacity
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            gymClass.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        Text("${gymClass.enrolled.size}/${gymClass.capacity}", color = Gold, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "${gymClass.day.label()} ${gymClass.time} · ${gymClass.coach}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LinearProgressIndicator(
                        progress = { occupancy },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (occupancy >= 1f) MaterialTheme.colorScheme.error else Gold,
                    )
                    Text(
                        if (gymClass.enrolled.isEmpty()) "Sin inscritos"
                        else "Inscritos: " + gymClass.enrolled.mapNotNull { namesById[it] }.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyText(text: String) {
    Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
}
