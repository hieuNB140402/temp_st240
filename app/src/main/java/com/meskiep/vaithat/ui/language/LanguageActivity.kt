package com.meskiep.vaithat.ui.language

import android.annotation.SuppressLint
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseActivity
import com.meskiep.vaithat.core.extension.gone
import com.meskiep.vaithat.core.extension.handleBackLeftToRight
import com.meskiep.vaithat.core.extension.invisible
import com.meskiep.vaithat.core.extension.margin
import com.meskiep.vaithat.core.extension.setTextWithOption
import com.meskiep.vaithat.core.extension.startIntentRightToLeft
import com.meskiep.vaithat.core.extension.startIntentWithClearTop
import com.meskiep.vaithat.core.extension.strings
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.core.extension.visible
import com.meskiep.vaithat.core.utils.key.IntentKey
import com.meskiep.vaithat.databinding.ActivityLanguageBinding
import com.meskiep.vaithat.ui.home.HomeActivity
import com.meskiep.vaithat.ui.intro.IntroActivity
import kotlinx.coroutines.launch
import kotlin.jvm.java

class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {
    private val viewModel: LanguageViewModel by viewModels()

    private val languageAdapter by lazy { LanguageAdapter(this) }

    override fun setViewBinding(): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRcv()
        val intentValue = intent.getStringExtra(IntentKey.INTENT_KEY)
        viewModel.setFirstLanguage(intentValue == null)
        viewModel.loadLanguages(sharePreference.getPreLanguage())
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            launch { viewModel.isFirstLanguage.collect { isFirst -> setupUI(isFirst) } }
            launch { viewModel.languageList.collect { list -> languageAdapter.submitList(list) } }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.tap { handleBackLeftToRight() }
            actionBar.btnActionBarRight.tap { handleDone() }
        }
        handleRcv()
    }

    // Init
    //==================================================================================================================
    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarLeft.setImageResource(R.drawable.ic_back)
            btnActionBarRight.setImageResource(R.drawable.ic_lang_done)
        }
    }

    private fun initRcv() {
        binding.rcvLanguage.apply {
            adapter = languageAdapter
            itemAnimator = null
        }
    }

    // Handle
    //==================================================================================================================
    private fun handleRcv() {
        binding.apply {
            languageAdapter.onItemClick = { code ->
                actionBar.btnActionBarRight.visible()
                viewModel.selectLanguage(code)
            }
        }
    }

    private fun handleDone() {
        val code = viewModel.codeLang
        if (code.isEmpty()) {
            showToast(R.string.not_select_lang)
            return
        }
        sharePreference.setPreLanguage(code)

        if (viewModel.isFirstLanguage.value) {
            sharePreference.setIsFirstLang(false)
            startIntentRightToLeft(IntroActivity::class.java)
            finishAffinity()
        } else {
            startIntentWithClearTop(HomeActivity::class.java)
        }
    }

    // Observable
    //==================================================================================================================
    private fun setupUI(isFirst: Boolean) {
        binding.actionBar.apply {
            if (!isFirst) {
                btnActionBarLeft.visible()
            } else {
                btnActionBarRight.invisible()
                tvCenter.margin("left", -32)
            }
            tvCenter.setTextWithOption(strings(R.string.language), !isFirst)
        }
    }

    // Result + Permission
    //==================================================================================================================

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        if (!viewModel.isFirstLanguage.value) {
            handleBackLeftToRight()
        } else {
            finishAffinity()
        }
    }

    // Ads
    //==================================================================================================================

//    override fun initAds() {
//        Admob.getInstance().loadNativeAd(
//            this@LanguageActivity,
//            getString(R.string.native_language),
//            binding.nativeAds,
//            R.layout.ads_native_big_btn_top
//        )
//    }
}