package org.softwareanvil.project.presentation.commissiondetail

import org.softwareanvil.project.domain.model.Commission

data class CommissionDetailUiState(
    val commission: Commission? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleted: Boolean = false
)