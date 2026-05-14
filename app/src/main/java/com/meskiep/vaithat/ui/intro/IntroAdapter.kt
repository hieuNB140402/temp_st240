package com.meskiep.vaithat.ui.intro

import android.content.Context
import com.meskiep.vaithat.core.base.BaseAdapter
import com.meskiep.vaithat.core.extension.loadImage
import com.meskiep.vaithat.core.extension.select
import com.meskiep.vaithat.core.extension.strings
import com.meskiep.vaithat.core.helper.LanguageHelper
import com.meskiep.vaithat.data.model.IntroModel
import com.meskiep.vaithat.databinding.ItemIntroBinding
import kotlin.apply

class IntroAdapter(val context: Context) : BaseAdapter<IntroModel, ItemIntroBinding>(ItemIntroBinding::inflate) {
    override fun onBind(binding: ItemIntroBinding, item: IntroModel, position: Int) {
        binding.apply {
            LanguageHelper.setLocale(context)
            loadImage(item.image, imvImage, false)
            tvTitle.text = context.strings(item.content)
            tvTitle.select()
        }
    }
}