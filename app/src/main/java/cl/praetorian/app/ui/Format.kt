package cl.praetorian.app.ui

import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val esCL: Locale = Locale.forLanguageTag("es-CL")
private val clp = NumberFormat.getCurrencyInstance(esCL).apply { maximumFractionDigits = 0 }
private val dateFormat = DateTimeFormatter.ofPattern("d 'de' MMMM", esCL)

fun formatClp(amount: Int): String = clp.format(amount)

fun LocalDate.pretty(): String = format(dateFormat)

fun DayOfWeek.label(): String = getDisplayName(TextStyle.FULL, esCL).replaceFirstChar { it.uppercase() }

fun Double.kg(): String = if (this % 1.0 == 0.0) toInt().toString() else String.format(esCL, "%.1f", this)
