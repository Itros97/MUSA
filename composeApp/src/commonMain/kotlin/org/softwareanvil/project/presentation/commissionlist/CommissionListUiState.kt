package org.softwareanvil.project.presentation.commissionlist

import org.softwareanvil.project.domain.model.Commission
import org.softwareanvil.project.domain.model.CommissionStatus

data class CommissionListUiState(
    val commissions: List<Commission> = emptyList(),
    val filterStatus: CommissionStatus? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)