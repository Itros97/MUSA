package org.softwareanvil.project.presentation.commissionlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.softwareanvil.project.domain.model.CommissionStatus
import org.softwareanvil.project.domain.usecase.DeleteCommissionUseCase
import org.softwareanvil.project.domain.usecase.GetCommissionsUseCase

class CommissionListViewModel(
    private val getCommissions: GetCommissionsUseCase,
    private val deleteCommission: DeleteCommissionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommissionListUiState())
    val uiState: StateFlow<CommissionListUiState> = _uiState.asStateFlow()

    init {
        loadCommissions()
    }

    private fun loadCommissions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val flow = when {
                _uiState.value.searchQuery.isNotBlank() -> getCommissions.search(_uiState.value.searchQuery)
                _uiState.value.filterStatus != null -> getCommissions.byStatus(_uiState.value.filterStatus!!)
                else -> getCommissions.all()
            }

            flow.collect { commissions ->
                _uiState.value = _uiState.value.copy(
                    commissions = commissions,
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    fun onFilterByStatus(status: CommissionStatus?) {
        _uiState.value = _uiState.value.copy(filterStatus = status, searchQuery = "")
        loadCommissions()
    }

    fun onSearch(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, filterStatus = null)
        loadCommissions()
    }

    fun onDelete(id: Long, currentStatus: CommissionStatus) {
        viewModelScope.launch {
            deleteCommission(id, currentStatus)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }

    fun onClearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}