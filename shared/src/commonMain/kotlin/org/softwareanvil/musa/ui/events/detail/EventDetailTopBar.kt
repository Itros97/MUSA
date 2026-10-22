package org.softwareanvil.musa.ui.events.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow

/** Back, the event name, edit and — in the menu — close and delete. */
@Composable
fun EventDetailTopBar(state: EventDetailState, viewModel: EventDetailViewModel, onBack: () -> Unit) {
	val event = state.detail?.event
	TopAppBar(
		title = { Text(event?.name.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
		navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver a eventos") } },
		actions = {
			if (event == null) return@TopAppBar
			IconButton(onClick = { viewModel.onSheetOpen(DetailSheet.Edit) }) { Icon(Icons.Filled.Edit, contentDescription = "Editar evento") }
			OverflowMenu(isOpen = event.isOpen, onDialog = viewModel::onDialogOpen)
		},
	)
}

@Composable
private fun OverflowMenu(isOpen: Boolean, onDialog: (DetailDialog) -> Unit) {
	var expanded by remember { mutableStateOf(false) }
	Box {
		IconButton(onClick = { expanded = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "Más acciones") }
		DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
			if (isOpen) {
				DropdownMenuItem(
					text = { Text("Cerrar evento") },
					leadingIcon = { Icon(Icons.Filled.Lock, null) },
					onClick = { expanded = false; onDialog(DetailDialog.CLOSE) },
				)
			}
			DropdownMenuItem(
				text = { Text("Eliminar") },
				leadingIcon = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error) },
				onClick = { expanded = false; onDialog(DetailDialog.DELETE) },
			)
		}
	}
}
