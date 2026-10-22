package org.softwareanvil.musa.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.softwareanvil.musa.domain.logic.DateFormatter

/** Read-only field showing a date; tapping it opens a calendar. */
@Composable
fun DateField(value: LocalDate, onValueChange: (LocalDate) -> Unit, label: String, modifier: Modifier = Modifier, error: String? = null) {
	var picking by remember { mutableStateOf(false) }
	Box(modifier) {
		OutlinedTextField(
			value = DateFormatter.format(value),
			onValueChange = {},
			readOnly = true,
			label = { Text(label) },
			trailingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
			isError = error != null,
			supportingText = error?.let { { Text(it) } },
			modifier = Modifier.fillMaxWidth(),
		)
		// A read-only field swallows taps, so a transparent layer on top opens the calendar.
		Box(Modifier.matchParentSize().clickable { picking = true })
	}
	if (picking) DatePickerPopup(value, onPick = { picking = false; onValueChange(it) }, onDismiss = { picking = false })
}

@Composable
private fun DatePickerPopup(value: LocalDate, onPick: (LocalDate) -> Unit, onDismiss: () -> Unit) {
	// The Material picker works in UTC milliseconds at midnight.
	val state = rememberDatePickerState(initialSelectedDateMillis = value.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds())
	DatePickerDialog(
		onDismissRequest = onDismiss,
		confirmButton = {
			TextButton(onClick = {
				val millis = state.selectedDateMillis ?: return@TextButton onDismiss()
				onPick(Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date)
			}) { Text("Aceptar") }
		},
		dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
	) { DatePicker(state) }
}
