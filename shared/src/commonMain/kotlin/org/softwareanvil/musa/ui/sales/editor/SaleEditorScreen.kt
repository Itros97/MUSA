package org.softwareanvil.musa.ui.sales.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.softwareanvil.musa.ui.components.ConfirmDialog
import org.softwareanvil.musa.ui.components.DateField
import org.softwareanvil.musa.ui.components.MusaTextField
import org.softwareanvil.musa.ui.components.SectionCard

/** Record a sale outside events: channel, date, customer, products and shipping. */
@Composable
fun SaleEditorScreen(onClose: () -> Unit, viewModel: SaleEditorViewModel = koinViewModel()) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	LaunchedEffect(viewModel) { viewModel.closed.collect { onClose() } }

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Nueva venta") },
				navigationIcon = { IconButton(onClick = viewModel::onClose) { Icon(Icons.Filled.Close, contentDescription = "Cerrar") } },
				actions = { TextButton(onClick = viewModel::onSave, enabled = !state.isSaving) { Text("Guardar") } },
			)
		},
	) { padding ->
		val form = state.form ?: return@Scaffold
		Column(
			modifier = Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			Column(modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
				SectionCard(title = "Venta") {
					ChannelChips(form.channel, viewModel::onChannelSelect)
					Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
						DateField(form.date, viewModel::onDateChange, "Fecha", Modifier.weight(1f))
						MusaTextField(form.customer, viewModel::onCustomerChange, "Cliente", Modifier.weight(1f), placeholder = "Nombre o @usuario")
					}
					MusaTextField(form.note, viewModel::onNoteChange, "Nota", Modifier.fillMaxWidth(), placeholder = "Nº de pedido, tienda…")
				}
				LinesSection(state, form, viewModel)
				ShippingAndTotals(state, form, viewModel)
				Button(onClick = viewModel::onSave, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
					Text("Guardar venta", modifier = Modifier.padding(vertical = 6.dp))
				}
			}
		}
	}

	if (state.isPicking) LinePickerSheet(state.options, onPick = viewModel::onOptionPick, onDismiss = viewModel::onPickerDismiss)
	if (state.isConfirmingDiscard) {
		ConfirmDialog(
			title = "¿Descartar la venta?",
			message = "Aún no la has guardado.",
			confirmLabel = "Descartar",
			onConfirm = viewModel::onDiscardConfirm,
			onDismiss = viewModel::onDiscardDismiss,
			destructive = true,
		)
	}
}
