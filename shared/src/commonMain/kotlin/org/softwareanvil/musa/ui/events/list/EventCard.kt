package org.softwareanvil.musa.ui.events.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.softwareanvil.musa.domain.model.Event
import org.softwareanvil.musa.ui.components.Pill
import org.softwareanvil.musa.ui.events.label
import org.softwareanvil.musa.ui.events.whenAndWhereLabel
import org.softwareanvil.musa.ui.theme.ToneColors

/** One event in the list; closed events are dimmed. */
@Composable
fun EventCard(event: Event, onClick: () -> Unit, modifier: Modifier = Modifier) {
	Card(
		onClick = onClick,
		modifier = modifier.fillMaxWidth().alpha(if (event.isOpen) 1f else 0.7f),
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
	) {
		Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
			Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
				Text(event.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
				Text(event.whenAndWhereLabel(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
			}
			val scheme = MaterialTheme.colorScheme
			val tones = if (event.isOpen) ToneColors(scheme.tertiaryContainer, scheme.onTertiaryContainer)
			else ToneColors(scheme.surfaceContainerHighest, scheme.onSurfaceVariant)
			Pill(event.status.label(), tones)
		}
	}
}
