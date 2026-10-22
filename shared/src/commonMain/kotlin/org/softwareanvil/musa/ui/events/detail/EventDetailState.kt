package org.softwareanvil.musa.ui.events.detail

import org.softwareanvil.musa.domain.logic.EventSummary
import org.softwareanvil.musa.domain.model.EventDetail
import org.softwareanvil.musa.domain.model.VariantId

/** Everything the event screen draws; [detail] is null while loading or once the event is gone. */
data class EventDetailState(
	val isLoading: Boolean = true,
	val detail: EventDetail? = null,
	val summary: EventSummary? = null,
	val options: List<StockOption> = emptyList(),
	val sheet: DetailSheet? = null,
	val sheetError: String? = null,
	val dialog: DetailDialog? = null,
)

/** Bottom sheets of the event screen. */
sealed interface DetailSheet {
	data object AddStock : DetailSheet
	data class Sell(val variantId: VariantId) : DetailSheet
	data object AddExpense : DetailSheet
	data object Edit : DetailSheet
}

/** Confirmation dialogs of the event screen. */
enum class DetailDialog { CLOSE, DELETE }

/** One-off things the screen must do. */
sealed interface DetailEvent {
	data object Close : DetailEvent
	data class Message(val text: String) : DetailEvent
}
