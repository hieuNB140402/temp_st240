package com.meskiep.vaithat.ui.add_character.adapter

import com.meskiep.vaithat.core.base.BaseAdapter
import com.meskiep.vaithat.core.extension.loadImage
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.databinding.ItemAddCharacterStickerBinding

class AddCharacterStickerAdapter : BaseAdapter<String, ItemAddCharacterStickerBinding>(ItemAddCharacterStickerBinding::inflate) {
    var onItemClick: ((String) -> Unit) = {}

    override fun onBind(binding: ItemAddCharacterStickerBinding, item: String, position: Int) {
        binding.apply {
            loadImage(item, imvImage, false)
            root.tap { onItemClick.invoke(item) }
        }
    }
}