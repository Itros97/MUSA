package org.softwareanvil.project.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.softwareanvil.project.domain.model.CommissionStatus
import org.softwareanvil.project.ui.theme.Green
import org.softwareanvil.project.ui.theme.Orange
import org.softwareanvil.project.ui.theme.Pink
import org.softwareanvil.project.ui.theme.Purple
import org.softwareanvil.project.ui.theme.Red

fun CommissionStatus.color(): Color = when (this) {
    CommissionStatus.INQUIRY -> Purple
    CommissionStatus.ACCEPTED -> Color(0xFF2196F3)
    CommissionStatus.IN_PROGRESS -> Orange
    CommissionStatus.PAUSED -> Color(0xFF9E9E9E)
    CommissionStatus.REVIEW -> Pink
    CommissionStatus.COMPLETED -> Green
    CommissionStatus.CANCELLED -> Red
    CommissionStatus.REJECTED -> Color(0xFF795548)
}

fun CommissionStatus.label(): String = when (this) {
    CommissionStatus.INQUIRY -> "Consulta"
    CommissionStatus.ACCEPTED -> "Aceptada"
    CommissionStatus.IN_PROGRESS -> "En progreso"
    CommissionStatus.PAUSED -> "Pausada"
    CommissionStatus.REVIEW -> "En revisión"
    CommissionStatus.COMPLETED -> "Completada"
    CommissionStatus.CANCELLED -> "Cancelada"
    CommissionStatus.REJECTED -> "Rechazada"
}

@Composable
fun StatusChip(status: CommissionStatus) {
    AssistChip(
        onClick = {},
        label = { Text(status.label()) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = status.color().copy(alpha = 0.15f),
            labelColor = status.color()
        )
    )
}