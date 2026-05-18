package com.meskiep.vaithat.ui.emoji_maker.viewModel

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModel
import com.meskiep.vaithat.data.model.draw.DrawableDraw
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date

class EmojiMakerViewModel : ViewModel() {
    // Flow Declaration
    //==================================================================================================================
    private val _bitmapBush = MutableStateFlow<Bitmap?>(null)
    val bitmapBush = _bitmapBush.asStateFlow()

    // Normal Declaration
    //==================================================================================================================


    // Getter Setter
    //==================================================================================================================
    fun setBitmapBush(bitmap: Bitmap) {
        _bitmapBush.value = bitmap
    }

    // Function feature
    //==================================================================================================================
    fun loadDrawableEmoji(context: Context, bitmap: Bitmap): DrawableDraw {
        val drawable = bitmap.toDrawable(context.resources)
        val drawableEmoji = DrawableDraw(drawable, "${SimpleDateFormat("dd_MM_yyyy_hh_mm_ss").format(Date())}.png")
        return drawableEmoji
    }

//    fun updateDrawList(drawList: List<DrawableDraw>) : List<SortEmojiLayerModel>{
//        val sortEmojiLayerModelList = drawList.map {
//            SortEmojiLayerModel(
//                drawableDraw = it,
//                isLock = it.isLock,
//                isVisible = !it.isHide
//            )
//        }
//        return sortEmojiLayerModelList
//    }
}