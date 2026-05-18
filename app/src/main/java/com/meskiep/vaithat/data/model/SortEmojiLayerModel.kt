package com.meskiep.vaithat.data.model

import androidx.room.PrimaryKey
import com.meskiep.vaithat.data.model.draw.DrawableDraw

data class SortEmojiLayerModel(
    @PrimaryKey(autoGenerate = true)
    val id : Int= 0,
    val drawableDraw: DrawableDraw,
    val isVisible: Boolean = true,
    val isLock: Boolean = false,
)