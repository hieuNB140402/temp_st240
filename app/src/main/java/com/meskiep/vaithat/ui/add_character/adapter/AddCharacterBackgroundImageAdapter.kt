package com.meskiep.vaithat.ui.add_character.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.extension.gone
import com.meskiep.vaithat.core.extension.loadImage
import com.meskiep.vaithat.core.extension.select
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.core.extension.visible
import com.meskiep.vaithat.data.model.SelectedModel
import com.meskiep.vaithat.databinding.ItemAddCharacterImageBinding

class AddCharacterBackgroundImageAdapter :
    ListAdapter<SelectedModel, AddCharacterBackgroundImageAdapter.AddCharacterBackgroundImageHolder>(
        AddCharacterBackgroundImageCallback()
    ) {

    var onAddImageClick: (() -> Unit) = {}
    var onBackgroundImageClick: ((String, Int) -> Unit) = {_,_ ->}


    inner class AddCharacterBackgroundImageHolder(val binding: ItemAddCharacterImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SelectedModel) {
            binding.apply {
                if (bindingAdapterPosition == 0) {
                    lnlAddImage.visible()
                    tvAddImage.select()
                    imvImage.gone()

                    vFocus.visible()
                    vFocus.setBackgroundResource(R.drawable.bg_4_stroke_green_00838f_1)

                    lnlAddImage.tap { onAddImageClick.invoke() }
                } else {
                    lnlAddImage.gone()
                    imvImage.visible()
                    loadImage(item.path, imvImage, 256)

                    vFocus.isVisible = item.isSelected
                    vFocus.setBackgroundResource(R.drawable.bg_4_stroke_green_003b50_3)

                    imvImage.tap { onBackgroundImageClick.invoke(item.path,  bindingAdapterPosition) }
                }
            }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): AddCharacterBackgroundImageHolder {
        return AddCharacterBackgroundImageHolder(ItemAddCharacterImageBinding.inflate(LayoutInflater.from(p0.context), p0, false))
    }

    override fun onBindViewHolder(p0: AddCharacterBackgroundImageHolder, p1: Int) {
        p0.bind(getItem(p1))
    }


    class AddCharacterBackgroundImageCallback : DiffUtil.ItemCallback<SelectedModel>() {
        override fun areItemsTheSame(p0: SelectedModel, p1: SelectedModel): Boolean {
            return p0.id == p1.id
        }

        override fun areContentsTheSame(p0: SelectedModel, p1: SelectedModel): Boolean {
            return p0 == p1
        }
    }
}