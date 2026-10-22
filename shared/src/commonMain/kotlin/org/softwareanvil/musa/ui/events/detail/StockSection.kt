package org.softwareanvil.musa.ui.events.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.softwareanvil.musa.domain.logic.MoneyFormatter
import org.softwareanvil.musa.domain.model.EventStockLine
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.ui.components.SectionCard
import org.softwareanvil.musa.ui.components.StockStepper
import org.softwareanvil.musa.ui.events.itemLabel

/** What was taken to the event; while open, −/+ move units with the catalogue and each line can be sold. */
@Composable
fun StockSection(
	lines: List<EventStockLine>,
	isOpen: Boolean,
	onSell: (VariantId) -> Unit,
	onStep: (VariantId, Int) -> Unit,
	onAddClick: () -> Unit,
) {
	val remaining = lines.sumOf { it.remaining }
	SectionCard(title = "Stock en el evento", subtitle = if (lines.isEmpty()) null else "Quedan $remaining de ${lines.sumOf { it.taken }}") {
		if (lines.isEmpty()) {
			Text(
				if (isOpen) "Aún no has apuntado nada. Elige qué te llevas del catálogo." else "No se llevó stock a este evento.",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}
		lines.forEachIndexed { index, line ->
			if (index > 0) HorizontalDivider()
			StockLineRow(line, isOpen, onSell = { onSell(line.variantId) }, onStep = { onStep(line.variantId, it) })
		}
		if (isOpen) {
			OutlinedButton(onClick = onAddClick, modifier = Modifier.fillMaxWidth()) {
				Icon(Icons.Filled.Add, contentDescription = null)
				Text("Llevar productos")
			}
		}
	}
}

@Composable
private fun StockLineRow(line: EventStockLine, isOpen: Boolean, onSell: () -> Unit, onStep: (Int) -> Unit) {
	Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
		Text(itemLabel(line.productName, line.variantName), style = MaterialTheme.typography.titleMedium)
		Text(
			"Quedan ${line.remaining} de ${line.taken} · vendidas ${line.sold} · ${MoneyFormatter.format(line.price)}",
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
		)
		if (!isOpen) return@Column
		Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
			StockStepper(line.remaining, onStep = onStep)
			Spacer(Modifier.weight(1f))
			Button(onClick = onSell, enabled = line.remaining > 0) {
				Icon(Icons.Filled.ShoppingCart, contentDescription = null)
				Text("Vender")
			}
		}
	}
}
