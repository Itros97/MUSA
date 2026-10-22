package org.softwareanvil.musa.ui.sales.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.softwareanvil.musa.domain.logic.MoneyFormatter
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.Sale
import org.softwareanvil.musa.ui.events.itemLabel
import org.softwareanvil.musa.ui.sales.label

/** Every line of a sale with shipping and totals; sales outside events can be deleted from here. */
@Composable
fun SaleDetailSheet(sale: Sale, onDelete: () -> Unit, onDismiss: () -> Unit) {
	ModalBottomSheet(onDismissRequest = onDismiss) {
		Column(
			modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			Column {
				Text(sale.channel.label(), style = MaterialTheme.typography.headlineSmall)
				val who = listOf(soldOnLabel(sale), sale.customer, sale.note).filter { it.isNotBlank() }.joinToString(" · ")
				Text(who, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
			}
			sale.lines.forEach { line ->
				AmountRow(
					"${line.quantity} × ${itemLabel(line.productName, line.variantName)}",
					"${MoneyFormatter.format(line.unitPrice)} c/u · coste ${MoneyFormatter.format(line.unitCost)}",
					line.total,
				)
			}
			if (sale.shippingCharged != Money.ZERO || sale.shippingCost != Money.ZERO) {
				AmountRow("Envío cobrado", "te costó ${MoneyFormatter.format(sale.shippingCost)}", sale.shippingCharged)
			}
			HorizontalDivider()
			AmountRow("Total", "coste ${MoneyFormatter.format(sale.cost)}", sale.total)
			Row {
				Text("Margen", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
				MarginText(sale.margin, MaterialTheme.typography.titleLarge)
			}
			if (sale.eventId != null) {
				Text(
					"Es una venta de evento: se deshace desde su evento.",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			} else {
				OutlinedButton(
					onClick = onDelete,
					modifier = Modifier.fillMaxWidth(),
					colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
				) {
					Icon(Icons.Filled.Delete, contentDescription = null)
					Text("Eliminar venta")
				}
			}
		}
	}
}

@Composable
private fun AmountRow(title: String, detail: String, amount: Money) {
	Row {
		Column(modifier = Modifier.weight(1f)) {
			Text(title, style = MaterialTheme.typography.bodyLarge)
			Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
		}
		Text(MoneyFormatter.format(amount), style = MaterialTheme.typography.titleMedium)
	}
}
