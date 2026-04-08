package org.softwareanvil.project.presentation.commissiondetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.softwareanvil.project.domain.model.CommissionStatus
import org.softwareanvil.project.domain.usecase.DeleteCommissionUseCase
import org.softwareanvil.project.domain.usecase.GetCommissionsUseCase
import org.softwareanvil.project.domain.usecase.UpdateStatusUseCase

class CommissionDetailViewModel(
    private val getCommissions: GetCommissionsUseCase,
    private val updateStatus: UpdateStatusUseCase,
    private val deleteCommission: DeleteCommissionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommissionDetailUiState())
    val uiState: StateFlow<CommissionDetailUiState> = _uiState.asStateFlow()

    fun loadCommission(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val commission = getCommissions.byId(id)
            _uiState.value = _uiState.value.copy(
                commission = commission,
                isLoading = false
            )
        }
    }

    fun onChangeStatus(newStatus: CommissionStatus) {
        val commission = _uiState.value.commission ?: return
        viewModelScope.launch {
            updateStatus(commission.id, commission.status, newStatus)
                .onSuccess { loadCommission(commission.id) }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }

    fun onDelete() {
        val commission = _uiState.value.commission ?: return
        viewModelScope.launch {
            deleteCommission(commission.id, commission.status)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isDeleted = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }

    fun onClearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}