package com.meskiep.vaithat.core.extension

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.helper.LanguageHelper
import com.meskiep.vaithat.dialog.ConfirmDialog


fun Context.checkPermissions(listPermission: Array<String>): Boolean {
    return listPermission.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
}

fun Activity.requestPermission(permissions: Array<String>, requestCode: Int) {
    ActivityCompat.requestPermissions(this, permissions, requestCode)
}

fun Activity.goToSettings() {
    val dialog = ConfirmDialog(this, R.string.permission, R.string.go_to_setting_message)
    LanguageHelper.setLocale(this)
    dialog.show()

    dialog.onYesClick = {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:${this@goToSettings.packageName}".toUri()
        }
        this.startActivity(intent)
        dialog.dismiss()
//        hideNavigation(true)
    }
}
