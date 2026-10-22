package org.softwareanvil.musa.ui.events.detail

import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.Product
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.ui.events.itemLabel

/** A physical variant with catalogue stock that can be taken to an event. */
data class StockOption(val variantId: VariantId, val label: String, val stock: Int) {

	companion object {

		/** Every physical variant of [products] that has stock, keeping the catalogue order. */
		fun from(products: List<Product>): List<StockOption> = products
			.filterIsInstance<PhysicalProduct>()
			.flatMap { product ->
				product.variants.filter { it.stock > 0 }.map { StockOption(it.id, itemLabel(product.name, it.name), it.stock) }
			}
	}
}
