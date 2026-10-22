package org.softwareanvil.musa.data.repository

import org.softwareanvil.musa.db.MusaDatabase

/** Changes the catalogue stock of a variant and touches its rows so product observers refresh; callers wrap it in a transaction. */
internal class CatalogueStockWriter(private val database: MusaDatabase) {

	/** Adds [delta] units to [variantId], never going below zero. */
	fun adjust(variantId: String, delta: Long, at: Long) {
		val productId = checkNotNull(database.productVariantQueries.selectProductId(variantId).executeAsOneOrNull()) {
			"Cannot adjust stock of missing variant $variantId"
		}
		database.physicalVariantQueries.adjustStock(delta, variantId)
		database.productVariantQueries.touch(at, variantId)
		database.productQueries.touch(at, productId)
	}
}
