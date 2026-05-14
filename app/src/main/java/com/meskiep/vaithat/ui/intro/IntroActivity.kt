package com.meskiep.vaithat.ui.intro

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseActivity
import com.meskiep.vaithat.core.utils.DataLocal
import com.meskiep.vaithat.databinding.ActivityIntroBinding
import com.meskiep.vaithat.ui.home.HomeActivity
import com.meskiep.vaithat.ui.permission.PermissionActivity
import kotlin.jvm.java

class IntroActivity : BaseActivity<ActivityIntroBinding>() {
    private val adapter = IntroAdapter(this)

    override fun setViewBinding(): ActivityIntroBinding {
        return ActivityIntroBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initVpg()
    }

    override fun viewListener() {
        binding.btnNext.setOnClickListener { handleNext() }
//        binding.btnNextFirst.setOnClickListener { handleNext() }

//        binding.vpgTutorial.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//            override fun onPageSelected(position: Int) {
//                super.onPageSelected(position)
//                binding.nativeAds.isVisible = position != 0
//                binding.btnNext.isVisible = position != 0
//                binding.btnNextFirst.isVisible = position == 0
//            }
//        })
    }

    // Init
    //==================================================================================================================

    override fun initActionBar() {}

    override fun initText() {}

    private fun initVpg() {
        binding.apply {
            vpgTutorial.adapter = adapter
            binding.dotsIndicator.attachTo(binding.vpgTutorial)
            adapter.submitList(DataLocal.itemIntroList)
        }
    }

    // Handle
    //==================================================================================================================
    private fun handleNext() {
        binding.apply {
            val nextItem = binding.vpgTutorial.currentItem + 1
            if (nextItem < DataLocal.itemIntroList.size) {
                vpgTutorial.setCurrentItem(nextItem, true)
            } else {
                val intent = if (sharePreference.getIsFirstPermission()) {
                    Intent(this@IntroActivity, PermissionActivity::class.java)
                } else {
                    Intent(this@IntroActivity, HomeActivity::class.java)
                }
                startActivity(intent)
                finishAffinity()
            }
        }
    }

    // Result + Permission
    //==================================================================================================================
    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        finishAffinity()
    }

    // Ads
    //==================================================================================================================
//    override fun initAds() {
//
//        Admob.getInstance().loadNativeAd(
//            this,
//            getString(R.string.native_intro),
//            binding.nativeAds,
//            R.layout.ads_native_medium_btn_bottom_2
//        )
//    }
}