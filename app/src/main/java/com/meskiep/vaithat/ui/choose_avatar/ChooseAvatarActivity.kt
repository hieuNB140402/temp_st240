package com.meskiep.vaithat.ui.choose_avatar


import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseActivity
import com.meskiep.vaithat.core.extension.checkInternet
import com.meskiep.vaithat.core.extension.handleBackLeftToRight
import com.meskiep.vaithat.core.extension.setImageWithOption
import com.meskiep.vaithat.core.extension.setTextWithOption
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.data.app.DataViewModel
import com.meskiep.vaithat.data.local.data_character.DataCharacter
import com.meskiep.vaithat.databinding.ActivityChooseAvatarBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.isNotEmpty

@AndroidEntryPoint
class ChooseAvatarActivity : BaseActivity<ActivityChooseAvatarBinding>() {
    private val avatarAdapter by lazy { ChooseAvatarAdapter(this) }
    private val dataViewModel: DataViewModel by viewModels()

    override fun setViewBinding(): ActivityChooseAvatarBinding {
        return ActivityChooseAvatarBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRcv()
        dataViewModel.ensureData(this@ChooseAvatarActivity)
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            launch { showLoading() }
            launch { dataViewModel.allData.collect { list -> setupLoadDataSuccess(list) } }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.tap { handleBackLeftToRight() }
        }
        avatarAdapter.onItemClick = { model, position -> handleItemClick(model) }
    }

    // Init
    //==================================================================================================================
    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarLeft.setImageWithOption(R.drawable.ic_back)
            tvCenter.setTextWithOption(getString(R.string.category))
        }
    }

    private fun initRcv() {
        binding.apply {
            rcvChooseAvatar.adapter = avatarAdapter
            rcvChooseAvatar.itemAnimator = null
        }
    }

    // Handle
    //==================================================================================================================

    private fun handleItemClick(model: DataCharacter) {
        checkInternet {
//            logEvent("click_item_${model.dataName}", model.avatar)
//            startIntentRightToLeft(CustomizeActivity::class.java, model.dataName)
        }
    }

    // Observable
    //==================================================================================================================
    private suspend fun setupLoadDataSuccess(list: ArrayList<DataCharacter>) {
        if (list.isNotEmpty()) {
            delay(300)
            dismissLoading()
            avatarAdapter.submitList(list)
        }
    }

    // Result + Permission
    //==================================================================================================================
    override fun onRestart() {
        super.onRestart()
        initNativeCollab()
    }

    // Ads
    //==================================================================================================================
    private fun initNativeCollab() {
//        Admob.getInstance()
//            .loadNativeCollapNotBanner(this, getString(R.string.native_collap_list), binding.flNativeCollab)
    }

//    override fun initAds() {
//        initNativeCollab()
//        Admob.getInstance().loadNativeAd(
//            this,
//            getString(R.string.native_list),
//            binding.nativeAds,
//            R.layout.ads_native_banner
//        )
//    }
}