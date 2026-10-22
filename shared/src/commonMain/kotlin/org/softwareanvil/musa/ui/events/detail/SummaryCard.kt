package org.softwareanvil.musa.ui.events.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.softwareanvil.musa.domain.logic.EventSummary
import org.softwareanvil.musa.domain.logic.MoneyFormatter
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.ui.components.SectionCard

/** Gross, cost of goods, expenses and the resulting profit of an event. */
@Composable
fun SummaryCard(summary: EventSummary, modifier: Modifier = Modifier) {
	val units = summary.unitsSold
	SectionCard(title = "Resumen", subtitle = "$units ${if (units == 1) "unidad vendida" else "unidades vendidas"}", modifier = modifier) {
		FlowRow(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
			Figure("Ganancia bruta", summary.gross)
			Figure("Coste de lo vendido", summary.costOfGoods)
			Figure("Gastos", summary.expenses)
		}
		HorizontalDivider()
		Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
			Text("Beneficio", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
			Text(
				MoneyFormatter.format(summary.profit),
				style = MaterialTheme.typography.headlineSmall,
				color = if (summary.profit.cents < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
			)
		}
	}
}

@Composable
private fun Figure(label: String, amount: Money) {
	Column {
		Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
		Text(MoneyFormatter.format(amount), style = MaterialTheme.typography.titleMedium)
	}
}
