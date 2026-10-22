package org.softwareanvil.musa.ui.events.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.softwareanvil.musa.domain.logic.DateFormatter
import org.softwareanvil.musa.domain.logic.MoneyFormatter
import org.softwareanvil.musa.domain.model.EventExpense
import org.softwareanvil.musa.domain.model.Sale
import org.softwareanvil.musa.domain.model.SaleId
import org.softwareanvil.musa.ui.components.MusaIcons
import org.softwareanvil.musa.ui.components.SectionCard
import org.softwareanvil.musa.ui.events.itemLabel

/** Sales of the event, newest first; while open each one can be undone. */
@Composable
fun SalesSection(sales: List<Sale>, isOpen: Boolean, onUndo: (SaleId) -> Unit) {
	SectionCard(title = "Ventas", subtitle = if (sales.isEmpty()) null else "${sales.size} ${if (sales.size == 1) "venta" else "ventas"}") {
		if (sales.isEmpty()) EmptyLine("Todavía no has vendido nada aquí.")
		sales.forEach { sale ->
			ItemRow(
				title = "${sale.quantity} × ${itemLabel(sale.productName, sale.variantName)}",
				detail = "${soldAtLabel(sale)} · ${MoneyFormatter.format(sale.unitPrice)} c/u",
				amount = MoneyFormatter.format(sale.total),
			) {
				if (isOpen) IconButton(onClick = { onUndo(sale.id) }) { Icon(MusaIcons.Undo, contentDescription = "Deshacer venta") }
			}
		}
	}
}

/** What attending cost; while open expenses can be added and removed. */
@Composable
fun ExpensesSection(expenses: List<EventExpense>, isOpen: Boolean, onDelete: (EventExpense) -> Unit, onAddClick: () -> Unit) {
	SectionCard(title = "Gastos", subtitle = "Stand, viaje, alojamiento…") {
		if (expenses.isEmpty()) EmptyLine("Sin gastos apuntados.")
		expenses.forEach { expense ->
			ItemRow(title = expense.concept, detail = null, amount = MoneyFormatter.format(expense.amount)) {
				if (isOpen) IconButton(onClick = { onDelete(expense) }) { Icon(Icons.Filled.Delete, contentDescription = "Borrar gasto") }
			}
		}
		if (isOpen) {
			OutlinedButton(onClick = onAddClick, modifier = Modifier.fillMaxWidth()) {
				Icon(Icons.Filled.Add, contentDescription = null)
				Text("Añadir gasto")
			}
		}
	}
}

@Composable
private fun ItemRow(title: String, detail: String?, amount: String, action: @Composable () -> Unit) {
	Row(verticalAlignment = Alignment.CenterVertically) {
		Column(modifier = Modifier.weight(1f)) {
			Text(title, style = MaterialTheme.typography.bodyLarge)
			detail?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
		}
		Text(amount, style = MaterialTheme.typography.titleMedium)
		action()
	}
}

@Composable
private fun EmptyLine(text: String) =
	Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

private fun soldAtLabel(sale: Sale): String {
	val local = sale.soldAt.toLocalDateTime(TimeZone.currentSystemDefault())
	val time = "${local.hour}:${local.minute.toString().padStart(2, '0')}"
	return "${DateFormatter.format(local.date)}, $time"
}
