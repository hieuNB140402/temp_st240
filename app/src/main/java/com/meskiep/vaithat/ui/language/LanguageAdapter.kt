package com.meskiep.vaithat.ui.language

import android.content.Context
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseAdapter
import com.meskiep.vaithat.core.extension.loadImage
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.core.helper.UnitHelper
import com.meskiep.vaithat.data.model.LanguageModel
import com.meskiep.vaithat.databinding.ItemLanguageBinding
import kotlin.apply

class LanguageAdapter(val context: Context) : BaseAdapter<LanguageModel, ItemLanguageBinding>(ItemLanguageBinding::inflate) {
    var onItemClick: ((String) -> Unit) = {}
    override fun onBind(binding: ItemLanguageBinding, item: LanguageModel, position: Int) {
        binding.apply {
            loadImage(item.flag, imvFlag, false)

            val (solidColor, strokeColor, res) = if (item.activate) {
                Triple(R.color.white, R.color.green_003B50, R.drawable.bg_focus_long)
            } else {
                Triple(R.color.green_003B50, R.color.white, R.drawable.bg_unfocus_long)
            }

            tvLang.apply {
                text = item.name
                setTextColor(context.getColor(solidColor))
                setStroke(UnitHelper.pxToDpFloat(context, 2f), context.getColor(strokeColor))
            }

            flMain.setBackgroundResource(res)

            val ratio = if (item.activate) R.drawable.ic_lang_radio_selected else R.drawable.ic_lang_radio_unselect
            loadImage(ratio, btnRadio, false)

            root.tap { onItemClick.invoke(item.code) }
        }
    }
}