package org.softwareanvil.musa.ui.sales.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.softwareanvil.musa.domain.logic.IdGenerator
import org.softwareanvil.musa.domain.logic.MoneyFormatter
import org.softwareanvil.musa.domain.logic.ProductQuery
import org.softwareanvil.musa.domain.model.SaleChannel
import org.softwareanvil.musa.domain.model.SaleLineId
import org.softwareanvil.musa.domain.usecase.ObserveProductsUseCase
import org.softwareanvil.musa.domain.usecase.RecordSaleUseCase
import org.softwareanvil.musa.domain.usecase.SaleResult

/** Drives the new-sale editor: channel, date, customer, lines from the catalogue and shipping. */
class SaleEditorViewModel(
	observeProducts: ObserveProductsUseCase,
	private val recordSale: RecordSaleUseCase,
	private val clock: Clock,
	private val timeZone: TimeZone,
	private val ids: IdGenerator,
) : ViewModel() {

	private val _state = MutableStateFlow(SaleEditorState(form = SaleForm(date = clock.todayIn(timeZone))))
	val state: StateFlow<SaleEditorState> = _state.asStateFlow()

	private val _closed = Channel<Unit>(Channel.BUFFERED)
	val closed: Flow<Unit> = _closed.receiveAsFlow()

	init {
		viewModelScope.launch {
			observeProducts(flowOf(ProductQuery())).collect { catalogue ->
				_state.update { it.copy(options = SaleOption.from(catalogue.products)) }
			}
		}
	}

	// region fields

	fun onChannelSelect(channel: SaleChannel) = updateForm { it.copy(channel = channel) }

	fun onDateChange(date: LocalDate) = updateForm { it.copy(date = date) }

	fun onCustomerChange(value: String) = updateForm { it.copy(customer = value) }

	fun onNoteChange(value: String) = updateForm { it.copy(note = value) }

	fun onShippingChargedChange(value: String) = updateForm(SaleField.Shipping) { it.copy(shippingCharged = value) }

	fun onShippingCostChange(value: String) = updateForm(SaleField.Shipping) { it.copy(shippingCost = value) }

	// endregion

	// region lines

	fun onPickerOpen() = _state.update { it.copy(isPicking = true) }

	fun onPickerDismiss() = _state.update { it.copy(isPicking = false) }

	/** Adds one unit at the variant's price; both can be changed afterwards. */
	fun onOptionPick(option: SaleOption) {
		val line = LineForm(SaleLineId(ids.next()), option, quantity = 1, price = MoneyFormatter.formatForInput(option.price))
		updateForm(SaleField.Lines) { it.copy(lines = it.lines + line) }
		onPickerDismiss()
	}

	fun onQuantityStep(id: SaleLineId, delta: Int) = updateLine(id, SaleField.LineQuantity(id)) { line ->
		line.copy(quantity = (line.quantity + delta).coerceIn(1, line.option.stock ?: Int.MAX_VALUE))
	}

	fun onPriceChange(id: SaleLineId, text: String) = updateLine(id, SaleField.LinePrice(id)) { it.copy(price = text) }

	fun onLineRemove(id: SaleLineId) = updateForm { form -> form.copy(lines = form.lines.filterNot { it.id == id }) }

	// endregion

	// region save and close

	fun onSave() {
		val form = _state.value.form ?: return
		if (_state.value.isSaving) return
		val parsed = SaleFormParser.parse(form, clock, timeZone)
		if (parsed.errors.isNotEmpty()) return _state.update { it.copy(errors = parsed.errors) }
		_state.update { it.copy(isSaving = true) }
		viewModelScope.launch {
			when (val result = recordSale(parsed.draft)) {
				is SaleResult.Done -> _closed.send(Unit)
				is SaleResult.Rejected -> _state.update { it.copy(errors = SaleFormParser.fieldErrors(result.errors, form)) }
			}
			_state.update { it.copy(isSaving = false) }
		}
	}

	/** Closing with lines asks first, so a half-typed order is not lost by accident. */
	fun onClose() {
		if (_state.value.form?.lines.orEmpty().isNotEmpty()) return _state.update { it.copy(isConfirmingDiscard = true) }
		viewModelScope.launch { _closed.send(Unit) }
	}

	fun onDiscardDismiss() = _state.update { it.copy(isConfirmingDiscard = false) }

	fun onDiscardConfirm() {
		onDiscardDismiss()
		viewModelScope.launch { _closed.send(Unit) }
	}

	// endregion

	private fun updateLine(id: SaleLineId, clearing: SaleField, transform: (LineForm) -> LineForm) =
		updateForm(clearing) { form -> form.copy(lines = form.lines.map { if (it.id == id) transform(it) else it }) }

	private fun updateForm(clearing: SaleField? = null, transform: (SaleForm) -> SaleForm) = _state.update { state ->
		val form = state.form ?: return@update state
		state.copy(form = transform(form), errors = clearing?.let { state.errors - it } ?: state.errors)
	}
}
