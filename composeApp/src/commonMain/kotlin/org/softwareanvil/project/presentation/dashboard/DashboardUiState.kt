package org.softwareanvil.project.presentation.dashboard

import org.softwareanvil.project.domain.model.Commission

data class DashboardUiState(
    val activeCommissions: List<Commission> = emptyList(),
    val upcomingDeadlines: List<Commission> = emptyList(),
    val completedThisMonth: Int = 0,
    val totalNetThisMonth: Double = 0.0,
    val totalGrossThisMonth: Double = 0.0,
    val isLoading: Boolean = true
)