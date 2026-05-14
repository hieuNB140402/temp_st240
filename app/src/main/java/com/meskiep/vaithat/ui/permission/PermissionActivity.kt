package com.meskiep.vaithat.ui.permission

import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseActivity
import com.meskiep.vaithat.core.extension.checkPermissions
import com.meskiep.vaithat.core.extension.goToSettings
import com.meskiep.vaithat.core.extension.gone
import com.meskiep.vaithat.core.extension.requestPermission
import com.meskiep.vaithat.core.extension.startIntentRightToLeft
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.core.extension.visible
import com.meskiep.vaithat.core.helper.StringHelper
import com.meskiep.vaithat.core.helper.UnitHelper
import com.meskiep.vaithat.core.utils.key.RequestKey
import com.meskiep.vaithat.databinding.ActivityPermissionBinding
import com.meskiep.vaithat.ui.home.HomeActivity
import kotlinx.coroutines.launch
import kotlin.getValue

class PermissionActivity : BaseActivity<ActivityPermissionBinding>() {
    private val viewModel: PermissionViewModel by viewModels()

    //    private var inter: InterstitialAd? = null
    override fun setViewBinding(): ActivityPermissionBinding {
        return ActivityPermissionBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initData()
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.storageGranted.collect { granted -> updatePermissionUI(granted, true) } }
                launch { viewModel.notificationGranted.collect { granted -> updatePermissionUI(granted, false) } }
            }
        }
    }

    override fun viewListener() {
        binding.swStorage.tap { handlePermissionRequest(isStorage = true) }
        binding.swNotification.tap { handlePermissionRequest(isStorage = false) }
        binding.btnContinue.tap(1500) { handleContinue() }
    }

    // Init
    //==================================================================================================================
    override fun initText() {
        binding.apply {

            val textRes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) R.string.to_access_13 else R.string.to_access
            val allText = TextUtils.concat(
                createColoredText(R.string.allow),
                " ",
                createColoredText(R.string.app_name, R.color.color_15),
                " ",
                createColoredText(textRes)
            )

            binding.tvPermission.apply {
                text = allText
                setStroke(UnitHelper.pxToDpFloat(this@PermissionActivity, 2f), getColor(R.color.green_003B50))
            }
        }
    }

    override fun initActionBar() {}

    private fun initData() = with(binding) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            layoutNotification.visible()
            layoutStorage.gone()
        } else {
            layoutStorage.visible()
            layoutNotification.gone()
        }
    }

    // Handle
    //==================================================================================================================
    private fun handlePermissionRequest(isStorage: Boolean) {
        val perms = if (isStorage) viewModel.getStoragePermissions() else viewModel.getNotificationPermissions()
        if (checkPermissions(perms)) {
            showToast(if (isStorage) R.string.granted_storage else R.string.granted_notification)
        } else if (viewModel.needGoToSettings(sharePreference, isStorage)) {
            goToSettings()
        } else {
            val requestCode = if (isStorage) RequestKey.STORAGE_PERMISSION_CODE else RequestKey.NOTIFICATION_PERMISSION_CODE
            requestPermission(perms, requestCode)
        }
    }

    private fun createColoredText(
        @androidx.annotation.StringRes textRes: Int,
        @androidx.annotation.ColorRes colorRes: Int = R.color.white,
        font: Int = R.font.cookies
    ) = StringHelper.changeColor(this, getString(textRes), colorRes, font)

    private fun handleContinue() {
//        Admob.getInstance().showInterAds(this@PermissionActivity, inter, object : InterCallback() {
//            override fun onNextAction() {
//                super.onNextAction()
//                sharePreference.setIsFirstPermission(false)
//                startIntentRightToLeft(HomeActivity::class.java)
//                finishAffinity()
//            }
//        })
        sharePreference.setIsFirstPermission(false)
        startIntentRightToLeft(HomeActivity::class.java)
        finishAffinity()
    }

    // Observable
    //==================================================================================================================
    private fun updatePermissionUI(granted: Boolean, isStorage: Boolean) {
        val imageView = if (isStorage) binding.swStorage else binding.swNotification
        imageView.setImageResource(if (granted) R.drawable.ic_sw_on else R.drawable.ic_sw_off)
    }

    // Result + Permission
    //==================================================================================================================
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        when (requestCode) {
            RequestKey.STORAGE_PERMISSION_CODE -> viewModel.updateStorageGranted(sharePreference, granted)

            RequestKey.NOTIFICATION_PERMISSION_CODE -> viewModel.updateNotificationGranted(sharePreference, granted)
        }
        if (granted) {
            showToast(if (requestCode == RequestKey.STORAGE_PERMISSION_CODE) R.string.granted_storage else R.string.granted_notification)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.updateStorageGranted(
            sharePreference, checkPermissions(viewModel.getStoragePermissions())
        )
        viewModel.updateNotificationGranted(
            sharePreference, checkPermissions(viewModel.getNotificationPermissions())
        )
    }

//    override fun initAds() {
//        Admob.getInstance().loadInterAds(this@PermissionActivity, getString(R.string.inter_per), object : InterCallback() {
//            override fun onAdLoadSuccess(interstitialAd: InterstitialAd?) {
//                super.onAdLoadSuccess(interstitialAd)
//                inter = interstitialAd
//            }
//        })
//
//        if (InternetHelper.isInternetAvailable(this)) {
//            binding.nativeAds.visible()
//            Admob.getInstance()
//                .loadNativeAd(this, getString(R.string.native_per), binding.nativeAds, R.layout.ads_native_big_btn_top)
//        } else {
//            binding.nativeAds.invisible()
//        }
//    }
}