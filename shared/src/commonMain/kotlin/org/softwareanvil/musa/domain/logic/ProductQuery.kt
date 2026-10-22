package org.softwareanvil.musa.domain.logic

import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.domain.model.TagId

/** Search and filters the artist applies to the catalogue. */
data class ProductQuery(
	val text: String = "",
	val kind: ProductKind? = null,
	val tagIds: Set<TagId> = emptySet(),
	val lowStockOnly: Boolean = false,
	val archived: Boolean = false,
) {

	/** True when any filter or search narrows the catalogue. */
	val isFiltering: Boolean get() = this != ProductQuery()
}
