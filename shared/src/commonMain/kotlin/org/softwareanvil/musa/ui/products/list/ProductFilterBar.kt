package org.softwareanvil.musa.ui.products.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.softwareanvil.musa.domain.logic.ProductQuery
import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.domain.model.Tag
import org.softwareanvil.musa.domain.model.TagId
import org.softwareanvil.musa.ui.components.icon
import org.softwareanvil.musa.ui.theme.MusaColors

/** Search box and filter chips above the catalogue. */
@Composable
fun ProductFilterBar(
	query: ProductQuery,
	tags: List<Tag>,
	onSearchChange: (String) -> Unit,
	onKindToggle: (ProductKind) -> Unit,
	onTagToggle: (TagId) -> Unit,
	onLowStockToggle: () -> Unit,
	onArchivedToggle: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
		OutlinedTextField(
			value = query.text,
			onValueChange = onSearchChange,
			placeholder = { Text("Buscar productos o SKU") },
			leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
			trailingIcon = {
				if (query.text.isNotEmpty()) {
					IconButton(onClick = { onSearchChange("") }) { Icon(Icons.Filled.Clear, contentDescription = "Borrar búsqueda") }
				}
			},
			singleLine = true,
			shape = CircleShape,
			colors = OutlinedTextFieldDefaults.colors(
				unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
				focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
				unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainerLow,
			),
			modifier = Modifier.fillMaxWidth(),
		)
		LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			item { KindChip(ProductKind.PHYSICAL, "Físicos", query, onKindToggle) }
			item { KindChip(ProductKind.DIGITAL, "Digitales", query, onKindToggle) }
			item { FilterChip(selected = query.lowStockOnly, onClick = onLowStockToggle, label = { Text("Stock bajo") }) }
			item { FilterChip(selected = query.archived, onClick = onArchivedToggle, label = { Text("Archivados") }) }
			items(tags, key = { it.id.value }) { tag ->
				FilterChip(
					selected = tag.id in query.tagIds,
					onClick = { onTagToggle(tag.id) },
					label = { Text(tag.name) },
					leadingIcon = { TagDot(tag) },
				)
			}
		}
	}
}

@Composable
private fun KindChip(kind: ProductKind, label: String, query: ProductQuery, onToggle: (ProductKind) -> Unit) =
	FilterChip(
		selected = query.kind == kind,
		onClick = { onToggle(kind) },
		label = { Text(label) },
		leadingIcon = { Icon(kind.icon(), contentDescription = null, modifier = Modifier.size(18.dp)) },
	)

@Composable
private fun TagDot(tag: Tag) = Box(
	Modifier.size(10.dp).clip(CircleShape).background(MusaColors.extra.tags.getValue(tag.color).content),
)
