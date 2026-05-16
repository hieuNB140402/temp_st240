package com.meskiep.vaithat.ui.add_character.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.extension.setFont
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.data.model.SelectedModel
import com.meskiep.vaithat.databinding.ItemAddCharacterFontBinding

class AddCharacterTextFontAdapter(val context: Context) :
    ListAdapter<SelectedModel, AddCharacterTextFontAdapter.AddCharacterTextFontHolder>(AddCharacterTextFontCallback()) {
    var onTextFontClick: ((Int, Int) -> Unit) = { _, _ -> }

    inner class AddCharacterTextFontHolder(val binding: ItemAddCharacterFontBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SelectedModel) {
            binding.apply {
                val res =
                    if (item.isSelected) R.drawable.bg_1000_stroke_green_003b50_3 else R.drawable.bg_1000_stroke_green_00838f
                vFocus.isVisible = item.isSelected
                vFocusOutSide.setBackgroundResource(res)

                tvFont.setFont(item.color)
                val elevation = if (item.isSelected) 4f else 0f
                cvMain.cardElevation = elevation
                root.tap { onTextFontClick.invoke(item.color, bindingAdapterPosition) }
            }
        }
    }


    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): AddCharacterTextFontHolder {
        return AddCharacterTextFontHolder(ItemAddCharacterFontBinding.inflate(LayoutInflater.from(p0.context), p0, false))
    }

    override fun onBindViewHolder(
        p0: AddCharacterTextFontHolder,
        p1: Int
    ) {
        p0.bind(getItem(p1))
    }


    class AddCharacterTextFontCallback : DiffUtil.ItemCallback<SelectedModel>() {
        override fun areItemsTheSame(p0: SelectedModel, p1: SelectedModel): Boolean {
            return p0.id == p1.id
        }

        override fun areContentsTheSame(p0: SelectedModel, p1: SelectedModel): Boolean {
            return p0 == p1
        }
    }
}