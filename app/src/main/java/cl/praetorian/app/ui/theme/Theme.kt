package cl.praetorian.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Gold = Color(0xFFD4A537)
val GoldDark = Color(0xFF9C7A22)
val Crimson = Color(0xFFB3202A)
val Ink = Color(0xFF0E0E10)
val Stone = Color(0xFF1A1A1E)
val StoneLight = Color(0xFF26262B)
val Marble = Color(0xFFF2EEE6)
val Success = Color(0xFF4CAF7A)

private val PraetorianColors = darkColorScheme(
    primary = Gold,
    onPrimary = Ink,
    primaryContainer = GoldDark,
    onPrimaryContainer = Marble,
    secondary = Crimson,
    onSecondary = Marble,
    background = Ink,
    onBackground = Marble,
    surface = Ink,
    onSurface = Marble,
    surfaceVariant = StoneLight,
    onSurfaceVariant = Color(0xFFBDB8AE),
    surfaceContainer = Stone,
    surfaceContainerHigh = StoneLight,
    surfaceContainerHighest = StoneLight,
    error = Color(0xFFFF6B6B),
)

private val PraetorianTypography = Typography().let {
    it.copy(
        headlineMedium = it.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
        titleLarge = it.titleLarge.copy(fontWeight = FontWeight.Bold),
    )
}

/** La marca usa siempre tema oscuro: negro, dorado y carmesí. */
@Composable
fun PraetorianTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = PraetorianColors, typography = PraetorianTypography, content = content)
}
