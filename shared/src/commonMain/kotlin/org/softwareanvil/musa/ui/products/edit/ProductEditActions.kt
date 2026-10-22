package org.softwareanvil.musa.ui.products.edit

import org.softwareanvil.musa.domain.logic.ProductValidator
import org.softwareanvil.musa.domain.model.Product
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.domain.usecase.ArchiveProductUseCase
import org.softwareanvil.musa.domain.usecase.DeleteProductUseCase
import org.softwareanvil.musa.domain.usecase.DuplicateProductUseCase
import org.softwareanvil.musa.domain.usecase.GetProductUseCase
import org.softwareanvil.musa.domain.usecase.SaveProductUseCase
import org.softwareanvil.musa.domain.usecase.SaveResult

/** Groups the use cases the editor needs and translates their results into editor terms. */
class ProductEditActions(
	private val getProduct: GetProductUseCase,
	private val saveProduct: SaveProductUseCase,
	private val duplicateProduct: DuplicateProductUseCase,
	private val archiveProduct: ArchiveProductUseCase,
	private val deleteProduct: DeleteProductUseCase,
	private val validator: ProductValidator,
) {

	/** Result of saving the form. */
	sealed interface Outcome {
		data object Saved : Outcome
		data class Invalid(val errors: Map<FormField, String>) : Outcome
	}

	suspend fun get(id: ProductId): Product? = getProduct(id)

	/** Parses and saves the form; on unreadable fields it still reports the domain rules, so every error shows at once. */
	suspend fun save(form: ProductForm): Outcome {
		val parsed = ProductFormMapper.parse(form)
		if (parsed.errors.isNotEmpty()) {
			val ruleErrors = validator.validate(parsed.draft).associate { it.toFieldError() }
			return Outcome.Invalid(ruleErrors + parsed.errors)
		}
		return when (val result = saveProduct(parsed.draft)) {
			is SaveResult.Saved -> Outcome.Saved
			is SaveResult.Invalid -> Outcome.Invalid(result.errors.associate { it.toFieldError() })
		}
	}

	suspend fun duplicate(id: ProductId, copyName: String): ProductId = duplicateProduct(id, copyName)

	/** Returns the new status. */
	suspend fun archive(id: ProductId, archived: Boolean): ProductStatus {
		archiveProduct(id, archived)
		return if (archived) ProductStatus.ARCHIVED else ProductStatus.ACTIVE
	}

	suspend fun delete(id: ProductId) = deleteProduct(id)
}
