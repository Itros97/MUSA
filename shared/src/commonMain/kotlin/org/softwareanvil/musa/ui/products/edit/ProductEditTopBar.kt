package org.softwareanvil.musa.ui.products.edit

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.ui.components.MusaIcons

/** Close, title, save and — for stored products — duplicate, archive and delete. */
@Composable
fun ProductEditTopBar(state: ProductEditState, viewModel: ProductEditViewModel) {
	val form = state.form
	TopAppBar(
		title = {
			Text(
				when {
					!state.isNew -> "Editar producto"
					form?.kind == ProductKind.DIGITAL -> "Nuevo producto digital"
					else -> "Nuevo producto físico"
				},
			)
		},
		navigationIcon = { IconButton(onClick = viewModel::onBack) { Icon(Icons.Filled.Close, contentDescription = "Cerrar") } },
		actions = {
			TextButton(onClick = viewModel::onSave, enabled = form != null && !state.isSaving) { Text("Guardar") }
			if (!state.isNew && form != null) OverflowMenu(archived = form.status == ProductStatus.ARCHIVED, viewModel)
		},
	)
}

@Composable
private fun OverflowMenu(archived: Boolean, viewModel: ProductEditViewModel) {
	var open by remember { mutableStateOf(false) }
	Box {
		IconButton(onClick = { open = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "Más acciones") }
		DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
			MenuItem("Duplicar", { Icon(MusaIcons.Copy, null) }) { open = false; viewModel.onDuplicate() }
			MenuItem(if (archived) "Restaurar" else "Archivar", { Icon(if (archived) MusaIcons.Unarchive else MusaIcons.Archive, null) }) {
				open = false
				viewModel.onArchiveToggle()
			}
			MenuItem("Eliminar", { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error) }) {
				open = false
				viewModel.onDeleteRequest()
			}
		}
	}
}

@Composable
private fun MenuItem(label: String, icon: @Composable () -> Unit, onClick: () -> Unit) =
	DropdownMenuItem(text = { Text(label) }, leadingIcon = icon, onClick = onClick)
