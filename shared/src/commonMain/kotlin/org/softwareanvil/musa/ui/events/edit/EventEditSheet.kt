package org.softwareanvil.musa.ui.events.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.ui.components.DateField
import org.softwareanvil.musa.ui.components.MusaTextField

/** Sheet with the name, place and dates of an event; calls [onSaved] once stored. */
@Composable
fun EventEditSheet(
	target: EventEditTarget,
	onSaved: (EventId) -> Unit,
	onDismiss: () -> Unit,
	viewModel: EventEditViewModel = koinViewModel(key = target.toString()) { parametersOf(target) },
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	LaunchedEffect(viewModel) { viewModel.saved.collect(onSaved) }

	ModalBottomSheet(onDismissRequest = onDismiss) {
		val draft = state.draft ?: return@ModalBottomSheet
		Column(
			modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			Text(if (state.isNew) "Nuevo evento" else "Editar evento", style = MaterialTheme.typography.headlineSmall)
			MusaTextField(
				value = draft.name,
				onValueChange = viewModel::onNameChange,
				label = "Nombre",
				placeholder = "Feria del cómic, mercadillo de Navidad…",
				error = state.errors[EventField.NAME],
				modifier = Modifier.fillMaxWidth(),
			)
			MusaTextField(
				value = draft.location,
				onValueChange = viewModel::onLocationChange,
				label = "Lugar",
				placeholder = "Ciudad, recinto o stand",
				modifier = Modifier.fillMaxWidth(),
			)
			Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
				DateField(draft.startDate, viewModel::onStartDateChange, "Empieza", Modifier.weight(1f))
				DateField(draft.endDate, viewModel::onEndDateChange, "Termina", Modifier.weight(1f), error = state.errors[EventField.END_DATE])
			}
			Button(onClick = viewModel::onSave, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) {
				Text(if (state.isNew) "Crear evento" else "Guardar cambios")
			}
		}
	}
}
