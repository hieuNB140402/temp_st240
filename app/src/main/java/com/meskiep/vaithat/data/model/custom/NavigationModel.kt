package com.meskiep.vaithat.data.model.custom

import androidx.room.PrimaryKey

data class NavigationModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val imageNavigation: String,
    var isSelected: Boolean = false
)

