package com.meskiep.vaithat.data.model.custom

data class LayerModel(
    val imagePath: String,
    val thumbPath: String,
    val isMoreColors: Boolean = false,
    var listColor: ArrayList<ColorModel> = arrayListOf()
)