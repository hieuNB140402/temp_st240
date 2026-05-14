package com.meskiep.vaithat.ui.home

import android.content.Context
import com.meskiep.vaithat.core.base.BaseAdapter
import com.meskiep.vaithat.core.extension.loadImage
import com.meskiep.vaithat.core.extension.select
import com.meskiep.vaithat.core.extension.strings
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.core.helper.LanguageHelper
import com.meskiep.vaithat.data.model.HomeFeatureModel
import com.meskiep.vaithat.databinding.ItemHomeFeatureBinding
import kotlin.apply

class HomeFeatureAdapter(val context: Context) : BaseAdapter<HomeFeatureModel, ItemHomeFeatureBinding>(ItemHomeFeatureBinding::inflate) {
    var onItemClick: ((Int) -> Unit) = {}

    override fun onBind(binding: ItemHomeFeatureBinding, item: HomeFeatureModel, position: Int) {
        binding.apply {
            LanguageHelper.setLocale(context)

            loadImage(item.thumbImage, imvThumbImage)

            lnlThumbBg.setBackgroundResource(item.thumbBackground)

            tvThumbText.text = context.strings(item.thumbText)
            tvThumbText.select()

            root.tap { onItemClick(item.feature) }
        }
    }
}