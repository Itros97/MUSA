package org.softwareanvil.musa.ui.products.edit

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.ui.components.MusaTextField
import org.softwareanvil.musa.ui.components.SectionCard

/** Name, description, material and tags. */
@Composable
fun BasicsSection(state: ProductEditState, form: ProductForm, viewModel: ProductEditViewModel) {
	SectionCard(title = "Lo básico") {
		MusaTextField(
			value = form.name,
			onValueChange = viewModel::onNameChange,
			label = "Nombre",
			placeholder = if (form.kind == ProductKind.PHYSICAL) "Lámina «Gato astronauta»" else "Comisión retrato",
			error = state.errors[FormField.Name],
			modifier = Modifier.fillMaxWidth(),
		)
		MusaTextField(
			value = form.description,
			onValueChange = viewModel::onDescriptionChange,
			label = "Descripción",
			placeholder = "Lo que le contarías a quien lo compra",
			singleLine = false,
			minLines = 3,
			modifier = Modifier.fillMaxWidth(),
		)
		if (form.kind == ProductKind.PHYSICAL) {
			MusaTextField(
				value = form.material,
				onValueChange = viewModel::onMaterialChange,
				label = "Material",
				placeholder = "Papel 300 g, vinilo mate…",
				modifier = Modifier.fillMaxWidth(),
			)
		}
		TagInput(
			selected = form.tagNames,
			query = state.tagQuery,
			suggestions = state.tagSuggestions,
			onQueryChange = viewModel::onTagQueryChange,
			onAdd = viewModel::onTagAdd,
			onRemove = viewModel::onTagRemove,
		)
	}
}
