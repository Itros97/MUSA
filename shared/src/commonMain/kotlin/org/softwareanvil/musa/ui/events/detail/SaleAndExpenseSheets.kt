package org.softwareanvil.musa.ui.events.detail

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.softwareanvil.musa.domain.logic.MoneyFormatter
import org.softwareanvil.musa.domain.logic.MoneyParser
import org.softwareanvil.musa.domain.model.EventStockLine
import org.softwareanvil.musa.ui.components.MoneyField
import org.softwareanvil.musa.ui.components.MusaTextField
import org.softwareanvil.musa.ui.components.StockStepper
import org.softwareanvil.musa.ui.events.itemLabel

/** Sell units of a line: quantity starts at one and the price at the variant's, both editable. */
@Composable
fun SaleSheet(line: EventStockLine, error: String?, onSell: (Int, String) -> Unit, onDismiss: () -> Unit) {
	var quantity by remember { mutableIntStateOf(1) }
	var price by remember { mutableStateOf(MoneyFormatter.formatForInput(line.price)) }
	EventSheet(itemLabel(line.productName, line.variantName), error, onDismiss) {
		Text("Quedan ${line.remaining} en el evento", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
		Row(verticalAlignment = Alignment.CenterVertically) {
			Text("Cantidad", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
			StockStepper(quantity, onStep = { quantity = (quantity + it).coerceIn(1, maxOf(1, line.remaining)) })
		}
		MoneyField(price, { price = it }, label = "Precio por unidad", modifier = Modifier.fillMaxWidth())
		val total = MoneyParser.parse(price)?.let { MoneyFormatter.format(it * quantity) }
		Button(onClick = { onSell(quantity, price) }, modifier = Modifier.fillMaxWidth()) {
			Text(total?.let { "Vender · $it" } ?: "Vender")
		}
	}
}

/** Record a cost of attending: a concept and an amount. */
@Composable
fun ExpenseSheet(error: String?, onAdd: (String, String) -> Unit, onDismiss: () -> Unit) {
	var concept by remember { mutableStateOf("") }
	var amount by remember { mutableStateOf("") }
	EventSheet("Nuevo gasto", error, onDismiss) {
		MusaTextField(concept, { concept = it }, label = "Concepto", placeholder = "Stand, gasolina, hotel…", modifier = Modifier.fillMaxWidth())
		MoneyField(amount, { amount = it }, label = "Importe", modifier = Modifier.fillMaxWidth())
		Button(onClick = { onAdd(concept, amount) }, modifier = Modifier.fillMaxWidth()) { Text("Añadir gasto") }
	}
}
