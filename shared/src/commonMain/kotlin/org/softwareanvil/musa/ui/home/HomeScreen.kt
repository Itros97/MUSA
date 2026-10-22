package org.softwareanvil.musa.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Landing screen: one tile per section of the app. */
@Composable
fun HomeScreen(onOpenSection: (HomeSection) -> Unit) {
	Scaffold { padding ->
		LazyVerticalGrid(
			columns = GridCells.Adaptive(minSize = 300.dp),
			contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp),
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
			modifier = Modifier.fillMaxSize().padding(padding),
		) {
			item(span = { GridItemSpan(maxLineSpan) }) { Header() }
			items(HomeSection.entries, key = { it.name }) { section ->
				SectionTile(section, onClick = { onOpenSection(section) })
			}
		}
	}
}

@Composable
private fun Header() {
	Column(modifier = Modifier.padding(top = 8.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
		Text("MUSA", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
		Text(
			"Tu estudio, tu stock y tus ventas, en un sitio",
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
		)
	}
}
