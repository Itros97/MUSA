package org.softwareanvil.musa.ui.products.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.softwareanvil.musa.domain.logic.TagNormalizer
import org.softwareanvil.musa.domain.model.Tag
import org.softwareanvil.musa.ui.components.MusaIcons
import org.softwareanvil.musa.ui.theme.MusaColors

/** Selected tags as removable chips, a field to type new ones and tappable suggestions. */
@Composable
fun TagInput(
	selected: List<String>,
	query: String,
	suggestions: List<Tag>,
	onQueryChange: (String) -> Unit,
	onAdd: (String) -> Unit,
	onRemove: (String) -> Unit,
) {
	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		if (selected.isNotEmpty()) {
			FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
				selected.forEach { name -> SelectedTag(name, onRemove) }
			}
		}
		OutlinedTextField(
			value = query,
			onValueChange = onQueryChange,
			label = { Text("Etiquetas") },
			placeholder = { Text("Pokémon, Navidad, Feria…") },
			leadingIcon = { Icon(MusaIcons.Label, contentDescription = null) },
			supportingText = { Text("Pulsa Intro o escribe una coma para añadirla") },
			singleLine = true,
			keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
			keyboardActions = KeyboardActions(onDone = { onAdd(query) }),
			modifier = Modifier.fillMaxWidth().onPreviewKeyEvent {
				// Desktop keyboards send Enter as a key event instead of an IME action.
				if (it.key == Key.Enter && query.isNotBlank()) { onAdd(query); true } else false
			},
		)
		val canCreate = query.isNotBlank() && suggestions.none { TagNormalizer.key(it.name) == TagNormalizer.key(query) }
		if (suggestions.isNotEmpty() || canCreate) {
			FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
				if (canCreate) {
					SuggestionChip(
						onClick = { onAdd(query) },
						label = { Text("Crear «${TagNormalizer.displayName(query)}»") },
						icon = { Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp)) },
					)
				}
				suggestions.forEach { tag -> SuggestionChip(onClick = { onAdd(tag.name) }, label = { Text(tag.name) }) }
			}
		}
	}
}

@Composable
private fun SelectedTag(name: String, onRemove: (String) -> Unit) {
	// Same rule the save uses, so a new tag already shows the colour it will get.
	val tones = MusaColors.extra.tags.getValue(TagNormalizer.colorFor(TagNormalizer.key(name)))
	InputChip(
		selected = true,
		onClick = { onRemove(name) },
		label = { Text(name) },
		trailingIcon = { Icon(Icons.Filled.Close, contentDescription = "Quitar $name", modifier = Modifier.size(16.dp)) },
		colors = InputChipDefaults.inputChipColors(
			selectedContainerColor = tones.container,
			selectedLabelColor = tones.content,
			selectedTrailingIconColor = tones.content,
		),
		border = null,
	)
}
