package com.meskiep.vaithat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.meskiep.vaithat.data.local.data_character.DataCharacter
import com.meskiep.vaithat.data.local.data_character.DataCharacterDAO

@Database(entities = [DataCharacter::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase(){
//    abstract fun clothesSavedDao(): ClothesSavedDAO
    abstract fun dataCharacterDao(): DataCharacterDAO

    companion object {

//        val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//
//                database.execSQL("DROP TABLE IF EXISTS finger")
//
//                database.execSQL(
//                    """
//            CREATE TABLE finger (
//                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
//                category TEXT NOT NULL,
//                path TEXT NOT NULL,
//                thumbnail TEXT NOT NULL,
//                type TEXT NOT NULL,
//                isFavorite INTEGER NOT NULL DEFAULT 0,
//                isUnlock INTEGER NOT NULL DEFAULT 0,
//                level INTEGER NOT NULL DEFAULT 0
//            )
//            """.trimIndent()
//                )
//            }
//        }
    }
}