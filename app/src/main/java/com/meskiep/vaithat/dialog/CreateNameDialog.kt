package com.meskiep.vaithat.dialog

import android.app.Activity
import android.widget.Toast
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseDialog
import com.meskiep.vaithat.core.extension.hideNavigation
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.databinding.DialogCreateNameBinding

class CreateNameDialog(val context: Activity) :
    BaseDialog<DialogCreateNameBinding>(context, isAnim = true) {
    override val layoutId: Int = R.layout.dialog_create_name
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    var onYesClick: ((String) -> Unit) = {}

    override fun initView() {
        context.hideNavigation(true)
    }

    override fun initAction() {
        binding.apply {
            tvNo.tap { dismissDialog() }
            tvYes.tap {
                val input = edtName.text.toString().trim()

                when {
                    input == "" -> {
                        Toast.makeText(context, context.getString(R.string.please_enter_your_package_name), Toast.LENGTH_SHORT)
                            .show()
                    }

                    else -> {
                        onYesClick.invoke(input)
                        dismissDialog()
                    }
                }
            }
            flOutSide.tap {
                dismissDialog()
            }
        }
    }

    override fun onDismissListener() {

    }
}