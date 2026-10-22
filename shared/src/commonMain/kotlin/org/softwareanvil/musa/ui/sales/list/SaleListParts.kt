package org.softwareanvil.musa.ui.sales.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.softwareanvil.musa.domain.logic.DateFormatter
import org.softwareanvil.musa.domain.logic.MoneyFormatter
import org.softwareanvil.musa.domain.logic.SalesPeriod
import org.softwareanvil.musa.domain.logic.SalesQuery
import org.softwareanvil.musa.domain.logic.SalesSummary
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.Sale
import org.softwareanvil.musa.domain.model.SaleChannel
import org.softwareanvil.musa.ui.components.Pill
import org.softwareanvil.musa.ui.components.SectionCard
import org.softwareanvil.musa.ui.events.itemLabel
import org.softwareanvil.musa.ui.sales.label
import org.softwareanvil.musa.ui.theme.ToneColors

/** Period chips on one row and channel chips on another. */
@Composable
fun SaleFilters(query: SalesQuery, onPeriodSelect: (SalesPeriod) -> Unit, onChannelToggle: (SaleChannel) -> Unit) {
	Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
		LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			items(SalesPeriod.entries) {
				FilterChip(selected = query.period == it, onClick = { onPeriodSelect(it) }, label = { Text(it.label()) })
			}
		}
		LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			items(SaleChannel.entries) {
				FilterChip(selected = query.channel == it, onClick = { onChannelToggle(it) }, label = { Text(it.label()) })
			}
		}
	}
}

/** Count, units, revenue, cost and margin of the filtered sales. */
@Composable
fun SalesSummaryCard(summary: SalesSummary) {
	val count = summary.count
	SectionCard(title = "Resumen", subtitle = "$count ${if (count == 1) "venta" else "ventas"} · ${summary.units} unidades") {
		FlowRow(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
			Figure("Ingresos", summary.revenue)
			Figure("Coste", summary.cost)
		}
		HorizontalDivider()
		Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
			Text("Margen", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
			MarginText(summary.margin, MaterialTheme.typography.headlineSmall)
		}
	}
}

/** One sale: date and channel, what was sold, total and margin. */
@Composable
fun SaleCard(sale: Sale, onClick: () -> Unit) {
	Card(
		onClick = onClick,
		modifier = Modifier.fillMaxWidth(),
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
	) {
		Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
			Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
				Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
					Text(soldOnLabel(sale), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
					Pill(sale.channel.label(), channelTones())
				}
				Text(linesLabel(sale), style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
				sale.customer.takeIf { it.isNotBlank() }?.let {
					Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
				}
			}
			Column(horizontalAlignment = Alignment.End) {
				Text(MoneyFormatter.format(sale.total), style = MaterialTheme.typography.titleMedium)
				MarginText(sale.margin, MaterialTheme.typography.bodySmall, prefix = "margen ")
			}
		}
	}
}

/** Margin in the primary colour, or the error colour when negative. */
@Composable
fun MarginText(margin: Money, style: TextStyle, prefix: String = "") = Text(
	"$prefix${MoneyFormatter.format(margin)}",
	style = style,
	color = if (margin.cents < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
)

/** "2 × Lámina gato · A4, 1 × Chapa". */
fun linesLabel(sale: Sale): String = sale.lines.joinToString { "${it.quantity} × ${itemLabel(it.productName, it.variantName)}" }

/** Local day the sale happened: "18 abr 2026". */
fun soldOnLabel(sale: Sale): String = DateFormatter.format(sale.soldAt.toLocalDateTime(TimeZone.currentSystemDefault()).date)

@Composable
private fun Figure(label: String, amount: Money) {
	Column {
		Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
		Text(MoneyFormatter.format(amount), style = MaterialTheme.typography.titleMedium)
	}
}

@Composable
private fun channelTones() = ToneColors(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
