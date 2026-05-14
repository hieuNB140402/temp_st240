package com.meskiep.vaithat.core.extension

import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseActivity
import com.meskiep.vaithat.core.helper.InternetHelper

fun BaseActivity<*>.checkInternet(action: () -> Unit) {
    if (InternetHelper.isInternetAvailable(this)) {
        action.invoke()
    } else {
        showToast(R.string.please_check_your_network_connection)
    }
}