package org.softwareanvil.musa.data.repository

import org.softwareanvil.musa.data.mapper.SaleMapper
import org.softwareanvil.musa.db.MusaDatabase
import org.softwareanvil.musa.domain.model.Sale
import org.softwareanvil.musa.db.Sale as SaleRow

/** Reads and writes sales with their lines; writers run inside the caller's transaction. */
internal class SaleRowStore(private val database: MusaDatabase) {

	private val catalogue = CatalogueStockWriter(database)

	/** Attaches its lines to each header row, keeping the row order. */
	fun withLines(rows: List<SaleRow>): List<Sale> {
		if (rows.isEmpty()) return emptyList()
		val lines = database.saleLineQueries.selectBySales(rows.map { it.id }).executeAsList().groupBy { it.sale_id }
		return rows.map { SaleMapper.toSale(it, lines[it.id].orEmpty()) }
	}

	/** Inserts the header and lines; outside events, physical units leave the catalogue. */
	fun insert(sale: Sale) {
		val at = sale.soldAt.toEpochMilliseconds()
		database.saleQueries.insert(
			id = sale.id.value,
			channel = sale.channel.name,
			event_id = sale.eventId?.value,
			customer = sale.customer,
			note = sale.note,
			shipping_charged_cents = sale.shippingCharged.cents,
			shipping_cost_cents = sale.shippingCost.cents,
			sold_at = at,
			updated_at = at,
		)
		sale.lines.forEachIndexed { position, line ->
			database.saleLineQueries.insert(
				id = line.id.value,
				sale_id = sale.id.value,
				variant_id = line.variantId.value,
				product_name = line.productName,
				variant_name = line.variantName,
				quantity = line.quantity.toLong(),
				unit_price_cents = line.unitPrice.cents,
				unit_cost_cents = line.unitCost.cents,
				position = position.toLong(),
			)
		}
		moveCatalogueStock(sale, direction = -1, at)
	}

	/** Soft-deletes the sale; outside events, physical units go back to the catalogue. */
	fun delete(sale: Sale, at: Long) {
		database.saleQueries.softDelete(at = at, id = sale.id.value)
		moveCatalogueStock(sale, direction = 1, at)
	}

	// Event sales sell units that already left the catalogue when they were taken to the event.
	private fun moveCatalogueStock(sale: Sale, direction: Int, at: Long) {
		if (sale.eventId != null) return
		sale.lines
			.filter { database.physicalVariantQueries.isPhysical(it.variantId.value).executeAsOne() }
			.forEach { catalogue.adjust(it.variantId.value, direction * it.quantity.toLong(), at) }
	}
}
