package org.softwareanvil.musa.ui.events.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.ui.components.EmptyState
import org.softwareanvil.musa.ui.components.MusaIcons
import org.softwareanvil.musa.ui.events.edit.EventEditSheet
import org.softwareanvil.musa.ui.events.edit.EventEditTarget

/** Every fair and market, open ones first, with a button to add another. */
@Composable
fun EventListScreen(onOpenEvent: (EventId) -> Unit, onBack: () -> Unit, viewModel: EventListViewModel = koinViewModel()) {
	val state by viewModel.state.collectAsStateWithLifecycle()

	Scaffold(
		floatingActionButton = {
			ExtendedFloatingActionButton(
				onClick = viewModel::onCreateClick,
				icon = { Icon(Icons.Filled.Add, contentDescription = null) },
				text = { Text("Nuevo evento") },
			)
		},
	) { padding ->
		if (state.isLoading) {
			Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
			return@Scaffold
		}
		LazyVerticalGrid(
			columns = GridCells.Adaptive(minSize = 340.dp),
			contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
			modifier = Modifier.fillMaxSize().padding(padding),
		) {
			item(span = { GridItemSpan(maxLineSpan) }) { Header(state.events.size, onBack) }
			if (state.events.isEmpty()) {
				item(span = { GridItemSpan(maxLineSpan) }) {
					EmptyState(
						icon = MusaIcons.Event,
						title = "Aún no hay eventos",
						message = "Crea una feria o un mercado, apunta lo que te llevas y registra cada venta en el momento.",
						actionLabel = "Crear tu primer evento",
						onAction = viewModel::onCreateClick,
					)
				}
			}
			items(state.events, key = { it.id.value }) { event -> EventCard(event, onClick = { onOpenEvent(event.id) }) }
		}
	}

	if (state.isCreating) {
		EventEditSheet(
			target = EventEditTarget.New,
			onSaved = { id ->
				viewModel.onCreateDismiss()
				onOpenEvent(id)
			},
			onDismiss = viewModel::onCreateDismiss,
		)
	}
}

@Composable
private fun Header(count: Int, onBack: () -> Unit) {
	Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
				Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver al inicio")
			}
			Text("Eventos", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.offset(x = (-8).dp))
		}
		val subtitle = if (count == 0) "Ferias y mercados" else "$count ${if (count == 1) "evento" else "eventos"}"
		Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
	}
}
