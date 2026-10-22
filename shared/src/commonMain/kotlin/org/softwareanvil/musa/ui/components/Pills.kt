package org.softwareanvil.musa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.softwareanvil.musa.domain.model.Tag
import org.softwareanvil.musa.ui.theme.MusaColors
import org.softwareanvil.musa.ui.theme.ToneColors

/** Small rounded label; clickable when [onClick] is given. */
@Composable
fun Pill(text: String, tones: ToneColors, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
	Text(
		text = text,
		style = MaterialTheme.typography.labelMedium,
		color = tones.content,
		maxLines = 1,
		overflow = TextOverflow.Ellipsis,
		modifier = modifier
			.clip(CircleShape)
			.background(tones.container)
			.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
			.padding(horizontal = 10.dp, vertical = 4.dp),
	)
}

/** A tag painted with its own colour. */
@Composable
fun TagPill(tag: Tag, modifier: Modifier = Modifier) =
	Pill(tag.name, MusaColors.extra.tags.getValue(tag.color), modifier)
