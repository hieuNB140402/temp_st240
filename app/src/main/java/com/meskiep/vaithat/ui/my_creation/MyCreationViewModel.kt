package com.meskiep.vaithat.ui.my_creation

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.meskiep.vaithat.core.helper.MediaHelper
import com.meskiep.vaithat.core.helper.StringHelper
import com.meskiep.vaithat.core.share.telegram.TelegramSharing
import com.meskiep.vaithat.core.share.whatsapp.IdGenerator
import com.meskiep.vaithat.core.share.whatsapp.StickerBook
import com.meskiep.vaithat.core.share.whatsapp.StickerPack
import com.meskiep.vaithat.core.utils.key.ValueKey
import com.meskiep.vaithat.core.utils.state.DeleteState
import com.meskiep.vaithat.core.utils.state.HandleState
import com.meskiep.vaithat.core.utils.state.ShareState
import com.meskiep.vaithat.data.app.DataRepository
import com.meskiep.vaithat.data.local.edit.EditCharacter
import com.meskiep.vaithat.data.model.MyCreationModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import java.io.File
import kotlin.collections.any
import kotlin.collections.filter
import kotlin.collections.map
import kotlin.collections.mapIndexed
import kotlin.collections.reversed

@HiltViewModel
class MyCreationViewModel @Inject constructor(val dataRepository: DataRepository) : ViewModel() {

    // Flow Declaration
    //==================================================================================================================
    private val _typeSelected = MutableStateFlow<Int>(-1)
    val typeSelected = _typeSelected.asStateFlow()

    private val _isShowSelection = MutableSharedFlow<Boolean>()
    val isShowSelection = _isShowSelection.asSharedFlow()

    private val _isLastItem = MutableSharedFlow<Boolean>()
    val isLastItem = _isLastItem.asSharedFlow()

    private val _editList = MutableStateFlow<List<MyCreationModel>>(listOf())
    val editList = _editList.asStateFlow()

    private val _viewList = MutableStateFlow<List<MyCreationModel>>(listOf())
    val viewList = _viewList.asStateFlow()

    private val _downloadState = MutableSharedFlow<HandleState>()
    val downloadState: SharedFlow<HandleState> = _downloadState

    // Getter Setter
    //==================================================================================================================

    fun setTypeStatus(type: Int) {
        if (type == _typeSelected.value) return
        _typeSelected.value = type
    }

    fun setSelectionState(state: Boolean) {
        viewModelScope.launch {
            _isShowSelection.emit(state)
        }
    }

    fun setIsLastItem(isLastItem: Boolean) {
        viewModelScope.launch {
            _isLastItem.emit(isLastItem)
        }
    }

    fun isEditState(): Boolean {
        return _typeSelected.value == ValueKey.EDIT_CREATION
    }

    fun editListIsEmpty(): Boolean {
        return _editList.value.isEmpty()
    }

    fun viewListIsEmpty(): Boolean {
        return _viewList.value.isEmpty()
    }

    // Load Data
    //==================================================================================================================

    suspend fun getEditCreation() {
        val editCharacterList = getAllDataCharacterDesc()
        _editList.value = editCharacterList.map { editCharacterModel ->
            MyCreationModel(thumbPath = editCharacterModel.thumbPath)
        }
    }

    suspend fun getViewCreation(context: Context) {
        val viewList =
            MediaHelper.getImageInternal(context, ValueKey.DOWNLOAD_ALBUM).map { MyCreationModel(thumbPath = it) }
        viewList.reversed()
        _viewList.value = viewList
    }


    fun resetGetMyCreation(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isEditState()) {
                getEditCreation()
            } else {
                getViewCreation(context)
            }

