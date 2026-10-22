package org.softwareanvil.musa.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.softwareanvil.musa.ui.components.ConfirmDialog
import org.softwareanvil.musa.ui.components.MusaIcons
import org.softwareanvil.musa.ui.components.SectionCard

/** Development-only card to fill the database with toy data or empty it; results show in [snackbar]. */
@Composable
fun DevToolsCard(snackbar: SnackbarHostState, modifier: Modifier = Modifier, viewModel: DevToolsViewModel = koinViewModel()) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	LaunchedEffect(viewModel) { viewModel.messages.collect { snackbar.showSnackbar(it) } }

	SectionCard(
		title = "Herramientas de prueba",
		subtitle = "Solo en builds de desarrollo. Rellena la base de datos con datos de juguete o vacíala para empezar de cero.",
		modifier = modifier,
	) {
		FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
			Button(onClick = viewModel::onFillClick, enabled = !state.isBusy) {
				Icon(MusaIcons.Palette, contentDescription = null)
				Text("Rellenar con datos de prueba")
			}
			OutlinedButton(
				onClick = viewModel::onWipeClick,
				enabled = !state.isBusy,
				colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
			) {
				Icon(Icons.Filled.Delete, contentDescription = null)
				Text("Vaciar base de datos")
			}
		}
	}

	if (state.isConfirmingWipe) {
		ConfirmDialog(
			title = "¿Vaciar la base de datos?",
			message = "Se borrarán todos los productos, etiquetas, eventos, ventas y gastos. No se puede deshacer.",
			confirmLabel = "Vaciar",
			onConfirm = viewModel::onWipeConfirm,
			onDismiss = viewModel::onWipeDismiss,
			destructive = true,
		)
	}
}
