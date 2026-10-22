package org.softwareanvil.musa.ui.sales.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.softwareanvil.musa.domain.logic.SalesFilter
import org.softwareanvil.musa.domain.logic.SalesPeriod
import org.softwareanvil.musa.domain.logic.SalesQuery
import org.softwareanvil.musa.domain.logic.SalesSummary
import org.softwareanvil.musa.domain.model.Sale
import org.softwareanvil.musa.domain.model.SaleChannel
import org.softwareanvil.musa.domain.model.SaleId
import org.softwareanvil.musa.domain.usecase.DeleteSaleUseCase
import org.softwareanvil.musa.domain.usecase.ObserveSalesUseCase
import org.softwareanvil.musa.domain.usecase.SaleResult
import org.softwareanvil.musa.ui.sales.message

/** Everything the sales list draws. */
data class SaleListState(
	val isLoading: Boolean = true,
	val query: SalesQuery = SalesQuery(),
	val sales: List<Sale> = emptyList(),
	val summary: SalesSummary = SalesSummary.of(emptyList()),
	val hasAnySale: Boolean = false,
	val opened: Sale? = null,
	val isConfirmingDelete: Boolean = false,
)

/** Drives the sales list: period and channel filters, the summary and the detail of one sale. */
class SaleListViewModel(
	observeSales: ObserveSalesUseCase,
	private val deleteSale: DeleteSaleUseCase,
	private val clock: Clock,
	private val timeZone: TimeZone,
) : ViewModel() {

	private val query = MutableStateFlow(SalesQuery())
	private val opened = MutableStateFlow<SaleId?>(null)
	private val confirmingDelete = MutableStateFlow(false)

	private val _messages = Channel<String>(Channel.BUFFERED)
	val messages: Flow<String> = _messages.receiveAsFlow()

	val state: StateFlow<SaleListState> = combine(observeSales(), query, opened, confirmingDelete) { all, current, openedId, confirming ->
		val filtered = SalesFilter.apply(all, current, clock.todayIn(timeZone), timeZone)
		SaleListState(
			isLoading = false,
			query = current,
			sales = filtered,
			summary = SalesSummary.of(filtered),
			hasAnySale = all.isNotEmpty(),
			opened = all.firstOrNull { it.id == openedId },
			isConfirmingDelete = confirming,
		)
	}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SaleListState())

	fun onPeriodSelect(period: SalesPeriod) = query.update { it.copy(period = period) }

	/** Selecting the active channel again shows every channel. */
	fun onChannelToggle(channel: SaleChannel) = query.update { it.copy(channel = channel.takeIf { _ -> it.channel != channel }) }

	fun onSaleOpen(id: SaleId) = opened.update { id }

	fun onSaleDismiss() {
		opened.update { null }
		confirmingDelete.update { false }
	}

	fun onDeleteRequest() = confirmingDelete.update { true }

	fun onDeleteDismiss() = confirmingDelete.update { false }

	fun onDeleteConfirm() {
		val id = opened.value ?: return
		confirmingDelete.update { false }
		viewModelScope.launch {
			when (val result = deleteSale(id)) {
				is SaleResult.Done -> {
					opened.update { null }
					_messages.send("Venta eliminada; el stock ha vuelto al catálogo")
				}
				is SaleResult.Rejected -> _messages.send(result.errors.joinToString(". ") { it.message() })
			}
		}
	}
}
