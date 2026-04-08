package org.softwareanvil.project.ui.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import org.softwareanvil.project.ui.theme.Green
import org.softwareanvil.project.ui.theme.Orange
import org.softwareanvil.project.ui.theme.Red
import kotlin.time.Clock

@Composable
fun DeadLineIndicator(deadlineMillis: Long) {
    val now = Clock.System.now().toEpochMilliseconds()
    val diffDays = ((deadlineMillis - now) / (1000 * 60 * 60 * 24)).toInt()

    val color = when {
        diffDays < 0 -> Red
        diffDays < 3 -> Red
        diffDays < 7 -> Orange
        else -> Green
    }

    val text = when {
        diffDays < 0 -> "Vencido hace ${-diffDays}d"
        diffDays == 0 -> "Vence hoy"
        diffDays == 1 -> "Vence mañana"
        else -> "Faltan ${diffDays}d"
    }

    Text(
        text = text,
        color = color,
        fontWeight = FontWeight.Medium
    )
}