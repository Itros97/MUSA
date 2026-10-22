package org.softwareanvil.musa.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.softwareanvil.musa.demo.FillResult
import org.softwareanvil.musa.demo.FillWithDemoDataUseCase
import org.softwareanvil.musa.domain.usecase.WipeDatabaseUseCase

/** What the development tools card draws. */
data class DevToolsState(val isBusy: Boolean = false, val isConfirmingWipe: Boolean = false)

/** Drives the development tools: fill the database with toy data, or wipe it after confirming. */
class DevToolsViewModel(
	private val fillWithDemoData: FillWithDemoDataUseCase,
	private val wipeDatabase: WipeDatabaseUseCase,
) : ViewModel() {

	private val _state = MutableStateFlow(DevToolsState())
	val state: StateFlow<DevToolsState> = _state.asStateFlow()

	private val _messages = Channel<String>(Channel.BUFFERED)
	val messages: Flow<String> = _messages.receiveAsFlow()

	fun onFillClick() = runBusy {
		when (fillWithDemoData()) {
			FillResult.FILLED -> "Datos de prueba cargados"
			FillResult.NOT_EMPTY -> "Vacía la base de datos antes de rellenarla"
		}
	}

	fun onWipeClick() = _state.update { it.copy(isConfirmingWipe = true) }

	fun onWipeDismiss() = _state.update { it.copy(isConfirmingWipe = false) }

	fun onWipeConfirm() {
		onWipeDismiss()
		runBusy {
			wipeDatabase()
			"Base de datos vacía"
		}
	}

	/** Runs one action at a time; the buttons stay disabled until it ends. */
	private fun runBusy(action: suspend () -> String) {
		if (_state.value.isBusy) return
		_state.update { it.copy(isBusy = true) }
		viewModelScope.launch {
			val message = try {
				action()
			} finally {
				_state.update { it.copy(isBusy = false) }
			}
			_messages.send(message)
		}
	}
}
