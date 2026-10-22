package org.softwareanvil.musa.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.softwareanvil.musa.domain.logic.ProductFilter
import org.softwareanvil.musa.domain.logic.ProductQuery
import org.softwareanvil.musa.domain.model.Product
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.Tag
import org.softwareanvil.musa.domain.repository.ProductRepository
import org.softwareanvil.musa.domain.repository.TagRepository

/** Products matching a query, plus the whole catalogue they were picked from. */
data class FilteredCatalogue(val products: List<Product>, val all: List<Product>) {

	val isCatalogueEmpty: Boolean get() = all.isEmpty()
}

/** Emits the catalogue filtered by the latest query. */
class ObserveProductsUseCase(private val repository: ProductRepository) {
	operator fun invoke(query: Flow<ProductQuery>): Flow<FilteredCatalogue> =
		combine(repository.observeAll(), query) { products, current ->
			FilteredCatalogue(ProductFilter.apply(products, current), all = products)
		}
}

/** Loads one product for editing. */
class GetProductUseCase(private val repository: ProductRepository) {
	suspend operator fun invoke(id: ProductId): Product? = repository.findById(id)
}

/** Emits every tag, used for suggestions and filters. */
class ObserveTagsUseCase(private val repository: TagRepository) {
	operator fun invoke(): Flow<List<Tag>> = repository.observeAll()
}
