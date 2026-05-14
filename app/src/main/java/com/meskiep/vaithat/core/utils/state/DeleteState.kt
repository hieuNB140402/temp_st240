package com.meskiep.vaithat.core.utils.state

sealed class DeleteState {
    object Empty : DeleteState()
    object Success : DeleteState()
    data class Failure(val error: String?) : DeleteState()
}