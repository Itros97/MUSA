package org.softwareanvil.musa.ui.products.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue
import org.softwareanvil.musa.domain.logic.Margin
import org.softwareanvil.musa.domain.logic.MoneyFormatter
import org.softwareanvil.musa.domain.logic.MoneyParser
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.ui.components.MoneyField
import org.softwareanvil.musa.ui.components.MusaTextField

/** Fields of one variant; physical ones add stock, SKU and shipping. */
@Composable
fun VariantFields(kind: ProductKind, variant: VariantForm, errors: Map<FormField, String>, viewModel: ProductEditViewModel) {
	val error = { field: VariantField -> errors[FormField.Variant(variant.id, field)] }
	val change = { field: VariantField -> { value: String -> viewModel.onVariantChange(variant.id, field, value) } }

	Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
		MoneyField(variant[VariantField.PRICE], change(VariantField.PRICE), "Precio de venta", Modifier.weight(1f), error(VariantField.PRICE))
		MoneyField(variant[VariantField.COST], change(VariantField.COST), "Coste", Modifier.weight(1f), error(VariantField.COST))
	}
	MarginLine(MoneyParser.parse(variant[VariantField.PRICE]), MoneyParser.parse(variant[VariantField.COST]))
	if (kind != ProductKind.PHYSICAL) return

	Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
		StockField(variant, error(VariantField.STOCK), viewModel, Modifier.weight(1f))
		MusaTextField(
			value = variant[VariantField.MIN_STOCK],
			onValueChange = change(VariantField.MIN_STOCK),
			label = "Stock mínimo",
			placeholder = "Opcional",
			error = error(VariantField.MIN_STOCK),
			keyboardType = KeyboardType.Number,
			modifier = Modifier.weight(1f),
		)
	}
	MusaTextField(
		value = variant[VariantField.SKU],
		onValueChange = change(VariantField.SKU),
		label = "SKU o referencia (opcional)",
		placeholder = "LAM-GATO-A4",
		error = error(VariantField.SKU),
		keyboardType = KeyboardType.Ascii,
		modifier = Modifier.fillMaxWidth(),
	)
	ShippingFields(variant, error, change)
}

/** "Ganas 6,50 € por unidad (65 %)", or a warning when selling at a loss. */
@Composable
private fun MarginLine(price: Money?, cost: Money?) {
	if (price == null || cost == null) return
	val margin = Margin.of(price, cost)
	val color = if (margin.isNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
	Row(verticalAlignment = Alignment.CenterVertically) {
		if (margin.isNegative) Icon(Icons.Filled.Warning, null, tint = color, modifier = Modifier.size(18.dp).padding(end = 4.dp))
		val amount = MoneyFormatter.format(Money(margin.amount.cents.absoluteValue))
		val percent = margin.percent?.let { " ($it %)" }.orEmpty()
		Text(
			text = if (margin.isNegative) "Pierdes $amount por unidad" else "Ganas $amount por unidad$percent",
			style = MaterialTheme.typography.labelLarge,
			color = color,
		)
	}
}
