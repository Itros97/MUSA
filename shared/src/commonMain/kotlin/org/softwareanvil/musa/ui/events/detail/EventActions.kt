package org.softwareanvil.musa.ui.events.detail

import org.softwareanvil.musa.domain.usecase.AddExpenseUseCase
import org.softwareanvil.musa.domain.usecase.CloseEventUseCase
import org.softwareanvil.musa.domain.usecase.DeleteEventUseCase
import org.softwareanvil.musa.domain.usecase.DeleteExpenseUseCase
import org.softwareanvil.musa.domain.usecase.MoveStockToEventUseCase
import org.softwareanvil.musa.domain.usecase.RecordEventSaleUseCase
import org.softwareanvil.musa.domain.usecase.ReturnStockFromEventUseCase
import org.softwareanvil.musa.domain.usecase.UndoSaleUseCase

/** The use cases the event screen can trigger, grouped so the ViewModel takes one dependency. */
class EventActions(
	val moveStock: MoveStockToEventUseCase,
	val returnStock: ReturnStockFromEventUseCase,
	val recordSale: RecordEventSaleUseCase,
	val undoSale: UndoSaleUseCase,
	val addExpense: AddExpenseUseCase,
	val deleteExpense: DeleteExpenseUseCase,
	val close: CloseEventUseCase,
	val delete: DeleteEventUseCase,
)
