package org.softwareanvil.musa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.softwareanvil.musa.domain.model.ProductKind

/** Icon for each product kind. */
fun ProductKind.icon() = when (this) {
	ProductKind.PHYSICAL -> MusaIcons.Physical
	ProductKind.DIGITAL -> MusaIcons.Digital
}

/** Container and content colours that tell the two kinds apart at a glance. */
@Composable
fun ProductKind.tones(): Pair<Color, Color> = when (this) {
	ProductKind.PHYSICAL -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
	ProductKind.DIGITAL -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
}

/** Placeholder artwork until photos arrive: the product initial on a kind-tinted tile. */
@Composable
fun ProductThumbnail(name: String, kind: ProductKind, modifier: Modifier = Modifier, size: Dp = 72.dp) {
	val (container, content) = kind.tones()
	Box(
		modifier = modifier.size(size).clip(MaterialTheme.shapes.medium).background(container),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = name.trim().firstOrNull()?.uppercase() ?: "?",
			style = MaterialTheme.typography.headlineMedium,
			color = content,
		)
		Icon(
			imageVector = kind.icon(),
			contentDescription = null,
			tint = content,
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(6.dp)
				.size(20.dp)
				.clip(CircleShape)
				.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
				.padding(3.dp),
		)
	}
}
