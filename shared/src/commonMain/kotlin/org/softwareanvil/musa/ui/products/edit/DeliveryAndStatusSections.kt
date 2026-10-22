package org.softwareanvil.musa.ui.products.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.ui.components.MusaIcons
import org.softwareanvil.musa.ui.components.MusaTextField
import org.softwareanvil.musa.ui.components.SectionCard

/** How and when a digital work is delivered. */
@Composable
fun DeliverySection(state: ProductEditState, form: ProductForm, viewModel: ProductEditViewModel) {
	SectionCard(title = "Entrega") {
		MusaTextField(
			value = form.deliveryFormat,
			onValueChange = viewModel::onDeliveryFormatChange,
			label = "Formato",
			placeholder = "PNG de alta resolución, PSD…",
			modifier = Modifier.fillMaxWidth(),
		)
		MusaTextField(
			value = form.turnaroundDays,
			onValueChange = viewModel::onTurnaroundChange,
			label = "Plazo estimado",
			suffix = "días",
			error = state.errors[FormField.TurnaroundDays],
			keyboardType = KeyboardType.Number,
			modifier = Modifier.fillMaxWidth(),
		)
	}
}

private val EDITABLE_STATUSES = listOf(ProductStatus.DRAFT to "Borrador", ProductStatus.ACTIVE to "Activo")

/** Draft or active; archiving lives in the menu. */
@Composable
fun StatusSection(form: ProductForm, viewModel: ProductEditViewModel) {
	SectionCard(title = "Estado", subtitle = "Usa borrador mientras aún lo estás preparando.") {
		SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
			EDITABLE_STATUSES.forEachIndexed { index, (status, label) ->
				SegmentedButton(
					selected = form.status == status,
					onClick = { viewModel.onStatusChange(status) },
					shape = SegmentedButtonDefaults.itemShape(index, EDITABLE_STATUSES.size),
				) { Text(label) }
			}
		}
	}
}

/** Shown on top of an archived product, with a shortcut to restore it. */
@Composable
fun ArchivedBanner(onRestore: () -> Unit) {
	Card(
		modifier = Modifier.fillMaxWidth(),
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
	) {
		Row(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
			Icon(MusaIcons.Archive, contentDescription = null)
			Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
				Text("Producto archivado", style = MaterialTheme.typography.titleSmall)
				Text("No aparece en el catálogo principal.", style = MaterialTheme.typography.bodySmall)
			}
			TextButton(onClick = onRestore) { Text("Restaurar") }
		}
	}
}
