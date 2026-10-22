package org.softwareanvil.musa.ui.sales.editor

import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import org.softwareanvil.musa.domain.logic.MoneyParser
import org.softwareanvil.musa.domain.logic.SaleError
import org.softwareanvil.musa.domain.logic.SaleTime
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.SaleDraft
import org.softwareanvil.musa.domain.model.SaleLineDraft
import org.softwareanvil.musa.ui.sales.message

/** Draft built from the typed text; unreadable amounts hold zero and appear in [errors]. */
data class ParsedSale(val draft: SaleDraft, val errors: Map<SaleField, String>)

/** Turns the editor's raw text into a sale draft, and domain errors back into field errors. */
object SaleFormParser {

	private const val BAD_AMOUNT = "Escribe un importe válido, como 12 o 12,50"

	/** Parses every amount, collecting all problems so they can be fixed in one go. */
	fun parse(form: SaleForm, clock: Clock, timeZone: TimeZone): ParsedSale {
		val errors = mutableMapOf<SaleField, String>()
		val lines = form.lines.map { line ->
			val price = MoneyParser.parse(line.price) ?: Money.ZERO.also { errors[SaleField.LinePrice(line.id)] = BAD_AMOUNT }
			SaleLineDraft(line.id, line.option.variantId, line.quantity, price)
		}
		val shipping = listOf(form.shippingCharged, form.shippingCost)
		if (shipping.any { it.isNotBlank() && MoneyParser.parse(it) == null }) errors[SaleField.Shipping] = BAD_AMOUNT
		val draft = SaleDraft(
			channel = form.channel,
			customer = form.customer,
			note = form.note,
			lines = lines,
			shippingCharged = form.shippingCharged.asAmount(),
			shippingCost = form.shippingCost.asAmount(),
			soldAt = SaleTime.at(form.date, clock, timeZone),
		)
		return ParsedSale(draft, errors)
	}

	/** Places each domain error next to the field it is about; stock errors go on every line of that variant. */
	fun fieldErrors(errors: List<SaleError>, form: SaleForm): Map<SaleField, String> = errors.flatMap { error ->
		val fields = when (error) {
			SaleError.NoLines, SaleError.EventChannel, SaleError.ManagedByEvent -> listOf(SaleField.Lines)
			SaleError.NegativeShipping -> listOf(SaleField.Shipping)
			is SaleError.InvalidQuantity -> listOf(SaleField.LineQuantity(error.lineId))
			is SaleError.NegativePrice -> listOf(SaleField.LinePrice(error.lineId))
			is SaleError.NotEnoughStock -> form.lines.filter { it.option.variantId == error.variantId }.map { SaleField.LineQuantity(it.id) }
		}
		fields.map { it to error.message() }
	}.toMap()
}
