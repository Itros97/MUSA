package org.softwareanvil.musa.domain.model

/** Unique identifier of a product. */
data class ProductId(val value: String)

/** Unique identifier of a product variant. */
data class VariantId(val value: String)

/** Unique identifier of a tag. */
data class TagId(val value: String)

/** Unique identifier of an event. */
data class EventId(val value: String)

/** Unique identifier of an event expense. */
data class ExpenseId(val value: String)

/** Unique identifier of a sale. */
data class SaleId(val value: String)

/** Unique identifier of one line of a sale. */
data class SaleLineId(val value: String)
