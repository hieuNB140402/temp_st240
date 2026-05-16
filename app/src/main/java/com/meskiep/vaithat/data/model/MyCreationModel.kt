package com.meskiep.vaithat.data.model

import androidx.room.PrimaryKey

data class MyCreationModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val thumbPath: String,
    val isShowSelection: Boolean = false,
    val isSelected: Boolean = false
)
