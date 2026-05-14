package com.meskiep.vaithat.ui

import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseActivity
import com.meskiep.vaithat.core.extension.gone
import com.meskiep.vaithat.core.extension.handleBackLeftToRight
import com.meskiep.vaithat.core.extension.policy
import com.meskiep.vaithat.core.extension.setImageWithOption
import com.meskiep.vaithat.core.extension.setTextWithOption
import com.meskiep.vaithat.core.extension.shareApp
import com.meskiep.vaithat.core.extension.startIntentRightToLeft
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.core.helper.RateHelper
import com.meskiep.vaithat.core.utils.key.IntentKey
import com.meskiep.vaithat.core.utils.state.RateState
import com.meskiep.vaithat.databinding.ActivitySettingsBinding
import com.meskiep.vaithat.ui.language.LanguageActivity

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {
    override fun setViewBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRate()
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.tap { handleBackLeftToRight() }
            btnLanguage.tap { startIntentRightToLeft(LanguageActivity::class.java, IntentKey.INTENT_KEY) }
            btnShare.tap(1500) { shareApp() }
            btnRate.tap { handleRate() }
            btnPolicy.tap(1500) { policy() }
        }
    }

    // Init
    //==================================================================================================================
    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarLeft.setImageWithOption(R.drawable.ic_back)
            tvCenter.setTextWithOption(getString(R.string.settings))
        }
    }

    private fun initRate() {
        binding.btnRate.isVisible = !sharePreference.getIsRate(this)
    }

    // Handle
    //==================================================================================================================
    private fun handleRate() {
        RateHelper.showRateDialog(this@SettingsActivity, sharePreference) { state ->
            if (state != RateState.CANCEL) {
                binding.btnRate.gone()
                showToast(R.string.have_rated)
            }
        }
    }

    // Observable
    //==================================================================================================================

    // Result + Permission
    //==================================================================================================================

    // Ads
    //==================================================================================================================


}