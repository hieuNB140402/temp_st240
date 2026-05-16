package com.meskiep.vaithat.ui.view

import android.content.Context
import androidx.lifecycle.ViewModel
import com.meskiep.vaithat.core.helper.MediaHelper
import com.meskiep.vaithat.core.utils.key.ValueKey
import com.meskiep.vaithat.data.app.DataRepository
import com.meskiep.vaithat.data.model.custom.SuggestionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.collections.toCollection

@HiltViewModel
class ViewViewModel @Inject constructor(val dataRepository: DataRepository) : ViewModel() {

    // Flow Declaration
    //==================================================================================================================
    private val _typeView = MutableStateFlow<Int>(-1)
    val typeView = _typeView.asStateFlow()

    private val _imagePath = MutableStateFlow<String>("")
    val imagePath = _imagePath.asStateFlow()

    // Normal Declaration
    //==================================================================================================================
    var statusView = ValueKey.EDIT_CREATION


    // Getter Setter
    //==================================================================================================================
    fun setTypeView(type: Int) {
        _typeView.value = type
    }

    fun updateStatusView(type: Int) {
        statusView = type
    }

    fun setImagePath(path: String) {
        _imagePath.value = path
    }

    // Function feature
    //==================================================================================================================
    suspend fun deleteItem() {
        if (statusView == ValueKey.EDIT_CREATION) {
            deleteEditCharacterByFileNameInternal(_imagePath.value)

        } else {
            MediaHelper.deleteFileByPathNotFlow(arrayListOf(_imagePath.value))
        }
    }

    // Room
    suspend fun deleteEditCharacterByFileNameInternal(fileNameInternal: String) {
        dataRepository.deleteEditCharacterByFileNameInternal(fileNameInternal)
    }
}