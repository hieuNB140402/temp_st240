package com.meskiep.vaithat.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.meskiep.vaithat.core.base.BaseActivity
import com.meskiep.vaithat.core.extension.appVersionName
import com.meskiep.vaithat.core.helper.InternetHelper
import com.meskiep.vaithat.core.utils.state.CallApiState
import com.meskiep.vaithat.data.app.DataViewModel
import com.meskiep.vaithat.databinding.ActivitySplashBinding
import com.meskiep.vaithat.ui.intro.IntroActivity
import com.meskiep.vaithat.ui.language.LanguageActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.jvm.java

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    var intentActivity: Intent? = null

    private val dataViewModel: DataViewModel by viewModels()
//    var interCallBack: InterCallback? = null

    override fun setViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        if (!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intent.action != null && intent.action.equals(Intent.ACTION_MAIN)) {
            finish(); return
        }

        intentActivity =
            Intent(this, if (sharePreference.getIsFirstLang()) LanguageActivity::class.java else IntroActivity::class.java)


//        Admob.getInstance().setTimeLimitShowAds(30000)
//        Admob.getInstance().setTimeCountdownNativeCollab(15000)
//        Admob.getInstance().setOpenShowAllAds(false)

//        interCallBack = object : InterCallback() {
//            override fun onNextAction() {
//                super.onNextAction()
//                startActivity(intentActivity)
//                finishAffinity()
//            }
//        }

        lifecycleScope.launch(Dispatchers.IO) {
            checkCurrentVersion()

            if (InternetHelper.isInternetAvailable(this@SplashActivity)) {
                dataViewModel.regetData(this@SplashActivity).collect { state ->
                    when (state) {
                        CallApiState.Loading -> {}
                        else -> {
                            withContext(Dispatchers.Main) {
                                startActivity(intentActivity)
                                finishAffinity()
                            }
                        }
                    }
                }
            } else {
                delay(3000)
                withContext(Dispatchers.Main) {
                    startActivity(intentActivity)
                    finishAffinity()
                }
            }
        }
    }

    override fun viewListener() {}


    // Init
    //==================================================================================================================
    override fun initActionBar() {}

    // Handle
    //==================================================================================================================
    private suspend fun checkCurrentVersion(){
        val currentVersion = sharePreference.getCurrentVersion()
        if (currentVersion != appVersionName()){
            dataViewModel.deleteAllEditCharacterRoom(this)
        }
    }
    // Observable
    //==================================================================================================================

    // Result + Permission
    //==================================================================================================================
    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {}


    // Ads
    //==================================================================================================================

}