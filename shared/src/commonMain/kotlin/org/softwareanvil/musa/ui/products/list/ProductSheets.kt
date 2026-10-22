package org.softwareanvil.musa.ui.products.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.ui.components.StockStepper
import org.softwareanvil.musa.ui.components.icon
import org.softwareanvil.musa.ui.components.tones
import org.softwareanvil.musa.ui.products.stockLabel

/** First step of creating a product: pick physical or digital. */
@Composable
fun KindPickerSheet(onPick: (ProductKind) -> Unit, onDismiss: () -> Unit) {
	ModalBottomSheet(onDismissRequest = onDismiss) {
		Column(
			modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			Text("¿Qué vas a crear?", style = MaterialTheme.typography.headlineSmall)
			KindOption(ProductKind.PHYSICAL, "Producto físico", "Láminas, pegatinas, merch… Con stock y envío.", onPick)
			KindOption(ProductKind.DIGITAL, "Producto digital", "Comisiones y encargos: busto, cuerpo entero…", onPick)
		}
	}
}

@Composable
private fun KindOption(kind: ProductKind, title: String, description: String, onPick: (ProductKind) -> Unit) {
	val (container, content) = kind.tones()
	Card(
		onClick = { onPick(kind) },
		modifier = Modifier.fillMaxWidth(),
		colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
	) {
		Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
			Icon(kind.icon(), contentDescription = null, modifier = Modifier.size(36.dp))
			Column(modifier = Modifier.padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
				Text(title, style = MaterialTheme.typography.titleMedium)
				Text(description, style = MaterialTheme.typography.bodyMedium)
			}
		}
	}
}

/** Quick −/+ per variant, applied immediately. */
@Composable
fun StockAdjustSheet(product: PhysicalProduct, onStep: (VariantId, Int) -> Unit, onDismiss: () -> Unit) {
	ModalBottomSheet(onDismissRequest = onDismiss) {
		Column(
			modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp),
		) {
			Column {
				Text(product.name, style = MaterialTheme.typography.headlineSmall)
				Text(product.stockLabel(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
			}
			product.variants.forEach { variant ->
				Row(verticalAlignment = Alignment.CenterVertically) {
					Column(modifier = Modifier.weight(1f)) {
						Text(variant.name.ifBlank { "Unidades" }, style = MaterialTheme.typography.titleMedium)
						variant.minStock?.let {
							Text("Mínimo $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
						}
					}
					StockStepper(variant.stock, onStep = { onStep(variant.id, it) })
				}
			}
			Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Listo") }
		}
	}
}
