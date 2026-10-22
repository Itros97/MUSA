package org.softwareanvil.musa.demo

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.softwareanvil.musa.domain.logic.IdGenerator
import org.softwareanvil.musa.domain.logic.SaleTime
import org.softwareanvil.musa.domain.model.DigitalDetails
import org.softwareanvil.musa.domain.model.EventDraft
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.PhysicalVariant
import org.softwareanvil.musa.domain.model.ProductDraft
import org.softwareanvil.musa.domain.model.SaleChannel
import org.softwareanvil.musa.domain.model.SaleDraft
import org.softwareanvil.musa.domain.model.SaleLineDraft
import org.softwareanvil.musa.domain.model.SaleLineId
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.domain.usecase.AddExpenseUseCase
import org.softwareanvil.musa.domain.usecase.CloseEventUseCase
import org.softwareanvil.musa.domain.usecase.EventResult
import org.softwareanvil.musa.domain.usecase.MoveStockToEventUseCase
import org.softwareanvil.musa.domain.usecase.RecordEventSaleUseCase
import org.softwareanvil.musa.domain.usecase.RecordSaleUseCase
import org.softwareanvil.musa.domain.usecase.SaleResult
import org.softwareanvil.musa.domain.usecase.SaveEventUseCase
import org.softwareanvil.musa.domain.usecase.SaveProductUseCase
import org.softwareanvil.musa.domain.usecase.SaveResult

/** Fills an empty database with toy data through the real use cases; only for the demo database. */
class DemoSeeder(
	private val saveProduct: SaveProductUseCase,
	private val saveEvent: SaveEventUseCase,
	private val moveStock: MoveStockToEventUseCase,
	private val recordEventSale: RecordEventSaleUseCase,
	private val recordSale: RecordSaleUseCase,
	private val addExpense: AddExpenseUseCase,
	private val closeEvent: CloseEventUseCase,
	private val clock: Clock,
	private val ids: IdGenerator,
) {

	private lateinit var variants: Map<String, VariantId>
	private lateinit var digital: Map<String, VariantId>

	/** Creates the catalogue, a closed event from last month, one happening today, one coming up and four orders. */
	suspend fun seed() {
		val drafts = DemoCatalogue(ids).drafts()
		drafts.forEach { draft -> check(saveProduct(draft) is SaveResult.Saved) { "Demo product ${draft.name} was rejected" } }
		variants = drafts.skus()
		digital = drafts.digitalVariants()
		val zone = TimeZone.currentSystemDefault()
		val today = clock.todayIn(zone)

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

		val day = { daysAgo: Int -> SaleTime.at(today.minus(daysAgo, DateTimeUnit.DAY), clock, zone) }
		order(SaleChannel.IN_PERSON, "Vecina del taller", "", day(0), line(variant("LLA-DRAG"), 2, 800))
		order(SaleChannel.ONLINE_SHOP, "@lectora_de_comics", "Pedido web 0007", day(3), line(variant("LAM-BOS-A4"), 2, 1200),
			line(variant("PEG-HONG"), 1, 600), shipping = 450L to 380L)
		order(SaleChannel.SOCIAL, "@ranita_ilustra", "Encargo por DM", day(10), line(digital.getValue("Retrato digital · Busto"), 1, 3500))
		order(SaleChannel.MARKETPLACE, "", "Etsy", day(40), line(variant("LAM-GAT-A3"), 1, 2000), shipping = 500L to 420L)
	}

	private suspend fun order(
		channel: SaleChannel,
		customer: String,
		note: String,
		soldAt: Instant,
		vararg lines: SaleLineDraft,
		shipping: Pair<Long, Long> = 0L to 0L,
	) {
		val draft = SaleDraft(channel, customer, note, lines.toList(), Money(shipping.first), Money(shipping.second), soldAt)
		check(recordSale(draft) is SaleResult.Done) { "Demo order for $customer was rejected" }
	}

	private fun line(variantId: VariantId, quantity: Int, cents: Long) =
		SaleLineDraft(SaleLineId(ids.next()), variantId, quantity, Money(cents))

	private suspend fun event(name: String, location: String, start: LocalDate, end: LocalDate): EventId {
		val id = EventId(ids.next())
		saveEvent(EventDraft(id, name, location, start, end)).orFail("event $name")
		return id
	}

	private suspend fun take(event: EventId, vararg units: Pair<String, Int>) =
		units.forEach { (sku, quantity) -> moveStock(event, variant(sku), quantity).orFail("stock $sku") }

	private suspend fun sell(event: EventId, sku: String, quantity: Int, cents: Long) =
		recordEventSale(event, variant(sku), quantity, Money(cents)).orFail("sale $sku")

	private suspend fun spend(event: EventId, concept: String, cents: Long) =
		addExpense(event, concept, Money(cents)).orFail("expense $concept")

	private fun variant(sku: String) = checkNotNull(variants[sku]) { "No demo variant with SKU $sku" }
}

private fun List<ProductDraft>.skus(): Map<String, VariantId> = flatMap { it.details.variants }
	.filterIsInstance<PhysicalVariant>()
	.mapNotNull { variant -> variant.sku?.let { it to variant.id } }
	.toMap()

private fun List<ProductDraft>.digitalVariants(): Map<String, VariantId> = flatMap { draft ->
	(draft.details as? DigitalDetails)?.variants.orEmpty().map { "${draft.name} · ${it.name}" to it.id }
}.toMap()

private fun EventResult.orFail(what: String) = check(this == EventResult.Done) { "Demo $what was rejected: $this" }
