package com.meskiep.vaithat.core.extension

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.helper.RateHelper
import com.meskiep.vaithat.core.helper.SharePreferenceHelper
import com.meskiep.vaithat.core.utils.state.RateState
import com.meskiep.vaithat.dialog.ConfirmDialog

fun Activity.shareApp() {
    ShareCompat.IntentBuilder.from(this).setType("text/plain").setChooserTitle("Chooser title")
        .setText("http://play.google.com/store/apps/details?id=" + (this).packageName)
        .startChooser()
}

fun Activity.policy() {
    val url = "https://sites.google.com/view/clothes-maker-skin-editor-rbx/home"
    val i = Intent(Intent.ACTION_VIEW)
    i.data = url.toUri()
    startActivity(i)
}

fun Activity.rateApp(sharePreference: SharePreferenceHelper, onRateResult: (RateState) -> Unit = {}) {
    RateHelper.showRateDialog(this, sharePreference, onRateResult)
}

fun Context.appVersionName(): String = packageManager.getPackageInfo(packageName, 0).versionName ?: ""

fun Activity.showErrorDialog(action: (() -> Unit)? = null) {
    val errorDialog =
        ConfirmDialog(this, R.string.oops, R.string.an_error_occurred, isError = true)
    errorDialog.show()

    errorDialog.onYesClick = {
        action?.invoke()
    }
}

