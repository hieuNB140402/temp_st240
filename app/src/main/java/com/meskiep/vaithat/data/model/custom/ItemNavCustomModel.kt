package com.meskiep.vaithat.data.model.custom

data class ItemNavCustomModel(
    val pathThumb: String,
    val pathNoColor: String,
    val positionCustom: Int,
    val positionNavigation: Int,
    var isSelected: Boolean = false,
    val listImageColor: ArrayList<ItemColorImageModel> = arrayListOf()
)