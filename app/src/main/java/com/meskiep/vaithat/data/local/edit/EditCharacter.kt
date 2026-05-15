package com.meskiep.vaithat.data.local.edit

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "edit_character")
data class EditCharacter(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val dataName: String,
    val thumbPath: String,
    val fileNameInternal: String,
)
