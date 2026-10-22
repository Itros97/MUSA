package org.softwareanvil.musa.domain.usecase

import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import org.softwareanvil.musa.domain.logic.IdGenerator
import org.softwareanvil.musa.domain.logic.SaleError
import org.softwareanvil.musa.domain.logic.SaleValidator
import org.softwareanvil.musa.domain.model.PhysicalVariant
import org.softwareanvil.musa.domain.model.Product
import org.softwareanvil.musa.domain.model.Sale
import org.softwareanvil.musa.domain.model.SaleDraft
import org.softwareanvil.musa.domain.model.SaleId
import org.softwareanvil.musa.domain.model.SaleLine
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.domain.repository.ProductRepository
import org.softwareanvil.musa.domain.repository.SaleRepository

/** Outcome of recording or deleting a sale. */
sealed interface SaleResult {
	data class Done(val sale: Sale) : SaleResult
	data class Rejected(val errors: List<SaleError>) : SaleResult
}

/** Records a sale outside events: checks stock, copies names and costs from the catalogue and takes the units. */
class RecordSaleUseCase(
	private val saleRepository: SaleRepository,
	private val productRepository: ProductRepository,
	private val validator: SaleValidator,
	private val ids: IdGenerator,
) {

	suspend operator fun invoke(draft: SaleDraft): SaleResult {
		val errors = validator.validate(draft)
		if (errors.isNotEmpty()) return SaleResult.Rejected(errors)
		val owners = draft.lines.map { it.variantId }.distinct().associateWith { variantId ->
			checkNotNull(productRepository.findByVariant(variantId)) { "Variant ${variantId.value} is not in the catalogue" }
		}
		val stockErrors = draft.lines.groupBy { it.variantId }.mapNotNull { (variantId, lines) ->
			val variant = owners.getValue(variantId).variant(variantId) as? PhysicalVariant ?: return@mapNotNull null
			SaleError.NotEnoughStock(variantId, variant.stock).takeIf { lines.sumOf { it.quantity } > variant.stock }
		}
		if (stockErrors.isNotEmpty()) return SaleResult.Rejected(stockErrors)
		val sale = Sale(
			id = SaleId(ids.next()),
			channel = draft.channel,
			eventId = null,
			customer = draft.customer.trim(),
			note = draft.note.trim(),
			lines = draft.lines.map { line ->
				val product = owners.getValue(line.variantId)
				val variant = product.variant(line.variantId)
				SaleLine(line.id, line.variantId, product.name, variant.name, line.quantity, line.unitPrice, variant.cost)
			},
			shippingCharged = draft.shippingCharged,
			shippingCost = draft.shippingCost,
			soldAt = draft.soldAt,
		)
		saleRepository.record(sale)
		return SaleResult.Done(sale)
	}
}

/** Deletes a sale outside events, giving its physical units back; event sales are undone from their event. */
class DeleteSaleUseCase(private val repository: SaleRepository, private val clock: Clock) {

	suspend operator fun invoke(id: SaleId): SaleResult {
		val sale = checkNotNull(repository.findById(id)) { "Cannot delete missing sale ${id.value}" }
		if (sale.eventId != null) return SaleResult.Rejected(listOf(SaleError.ManagedByEvent))
		repository.delete(id, clock.now())
		return SaleResult.Done(sale)
	}
}

/** Emits every sale of every channel. */
class ObserveSalesUseCase(private val repository: SaleRepository) {
	operator fun invoke(): Flow<List<Sale>> = repository.observeAll()
}

private fun Product.variant(id: VariantId) = variants.first { it.id == id }
