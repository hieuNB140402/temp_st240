package com.meskiep.vaithat.ui.choose_avatar

import android.content.Context
import com.meskiep.vaithat.core.base.BaseAdapter
import com.meskiep.vaithat.core.extension.loadImage
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.data.local.data_character.DataCharacter
import com.meskiep.vaithat.data.model.custom.CustomizeModel
import com.meskiep.vaithat.databinding.ItemChooseAvatarBinding

class ChooseAvatarAdapter(val context: Context) :
    BaseAdapter<DataCharacter, ItemChooseAvatarBinding>(ItemChooseAvatarBinding::inflate) {
    var onItemClick: ((item: DataCharacter, position: Int) -> Unit) = { _, _ -> }

    override fun onBind(binding: ItemChooseAvatarBinding, item: DataCharacter, position: Int) {
        binding.apply {
            loadImage(item.avatarPath, imvImage)
            imvImage.tap { onItemClick.invoke(item, position) }
        }
    }
}