package com.meskiep.vaithat.ui.emoji_maker.viewModel

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModel
import androidx.media3.common.MimeTypes.isText
import com.meskiep.vaithat.data.model.draw.Draw
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

    var currentDraw: Draw? = null

    var drawViewList: ArrayList<Draw> = arrayListOf()

    // Getter Setter
    //==================================================================================================================
    fun setBitmapBush(bitmap: Bitmap) {
        _bitmapBush.value = bitmap
    }

    // Function feature
    //==================================================================================================================
    fun updateCurrentCurrentDraw(draw: Draw) {
        currentDraw = draw
    }

    fun addDrawView(draw: Draw) {
        drawViewList.add(draw)
    }

    fun deleteDrawView(draw: Draw) {
        drawViewList.removeIf { it == draw }
    }


    fun loadDrawableEmoji(context: Context, bitmap: Bitmap): DrawableDraw {
        val drawable = bitmap.toDrawable(context.resources)
        val drawableEmoji = DrawableDraw(drawable, "${SimpleDateFormat("dd_MM_yyyy_hh_mm_ss").format(Date())}.png")
        addDrawView(drawableEmoji)
        return drawableEmoji
    }

    fun resetDraw() {
        drawViewList.clear()
    }
}