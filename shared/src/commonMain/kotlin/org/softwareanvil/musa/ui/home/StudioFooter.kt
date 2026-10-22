package org.softwareanvil.musa.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val STUDIO = "Software Anvil"
private const val AUTHOR = "Itros97"
private const val SINCE = 2026
private const val FOOTER_ALPHA = 0.6f

/** Quiet signature at the end of the home screen: a spark between two rules, the app and who makes it. */
@Composable
fun StudioFooter(modifier: Modifier = Modifier) {
	val tint = MaterialTheme.colorScheme.onSurfaceVariant
	Column(
		modifier = modifier.fillMaxWidth().padding(top = 32.dp, bottom = 8.dp).alpha(FOOTER_ALPHA),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(10.dp),
	) {
		Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
			HorizontalDivider(modifier = Modifier.width(72.dp), color = tint)
			// A small square turned 45° reads as a spark struck off the anvil.
			Box(Modifier.size(6.dp).rotate(45f).background(tint))
			HorizontalDivider(modifier = Modifier.width(72.dp), color = tint)
		}
		Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			Text("MUSA", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, letterSpacing = 4.sp, color = tint)
			Text("by $STUDIO", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic, color = tint)
		}
		Text("© $SINCE · $AUTHOR", style = MaterialTheme.typography.labelSmall, color = tint)
	}
}
