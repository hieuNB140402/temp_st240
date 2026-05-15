package com.meskiep.vaithat.ui.add_character.adapter

import com.meskiep.vaithat.core.base.BaseAdapter
import com.meskiep.vaithat.core.extension.loadImage
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.databinding.ItemAddCharacterSpeechBinding

class AddCharacterSpeechAdapter : BaseAdapter<String, ItemAddCharacterSpeechBinding>(ItemAddCharacterSpeechBinding::inflate) {
    var onItemClick: ((String) -> Unit) = {}

    override fun onBind(binding: ItemAddCharacterSpeechBinding, item: String, position: Int) {
        binding.apply {
            loadImage(item, imvImage, false)
            root.tap { onItemClick.invoke(item) }
        }
    }
}