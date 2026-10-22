package org.softwareanvil.musa.ui.events.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.softwareanvil.musa.domain.model.EventDetail
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.ui.components.EmptyState
import org.softwareanvil.musa.ui.components.MusaIcons
import org.softwareanvil.musa.ui.events.whenAndWhereLabel

/** One event: summary, stock taken, sales and expenses. */
@Composable
fun EventDetailScreen(
	eventId: EventId,
	onBack: () -> Unit,
	viewModel: EventDetailViewModel = koinViewModel(key = eventId.value) { parametersOf(eventId) },
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val snackbar = remember { SnackbarHostState() }
	LaunchedEffect(viewModel) {
		viewModel.events.collect { event ->
			when (event) {
				DetailEvent.Close -> onBack()
				is DetailEvent.Message -> snackbar.showSnackbar(event.text)
			}
		}
	}

	Scaffold(topBar = { EventDetailTopBar(state, viewModel, onBack) }, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
		val detail = state.detail
		when {
			state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
			detail == null -> EmptyState(MusaIcons.Event, "Este evento ya no existe", "Puede que lo hayas eliminado.", Modifier.padding(padding))
			else -> Column(
				modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Column(modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
					DetailContent(detail, state, viewModel)
				}
			}
		}
	}

	EventDetailOverlays(state, viewModel)
}

@Composable
private fun DetailContent(detail: EventDetail, state: EventDetailState, viewModel: EventDetailViewModel) {
	val isOpen = detail.event.isOpen
	Text(detail.event.whenAndWhereLabel(), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
	if (!isOpen) {
		Text(
			"Evento cerrado. Lo que no se vendió volvió al catálogo y ya no se puede cambiar.",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
		)
	}
	state.summary?.let { SummaryCard(it) }
	StockSection(
		lines = detail.stock,
		isOpen = isOpen,
		onSell = { viewModel.onSheetOpen(DetailSheet.Sell(it)) },
		onStep = { variantId, delta -> if (delta > 0) viewModel.onTakeOneMore(variantId) else viewModel.onReturnOne(variantId) },
		onAddClick = { viewModel.onSheetOpen(DetailSheet.AddStock) },
	)
	SalesSection(detail.sales, isOpen, onUndo = viewModel::onUndoSale)
	ExpensesSection(
		expenses = detail.expenses,
		isOpen = isOpen,
		onDelete = { viewModel.onDeleteExpense(it.id) },
		onAddClick = { viewModel.onSheetOpen(DetailSheet.AddExpense) },
	)
}
