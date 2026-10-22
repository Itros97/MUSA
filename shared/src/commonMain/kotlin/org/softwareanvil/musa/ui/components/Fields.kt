package org.softwareanvil.musa.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType

/** Outlined field with label, optional hint and an error shown underneath. */
@Composable
fun MusaTextField(
	value: String,
	onValueChange: (String) -> Unit,
	label: String,
	modifier: Modifier = Modifier,
	placeholder: String? = null,
	error: String? = null,
	hint: String? = null,
	suffix: String? = null,
	singleLine: Boolean = true,
	minLines: Int = 1,
	keyboardType: KeyboardType = KeyboardType.Text,
) {
	OutlinedTextField(
		value = value,
		onValueChange = onValueChange,
		label = { Text(label) },
		placeholder = placeholder?.let { { Text(it) } },
		suffix = suffix?.let { { Text(it) } },
		isError = error != null,
		supportingText = (error ?: hint)?.let { { Text(it) } },
		singleLine = singleLine,
		minLines = minLines,
		keyboardOptions = KeyboardOptions(
			capitalization = if (keyboardType == KeyboardType.Text) KeyboardCapitalization.Sentences else KeyboardCapitalization.None,
			keyboardType = keyboardType,
			imeAction = if (singleLine) ImeAction.Next else ImeAction.Default,
		),
		modifier = modifier,
	)
}

/** Euro amount field with a decimal keyboard. */
@Composable
fun MoneyField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, error: String? = null) =
	MusaTextField(
		value = value,
		onValueChange = onValueChange,
		label = label,
		modifier = modifier,
		placeholder = "0,00",
		error = error,
		suffix = "€",
		keyboardType = KeyboardType.Decimal,
	)
