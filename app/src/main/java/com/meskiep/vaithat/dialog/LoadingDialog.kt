package com.meskiep.vaithat.dialog

import android.app.Activity
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseDialog
import com.meskiep.vaithat.databinding.DialogLoadingBinding

class LoadingDialog(val context: Activity) :
    BaseDialog<DialogLoadingBinding>(context, true) {
    override val layoutId: Int = R.layout.dialog_loading
    override val isCancelOnTouchOutside: Boolean
        get() = false
    override val isCancelableByBack: Boolean
        get() = false

    override fun initView() {}

    override fun initAction() {}

    override fun onDismissListener() {}
}