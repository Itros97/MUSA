package org.softwareanvil.musa.ui.events.detail

import androidx.compose.runtime.Composable
import org.softwareanvil.musa.ui.components.ConfirmDialog
import org.softwareanvil.musa.ui.events.edit.EventEditSheet
import org.softwareanvil.musa.ui.events.edit.EventEditTarget

/** The sheet or dialog currently open on the event screen, if any. */
@Composable
fun EventDetailOverlays(state: EventDetailState, viewModel: EventDetailViewModel) {
	val detail = state.detail ?: return
	when (val sheet = state.sheet) {
		DetailSheet.AddStock -> AddStockSheet(state.options, state.sheetError, onAdd = viewModel::onAddStock, onDismiss = viewModel::onSheetDismiss)
		is DetailSheet.Sell -> detail.lineOf(sheet.variantId)?.let { line ->
			SaleSheet(line, state.sheetError, onSell = { quantity, price -> viewModel.onSell(line.variantId, quantity, price) }, onDismiss = viewModel::onSheetDismiss)
		}
		DetailSheet.AddExpense -> ExpenseSheet(state.sheetError, onAdd = viewModel::onAddExpense, onDismiss = viewModel::onSheetDismiss)
		DetailSheet.Edit -> EventEditSheet(
			target = EventEditTarget.Existing(detail.event.id),
			onSaved = { viewModel.onSheetDismiss() },
			onDismiss = viewModel::onSheetDismiss,
		)
		null -> Unit
	}
	when (state.dialog) {
		DetailDialog.CLOSE -> ConfirmDialog(
			title = "¿Cerrar «${detail.event.name}»?",
			message = "Lo que no hayas vendido volverá al catálogo y el evento quedará solo para consultar.",
			confirmLabel = "Cerrar evento",
			onConfirm = viewModel::onCloseConfirm,
			onDismiss = viewModel::onDialogDismiss,
		)
		DetailDialog.DELETE -> ConfirmDialog(
			title = "¿Eliminar «${detail.event.name}»?",
			message = "El stock que llevaste volverá al catálogo. Si ya tiene ventas, ciérralo en vez de eliminarlo.",
			confirmLabel = "Eliminar",
			onConfirm = viewModel::onDeleteConfirm,
			onDismiss = viewModel::onDialogDismiss,
			destructive = true,
		)
		null -> Unit
	}
}
