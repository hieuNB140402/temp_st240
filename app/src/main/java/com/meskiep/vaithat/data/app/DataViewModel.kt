package com.meskiep.vaithat.data.app

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meskiep.vaithat.core.extension.dLog
import com.meskiep.vaithat.core.extension.eLog
import com.meskiep.vaithat.core.helper.InternetHelper
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
import kotlin.collections.sortedBy
import kotlin.collections.toCollection

@HiltViewModel
class DataViewModel @Inject constructor(val dataRepository: DataRepository) : ViewModel() {
    // Flow Declaration
    //==================================================================================================================
    private val _allData = MutableStateFlow<ArrayList<DataCharacter>>(arrayListOf())
    val allData: StateFlow<ArrayList<DataCharacter>> = _allData.asStateFlow()


    // Normal Declaration
    //==================================================================================================================

    // Getter Setter
    //==================================================================================================================

    // Function feature
    //==================================================================================================================
    fun ensureData(context: Context) {
        if (_allData.value.isEmpty()) {
            saveAndReadData(context)
        }
    }

    fun saveAndReadData(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val timeStart = System.currentTimeMillis()

            val getDataRoom = dataRepository.getAllDataCharacter()

            if (getDataRoom.isEmpty()) {
                if (InternetHelper.isInternetAvailable(context)) {
                    regetData(context).collect { state ->
                        when (state) {
                            CallApiState.Loading -> {}
                            is CallApiState.Error -> eLog("getAllParts: ${state.e}")
                            is CallApiState.Success -> attachData(state.models)
                        }
                    }
                }
            } else {
                attachData(getDataRoom)
            }

            val timeEnd = System.currentTimeMillis()
            dLog("time load data saveAndReadData(): ${timeEnd - timeStart}")
        }
    }

    fun attachData(getDataRoom: List<DataCharacter>) {
        getDataRoom.sortedBy { it.level }
        _allData.value = getDataRoom.toCollection(ArrayList())
    }

    suspend fun regetData(context: Context): Flow<CallApiState<DataCharacter>> = flow {
        deleteAllDataCharacter()

        dataRepository.getAllParts(context).collect { state ->
            emit(state)
        }
    }

    // Room
    suspend fun deleteAllDataCharacter() {
        dataRepository.deleteAllDataCharacter()
    }

}