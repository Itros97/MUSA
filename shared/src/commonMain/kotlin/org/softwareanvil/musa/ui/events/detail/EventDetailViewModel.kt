package org.softwareanvil.musa.ui.events.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.softwareanvil.musa.domain.logic.EventSummary
import org.softwareanvil.musa.domain.logic.MoneyFormatter
import org.softwareanvil.musa.domain.logic.MoneyParser
import org.softwareanvil.musa.domain.logic.ProductQuery
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.model.ExpenseId
import org.softwareanvil.musa.domain.model.SaleId
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.domain.usecase.EventResult
import org.softwareanvil.musa.domain.usecase.ObserveEventDetailUseCase
import org.softwareanvil.musa.domain.usecase.ObserveProductsUseCase
import org.softwareanvil.musa.ui.events.message

/** Drives one event: its stock, sales, expenses and closing. */
class EventDetailViewModel(
	private val eventId: EventId,
	observeDetail: ObserveEventDetailUseCase,
	observeProducts: ObserveProductsUseCase,
	private val actions: EventActions,
) : ViewModel() {

	private val ui = MutableStateFlow(EventDetailState())

	private val _events = Channel<DetailEvent>(Channel.BUFFERED)
	val events: Flow<DetailEvent> = _events.receiveAsFlow()

	val state: StateFlow<EventDetailState> = combine(observeDetail(eventId), observeProducts(flowOf(ProductQuery())), ui) { detail, catalogue, ui ->
		ui.copy(
			isLoading = false,
			detail = detail,
			summary = detail?.let(EventSummary::of),
			options = StockOption.from(catalogue.products),
		)
	}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EventDetailState())

	// region sheets and dialogs

	fun onSheetOpen(sheet: DetailSheet) = ui.update { it.copy(sheet = sheet, sheetError = null) }

	fun onSheetDismiss() = ui.update { it.copy(sheet = null, sheetError = null) }

	fun onDialogOpen(dialog: DetailDialog) = ui.update { it.copy(dialog = dialog) }

	fun onDialogDismiss() = ui.update { it.copy(dialog = null) }

	// endregion

	// region stock and sales

	fun onAddStock(variantId: VariantId, quantity: Int) =
		run({ actions.moveStock(eventId, variantId, quantity) }) { onSheetDismiss() }

	fun onTakeOneMore(variantId: VariantId) = run({ actions.moveStock(eventId, variantId, 1) })

	fun onReturnOne(variantId: VariantId) = run({ actions.returnStock(eventId, variantId, 1) })

	fun onSell(variantId: VariantId, quantity: Int, priceText: String) {
		val price = MoneyParser.parse(priceText) ?: return ui.update { it.copy(sheetError = "Escribe un precio válido, como 12 o 12,50") }
		run({ actions.recordSale(eventId, variantId, quantity, price) }) {
			onSheetDismiss()
			_events.send(DetailEvent.Message("Venta registrada · ${MoneyFormatter.format(price * quantity)}"))
		}
	}

	fun onUndoSale(id: SaleId) = run({ actions.undoSale(id) }) { _events.send(DetailEvent.Message("Venta deshecha")) }

	// endregion

	// region expenses and lifecycle

	fun onAddExpense(concept: String, amountText: String) {
		val amount = MoneyParser.parse(amountText) ?: return ui.update { it.copy(sheetError = "Escribe un importe válido, como 40 o 12,50") }
		run({ actions.addExpense(eventId, concept, amount) }) { onSheetDismiss() }
	}

	fun onDeleteExpense(id: ExpenseId) = run({ actions.deleteExpense(eventId, id) })

	fun onCloseConfirm() {
		onDialogDismiss()
		run({ actions.close(eventId) }) { _events.send(DetailEvent.Message("Evento cerrado; lo que sobró ha vuelto al catálogo")) }
	}

	fun onDeleteConfirm() {
		onDialogDismiss()
		run({ actions.delete(eventId) }) { _events.send(DetailEvent.Close) }
	}

	// endregion

	/** Runs an action; a rejection shows inside the open sheet, or as a message when there is none. */
	private fun run(action: suspend () -> EventResult, onDone: suspend () -> Unit = {}) {
		viewModelScope.launch {
			when (val result = action()) {
				EventResult.Done -> onDone()
				is EventResult.Rejected -> {
					val text = result.errors.joinToString(". ") { it.message() }
					if (ui.value.sheet != null) ui.update { it.copy(sheetError = text) } else _events.send(DetailEvent.Message(text))
				}
			}
		}
	}
}
