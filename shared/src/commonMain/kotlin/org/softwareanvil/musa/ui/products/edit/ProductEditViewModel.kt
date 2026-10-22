package org.softwareanvil.musa.ui.products.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.softwareanvil.musa.domain.logic.IdGenerator
import org.softwareanvil.musa.domain.logic.TagSuggester
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.domain.model.Tag
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.domain.usecase.ObserveTagsUseCase

/** Drives the product editor: typed text, variants, tags and the actions of an existing product. */
class ProductEditViewModel(
	target: EditTarget,
	private val actions: ProductEditActions,
	observeTags: ObserveTagsUseCase,
	private val ids: IdGenerator,
) : ViewModel() {

	private val _state = MutableStateFlow(ProductEditState(isNew = target is EditTarget.New))
	val state: StateFlow<ProductEditState> = _state.asStateFlow()

	private val _events = Channel<EditEvent>(Channel.BUFFERED)
	val events: Flow<EditEvent> = _events.receiveAsFlow()

	private var initialForm: ProductForm? = null
	private var allTags: List<Tag> = emptyList()

	init {
		viewModelScope.launch { load(target) }
		viewModelScope.launch {
			observeTags().collect {
				allTags = it
				refreshSuggestions()
			}
		}
	}

	private suspend fun load(target: EditTarget) {
		val form = when (target) {
			is EditTarget.New -> ProductForm(ProductId(ids.next()), target.kind, variants = listOf(VariantForm(VariantId(ids.next()))))
			is EditTarget.Existing -> actions.get(target.id)?.let(ProductFormMapper::fromProduct)
		}
		if (form == null) return _events.send(EditEvent.Close)
		initialForm = form
		_state.update { it.copy(form = form) }
		refreshSuggestions()
	}

	// region fields

	fun onNameChange(value: String) = updateForm(FormField.Name) { it.copy(name = value) }

	fun onDescriptionChange(value: String) = updateForm { it.copy(description = value) }

	fun onMaterialChange(value: String) = updateForm { it.copy(material = value) }

	fun onDeliveryFormatChange(value: String) = updateForm { it.copy(deliveryFormat = value) }

	fun onTurnaroundChange(value: String) = updateForm(FormField.TurnaroundDays) { it.copy(turnaroundDays = value) }

	fun onStatusChange(status: ProductStatus) = updateForm { it.copy(status = status) }

	// endregion

	// region tags

	/** Typing a comma confirms the tag, like pressing Enter. */
	fun onTagQueryChange(text: String) {
		if (text.endsWith(",")) return onTagAdd(text.dropLast(1))
		_state.update { it.copy(tagQuery = text) }
		refreshSuggestions()
	}

	fun onTagAdd(raw: String) {
		updateForm { ProductFormReducer.addTag(it, raw, allTags) }
		_state.update { it.copy(tagQuery = "") }
		refreshSuggestions()
	}

	fun onTagRemove(name: String) {
		updateForm { it.copy(tagNames = it.tagNames - name) }
		refreshSuggestions()
	}

	private fun refreshSuggestions() = _state.update { state ->
		val selected = state.form?.tagNames.orEmpty()
		state.copy(tagSuggestions = TagSuggester.suggest(allTags, selected, state.tagQuery))
	}

	// endregion

	// region variants

	fun onVariantChange(id: VariantId, field: VariantField, value: String) =
		updateForm(FormField.Variant(id, field)) { form -> ProductFormReducer.updateVariant(form, id) { it.with(field, value) } }

	fun onStockStep(id: VariantId, delta: Int) =
		updateForm(FormField.Variant(id, VariantField.STOCK)) { ProductFormReducer.stepStock(it, id, delta) }

	fun onAddVariant() = updateForm(FormField.Variants) { ProductFormReducer.addVariant(it, VariantId(ids.next())) }

	fun onRemoveVariant(id: VariantId) = updateForm { ProductFormReducer.removeVariant(it, id) }

	// endregion

	// region actions

	fun onSave() {
		val form = _state.value.form ?: return
		if (_state.value.isSaving) return
		viewModelScope.launch {
			_state.update { it.copy(isSaving = true) }
			when (val result = actions.save(form)) {
				is ProductEditActions.Outcome.Saved -> {
					initialForm = form
					_state.update { it.copy(isSaving = false, isDirty = false) }
					_events.send(EditEvent.Close)
				}
				is ProductEditActions.Outcome.Invalid -> {
					_state.update { it.copy(isSaving = false, errors = result.errors) }
					_events.send(EditEvent.Message("Revisa los campos marcados"))
				}
			}
		}
	}

	fun onBack() {
		if (_state.value.isDirty) _state.update { it.copy(dialog = EditDialog.DISCARD) }
		else viewModelScope.launch { _events.send(EditEvent.Close) }
	}

	fun onDiscardConfirm() {
		_state.update { it.copy(dialog = null) }
		viewModelScope.launch { _events.send(EditEvent.Close) }
	}

	fun onDeleteRequest() = _state.update { it.copy(dialog = EditDialog.DELETE) }

	fun onDialogDismiss() = _state.update { it.copy(dialog = null) }

	fun onDeleteConfirm() {
		val form = _state.value.form ?: return
		_state.update { it.copy(dialog = null) }
		viewModelScope.launch {
			actions.delete(form.id)
			_events.send(EditEvent.Close)
		}
	}

	/** Archiving applies at once and does not count as an unsaved change. */
	fun onArchiveToggle() {
		val form = _state.value.form ?: return
		val archive = form.status != ProductStatus.ARCHIVED
		viewModelScope.launch {
			val status = actions.archive(form.id, archive)
			initialForm = initialForm?.copy(status = status)
			updateForm { it.copy(status = status) }
			_events.send(EditEvent.Message(if (archive) "Producto archivado" else "Producto restaurado"))
		}
	}

	fun onDuplicate() {
		val form = _state.value.form ?: return
		viewModelScope.launch {
			if (_state.value.isDirty) return@launch _events.send(EditEvent.Message("Guarda los cambios antes de duplicar"))
			_events.send(EditEvent.OpenProduct(actions.duplicate(form.id, "Copia de ${form.name}")))
		}
	}

	// endregion

	private fun updateForm(clearing: FormField? = null, transform: (ProductForm) -> ProductForm) = _state.update { state ->
		val form = state.form ?: return@update state
		val updated = transform(form)
		state.copy(form = updated, isDirty = updated != initialForm, errors = clearing?.let { state.errors - it } ?: state.errors)
	}
}
