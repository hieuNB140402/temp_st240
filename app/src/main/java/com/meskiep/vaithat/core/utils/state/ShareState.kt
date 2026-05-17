package com.meskiep.vaithat.core.utils.state

sealed class ShareState {
    object Empty : ShareState()
    data class Success(val thumbPathList: List<String>) : ShareState()
}