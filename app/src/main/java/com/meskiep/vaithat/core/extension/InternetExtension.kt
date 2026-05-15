package com.meskiep.vaithat.core.extension

import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseActivity
import com.meskiep.vaithat.core.helper.InternetHelper
import com.meskiep.vaithat.core.helper.LanguageHelper
import com.meskiep.vaithat.dialog.ConfirmDialog

fun BaseActivity<*>.checkInternet(action: () -> Unit) {
    if (InternetHelper.isInternetAvailable(this)) {
        action.invoke()
    } else {
        val dialog = ConfirmDialog(
            context = this,
            title = R.string.oops,
            description = R.string.please_check_your_network_connection,
            isError = true
        )

        dialog.show()
    }
}