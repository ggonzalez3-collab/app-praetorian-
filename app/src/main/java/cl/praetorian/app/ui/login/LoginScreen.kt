package cl.praetorian.app.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.praetorian.app.R
import cl.praetorian.app.data.DEMO_PASSWORD
import cl.praetorian.app.ui.theme.Crimson
import cl.praetorian.app.ui.theme.Gold
import cl.praetorian.app.ui.theme.Ink

@Composable
fun LoginScreen(onLogin: (String, String) -> Boolean) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Crimson.copy(alpha = 0.35f), Ink)))
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(painterResource(R.drawable.ic_launcher), contentDescription = null, modifier = Modifier.size(96.dp))
            Text(
                "PRAETORIAN",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                color = Gold,
            )
            Text(
                "Entrena como la guardia de élite",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorText = null },
                label = { Text("Correo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorText = null },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
            errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = { if (!onLogin(email, password)) errorText = "Correo o contraseña incorrectos" },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("INGRESAR", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Cuentas de prueba (contraseña $DEMO_PASSWORD)", style = MaterialTheme.typography.labelMedium, color = Gold)
                    Text("Socio: socio@praetorian.cl", style = MaterialTheme.typography.bodySmall)
                    Text("Staff: staff@praetorian.cl", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
