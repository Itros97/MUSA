package org.softwareanvil.musa.ui.products.edit

import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.domain.model.Tag

/** What the editor opens: a blank product of a kind, or an existing one. */
sealed interface EditTarget {
	data class New(val kind: ProductKind) : EditTarget
	data class Existing(val id: ProductId) : EditTarget
}

/** Everything the editor draws; [form] is null while loading. */
data class ProductEditState(
	val form: ProductForm? = null,
	val isNew: Boolean = true,
	val errors: Map<FormField, String> = emptyMap(),
	val tagQuery: String = "",
	val tagSuggestions: List<Tag> = emptyList(),
	val isDirty: Boolean = false,
	val isSaving: Boolean = false,
	val dialog: EditDialog? = null,
)

/** Confirmation dialogs of the editor. */
enum class EditDialog { DISCARD, DELETE }

/** One-off things the screen must do. */
sealed interface EditEvent {
	data object Close : EditEvent
	data class OpenProduct(val id: ProductId) : EditEvent
	data class Message(val text: String) : EditEvent
}
