package cl.praetorian.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.praetorian.app.data.Session
import cl.praetorian.app.ui.GymViewModel
import cl.praetorian.app.ui.login.LoginScreen
import cl.praetorian.app.ui.member.MemberShell
import cl.praetorian.app.ui.staff.StaffShell
import cl.praetorian.app.ui.theme.PraetorianTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PraetorianTheme {
                PraetorianApp()
            }
        }
    }
}

/** Según quién inicie sesión, mostramos la app del socio o el panel del staff. */
@Composable
fun PraetorianApp(vm: GymViewModel = viewModel()) {
    when (val session = vm.session) {
        null -> LoginScreen(onLogin = vm::login)
        is Session.AsMember -> MemberShell(vm, session.memberId)
        is Session.AsStaff -> StaffShell(vm, session.staff)
    }
}
