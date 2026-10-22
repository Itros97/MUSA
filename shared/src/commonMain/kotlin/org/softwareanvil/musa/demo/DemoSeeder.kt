package org.softwareanvil.musa.demo

import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.softwareanvil.musa.domain.logic.IdGenerator
import org.softwareanvil.musa.domain.model.EventDraft
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.PhysicalVariant
import org.softwareanvil.musa.domain.model.ProductDraft
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.domain.usecase.AddExpenseUseCase
import org.softwareanvil.musa.domain.usecase.CloseEventUseCase
import org.softwareanvil.musa.domain.usecase.EventResult
import org.softwareanvil.musa.domain.usecase.MoveStockToEventUseCase
import org.softwareanvil.musa.domain.usecase.RecordEventSaleUseCase
import org.softwareanvil.musa.domain.usecase.SaveEventUseCase
import org.softwareanvil.musa.domain.usecase.SaveProductUseCase
import org.softwareanvil.musa.domain.usecase.SaveResult

/** Fills an empty database with toy data through the real use cases; only for the demo database. */
class DemoSeeder(
	private val saveProduct: SaveProductUseCase,
	private val saveEvent: SaveEventUseCase,
	private val moveStock: MoveStockToEventUseCase,
	private val recordSale: RecordEventSaleUseCase,
	private val addExpense: AddExpenseUseCase,
	private val closeEvent: CloseEventUseCase,
	private val clock: Clock,
	private val ids: IdGenerator,
) {

	private lateinit var variants: Map<String, VariantId>

	/** Creates the catalogue, a closed event from last month, one happening today and one coming up. */
	suspend fun seed() {
		val drafts = DemoCatalogue(ids).drafts()
		drafts.forEach { draft -> check(saveProduct(draft) is SaveResult.Saved) { "Demo product ${draft.name} was rejected" } }
		variants = drafts.skus()
		val today = clock.todayIn(TimeZone.currentSystemDefault())

		val autumn = event("Mercadillo de otoño", "Plaza Mayor", today.minus(30, DateTimeUnit.DAY), today.minus(29, DateTimeUnit.DAY))
		take(autumn, "LAM-GAT-A4" to 6, "PEG-LUNA" to 15, "CHA-RANA" to 10)
		sell(autumn, "LAM-GAT-A4", 4, 1200)
		sell(autumn, "LAM-GAT-A4", 1, 1000)
		sell(autumn, "PEG-LUNA", 9, 300)
		sell(autumn, "CHA-RANA", 6, 250)
		spend(autumn, "Stand", 4000)
		spend(autumn, "Gasolina", 2200)
		closeEvent(autumn).orFail("closing")

		val comic = event("Feria del cómic", "Pabellón 3", today, today.plus(1, DateTimeUnit.DAY))
		take(comic, "LAM-GAT-A4" to 5, "LAM-BOS-A3" to 2, "PEG-LUNA" to 12, "LLA-DRAG" to 6)
		sell(comic, "LAM-GAT-A4", 2, 1200)
		sell(comic, "PEG-LUNA", 3, 300)
		sell(comic, "LLA-DRAG", 1, 800)
		sell(comic, "LAM-BOS-A3", 1, 1800)
		spend(comic, "Stand", 6000)
		spend(comic, "Comida", 1450)

		event("Mercado de Navidad", "Centro cultural", today.plus(50, DateTimeUnit.DAY), today.plus(52, DateTimeUnit.DAY))
	}

	private suspend fun event(name: String, location: String, start: LocalDate, end: LocalDate): EventId {
		val id = EventId(ids.next())
		saveEvent(EventDraft(id, name, location, start, end)).orFail("event $name")
		return id
	}

	private suspend fun take(event: EventId, vararg units: Pair<String, Int>) =
		units.forEach { (sku, quantity) -> moveStock(event, variant(sku), quantity).orFail("stock $sku") }

	private suspend fun sell(event: EventId, sku: String, quantity: Int, cents: Long) =
		recordSale(event, variant(sku), quantity, Money(cents)).orFail("sale $sku")

	private suspend fun spend(event: EventId, concept: String, cents: Long) = addExpense(event, concept, Money(cents)).orFail("expense $concept")

	private fun variant(sku: String) = checkNotNull(variants[sku]) { "No demo variant with SKU $sku" }
}

private fun List<ProductDraft>.skus(): Map<String, VariantId> = flatMap { it.details.variants }
	.filterIsInstance<PhysicalVariant>()
	.mapNotNull { variant -> variant.sku?.let { it to variant.id } }
	.toMap()

private fun EventResult.orFail(what: String) = check(this == EventResult.Done) { "Demo $what was rejected: $this" }
