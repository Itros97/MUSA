package org.softwareanvil.musa.domain.logic

import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.PhysicalVariant
import org.softwareanvil.musa.domain.model.Product
import org.softwareanvil.musa.domain.model.ProductStatus

/** Applies a [ProductQuery] to the catalogue and sorts the result by name. */
object ProductFilter {

	/** Returns the products matching every filter in [query]. */
	fun apply(products: List<Product>, query: ProductQuery): List<Product> {
		val text = TextNormalizer.fold(query.text.trim())
		return products
			.filter { (it.status == ProductStatus.ARCHIVED) == query.archived }
			.filter { query.kind == null || it.kind == query.kind }
			.filter { product -> product.tags.map { it.id }.containsAll(query.tagIds) }
			.filter { !query.lowStockOnly || (it is PhysicalProduct && it.isLowStock) }
			.filter { text.isEmpty() || matchesText(it, text) }
			.sortedBy { TextNormalizer.fold(it.name) }
	}

	private fun matchesText(product: Product, foldedText: String): Boolean {
		val skus = product.variants.mapNotNull { (it as? PhysicalVariant)?.sku }
		return (listOf(product.name, product.description) + skus)
			.any { TextNormalizer.fold(it).contains(foldedText) }
	}
}
