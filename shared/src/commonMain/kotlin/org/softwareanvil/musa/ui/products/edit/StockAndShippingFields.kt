package org.softwareanvil.musa.ui.products.edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.softwareanvil.musa.ui.components.MusaIcons
import org.softwareanvil.musa.ui.components.MusaTextField

private val SHIPPING_FIELDS = listOf(VariantField.WEIGHT, VariantField.WIDTH, VariantField.HEIGHT, VariantField.DEPTH)

/** Stock count with −/+ buttons inside the field. */
@Composable
fun StockField(variant: VariantForm, error: String?, viewModel: ProductEditViewModel, modifier: Modifier = Modifier) {
	OutlinedTextField(
		value = variant[VariantField.STOCK],
		onValueChange = { viewModel.onVariantChange(variant.id, VariantField.STOCK, it) },
		label = { Text("En stock") },
		isError = error != null,
		supportingText = error?.let { { Text(it) } },
		singleLine = true,
		keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
		leadingIcon = {
			IconButton(onClick = { viewModel.onStockStep(variant.id, -1) }) { Icon(MusaIcons.Remove, "Quitar uno") }
		},
		trailingIcon = {
			IconButton(onClick = { viewModel.onStockStep(variant.id, 1) }) { Icon(Icons.Filled.Add, "Añadir uno") }
		},
		modifier = modifier,
	)
}

/** Optional weight and size, folded away until needed; opens by itself when filled or wrong. */
@Composable
fun ShippingFields(variant: VariantForm, error: (VariantField) -> String?, change: (VariantField) -> (String) -> Unit) {
	val hasContent = SHIPPING_FIELDS.any { variant[it].isNotBlank() || error(it) != null }
	var expanded by rememberSaveable(variant.id.value) { mutableStateOf(hasContent) }
	val open = expanded || SHIPPING_FIELDS.any { error(it) != null }

	Column {
		TextButton(onClick = { expanded = !open }) {
			Text("Envío: peso y medidas (opcional)")
			Icon(
				if (open) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
				contentDescription = null,
				modifier = Modifier.padding(start = 4.dp),
			)
		}
		AnimatedVisibility(open) {
			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				MeasureField(variant, VariantField.WEIGHT, "Peso", "g", error, change, Modifier.fillMaxWidth())
				Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
					MeasureField(variant, VariantField.WIDTH, "Ancho", "cm", error, change, Modifier.weight(1f))
					MeasureField(variant, VariantField.HEIGHT, "Alto", "cm", error, change, Modifier.weight(1f))
					MeasureField(variant, VariantField.DEPTH, "Fondo", "cm", error, change, Modifier.weight(1f))
				}
			}
		}
	}
}

@Composable
private fun MeasureField(
	variant: VariantForm,
	field: VariantField,
	label: String,
	unit: String,
	error: (VariantField) -> String?,
	change: (VariantField) -> (String) -> Unit,
	modifier: Modifier,
) = MusaTextField(
	value = variant[field],
	onValueChange = change(field),
	label = label,
	suffix = unit,
	error = error(field),
	keyboardType = KeyboardType.Decimal,
	modifier = modifier,
)
