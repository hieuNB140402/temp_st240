package com.meskiep.vaithat.ui.emoji_maker.viewModel

import androidx.lifecycle.ViewModel
import com.meskiep.vaithat.data.model.SelectedModel
import kotlin.collections.get

class BrushViewModel : ViewModel() {
    // Flow Declaration
    //==================================================================================================================

    // Normal Declaration
    //==================================================================================================================
    var brushSizeDefault = 0.3f
    var brushColorDefault = 0

    var textColorList: List<SelectedModel> = listOf()


    // Getter Setter
    //==================================================================================================================
    fun updateBrushSizeDefault(size: Float) {
        brushSizeDefault = size
    }

    fun updateBrushColorDefault(color: Int) {
        brushColorDefault = color
    }

    fun updateTextColorList(list: List<SelectedModel>) {
        textColorList = list
    }

    // Function feature
    //==================================================================================================================

    fun updateTextColorSelect(position: Int, color: Int): Int {
        textColorList = textColorList.mapIndexed { index, model -> model.copy(isSelected = index == position) }

        val finalColor = if (position != 0) {
            textColorList[position].color
        } else {
            color
        }
        updateBrushColorDefault(finalColor)
        return finalColor
    }
}