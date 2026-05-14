package com.meskiep.vaithat.data.local.data_character

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_character")
data class DataCharacter(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val dataName: String,
    val avatarPath: String,
    val level: Int,
    val fileNameInternal: String,
)