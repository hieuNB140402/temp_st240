package com.meskiep.vaithat.dialog

import android.app.Activity
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseDialog
import com.meskiep.vaithat.core.extension.gone
import com.meskiep.vaithat.core.extension.margin
import com.meskiep.vaithat.core.extension.setTextWithOption
import com.meskiep.vaithat.core.extension.strings
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.databinding.DialogConfirmBinding

class ConfirmDialog(val context: Activity, val title: Int, val description: Int, val isError: Boolean = false) :
    BaseDialog<DialogConfirmBinding>(context, isAnim = true) {
    override val layoutId: Int = R.layout.dialog_confirm
    override val isCancelOnTouchOutside: Boolean
        get() = false
    override val isCancelableByBack: Boolean
        get() = false

    var onYesClick: (() -> Unit) = {}

    override fun initView() {
        initText()
        initLayout()
    }

    override fun initAction() {
        binding.loDoubleButton.apply {
            btnLeft.tap { dismissDialog() }

            btnRight.tap {
                onYesClick.invoke()
                dismissDialog()
            }
        }
    }

    override fun onDismissListener() {}

    private fun initText() {
        binding.apply {
            tvTitle.text = context.strings(title)
            tvDescription.text = context.strings(description)
        }
    }

    private fun initLayout() {
        binding.loDoubleButton.apply {
            btnLeft.setTextWithOption(context.strings(R.string.no))


            val rightTextContent = if (isError) {
                btnLeft.gone()
                btnRight.margin("horizontal", 80)
                R.string.ok
            } else {
                R.string.yes
            }

            btnRight.setTextWithOption(context.strings(rightTextContent))
        }
    }
}