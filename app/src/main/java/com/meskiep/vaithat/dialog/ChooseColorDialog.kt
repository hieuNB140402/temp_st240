package com.meskiep.vaithat.dialog

import android.content.Context
import android.graphics.Color
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseDialog
import com.meskiep.vaithat.core.extension.setTextWithOption
import com.meskiep.vaithat.core.extension.strings
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.databinding.DialogColorPickerBinding
import kotlin.apply

class ChooseColorDialog(context: Context) : BaseDialog<DialogColorPickerBinding>(context, isAnim = true) {
    override val layoutId: Int = R.layout.dialog_color_picker
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    var onDoneEvent: ((Int) -> Unit) = {}
    private var color = Color.WHITE

    override fun initView() {
        binding.colorPickerView.hueSliderView = binding.hueSlider
        initLayout()
    }

    override fun initAction() {
        binding.apply {
            colorPickerView.setOnColorChangedListener { color = it }
            loDoubleButton.apply {
                btnRight.tap {
                    onDoneEvent.invoke(color)
                    dismissDialog()
                }
                btnLeft.tap { dismissDialog() }
            }

        }
    }

    override fun onDismissListener() {}


    private fun initLayout() {
        binding.loDoubleButton.apply {
            btnLeft.setTextWithOption(context.strings(R.string.cancel))
            btnRight.setTextWithOption(context.strings(R.string.done))
        }
    }
}