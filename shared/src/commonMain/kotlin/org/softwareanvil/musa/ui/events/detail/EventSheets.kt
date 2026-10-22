package org.softwareanvil.musa.ui.events.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.ui.components.StockStepper

/** Pick a catalogue variant and how many units to take to the event. */
@Composable
fun AddStockSheet(options: List<StockOption>, error: String?, onAdd: (VariantId, Int) -> Unit, onDismiss: () -> Unit) {
	var selected by remember { mutableStateOf(options.firstOrNull()) }
	var quantity by remember { mutableIntStateOf(1) }
	EventSheet("Llevar productos", error, onDismiss) {
		if (options.isEmpty()) {
			Text("No hay productos físicos con stock en el catálogo.", style = MaterialTheme.typography.bodyMedium)
			return@EventSheet
		}
		LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
			items(options, key = { it.variantId.value }) { option ->
				Row(
					modifier = Modifier.fillMaxWidth().clickable { selected = option; quantity = 1 },
					verticalAlignment = Alignment.CenterVertically,
				) {
					RadioButton(selected = option == selected, onClick = { selected = option; quantity = 1 })
					Text(option.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
					Text("${option.stock} en stock", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
				}
			}
		}
		val option = selected ?: return@EventSheet
		Row(verticalAlignment = Alignment.CenterVertically) {
			Text("Cantidad", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
			StockStepper(quantity, onStep = { quantity = (quantity + it).coerceIn(1, option.stock) })
		}
		Button(onClick = { onAdd(option.variantId, quantity) }, modifier = Modifier.fillMaxWidth()) { Text("Llevar $quantity") }
	}
}

/** Shared frame of the event sheets: title, the content and an error line under it. */
@Composable
fun EventSheet(title: String, error: String?, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
	ModalBottomSheet(onDismissRequest = onDismiss) {
		Column(
			modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			Text(title, style = MaterialTheme.typography.headlineSmall)
			content()
			error?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) }
		}
	}
}
