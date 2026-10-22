package org.softwareanvil.musa.ui.products.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.ui.components.ConfirmDialog

/** Create or edit a product, section by section. */
@Composable
fun ProductEditScreen(
	target: EditTarget,
	onClose: () -> Unit,
	onOpenProduct: (ProductId) -> Unit,
	viewModel: ProductEditViewModel = koinViewModel { parametersOf(target) },
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val snackbar = remember { SnackbarHostState() }

	LaunchedEffect(viewModel) {
		viewModel.events.collect { event ->
			when (event) {
				EditEvent.Close -> onClose()
				is EditEvent.OpenProduct -> onOpenProduct(event.id)
				is EditEvent.Message -> snackbar.showSnackbar(event.text)
			}
		}
	}
	// Only intercept the system back gesture when there is something to lose.
	NavigationBackHandler(
		state = rememberNavigationEventState(NavigationEventInfo.None),
		isBackEnabled = state.isDirty,
		onBackCompleted = viewModel::onBack,
	)

	Scaffold(topBar = { ProductEditTopBar(state, viewModel) }, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
		val form = state.form
		if (form == null) {
			Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
			return@Scaffold
		}
		Column(
			modifier = Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			Column(modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
				val archived = form.status == ProductStatus.ARCHIVED
				if (archived) ArchivedBanner(onRestore = viewModel::onArchiveToggle)
				BasicsSection(state, form, viewModel)
				VariantsSection(state, form, viewModel)
				if (form.kind == ProductKind.DIGITAL) DeliverySection(state, form, viewModel)
				if (!archived) StatusSection(form, viewModel)
				Button(onClick = viewModel::onSave, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
					Text(if (state.isNew) "Crear producto" else "Guardar cambios", modifier = Modifier.padding(vertical = 6.dp))
				}
			}
		}
	}

	when (state.dialog) {
		EditDialog.DISCARD -> ConfirmDialog(
			title = "¿Descartar cambios?",
			message = "Has hecho cambios que todavía no has guardado.",
			confirmLabel = "Descartar",
			onConfirm = viewModel::onDiscardConfirm,
			onDismiss = viewModel::onDialogDismiss,
			destructive = true,
		)
		EditDialog.DELETE -> ConfirmDialog(
			title = "¿Eliminar «${state.form?.name}»?",
			message = "Desaparecerá del catálogo. Si solo quieres ocultarlo, mejor archívalo.",
			confirmLabel = "Eliminar",
			onConfirm = viewModel::onDeleteConfirm,
			onDismiss = viewModel::onDialogDismiss,
			destructive = true,
		)
		null -> Unit
	}
}
