package com.meskiep.vaithat.data.model

import androidx.room.PrimaryKey

data class SortEmojiLayerModel(
    @PrimaryKey(autoGenerate = true)
    val id : Int= 0,
    val isVisible: Boolean = true,
    val isLock: Boolean = false,
)