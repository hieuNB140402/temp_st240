package com.meskiep.vaithat.dialog.text

import android.annotation.SuppressLint
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseAdapter
import com.meskiep.vaithat.core.extension.setFont
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.data.model.SelectedModel
import com.meskiep.vaithat.databinding.ItemDialogFontBinding

class TextFontDialogAdapter : BaseAdapter<SelectedModel, ItemDialogFontBinding>(ItemDialogFontBinding::inflate) {
    var onTextFontClick: ((Int, Int) -> Unit) = { _, _ -> }
    private var currentSelected = 0

    override fun onBind(binding: ItemDialogFontBinding, item: SelectedModel, position: Int) {
        binding.apply {

            val res = if (item.isSelected) R.drawable.bg_8_solid_yellow else R.drawable.bg_8_solid_white_opacity

            tvFont.apply {
                setBackgroundResource(res)
                text = item.path
                setFont(item.color)
            }

            root.tap { onTextFontClick.invoke(item.color, position) }
        }
    }

    fun submitItem(position: Int, list: ArrayList<SelectedModel>) {
        if (position != currentSelected) {
            items.clear()
            items.addAll(list)

            notifyItemChanged(currentSelected)
            notifyItemChanged(position)

            currentSelected = position
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitListReset(list: ArrayList<SelectedModel>) {
        items.clear()
        items.addAll(list)
        currentSelected = 0
        notifyDataSetChanged()
    }
}