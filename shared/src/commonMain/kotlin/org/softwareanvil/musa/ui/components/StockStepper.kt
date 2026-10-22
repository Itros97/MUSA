package org.softwareanvil.musa.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Big −/+ buttons around a count, designed for quick taps at a fair stall. */
@Composable
fun StockStepper(value: Int, onStep: (Int) -> Unit, modifier: Modifier = Modifier) {
	Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
		FilledTonalIconButton(onClick = { onStep(-1) }, enabled = value > 0) {
			Icon(MusaIcons.Remove, contentDescription = "Quitar uno")
		}
		Text(
			text = value.toString(),
			style = MaterialTheme.typography.titleLarge,
			textAlign = TextAlign.Center,
			modifier = Modifier.widthIn(min = 48.dp),
		)
		FilledTonalIconButton(onClick = { onStep(1) }) {
			Icon(Icons.Filled.Add, contentDescription = "Añadir uno")
		}
	}
}
