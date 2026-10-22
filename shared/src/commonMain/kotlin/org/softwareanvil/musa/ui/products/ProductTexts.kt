package org.softwareanvil.musa.ui.products

import org.softwareanvil.musa.domain.logic.MoneyFormatter
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.Product
import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.domain.model.hasPriceRange
import org.softwareanvil.musa.domain.model.minPrice

/** Spanish name of each kind. */
fun ProductKind.label(): String = when (this) {
	ProductKind.PHYSICAL -> "Físico"
	ProductKind.DIGITAL -> "Digital"
}

/** "8 €", or "desde 8 €" when variants have different prices. */
fun Product.priceLabel(): String {
	val price = MoneyFormatter.format(minPrice)
	return if (hasPriceRange) "desde $price" else price
}

/** Kind plus the number of variants when there is more than one. */
fun Product.summaryLabel(): String =
	if (variants.size > 1) "${kind.label()} · ${variants.size} variantes" else kind.label()

/** Stock status in a few words. */
fun PhysicalProduct.stockLabel(): String = when {
	totalStock == 0 -> "Agotado"
	isLowStock -> "Stock bajo · $totalStock"
	else -> "$totalStock en stock"
}
