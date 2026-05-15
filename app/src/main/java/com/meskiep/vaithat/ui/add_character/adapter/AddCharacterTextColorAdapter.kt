package com.meskiep.vaithat.ui.add_character.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.extension.loadImage
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.data.model.SelectedModel
import com.meskiep.vaithat.databinding.ItemAddCharacterTextColorBinding

class AddCharacterTextColorAdapter(val context: Context) : ListAdapter<SelectedModel, AddCharacterTextColorAdapter.AddCharacterTextColorHolder>(AddCharacterTextColorCallback()) {
    var onChooseColorClick: (() -> Unit) = {}
    var onTextColorClick: ((Int, Int) -> Unit) = { _, _ -> }

    inner class AddCharacterTextColorHolder(val binding: ItemAddCharacterTextColorBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SelectedModel){
            binding.apply {
                vFocus.isVisible = item.isSelected

                if (bindingAdapterPosition == 0) {
                    loadImage(R.drawable.ic_add_character_add_color_text, imvColor, false)
                    imvColor.setBackgroundColor(context.getColor(R.color.transparent))
                    root.tap { onChooseColorClick.invoke() }
                } else {
                    Glide.with(imvColor.context).clear(imvColor)
                    imvColor.setBackgroundColor(item.color)

                    root.tap { onTextColorClick.invoke(item.color, bindingAdapterPosition) }
                }
            }
        }
    }

    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): AddCharacterTextColorHolder {
        return AddCharacterTextColorHolder(ItemAddCharacterTextColorBinding.inflate(LayoutInflater.from(p0.context), p0, false))
    }

    override fun onBindViewHolder(
        p0: AddCharacterTextColorHolder,
        p1: Int
    ) {
        p0.bind(getItem(p1))
    }

    class AddCharacterTextColorCallback : DiffUtil.ItemCallback<SelectedModel>() {
        override fun areItemsTheSame(p0: SelectedModel, p1: SelectedModel): Boolean {
            return p0.id == p1.id
        }

        override fun areContentsTheSame(p0: SelectedModel, p1: SelectedModel): Boolean {
            return p0 == p1
        }
    }
}