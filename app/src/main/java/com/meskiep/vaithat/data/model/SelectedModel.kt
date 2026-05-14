package com.meskiep.vaithat.data.model

import androidx.room.PrimaryKey
import javax.annotation.processing.Generated

data class SelectedModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val path: String = "",
    val color: Int = -1,
    var isSelected: Boolean = false
)