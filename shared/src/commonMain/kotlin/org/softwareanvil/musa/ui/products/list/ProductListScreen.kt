package org.softwareanvil.musa.ui.products.list

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.ui.components.EmptyState
import org.softwareanvil.musa.ui.components.MusaIcons

/** The catalogue: every product with search, filters and a button to create more. */
@Composable
fun ProductListScreen(
	onOpenProduct: (ProductId) -> Unit,
	onCreateProduct: (ProductKind) -> Unit,
	onBack: () -> Unit,
	viewModel: ProductListViewModel = koinViewModel(),
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	var showKindPicker by remember { mutableStateOf(false) }

	Scaffold(
		floatingActionButton = {
			ExtendedFloatingActionButton(
				onClick = { showKindPicker = true },
				icon = { Icon(Icons.Filled.Add, contentDescription = null) },
				text = { Text("Nuevo producto") },
			)
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
			fullWidth { Header(state, onBack) }
			if (!state.isCatalogueEmpty) {
				fullWidth {
					ProductFilterBar(
						query = state.query,
						tags = state.tags,
						onSearchChange = viewModel::onSearchChange,
						onKindToggle = viewModel::onKindToggle,
						onTagToggle = viewModel::onTagToggle,
						onLowStockToggle = viewModel::onLowStockToggle,
						onArchivedToggle = viewModel::onArchivedToggle,
						modifier = Modifier.padding(bottom = 4.dp),
					)
				}
			}
			when {
				state.isCatalogueEmpty -> fullWidth {
					EmptyState(
						icon = MusaIcons.Digital,
						title = "Tu catálogo está vacío",
						message = "Añade tu primera lámina, pegatina o comisión y empieza a llevar la cuenta.",
						actionLabel = "Crear tu primer producto",
						onAction = { showKindPicker = true },
					)
				}
				state.products.isEmpty() -> fullWidth {
					EmptyState(
						icon = Icons.Filled.Search,
						title = "Nada por aquí",
						message = "Ningún producto coincide con la búsqueda o los filtros.",
						actionLabel = "Quitar filtros",
						onAction = viewModel::onClearFilters,
					)
				}
				else -> items(state.products, key = { it.id.value }) { product ->
					ProductCard(
						product = product,
						onClick = { onOpenProduct(product.id) },
						onStockClick = { viewModel.onStockClick(product.id) },
					)
				}
			}
		}
	}

	if (showKindPicker) {
		KindPickerSheet(
			onPick = { kind ->
				showKindPicker = false
				onCreateProduct(kind)
			},
			onDismiss = { showKindPicker = false },
		)
	}
	state.stockSheet?.let { product ->
		StockAdjustSheet(product, onStep = viewModel::onStockStep, onDismiss = viewModel::onStockSheetDismiss)
	}
}

@Composable
private fun Header(state: ProductListState, onBack: () -> Unit) {
	Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
				Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver al inicio")
			}
			Text("Catálogo", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.offset(x = (-8).dp))
		}
		val count = state.products.size
		val subtitle = when {
			state.isCatalogueEmpty -> "Todo lo que vendes, en un sitio"
			state.query.isFiltering -> "$count ${if (count == 1) "resultado" else "resultados"}"
			else -> "$count ${if (count == 1) "producto" else "productos"}"
		}
		Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
	}
}

private fun LazyGridScope.fullWidth(content: @Composable () -> Unit) =
	item(span = { GridItemSpan(maxLineSpan) }) { content() }
