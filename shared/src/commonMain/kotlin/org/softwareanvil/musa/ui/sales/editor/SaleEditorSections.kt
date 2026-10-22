package org.softwareanvil.musa.ui.sales.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.softwareanvil.musa.domain.logic.MoneyFormatter
import org.softwareanvil.musa.domain.model.SaleChannel
import org.softwareanvil.musa.ui.components.MoneyField
import org.softwareanvil.musa.ui.components.SectionCard
import org.softwareanvil.musa.ui.components.StockStepper
import org.softwareanvil.musa.ui.sales.SELECTABLE_CHANNELS
import org.softwareanvil.musa.ui.sales.label
import org.softwareanvil.musa.ui.sales.list.MarginText

/** Chips for every channel the artist can pick. */
@Composable
fun ChannelChips(selected: SaleChannel, onSelect: (SaleChannel) -> Unit) {
	FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
		SELECTABLE_CHANNELS.forEach { FilterChip(selected = it == selected, onClick = { onSelect(it) }, label = { Text(it.label()) }) }
	}
}

/** The lines of the sale, each with quantity and price, plus a button to add more. */
@Composable
fun LinesSection(state: SaleEditorState, form: SaleForm, viewModel: SaleEditorViewModel) {
	SectionCard(title = "Productos", subtitle = "El precio sale del catálogo; cámbialo si hubo descuento o pack") {
		form.lines.forEachIndexed { index, line ->
			if (index > 0) HorizontalDivider()
			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				Row(verticalAlignment = Alignment.CenterVertically) {
					Text(line.option.label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
					IconButton(onClick = { viewModel.onLineRemove(line.id) }) { Icon(Icons.Filled.Close, contentDescription = "Quitar producto") }
				}
				Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
					StockStepper(line.quantity, onStep = { viewModel.onQuantityStep(line.id, it) })
					MoneyField(
						value = line.price,
						onValueChange = { viewModel.onPriceChange(line.id, it) },
						label = "Precio por unidad",
						error = state.errors[SaleField.LinePrice(line.id)],
						modifier = Modifier.weight(1f),
					)
				}
				state.errors[SaleField.LineQuantity(line.id)]?.let { ErrorText(it) }
			}
		}
		state.errors[SaleField.Lines]?.let { ErrorText(it) }
		OutlinedButton(onClick = viewModel::onPickerOpen, modifier = Modifier.fillMaxWidth()) {
			Icon(Icons.Filled.Add, contentDescription = null)
			Text("Añadir producto")
		}
	}
}

/** What shipping was charged and what it cost, then the live totals. */
@Composable
fun ShippingAndTotals(state: SaleEditorState, form: SaleForm, viewModel: SaleEditorViewModel) {
	SectionCard(title = "Envío", subtitle = "Déjalo vacío si no hubo envío") {
		Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
			MoneyField(form.shippingCharged, viewModel::onShippingChargedChange, "Cobrado al cliente", Modifier.weight(1f))
			MoneyField(form.shippingCost, viewModel::onShippingCostChange, "Te costó", Modifier.weight(1f))
		}
		state.errors[SaleField.Shipping]?.let { ErrorText(it) }
	}
	val preview = state.preview ?: return
	SectionCard(title = "Totales") {
		Row {
			Text("Total ${MoneyFormatter.format(preview.total)} · coste ${MoneyFormatter.format(preview.cost)}", modifier = Modifier.weight(1f))
			MarginText(preview.margin, MaterialTheme.typography.titleMedium, prefix = "margen ")
		}
	}
}

/** Pick a catalogue variant to add as a new line. */
@Composable
fun LinePickerSheet(options: List<SaleOption>, onPick: (SaleOption) -> Unit, onDismiss: () -> Unit) {
	ModalBottomSheet(onDismissRequest = onDismiss) {
		Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
			Text("Añadir producto", style = MaterialTheme.typography.headlineSmall)
			if (options.isEmpty()) Text("No hay productos con stock ni digitales en el catálogo.")
			LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
				items(options, key = { it.variantId.value }) { option ->
					Row(modifier = Modifier.fillMaxWidth().clickable { onPick(option) }.padding(vertical = 12.dp)) {
						Text(option.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
						Text(
							listOfNotNull(MoneyFormatter.format(option.price), option.stock?.let { "$it en stock" }).joinToString(" · "),
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
						)
					}
				}
			}
		}
	}
}

@Composable
private fun ErrorText(text: String) = Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
