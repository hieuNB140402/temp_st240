package com.meskiep.vaithat.core.utils.state

sealed class SaveState {
    data class Success(val path: String) : SaveState()

    //    data class Error(val exception: Exception) : SaveState()
    data class Error(val throwable: Throwable) : SaveState()
    object Loading : SaveState()
    object Nothing : SaveState()
}