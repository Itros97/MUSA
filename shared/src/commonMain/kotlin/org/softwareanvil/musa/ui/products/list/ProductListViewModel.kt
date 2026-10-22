package org.softwareanvil.musa.ui.products.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.softwareanvil.musa.domain.logic.ProductQuery
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.domain.model.TagId
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.domain.usecase.AdjustStockUseCase
import org.softwareanvil.musa.domain.usecase.ObserveProductsUseCase
import org.softwareanvil.musa.domain.usecase.ObserveTagsUseCase

/** Drives the catalogue: search, filters and quick stock changes. */
class ProductListViewModel(
	observeProducts: ObserveProductsUseCase,
	observeTags: ObserveTagsUseCase,
	private val adjustStock: AdjustStockUseCase,
) : ViewModel() {

	private val query = MutableStateFlow(ProductQuery())
	private val stockSheetId = MutableStateFlow<ProductId?>(null)

	val state: StateFlow<ProductListState> = combine(
		observeProducts(query),
		observeTags(),
		query,
		stockSheetId,
	) { catalogue, tags, currentQuery, sheetId ->
		ProductListState(
			isLoading = false,
			products = catalogue.products,
			tags = tags,
			query = currentQuery,
			isCatalogueEmpty = catalogue.isCatalogueEmpty,
			// Looked up in the full catalogue so the sheet stays open if the product leaves the current filter.
			stockSheet = catalogue.all.firstOrNull { it.id == sheetId } as? PhysicalProduct,
		)
	}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductListState())

	// region filters

	fun onSearchChange(text: String) = query.update { it.copy(text = text) }

	/** Selecting the active kind again clears the kind filter. */
	fun onKindToggle(kind: ProductKind) = query.update { it.copy(kind = kind.takeIf { _ -> it.kind != kind }) }

	fun onTagToggle(id: TagId) = query.update {
		it.copy(tagIds = if (id in it.tagIds) it.tagIds - id else it.tagIds + id)
	}

	fun onLowStockToggle() = query.update { it.copy(lowStockOnly = !it.lowStockOnly) }

	fun onArchivedToggle() = query.update { it.copy(archived = !it.archived) }

	fun onClearFilters() = query.update { ProductQuery() }

	// endregion

	// region stock sheet

	fun onStockClick(id: ProductId) = stockSheetId.update { id }

	fun onStockSheetDismiss() = stockSheetId.update { null }

	fun onStockStep(variantId: VariantId, delta: Int) {
		viewModelScope.launch { adjustStock(variantId, delta) }
	}

	// endregion
}