            setSelectionState(false)
            setIsLastItem(false)
        }
    }

    // Selection
    //==================================================================================================================

    fun touchSelectMyCreation(indexTouch: Int) {
        if (isEditState()) {
            _editList.value = _editList.value.mapIndexed { index, model ->
                model.copy(isSelected = if (index == indexTouch) !model.isSelected else model.isSelected)
            }
        } else {
            _viewList.value = _viewList.value.mapIndexed { index, model ->
                model.copy(isSelected = if (index == indexTouch) !model.isSelected else model.isSelected)
            }
        }

        checkLastItem()
    }

    fun showSelectMyCreation(indexTouch: Int) {
        if (isEditState()) {
            _editList.value = _editList.value.mapIndexed { index, model ->
                model.copy(
                    isSelected = index == indexTouch,
                    isShowSelection = true
                )
            }
        } else {
            _viewList.value = _viewList.value.mapIndexed { index, model ->
                model.copy(
                    isSelected = index == indexTouch,
                    isShowSelection = true
                )
            }
        }
        setSelectionState(true)
        checkLastItem()
    }

    private fun checkLastItem() {
        setIsLastItem(
            if (isEditState()) {
                !_editList.value.any { !it.isSelected }
            } else {
                !_viewList.value.any { !it.isSelected }
            }
        )
    }

    suspend fun getItemSelected(isShareAnotherApp: Boolean = false): List<String> {

        var list = if (isEditState()) {
            _editList.value
                .filter { it.isSelected }
                .map { it.thumbPath }
        } else {
            _viewList.value
                .filter { it.isSelected }
                .map { it.thumbPath }
        }

        if (list.isEmpty() && isEditState() && isShareAnotherApp) {
            list = _editList.value.map { it.thumbPath }
        }

        return list
    }

    suspend fun getItemSelectedState(): ShareState {
        val thumbPathList = getItemSelected()

        return if (thumbPathList.isNotEmpty()) {
            ShareState.Success(thumbPathList)
        } else {
            ShareState.Empty
        }
    }

    fun handleSelectAll() {
        val isSelectedAll = if (isEditState()) {
            !_editList.value.any { !it.isSelected }
        } else {
            !_viewList.value.any { !it.isSelected }
        }

        if (isEditState()) {
            _editList.value = _editList.value.map { it.copy(isSelected = !isSelectedAll) }
        } else {
            _viewList.value = _viewList.value.map { it.copy(isSelected = !isSelectedAll) }
        }

        checkLastItem()
    }

    // Delete
    //==================================================================================================================

    suspend fun deleteMyCreation(context: Context, thumbPath: String): DeleteState {
        val listSelected = if (thumbPath == "") {
            getItemSelected()
        } else {
            listOf(thumbPath)
        }

        if (listSelected.isEmpty()) return DeleteState.Empty

        return try {
            if (isEditState()) {
                deleteEditCharacterByThumbPathInternals(listSelected)
            }

            MediaHelper.deleteFileByPathNotFlow(listSelected)
            getViewCreation(context)

            DeleteState.Success

        } catch (e: Exception) {
            DeleteState.Failure(e.message)
        }

    }

    // Share
    //==================================================================================================================

    fun addToTelegram(context: Context, thumbPathList: List<String>) {
        val uriList = getAllUrisFromList(context, thumbPathList)
        TelegramSharing.importToTelegram(context, uriList)
    }

    fun addToWhatsapp(context: Activity, packageName: String, list: List<String>, onResult: (StickerPack?) -> Unit) {
        val uriList = getAllUrisFromList(context, list)
        val packId = IdGenerator.generateIdFromUrl(context, StringHelper.generateRandomString(10))
        val stickerPack = StickerPack(packId, packageName, uriList, context)
        StickerBook.addPackIfNotAlreadyAdded(stickerPack)
        onResult(stickerPack)
    }

    fun getAllUrisFromList(context: Context, shareList: List<String>): List<Uri> {
        val contentUriList = ArrayList<Uri>()

        val listPath = arrayListOf<String>()

        shareList.forEach {
            listPath.add(it)
        }

        listPath.forEach { path ->
            val file = File(path)
            if (file.exists()) {
                val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                contentUriList.add(contentUri)
            }
        }

        return contentUriList
    }

    // Room
    //==================================================================================================================
    fun downloadThumbPathToExternal(context: Context, thumbPathList: List<String> = emptyList()) {
        viewModelScope.launch(Dispatchers.IO) {
            _downloadState.emitAll(MediaHelper.downloadPartsToExternal(context, thumbPathList.ifEmpty { getItemSelected() }))
        }
    }

    // Room
    //==================================================================================================================

    suspend fun getAllDataCharacterDesc(): List<EditCharacter> {
        return dataRepository.getAllDataCharacterDesc()
    }

    suspend fun deleteEditCharacterByFileNameInternals(fileNameInternals: List<String>) {
        dataRepository.deleteEditCharacterByFileNameInternals(fileNameInternals)
    }

    suspend fun deleteEditCharacterByThumbPathInternals(thumbPathInternals: List<String>) {
        val fileInternal = dataRepository.selectEditCharacterByThumbPaths(thumbPathInternals)

        MediaHelper.deleteFileByPathNotFlow(fileInternal.map { it.fileNameInternal })
        dataRepository.deleteEditCharacterByThumbPathInternals(thumbPathInternals)
    }

    suspend fun getDataNameAndFileNameInternalByThumbPath(thumbPath: String): Pair<String, String> {
        val editModel = dataRepository.selectEditCharacterByThumbPath(thumbPath)
        return editModel.dataName to editModel.fileNameInternal
    }

}
