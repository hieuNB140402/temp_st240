package com.meskiep.vaithat.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meskiep.vaithat.core.utils.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    protected fun <T> createStateFlow(): MutableStateFlow<UiState<T>> {
        return MutableStateFlow(UiState.Idle)
    }

    protected fun <T> launchRequest(state: MutableStateFlow<UiState<T>>, block: suspend () -> T) {
        viewModelScope.launch {

            state.value = UiState.Loading

            try {
                val result = block()
                state.value = UiState.Success(result)

            } catch (e: Exception) {
                state.value = UiState.Error(e.message)
            }
        }
    }
}