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
    suspend fun insertEditCharacterList(list: List<EditCharacter>)

    // Get
    @Query("SELECT * FROM edit_character")
    suspend fun getAllEditCharacter(): List<EditCharacter>

    @Query("SELECT * FROM edit_character ORDER BY id DESC")
    suspend fun getAllEditCharacterDesc(): List<EditCharacter>

    @Query("SELECT * FROM edit_character WHERE id = :id")
    suspend fun selectEditCharacterById(id: Int): EditCharacter

    @Query("SELECT * FROM edit_character WHERE thumbPath IN (:thumbPathList)")
    suspend fun selectEditCharacterByThumbPaths(thumbPathList: List<String>): List<EditCharacter>

    @Query("SELECT * FROM edit_character WHERE thumbPath = :thumbPath")
    suspend fun selectEditCharacterByThumbPath(thumbPath: String): EditCharacter

    @Query("SELECT * FROM edit_character WHERE fileNameInternal = :fileNameInternal")
    suspend fun selectEditCharacterByFileNameInternal(fileNameInternal: String): EditCharacter
    
    @Query("SELECT * FROM edit_character WHERE dataName = :dataName")
    suspend fun selectEditCharacterByDataName(dataName: String): EditCharacter
    
    // Update
    @Update
    suspend fun updateEditCharacter(editCharacter: EditCharacter)

    // Delete
    @Query("DELETE FROM edit_character")
    fun deleteAllEditCharacter()

    @Query("DELETE FROM edit_character WHERE fileNameInternal = :fileNameInternal")
    fun deleteEditCharacterByFileNameInternal(fileNameInternal: String)

    @Query("DELETE FROM edit_character WHERE fileNameInternal IN (:fileNameInternals)")
    suspend fun deleteEditCharacterByFileNameInternals(fileNameInternals: List<String>)

    @Query("DELETE FROM edit_character WHERE thumbPath IN (:thumbPathInternals)")
    suspend fun deleteEditCharacterByThumbPathInternals(thumbPathInternals: List<String>)
}