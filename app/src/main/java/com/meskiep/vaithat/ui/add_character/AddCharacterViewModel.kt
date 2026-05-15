package com.meskiep.vaithat.ui.add_character

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModel
import com.meskiep.vaithat.core.helper.AssetHelper
import com.meskiep.vaithat.core.helper.BitmapHelper
import com.meskiep.vaithat.core.helper.MediaHelper
import com.meskiep.vaithat.core.utils.DataLocal
import com.meskiep.vaithat.core.utils.key.AssetsKey
import com.meskiep.vaithat.core.utils.key.ValueKey
import com.meskiep.vaithat.core.utils.state.SaveState
import com.meskiep.vaithat.data.model.SelectedModel
import com.meskiep.vaithat.data.model.draw.Draw
import com.meskiep.vaithat.data.model.draw.DrawableDraw
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.collections.forEachIndexed
import kotlin.collections.map

class AddCharacterViewModel : ViewModel() {
    // Flow Declaration
    //==================================================================================================================
    private val _typeNavigation = MutableStateFlow<Int>(-1)
    val typeNavigation = _typeNavigation.asStateFlow()

    private val _typeBackground = MutableStateFlow<Int>(-1)
    val typeBackground = _typeBackground.asStateFlow()

    private val _isFocusEditText = MutableStateFlow<Boolean>(false)
    val isFocusEditText = _isFocusEditText.asStateFlow()

    // Normal Declaration
    //==================================================================================================================
    var backgroundImageList: List<SelectedModel> = listOf()
    var backgroundColorList: List<SelectedModel> = listOf()
    var stickerList: List<String> = listOf()
    var speechList: List<String> = listOf()
    var textFontList: List<SelectedModel> = listOf()
    var textColorList: List<SelectedModel> = listOf()

    var currentDraw: Draw? = null

    var drawViewList: ArrayList<Draw> = arrayListOf()

    lateinit var layoutParams: ViewGroup.MarginLayoutParams

    var originalMarginBottom: Int = 0

    var pathDefault = ""

    // Getter Setter
    //==================================================================================================================
    fun setTypeNavigation(type: Int) {
        _typeNavigation.value = type
    }

    fun setTypeBackground(type: Int) {
        _typeBackground.value = type
    }

    fun setIsFocusEditText(status: Boolean) {
        _isFocusEditText.value = status
    }

    // Function feature
    //==================================================================================================================

    suspend fun loadDataDefault(context: Context) {
        backgroundImageList = AssetHelper.getSubfoldersAsset(context, AssetsKey.BACKGROUND_ASSET).map { SelectedModel(path = it) }
        backgroundColorList = DataLocal.getBackgroundColorDefault(context)

        stickerList = AssetHelper.getSubfoldersAsset(context, AssetsKey.STICKER_ASSET)
        speechList = AssetHelper.getSubfoldersAsset(context, AssetsKey.SPEECH_ASSET)

        textFontList = DataLocal.getTextFontDefault()
        textColorList = DataLocal.getTextColorDefault(context)
    }

    suspend fun updateBackgroundImageSelected(position: Int) {
        backgroundColorList = backgroundColorList.map { it.copy(isSelected = false) }

        backgroundImageList = backgroundImageList.mapIndexed { index, model ->
            model.copy(isSelected = index == position)
        }
    }

    suspend fun updateBackgroundColorSelected(position: Int) {
        backgroundImageList = backgroundImageList.map { it.copy(isSelected = false) }

        backgroundColorList = backgroundColorList.mapIndexed { index, model ->
            model.copy(isSelected = index == position)
        }
    }

    fun updateTextFontSelected(position: Int) {
        textFontList = textFontList.mapIndexed { index, model -> model.copy(isSelected = position == index) }
    }

    fun updateTextColorSelected(position: Int) {
        textColorList = textColorList.mapIndexed { index, model -> model.copy(isSelected = position == index) }
    }

    fun updateCurrentCurrentDraw(draw: Draw) {
        currentDraw = draw
    }

    fun addDrawView(draw: Draw) {
        drawViewList.add(draw)
    }

    fun deleteDrawView(draw: Draw) {
        drawViewList.removeIf { it == draw }
    }

    fun updatePathDefault(path: String) {
        pathDefault = path
    }

    fun loadDrawableEmoji(context: Context, bitmap: Bitmap, isCharacter: Boolean = false, isText: Boolean = false): DrawableDraw {
        val drawable = bitmap.toDrawable(context.resources)
        val drawableEmoji = DrawableDraw(drawable, "${SimpleDateFormat("dd_MM_yyyy_hh_mm_ss").format(Date())}.png")
        drawableEmoji.isCharacter = isCharacter
        drawableEmoji.isText = isText
        return drawableEmoji
    }

    fun resetDraw() {
        drawViewList.clear()
    }

    fun saveImageFromView(context: Context, view: View): Flow<SaveState> = flow {
        emit(SaveState.Loading)
        val bitmap = BitmapHelper.createBimapFromView(view)
        MediaHelper.saveBitmapToInternalStorage(context, ValueKey.DOWNLOAD_ALBUM, bitmap).collect { state ->
            emit(state)
        }
    }.flowOn(Dispatchers.IO)

    fun getTextColorDefault(): Int {
        return textColorList[1].color
    }

    fun getTextFontDefault(): Int {
        return textFontList.first().color
    }
}