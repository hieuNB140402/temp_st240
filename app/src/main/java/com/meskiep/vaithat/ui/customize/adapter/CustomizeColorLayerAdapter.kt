package com.meskiep.vaithat.ui.customize.adapter

import android.content.Context
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import com.meskiep.vaithat.core.base.BaseAdapter
import com.meskiep.vaithat.core.extension.margin
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.core.helper.UnitHelper
import com.meskiep.vaithat.data.model.custom.ItemColorModel
import com.meskiep.vaithat.databinding.ItemCustomizeColorBinding

class CustomizeColorLayerAdapter(val context: Context) :
    BaseAdapter<ItemColorModel, ItemCustomizeColorBinding>(ItemCustomizeColorBinding::inflate) {
    var onItemClick: ((Int) -> Unit) = {}

    override fun onBind(binding: ItemCustomizeColorBinding, item: ItemColorModel, position: Int) {
        binding.apply {
            imvColor.setBackgroundColor(item.color.toColorInt())
            vFocus.isVisible = item.isSelected
            val margin = if (item.isSelected) 2 else 0
            cvColor.margin("", UnitHelper.pxToDpInt(context, margin))
            root.tap { onItemClick.invoke(position) }
        }
    }
}