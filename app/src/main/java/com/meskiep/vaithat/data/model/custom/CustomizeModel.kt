package com.meskiep.vaithat.data.model.custom

data class CustomizeModel(
    val dataName: String = "",
    val avatar: String = "",
    val level: Int = 100,
    val layerList: ArrayList<LayerListModel> = arrayListOf()
)
