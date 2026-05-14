package com.meskiep.vaithat.dialog.text

import android.annotation.SuppressLint
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseAdapter
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.data.model.SelectedModel
import com.meskiep.vaithat.databinding.ItemDialogTextColorBinding

class TextColorDialogAdapter : BaseAdapter<SelectedModel, ItemDialogTextColorBinding>(ItemDialogTextColorBinding::inflate) {
    var onChooseColorClick: (() -> Unit) = {}
    var onTextColorClick: ((Int, Int) -> Unit) = { _, _ -> }

    private var currentSelected = 1


    override fun onBind(binding: ItemDialogTextColorBinding, item: SelectedModel, position: Int) {
        binding.apply {
            val res = if (item.isSelected) R.drawable.bg_1000_stroke_green_00445c else R.drawable.bg_1000_stroke_green_00838f
            vFocus.setBackgroundResource(res)

            if (position == 0) {
                Glide.with(imvColor.context).load(R.drawable.ic_dialog_add_color).into(imvColor)
                root.tap { onChooseColorClick.invoke() }
            } else {
                imvColor.setBackgroundColor(item.color)
                Glide.with(imvColor.context).clear(imvColor)
                root.tap { onTextColorClick.invoke(item.color, position) }
            }
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
        currentSelected = 1
        notifyDataSetChanged()
    }
}