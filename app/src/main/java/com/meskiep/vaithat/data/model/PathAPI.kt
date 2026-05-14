package com.meskiep.vaithat.data.model

data class PathAPI(
    val position: String,
    val parts: String,
    val colorArray: String,
    val quantity: Int,
    val level : Int
)

data class DataAPI(val name: String, val parts: List<PathAPI>)