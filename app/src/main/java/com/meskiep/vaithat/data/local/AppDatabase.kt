package com.meskiep.vaithat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.meskiep.vaithat.data.local.data_character.DataCharacter
import com.meskiep.vaithat.data.local.data_character.DataCharacterDAO
import com.meskiep.vaithat.data.local.edit.EditCharacter
import com.meskiep.vaithat.data.local.edit.EditCharacterDAO

@Database(entities = [DataCharacter::class, EditCharacter::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase(){
    abstract fun dataCharacterDao(): DataCharacterDAO
    abstract fun editCharacterDao(): EditCharacterDAO
}