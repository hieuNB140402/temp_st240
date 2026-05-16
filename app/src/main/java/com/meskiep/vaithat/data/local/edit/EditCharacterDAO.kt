package com.meskiep.vaithat.data.local.edit

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface EditCharacterDAO {
    // Inset
    @Insert
    suspend fun insertEditCharacter(editCharacter: EditCharacter)

    @Insert
    suspend fun insertDataCharacterList(list: List<EditCharacter>)

    // Get
    @Query("SELECT * FROM edit_character")
    suspend fun getAllDataCharacter(): List<EditCharacter>

    @Query("SELECT * FROM edit_character ORDER BY id DESC")
    suspend fun getAllDataCharacterDesc(): List<EditCharacter>

    @Query("SELECT * FROM edit_character WHERE id = :id")
    suspend fun selectDataCharacterById(id: Int): EditCharacter

    @Query("SELECT * FROM edit_character WHERE dataName = :dataName")
    suspend fun selectDataCharacterByDataName(dataName: String): EditCharacter

    // Update
    @Update
    suspend fun updateDataCharacter(editCharacter: EditCharacter)

    // Delete
    @Query("DELETE FROM edit_character")
    fun deleteAllEditCharacter()

    @Query("DELETE FROM edit_character WHERE fileNameInternal = :fileNameInternal")
    fun deleteEditCharacterByFileNameInternal(fileNameInternal: String)

    @Query("DELETE FROM edit_character WHERE fileNameInternal IN (:fileNameInternals)")
    suspend fun deleteEditCharacterByFileNameInternals(fileNameInternals: List<String>)
}