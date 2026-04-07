package org.softwareanvil.project.presentation.commissionform

data class CommissionFormUiState(
    val id: Long? = null,
    val clientName: String = "",
    val title: String = "",
    val description: String = "",
    val priceGross: String = "",
    val paypalTax: String = "0.0",
    val tip: String = "",
    val deadline: Long? = null,
    val tags: String = "",
    val notes: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errors: Map<String, String> = emptyMap()
)