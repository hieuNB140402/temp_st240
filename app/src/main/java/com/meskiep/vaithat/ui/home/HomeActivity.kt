package com.meskiep.vaithat.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseActivity
import com.meskiep.vaithat.core.extension.rateApp
import com.meskiep.vaithat.core.extension.setImageWithOption
import com.meskiep.vaithat.core.extension.startIntentRightToLeft
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.core.helper.LanguageHelper
import com.meskiep.vaithat.core.utils.DataLocal
import com.meskiep.vaithat.core.utils.key.ValueKey
import com.meskiep.vaithat.core.utils.state.RateState
import com.meskiep.vaithat.databinding.ActivityHomeBinding
import com.meskiep.vaithat.ui.SettingsActivity
import com.meskiep.vaithat.ui.choose_avatar.ChooseAvatarActivity
import com.meskiep.vaithat.ui.emoji_maker.EmojiMakerActivity
import com.meskiep.vaithat.ui.my_creation.MyCreationActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    private val homeFeatureAdapter by lazy { HomeFeatureAdapter(this) }

    override fun setViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        sharePreference.setCountBack(sharePreference.getCountBack() + 1)
        initRcv()
    }

    override fun dataObservable() {

    }

    override fun viewListener() = with(binding) {
        actionBar.btnActionBarRight.tap { startIntentRightToLeft(SettingsActivity::class.java) }
        homeFeatureAdapter.onItemClick = { feature -> handleFeature(feature) }
    }

    // Init
    //==================================================================================================================
    override fun initActionBar() = with(binding.actionBar) {
        btnActionBarRight.setImageWithOption(R.drawable.ic_settings)
    }

    private fun initRcv() = with(binding) {
        rcvHome.apply {
            adapter = homeFeatureAdapter
            itemAnimator = null
        }
        submitHomeFeature()
    }

    // Handle
    //==================================================================================================================
    private fun submitHomeFeature() {
        homeFeatureAdapter.submitList(DataLocal.getHomeFeatureList())
    }

    private fun handleFeature(feature: Int) {
        when (feature) {
            ValueKey.CREATION_EMOJI -> startIntentRightToLeft(ChooseAvatarActivity::class.java)
            ValueKey.EMOJI_MAKER -> startIntentRightToLeft(EmojiMakerActivity::class.java)
            ValueKey.COSPLAY_EMOJI -> startIntentRightToLeft(ChooseAvatarActivity::class.java)
            ValueKey.MY_CREATION -> startIntentRightToLeft(MyCreationActivity::class.java)
        }
    }

    // Observable
    //==================================================================================================================


    // Result + Permission
    //==================================================================================================================
    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        if (!sharePreference.getIsRate(this) && sharePreference.getCountBack() % 2 == 0) {
            rateApp(sharePreference) { state ->
                if (state != RateState.CANCEL) {
                    showToast(R.string.have_rated)
                }
                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        delay(1000)
                        exitProcess(0)
                    }
                }
            }
        } else {
            exitProcess(0)
        }
    }

    override fun onRestart() {
        super.onRestart()
        LanguageHelper.setLocale(this)
        submitHomeFeature()
//        initNativeCollab()
    }

    override fun onResume() {
        super.onResume()
//        lifecycleScope.launch(Dispatchers.IO) { viewModel.deleteCacheFolder(this@HomeActivity) }
    }

    // Ads
    //==================================================================================================================
//    private fun initNativeCollab() {
//        Admob.getInstance().loadNativeCollapNotBanner(this, getString(R.string.native_cl_home), binding.flNativeCollab)
//    }
//
//    override fun initAds() {
//        Admob.getInstance().loadInterAll(this@HomeActivity, getString(R.string.inter_all))
//        Admob.getInstance().loadNativeAll(this, getString(R.string.native_all))
//        initNativeCollab()
//    }
}