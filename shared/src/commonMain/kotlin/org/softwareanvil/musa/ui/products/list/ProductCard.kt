package org.softwareanvil.musa.ui.products.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.softwareanvil.musa.domain.model.DigitalProduct
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.Product
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.ui.components.Pill
import org.softwareanvil.musa.ui.components.ProductThumbnail
import org.softwareanvil.musa.ui.components.TagPill
import org.softwareanvil.musa.ui.products.priceLabel
import org.softwareanvil.musa.ui.products.stockLabel
import org.softwareanvil.musa.ui.products.summaryLabel
import org.softwareanvil.musa.ui.theme.MusaColors
import org.softwareanvil.musa.ui.theme.ToneColors

private const val MAX_TAGS = 3

/** One product in the catalogue; tapping the stock pill opens the quick stock sheet. */
@Composable
fun ProductCard(product: Product, onClick: () -> Unit, onStockClick: () -> Unit, modifier: Modifier = Modifier) {
	Card(
		onClick = onClick,
		modifier = modifier.fillMaxWidth().alpha(if (product.status == ProductStatus.ARCHIVED) 0.6f else 1f),
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
	) {
		Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
			ProductThumbnail(product.name, product.kind)
			Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
				Text(product.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
				Text(
					product.summaryLabel(),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
				TagRow(product)
				Row(
					modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(8.dp),
				) {
					Text(
						product.priceLabel(),
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.primary,
						modifier = Modifier.weight(1f),
					)
					StatusPills(product, onStockClick)
				}
			}
		}
	}
}

@Composable
private fun TagRow(product: Product) {
	if (product.tags.isEmpty()) return
	FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
		product.tags.take(MAX_TAGS).forEach { TagPill(it) }
		val hidden = product.tags.size - MAX_TAGS
		if (hidden > 0) Pill("+$hidden", neutralTones())
	}
}

@Composable
private fun StatusPills(product: Product, onStockClick: () -> Unit) {
	if (product.status == ProductStatus.DRAFT) Pill("Borrador", neutralTones())
	when (product) {
		is PhysicalProduct -> Pill(product.stockLabel(), stockTones(product), onClick = onStockClick)
		is DigitalProduct -> Pill("Por encargo", neutralTones())
	}
}

@Composable
private fun stockTones(product: PhysicalProduct): ToneColors = when {
	product.totalStock == 0 -> ToneColors(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
	product.isLowStock -> MusaColors.extra.warning
	else -> ToneColors(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
}

@Composable
private fun neutralTones() =
	ToneColors(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurfaceVariant)
