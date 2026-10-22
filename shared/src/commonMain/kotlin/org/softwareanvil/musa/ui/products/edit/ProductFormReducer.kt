package org.softwareanvil.musa.ui.products.edit

import org.softwareanvil.musa.domain.logic.NumberParser
import org.softwareanvil.musa.domain.logic.TagNormalizer
import org.softwareanvil.musa.domain.model.Tag
import org.softwareanvil.musa.domain.model.VariantId

/** Pure edits of the form that need more than a field copy. */
object ProductFormReducer {

	/** Adds a tag unless already present, reusing the spelling of an existing tag with the same key. */
	fun addTag(form: ProductForm, raw: String, existing: List<Tag>): ProductForm {
		val key = TagNormalizer.key(raw)
		if (key.isEmpty() || form.tagNames.any { TagNormalizer.key(it) == key }) return form
		val name = existing.firstOrNull { TagNormalizer.key(it.name) == key }?.name ?: TagNormalizer.displayName(raw)
		return form.copy(tagNames = form.tagNames + name)
	}

	/** Moves the stock count by [delta], never below zero; unreadable text counts as zero. */
	fun stepStock(form: ProductForm, id: VariantId, delta: Int): ProductForm = updateVariant(form, id) {
		val current = NumberParser.parseCount(it[VariantField.STOCK]) ?: 0
		it.with(VariantField.STOCK, maxOf(0, current + delta).toString())
	}

	/** Appends a variant prefilled with the first variant's price and cost, the usual starting point. */
	fun addVariant(form: ProductForm, id: VariantId): ProductForm {
		val first = form.variants.first()
		val variant = VariantForm(id)
			.with(VariantField.PRICE, first[VariantField.PRICE])
			.with(VariantField.COST, first[VariantField.COST])
		return form.copy(variants = form.variants + variant)
	}

	/** Removes a variant, always keeping at least one. */
	fun removeVariant(form: ProductForm, id: VariantId): ProductForm =
		if (form.variants.size < 2) form else form.copy(variants = form.variants.filterNot { it.id == id })

	/** Applies [transform] to the variant with [id]. */
	fun updateVariant(form: ProductForm, id: VariantId, transform: (VariantForm) -> VariantForm): ProductForm =
		form.copy(variants = form.variants.map { if (it.id == id) transform(it) else it })
}
