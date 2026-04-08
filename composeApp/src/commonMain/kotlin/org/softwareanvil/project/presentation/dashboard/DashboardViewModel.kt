package org.softwareanvil.project.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import org.softwareanvil.project.domain.model.CommissionStatus
import org.softwareanvil.project.domain.usecase.GetCommissionsUseCase
import kotlin.time.Clock

class DashboardViewModel(
    private val getCommissions: GetCommissionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val allCommissions = getCommissions.all().first()
            val deadlines = getCommissions.upcomingDeadlines().first()

            val nowMillis = kotlin.time.Clock.System.now().toEpochMilliseconds()
            val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(nowMillis)
            val tz = TimeZone.currentSystemDefault()
            val localNow = instant.toLocalDateTime(tz)
            val currentMonth = localNow.month
            val currentYear = localNow.year

            val startOfMonth = LocalDateTime(currentYear, currentMonth, 1, 0, 0)
                .toInstant(tz).toEpochMilliseconds()

            val activeCommissions = allCommissions.filter { it.status.isActive() }

            val completedThisMonth = allCommissions.filter {
                it.status == CommissionStatus.COMPLETED &&
                        it.completedAt != null &&
                        it.completedAt >= startOfMonth
            }

            _uiState.value = DashboardUiState(
                activeCommissions = activeCommissions,
                upcomingDeadlines = deadlines.take(5),
                completedThisMonth = completedThisMonth.size,
                totalNetThisMonth = completedThisMonth.sumOf { it.netPrice },
                totalGrossThisMonth = completedThisMonth.sumOf { it.priceGross + (it.tip ?: 0.0) },
                isLoading = false
            )
        }
    }

    fun refresh() {
        loadDashboard()
    }
}