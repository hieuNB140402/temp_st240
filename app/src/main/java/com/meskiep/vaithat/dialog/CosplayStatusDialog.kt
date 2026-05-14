package com.meskiep.vaithat.dialog

import android.app.Activity
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseDialog
import com.meskiep.vaithat.core.extension.gone
import com.meskiep.vaithat.core.extension.invisible
import com.meskiep.vaithat.core.extension.startRotationInfinity
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.core.extension.visible
import com.meskiep.vaithat.databinding.DialogCosplayStatusBinding

class CosplayStatusDialog(val context: Activity, val isWin: Boolean = true) :
    BaseDialog<DialogCosplayStatusBinding>(context, isAnim = true) {
    override val layoutId: Int = R.layout.dialog_cosplay_status
    override val isCancelOnTouchOutside: Boolean
        get() = false
    override val isCancelableByBack: Boolean
        get() = false

    var onDoneClick: (() -> Unit) = {}
    override fun initView() {
        initLayout()
    }

    override fun initAction() {
        binding.btnNext.tap {
            onDoneClick.invoke()
            dismissDialog()
        }
    }

    override fun onDismissListener() {}

    private fun initLayout() {
        binding.apply {
            if (isWin) {
                imvBack.visible()
                imvAura.visible()
                imvAura.startRotationInfinity(5000)
                imvStatus.setImageResource(R.drawable.img_dialog_cosplay_win)
                imvContent.setImageResource(R.drawable.img_dialog_cosplay_content_win)
            } else {
                imvBack.gone()
                imvAura.invisible()
                imvStatus.setImageResource(R.drawable.img_dialog_cosplay_loss)
                imvContent.setImageResource(R.drawable.img_dialog_cosplay_content_loss)
            }
        }
    }
}