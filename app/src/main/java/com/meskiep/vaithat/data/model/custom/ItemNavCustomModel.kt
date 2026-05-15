package com.meskiep.vaithat.data.model.custom

import androidx.room.PrimaryKey

data class ItemNavCustomModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val pathThumb: String,
    val pathNoColor: String,
    val positionCustom: Int,
    val positionNavigation: Int,
    var isSelected: Boolean = false,
    val listImageColor: ArrayList<ItemColorImageModel> = arrayListOf()
)