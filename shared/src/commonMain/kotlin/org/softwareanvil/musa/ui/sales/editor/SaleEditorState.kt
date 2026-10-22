package org.softwareanvil.musa.ui.sales.editor

import kotlinx.datetime.LocalDate
import org.softwareanvil.musa.domain.logic.MoneyParser
import org.softwareanvil.musa.domain.model.DigitalProduct
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.Product
import org.softwareanvil.musa.domain.model.SaleChannel
import org.softwareanvil.musa.domain.model.SaleLineId
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.domain.model.sumOfMoney
import org.softwareanvil.musa.ui.events.itemLabel

/** A catalogue variant that can go into a sale; [stock] is null for digital variants, which have none. */
data class SaleOption(val variantId: VariantId, val label: String, val price: Money, val cost: Money, val stock: Int?) {

	companion object {

		/** Physical variants with stock and every digital variant of [products], in catalogue order. */
		fun from(products: List<Product>): List<SaleOption> = products.flatMap { product ->
			when (product) {
				is PhysicalProduct -> product.variants.filter { it.stock > 0 }
					.map { SaleOption(it.id, itemLabel(product.name, it.name), it.price, it.cost, it.stock) }
				is DigitalProduct -> product.variants.map { SaleOption(it.id, itemLabel(product.name, it.name), it.price, it.cost, null) }
			}
		}
	}
}

/** One line of the editor: what was picked, how many and the price as typed. */
data class LineForm(val id: SaleLineId, val option: SaleOption, val quantity: Int, val price: String)

/** Raw input of the sale editor; amounts stay as typed until saving. */
data class SaleForm(
	val date: LocalDate,
	val channel: SaleChannel = SaleChannel.ONLINE_SHOP,
	val customer: String = "",
	val note: String = "",
	val lines: List<LineForm> = emptyList(),
	val shippingCharged: String = "",
	val shippingCost: String = "",
)

/** Where an error is shown in the sale editor. */
sealed interface SaleField {
	data object Lines : SaleField
	data object Shipping : SaleField
	data class LineQuantity(val id: SaleLineId) : SaleField
	data class LinePrice(val id: SaleLineId) : SaleField
}

/** Totals shown while typing; unreadable amounts count as zero until fixed. */
data class SalePreview(val total: Money, val cost: Money) {

	val margin: Money get() = total - cost

	companion object {

		fun of(form: SaleForm): SalePreview {
			val goods = form.lines.sumOfMoney { (MoneyParser.parse(it.price) ?: Money.ZERO) * it.quantity }
			val goodsCost = form.lines.sumOfMoney { it.option.cost * it.quantity }
			return SalePreview(total = goods + form.shippingCharged.asAmount(), cost = goodsCost + form.shippingCost.asAmount())
		}
	}
}

/** Everything the sale editor draws; [form] is null while loading. */
data class SaleEditorState(
	val form: SaleForm? = null,
	val options: List<SaleOption> = emptyList(),
	val errors: Map<SaleField, String> = emptyMap(),
	val isPicking: Boolean = false,
	val isConfirmingDiscard: Boolean = false,
	val isSaving: Boolean = false,
) {

	val preview: SalePreview? get() = form?.let(SalePreview::of)
}

/** An empty shipping field means no shipping. */
internal fun String.asAmount(): Money = if (isBlank()) Money.ZERO else MoneyParser.parse(this) ?: Money.ZERO
