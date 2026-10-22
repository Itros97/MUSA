package org.softwareanvil.musa.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import org.softwareanvil.musa.ui.components.Pill
import org.softwareanvil.musa.ui.theme.ToneColors

private const val UNAVAILABLE_ALPHA = 0.55f

/** Card that opens a section; dimmed and disabled while the section is not built yet. */
@Composable
fun SectionTile(section: HomeSection, onClick: () -> Unit, modifier: Modifier = Modifier) {
	val tones = section.tone.colors()
	Card(
		onClick = onClick,
		enabled = section.isAvailable,
		modifier = modifier.fillMaxWidth().heightIn(min = 168.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
			// Dimming comes from UNAVAILABLE_ALPHA alone, so the card itself keeps its enabled colours.
			disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
			disabledContentColor = MaterialTheme.colorScheme.onSurface,
		),
	) {
		Column(
			modifier = Modifier.padding(20.dp).alpha(if (section.isAvailable) 1f else UNAVAILABLE_ALPHA),
			verticalArrangement = Arrangement.spacedBy(16.dp),
		) {
			Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
				ToneIcon(section, tones)
				if (!section.isAvailable) Pill("Próximamente", tones)
			}
			Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
				Text(section.title, style = MaterialTheme.typography.titleLarge)
				Text(
					section.description,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}
		}
	}
}

@Composable
private fun ToneIcon(section: HomeSection, tones: ToneColors) {
	Box(
		modifier = Modifier.size(52.dp).background(tones.container, CircleShape),
		contentAlignment = Alignment.Center,
	) {
		Icon(section.icon, contentDescription = null, tint = tones.content, modifier = Modifier.size(28.dp))
	}
}
