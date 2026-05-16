package com.meskiep.vaithat.data.app

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meskiep.vaithat.core.extension.dLog
import com.meskiep.vaithat.core.extension.eLog
import com.meskiep.vaithat.core.helper.InternetHelper
import com.meskiep.vaithat.core.utils.key.ValueKey
import com.meskiep.vaithat.core.utils.state.CallApiState
import com.meskiep.vaithat.data.local.data_character.DataCharacter
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.collections.sortedBy
import kotlin.collections.toCollection

@HiltViewModel
class DataViewModel @Inject constructor(val dataRepository: DataRepository) : ViewModel() {
    // Flow Declaration
    //==================================================================================================================
    private val _isDataCallSuccess = MutableStateFlow(false)
    val isDataCallSuccess = _isDataCallSuccess.asStateFlow()


    // Normal Declaration
    //==================================================================================================================

    // Getter Setter
    //==================================================================================================================
    private fun setIsDataCallSuccess(status: Boolean) {
        _isDataCallSuccess.value = status
    }

    // Function feature
    //==================================================================================================================
    fun ensureData(context: Context) {
        if (!_isDataCallSuccess.value) {
            saveAndReadData(context)
        }
    }

    fun saveAndReadData(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val timeStart = System.currentTimeMillis()

            val getDataRoom = getAllDataCharacter()

            if (getDataRoom.isEmpty()) {
                if (InternetHelper.isInternetAvailable(context)) {
                    regetData(context).collect { state ->
                        when (state) {
                            CallApiState.Loading -> {}
                            is CallApiState.Error -> eLog("getAllParts: ${state.e}")
                            is CallApiState.Success -> setIsDataCallSuccess(true)
                        }
                    }
                } else {
                    setIsDataCallSuccess(false)
                }
            } else {
                setIsDataCallSuccess(true)
            }

            val timeEnd = System.currentTimeMillis()
            dLog("time load data saveAndReadData(): ${timeEnd - timeStart}")
        }
    }

    suspend fun regetData(context: Context): Flow<CallApiState<DataCharacter>> = flow {
        deleteAllDataCharacterRoom()
        deleteAllFileInternal(context, ValueKey.DATA_CHARACTER_ALBUM)


        dataRepository.getAllParts(context).collect { state ->
            emit(state)
        }
    }

    suspend fun deleteAllFileInternal(context: Context, folder: String) {
        try {
            val folder = File(context.filesDir, folder)

            if (folder.exists()) {
                folder.deleteRecursively()
            } else {
                eLog("Folder does not exist")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            eLog("deleteAllDataCharacterInternal: ${e.message}")
        }
    }

    // Room
    // Data Character
    // ==================================================
    suspend fun deleteAllDataCharacterRoom() {
        dataRepository.deleteAllDataCharacter()
    }

    suspend fun getAllDataCharacter(): List<DataCharacter> {
        return dataRepository.getAllDataCharacter()
    }

    // Edit Character
    // ==================================================
    suspend fun deleteAllEditCharacterRoom(context: Context) {
        dataRepository.deleteAllEditCharacter()
        deleteAllFileInternal(context, ValueKey.CHARACTER_CUSTOMIZE_ALBUM)
    }
}