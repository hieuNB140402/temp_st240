package com.meskiep.vaithat.data.local.data_character

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DataCharacterDAO {
    // Inset
    @Insert
    suspend fun insertDataCharacter(dataCharacter: DataCharacter)

    @Insert
    suspend fun insertDataCharacterList(list: List<DataCharacter>)

    // Get
    @Query("SELECT * FROM data_character")
    suspend fun getAllDataCharacter(): List<DataCharacter>

    @Query("SELECT * FROM data_character WHERE id = :id")
    suspend fun selectDataCharacterById(id: Int): DataCharacter

    @Query("SELECT * FROM data_character WHERE dataName = :dataName")
    suspend fun selectDataCharacterByDataName(dataName: String): DataCharacter

    // Update
    @Update
    suspend fun updateDataCharacter(dataCharacter: DataCharacter)

    // Delete
    @Query("DELETE FROM data_character")
    fun deleteAllDataCharacter()
}