package org.softwareanvil.musa.ui.products.list

import org.softwareanvil.musa.domain.logic.ProductQuery
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.Product
import org.softwareanvil.musa.domain.model.Tag

/** Everything the catalogue screen draws. */
data class ProductListState(
	val isLoading: Boolean = true,
	val products: List<Product> = emptyList(),
	val tags: List<Tag> = emptyList(),
	val query: ProductQuery = ProductQuery(),
	val isCatalogueEmpty: Boolean = false,
	val stockSheet: PhysicalProduct? = null,
)
