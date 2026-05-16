package com.meskiep.vaithat.ui.my_creation

sealed class MyCreationPayload {
    data class SelectedChanged(val isSelected: Boolean) : MyCreationPayload()
    data class ShowSelectChanged(val isShowSelection: Boolean) : MyCreationPayload()
}