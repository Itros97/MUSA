package org.softwareanvil.project.presentation.commissionform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.softwareanvil.project.domain.model.Commission
import org.softwareanvil.project.domain.model.CommissionStatus
import org.softwareanvil.project.domain.usecase.CreateCommissionUseCase
import org.softwareanvil.project.domain.usecase.GetCommissionsUseCase
import org.softwareanvil.project.domain.usecase.UpdateCommissionUseCase

class CommissionFormViewModel(
    private val createCommission: CreateCommissionUseCase,
    private val updateCommission: UpdateCommissionUseCase,
    private val getCommissions: GetCommissionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommissionFormUiState())
    val uiState: StateFlow<CommissionFormUiState> = _uiState.asStateFlow()

    fun loadCommission(id: Long) {
        viewModelScope.launch {
            getCommissions.byId(id)?.let { commission ->
                _uiState.value = CommissionFormUiState(
                    id = commission.id,
                    clientName = commission.clientName,
                    title = commission.title,
                    description = commission.description ?: "",
                    priceGross = commission.priceGross.toString(),
                    paypalTax = commission.paypalTax.toString(),
                    tip = commission.tip?.toString() ?: "",
                    deadline = commission.deadline,
                    tags = commission.tags.joinToString(", "),
                    notes = commission.notes ?: "",
                    isEditing = true
                )
            }
        }
    }

    fun onClientNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(clientName = value, errors = _uiState.value.errors - "clientName")
    }

    fun onTitleChanged(value: String) {
        _uiState.value = _uiState.value.copy(title = value, errors = _uiState.value.errors - "title")
    }

    fun onDescriptionChanged(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun onPriceGrossChanged(value: String) {
        _uiState.value = _uiState.value.copy(priceGross = value, errors = _uiState.value.errors - "priceGross")
    }

    fun onPaypalTaxChanged(value: String) {
        _uiState.value = _uiState.value.copy(paypalTax = value)
    }

    fun onTipChanged(value: String) {
        _uiState.value = _uiState.value.copy(tip = value)
    }

    fun onDeadlineChanged(value: Long?) {
        _uiState.value = _uiState.value.copy(deadline = value)
    }

    fun onTagsChanged(value: String) {
        _uiState.value = _uiState.value.copy(tags = value)
    }

    fun onNotesChanged(value: String) {
        _uiState.value = _uiState.value.copy(notes = value)
    }

    fun onSave() {
        val state = _uiState.value
        val errors = mutableMapOf<String, String>()

        if (state.clientName.isBlank()) errors["clientName"] = "El nombre del cliente es obligatorio"
        if (state.title.isBlank()) errors["title"] = "El título es obligatorio"
        val price = state.priceGross.toDoubleOrNull()
        if (price == null || price <= 0) errors["priceGross"] = "El precio debe ser mayor que 0"

        if (errors.isNotEmpty()) {
            _uiState.value = state.copy(errors = errors)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)

            val commission = Commission(
                id = state.id ?: 0,
                clientName = state.clientName.trim(),
                title = state.title.trim(),
                description = state.description.trim().ifBlank { null },
                status = if (state.isEditing) CommissionStatus.INQUIRY else CommissionStatus.INQUIRY,
                priceGross = price!!,
                paypalTax = state.paypalTax.toDoubleOrNull() ?: 0.0,
                tip = state.tip.toDoubleOrNull(),
                createdAt = 0,
                deadline = state.deadline,
                tags = state.tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                notes = state.notes.trim().ifBlank { null }
            )

            val result = if (state.isEditing) {
                updateCommission(commission)
            } else {
                createCommission(commission)
            }

            result
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errors = mapOf("general" to (error.message ?: "Error desconocido"))
                    )
                }
        }
    }
}