package org.softwareanvil.musa.ui.sales.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import org.softwareanvil.musa.ui.components.ConfirmDialog
import org.softwareanvil.musa.ui.components.EmptyState
import org.softwareanvil.musa.ui.components.MusaIcons

/** Every sale of every channel, with period and channel filters, a summary and a button to record one. */
@Composable
fun SaleListScreen(onCreateSale: () -> Unit, onBack: () -> Unit, viewModel: SaleListViewModel = koinViewModel()) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val snackbar = remember { SnackbarHostState() }
	LaunchedEffect(viewModel) { viewModel.messages.collect { snackbar.showSnackbar(it) } }

	Scaffold(
		snackbarHost = { SnackbarHost(snackbar) },
		floatingActionButton = {
			ExtendedFloatingActionButton(onClick = onCreateSale, icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Nueva venta") })
		},
	) { padding ->
		if (state.isLoading) {
			Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
			return@Scaffold
		}
		LazyVerticalGrid(
			columns = GridCells.Adaptive(minSize = 340.dp),
			contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
			modifier = Modifier.fillMaxSize().padding(padding),
		) {
			fullWidth { Header(onBack) }
			if (!state.hasAnySale) {
				fullWidth {
					EmptyState(
						icon = MusaIcons.Sales,
						title = "Aún no hay ventas",
						message = "Apunta un pedido online, una venta por redes o en persona. Las ventas de tus eventos también saldrán aquí.",
						actionLabel = "Apuntar tu primera venta",
						onAction = onCreateSale,
					)
				}
				return@LazyVerticalGrid
			}
			fullWidth { SaleFilters(state.query, viewModel::onPeriodSelect, viewModel::onChannelToggle) }
			fullWidth { SalesSummaryCard(state.summary) }
			if (state.sales.isEmpty()) {
				fullWidth { EmptyState(Icons.Filled.Search, "Nada por aquí", "No hay ventas en este periodo o canal.") }
			}
			items(state.sales, key = { it.id.value }) { sale -> SaleCard(sale, onClick = { viewModel.onSaleOpen(sale.id) }) }
		}
	}

	state.opened?.let { sale ->
		SaleDetailSheet(sale, onDelete = viewModel::onDeleteRequest, onDismiss = viewModel::onSaleDismiss)
	}
	if (state.isConfirmingDelete) {
		ConfirmDialog(
			title = "¿Eliminar esta venta?",
			message = "Los productos físicos volverán al catálogo.",
			confirmLabel = "Eliminar",
			onConfirm = viewModel::onDeleteConfirm,
			onDismiss = viewModel::onDeleteDismiss,
			destructive = true,
		)
	}
}

@Composable
private fun Header(onBack: () -> Unit) {
	Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
				Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver al inicio")
			}
			Text("Ventas", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.offset(x = (-8).dp))
		}
		Text("Todos los canales, con su margen", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
	}
}

private fun LazyGridScope.fullWidth(content: @Composable () -> Unit) =
	item(span = { GridItemSpan(maxLineSpan) }) { content() }
