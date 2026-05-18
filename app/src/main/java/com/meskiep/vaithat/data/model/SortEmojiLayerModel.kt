package com.meskiep.vaithat.data.model

import android.graphics.drawable.Drawable
import androidx.room.PrimaryKey
import com.meskiep.vaithat.data.model.draw.DrawableDraw

data class SortEmojiLayerModel (
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val drawable: DrawableDraw
)