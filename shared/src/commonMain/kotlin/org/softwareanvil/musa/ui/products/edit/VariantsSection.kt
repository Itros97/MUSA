package org.softwareanvil.musa.ui.products.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.ui.components.MusaTextField
import org.softwareanvil.musa.ui.components.SectionCard

/** Price, cost and stock; a single variant looks like a plain product until the artist adds more. */
@Composable
fun VariantsSection(state: ProductEditState, form: ProductForm, viewModel: ProductEditViewModel) {
	val single = form.variants.size == 1
	SectionCard(
		title = if (form.kind == ProductKind.PHYSICAL) "Precio y stock" else "Precio",
		subtitle = if (single) "¿Tienes tamaños o versiones? Añade variantes con su propio precio." else null,
	) {
		if (single) {
			VariantFields(form.kind, form.variants.single(), state.errors, viewModel)
		} else {
			form.variants.forEach { variant ->
				OutlinedCard(modifier = Modifier.fillMaxWidth()) {
					Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
						Row(verticalAlignment = Alignment.CenterVertically) {
							MusaTextField(
								value = variant[VariantField.NAME],
								onValueChange = { viewModel.onVariantChange(variant.id, VariantField.NAME, it) },
								label = "Nombre de la variante",
								placeholder = if (form.kind == ProductKind.PHYSICAL) "A4, A3…" else "Busto, cuerpo entero…",
								error = state.errors[FormField.Variant(variant.id, VariantField.NAME)],
								modifier = Modifier.weight(1f),
							)
							IconButton(onClick = { viewModel.onRemoveVariant(variant.id) }) {
								Icon(Icons.Filled.Delete, contentDescription = "Quitar variante")
							}
						}
						VariantFields(form.kind, variant, state.errors, viewModel)
					}
				}
			}
		}
		state.errors[FormField.Variants]?.let {
			Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
		}
		OutlinedButton(onClick = viewModel::onAddVariant) {
			Icon(Icons.Filled.Add, contentDescription = null)
			Text("Añadir variante", modifier = Modifier.padding(start = 8.dp))
		}
	}
}
