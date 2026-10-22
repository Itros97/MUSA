package org.softwareanvil.musa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Friendly full-width message with an optional call to action. */
@Composable
fun EmptyState(
	icon: ImageVector,
	title: String,
	message: String,
	modifier: Modifier = Modifier,
	actionLabel: String? = null,
	onAction: () -> Unit = {},
) {
	Column(
		modifier = modifier.fillMaxWidth().padding(vertical = 48.dp, horizontal = 24.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(12.dp),
	) {
		Box(
			modifier = Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
			contentAlignment = Alignment.Center,
		) {
			Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(44.dp))
		}
		Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
		Text(
			message,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			textAlign = TextAlign.Center,
		)
		actionLabel?.let { Button(onClick = onAction, modifier = Modifier.padding(top = 8.dp)) { Text(it) } }
	}
}

/** Yes/no dialog; [destructive] paints the confirm button in the error colour. */
@Composable
fun ConfirmDialog(
	title: String,
	message: String,
	confirmLabel: String,
	onConfirm: () -> Unit,
	onDismiss: () -> Unit,
	destructive: Boolean = false,
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(title) },
		text = { Text(message) },
		confirmButton = {
			Button(
				onClick = onConfirm,
				colors = if (destructive) {
					ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
				} else {
					ButtonDefaults.buttonColors()
				},
			) { Text(confirmLabel) }
		},
		dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
	)
}
