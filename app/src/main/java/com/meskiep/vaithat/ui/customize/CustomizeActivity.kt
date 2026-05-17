package com.meskiep.vaithat.ui.customize

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.StatsLog.logEvent
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseActivity
import com.meskiep.vaithat.core.extension.checkInternet
import com.meskiep.vaithat.core.extension.dLog
import com.meskiep.vaithat.core.extension.eLog
import com.meskiep.vaithat.core.extension.handleBackLeftToRight
import com.meskiep.vaithat.core.extension.hideNavigation
import com.meskiep.vaithat.core.extension.invisible
import com.meskiep.vaithat.core.extension.launchIO
import com.meskiep.vaithat.core.extension.loadImage
import com.meskiep.vaithat.core.extension.setBackgroundWithOption
import com.meskiep.vaithat.core.extension.setImageWithOption
import com.meskiep.vaithat.core.extension.setTextWithOption
import com.meskiep.vaithat.core.extension.showErrorDialog
import com.meskiep.vaithat.core.extension.startIntentRightToLeft
import com.meskiep.vaithat.core.extension.strings
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.core.extension.visible
import com.meskiep.vaithat.core.helper.LanguageHelper
import com.meskiep.vaithat.core.helper.MediaHelper
import com.meskiep.vaithat.core.helper.UnitHelper
import com.meskiep.vaithat.core.utils.key.IntentKey
import com.meskiep.vaithat.core.utils.key.ValueKey
import com.meskiep.vaithat.core.utils.state.SaveState
import com.meskiep.vaithat.data.app.DataViewModel
import com.meskiep.vaithat.data.local.data_character.DataCharacter
import com.meskiep.vaithat.data.model.custom.CustomizeModel
import com.meskiep.vaithat.data.model.custom.ItemNavCustomModel
import com.meskiep.vaithat.data.model.custom.NavigationModel
import com.meskiep.vaithat.data.model.custom.SuggestionModel
import com.meskiep.vaithat.databinding.ActivityCustomizeBinding
import com.meskiep.vaithat.dialog.ConfirmDialog
import com.meskiep.vaithat.ui.add_character.AddCharacterActivity
import com.meskiep.vaithat.ui.customize.adapter.CustomizeBottomNavigationAdapter
import com.meskiep.vaithat.ui.customize.adapter.CustomizeColorLayerAdapter
import com.meskiep.vaithat.ui.customize.adapter.CustomizeLayerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.get
import kotlin.collections.isNotEmpty
import kotlin.getValue
import kotlin.jvm.java

@AndroidEntryPoint
class CustomizeActivity : BaseActivity<ActivityCustomizeBinding>() {
    private val viewModel: CustomizeViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()
    val colorLayerAdapter by lazy { CustomizeColorLayerAdapter(this) }
    val layerAdapter by lazy { CustomizeLayerAdapter(this) }
    val bottomNavigationAdapter by lazy { CustomizeBottomNavigationAdapter() }

    override fun setViewBinding(): ActivityCustomizeBinding {
        return ActivityCustomizeBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRcv()
        initInternet()

    }

    override fun dataObservable() {
        lifecycleScope.launch {
            launch { dataViewModel.isDataCallSuccess.collect { isDataCallSuccess -> setupLoadData(isDataCallSuccess) } }
            launch { viewModel.bottomNavigationList.collect { bottomNavigationList -> setupNavigation(bottomNavigationList) } }
            launch { viewModel.isShowMoreColor.collect { status -> handleStatusColor(status) } }
            launch { viewModel.isFlip.collect { status -> setupFlip(status) } }

        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnActionBarLeft.tap { confirmExit() }
                btnActionBarCenterLeft.tap { viewModel.setIsFlip() }
                btnActionBarCenterRight.tap { handleReset() }
                btnActionBarRightText.tap { handleSave() }
            }

            btnRandom.tap { checkInternet { handleRandomAllLayer() } }
            btnColor.tap { viewModel.setIsShowMoreColor() }
        }
        handleRcv()
    }

    // Init
    //==================================================================================================================

    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarLeft.setImageWithOption(R.drawable.ic_back)
        }
    }

    private fun initRcv() {
        binding.apply {
            rcvLayer.apply {
                adapter = layerAdapter
                itemAnimator = null
            }

            rcvColor.apply {
                adapter = colorLayerAdapter
                itemAnimator = null
            }

            rcvNavigation.apply {
                adapter = bottomNavigationAdapter
                itemAnimator = null
            }
        }
    }

    private fun initInternet() {
        if (!viewModel.isCreated.value) {
            checkInternet(
                action = {
                    lifecycleScope.launch { showLoading() }
                    dataViewModel.ensureData(this)
                },
                onYesClick = {
                    initInternet()
                }
            )
        }
    }

    private fun initData() {
        val handleExceptionCoroutine = CoroutineExceptionHandler { _, throwable ->
            eLog("initData: ${throwable.message}")
            CoroutineScope(Dispatchers.Main).launch {
                dismissLoading()

                showErrorDialog {
                    finish()
                }
            }
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO + handleExceptionCoroutine).launch {
            var pathImageDefault = ""

            // Get data from list
            val deferred1 = async {
                viewModel.initValueData()

                when (viewModel.customizeStatusPlay) {
                    ValueKey.CREATE -> {}

                    ValueKey.EDIT -> {
                        viewModel.initDataEdit(intent.getStringExtra(IntentKey.PATH_EDIT_KEY) ?: "")
                    }

                    else -> {

                    }
                }

                viewModel.initValueDefault()

                dLog("deferred1")
                return@async true
            }

            // Add custom view in FrameLayout
            val deferred2 = async(Dispatchers.Main) {
                if (deferred1.await()) {
                    viewModel.setImageViewList(binding.layoutCustomLayer)
                    dLog("deferred2")
                }
                return@async true
            }

            // Fill data default
            val deferred3 = async {
                if (deferred1.await() && deferred2.await()) {
                    if (viewModel.customizeStatusPlay == ValueKey.CREATE) {
                        pathImageDefault = viewModel.getDefaultPath()
                    }
                    dLog("deferred3")
                }
                return@async true
            }

            withContext(Dispatchers.Main) {
                if (deferred1.await() && deferred2.await() && deferred3.await()) {
                    loadImageDefault(pathImageDefault)
                }
            }
        }
    }

    private suspend fun loadImageDefault(pathImageDefault: String) {
        when (viewModel.customizeStatusPlay) {
            ValueKey.CREATE -> {
                loadPathToImage(viewModel.getFirstImageView(), pathImageDefault)
                setupActionNorma()
            }

            ValueKey.EDIT -> {
                loadPathToImage(viewModel.getFirstImageView(), pathImageDefault)
                setupActionNorma()
                viewModel.pathSelectedList.forEachIndexed { index, path ->
                    if (path != "") {
                        loadPathToImage(viewModel.imageViewList[index], path)
                    }
                }
            }


            else -> {

            }
        }

        layerAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
        colorLayerAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])

        checkStatusColor()
        viewModel.setIsCreated(true)

        delay(300)
        dismissLoading()
        dLog("main")
    }

    private fun setupActionNorma() = with(binding.actionBar) {
        btnActionBarCenterLeft.setImageWithOption(R.drawable.ic_flip_draw_horizontal)
        btnActionBarCenterRight.setImageWithOption(R.drawable.ic_reset)
        btnActionBarRightText.setBackgroundWithOption(R.drawable.bg_focus_very_short)
        tvActionBarRightText.apply {
            setTextWithOption(strings(R.string.next))
            setTextColor(getColor(R.color.white))
            setStroke(
                UnitHelper.pxToDpFloat(this@CustomizeActivity, 2f),
                getColor(R.color.green_003B50)
            )
        }
    }

    // Handle
    //==================================================================================================================
    private fun handleRcv() {
        layerAdapter.onItemClick = { item, position -> checkInternet { handleFillLayer(item, position) } }

        layerAdapter.onNoneClick = { position -> checkInternet { handleNoneLayer(position) } }

        layerAdapter.onRandomClick = { checkInternet { handleRandomLayer() } }

        colorLayerAdapter.onItemClick = { position -> checkInternet { handleChangeColorLayer(position) } }

        bottomNavigationAdapter.onItemClick =
            { positionBottomNavigation -> checkInternet { handleClickBottomNavigation(positionBottomNavigation) } }
    }

    private fun handleClickBottomNavigation(positionBottomNavigation: Int) {
        if (positionBottomNavigation == viewModel.positionNavSelected) return

        launchIO(
            blockIO = {
                viewModel.updatePositionNavSelected(positionBottomNavigation)
                viewModel.updatePositionCustom(viewModel.getPositionCustomByPositionNavigation(positionBottomNavigation))
                viewModel.setClickBottomNavigation(positionBottomNavigation)
            },
            blockMain = { checkStatusColor() }
        )
    }

    private fun handleFillLayer(item: ItemNavCustomModel, position: Int) {
        launchIO(
            blockIO = { viewModel.setClickFillLayer(item, position) },
            blockMain = { pathSelected ->
                loadPathToImage(viewModel.getCurrentImageView(), pathSelected)
                layerAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                dLog("path: $pathSelected")
            }
        )
    }

    private fun handleChangeColorLayer(position: Int) {
        launchIO(
            blockIO = { viewModel.setClickChangeColor(position) },
            blockMain = { colorPath ->
                loadPathToImage(viewModel.getCurrentImageView(), colorPath)
                colorLayerAdapter.submitList(viewModel.getCurrentColorItemNavList())
            }
        )
    }

    private fun handleNoneLayer(position: Int) {
        launchIO(
            blockIO = { viewModel.handleNoneLayer(position) },
            blockMain = {
                Glide.with(this@CustomizeActivity).clear(viewModel.getCurrentImageView())
                layerAdapter.submitList(viewModel.getCurrentItemNavList())
            }
        )
    }

    private fun handleStatusColor(isClose: Boolean = false) {
        binding.flColor.isInvisible = isClose
    }

    private fun checkStatusColor() {
        if (viewModel.positionNavSelected == -1) return

        binding.apply {
            if (viewModel.getCurrentColorItemNavList().isNotEmpty()) {
                btnColor.visible()
                flColor.visible()
            } else {
                btnColor.invisible()
                flColor.invisible()
            }
        }
    }

    private fun handleReset() {
        val dialog = ConfirmDialog(this, R.string.reset, R.string.do_you_want_to_reset_all)

        dialog.show()

        dialog.onYesClick = {
            checkInternet {
                launchIO(
                    blockIO = { viewModel.setClickReset() },
                    blockMain = { defaultPath ->
                        viewModel.imageViewList.forEach { imageView -> Glide.with(this@CustomizeActivity).clear(imageView) }
                        loadPathToImage(viewModel.getFirstImageView(), defaultPath)
                        layerAdapter.submitList(viewModel.getCurrentItemNavList())
                        colorLayerAdapter.submitList(viewModel.getCurrentColorItemNavList())
                    }
                )
            }
        }
    }

    private fun handleRandomLayer() {
        launchIO(
            blockIO = { viewModel.setClickRandomLayer() },
            blockMain = { randomPath, isMoreColors ->
                loadPathToImage(viewModel.getCurrentImageView(), randomPath)
                layerAdapter.submitList(viewModel.getCurrentItemNavList())

                if (isMoreColors) {
                    val currentColorNavList = viewModel.getCurrentColorItemNavList()
                    colorLayerAdapter.submitList(currentColorNavList)
                    binding.rcvColor.smoothScrollToPosition(currentColorNavList.indexOfFirst { it.isSelected })
                }
            }
        )

    }

    private fun handleRandomAllLayer() {
        launchIO(
            blockIO = { viewModel.setClickRandomFullLayer() },
            blockMain = { isOutTurn ->
                viewModel.pathSelectedList.forEachIndexed { index, path ->
                    if (path != "") loadPathToImage(viewModel.imageViewList[index], path)
                }

                layerAdapter.submitList(viewModel.getCurrentItemNavList())
                colorLayerAdapter.submitList(viewModel.getCurrentColorItemNavList())

                binding.btnRandom.isInvisible = isOutTurn
            }
        )
    }

    private fun handleSave() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.saveImageFromView(this@CustomizeActivity, binding.layoutCustomLayer).collect { result ->
                when (result) {
                    is SaveState.Loading -> showLoading()
                    is SaveState.Nothing -> {}

                    is SaveState.Error -> {
                        dismissLoading()

                        withContext(Dispatchers.Main) {
                            showErrorDialog()
                        }
                    }

                    is SaveState.Success -> {
                        when (viewModel.customizeStatusPlay) {
                            ValueKey.EDIT -> {
//                                logEvent(
//                                    "click_item_${viewModel.dataCustomize.value!!.dataName}_edit",
//                                    viewModel.dataCustomize.value!!.avatar
//                                )
//                                viewModel.updateEditCharacter(this@CustomizeActivity, result.path)
//                                dismissLoading(true)
//                                withContext(Dispatchers.Main) {
//                                    startIntentRightToLeft(
//                                        AddCharacterActivity::class.java, result.path
//                                    )
//                                }
                            }

                            else -> {
//                                logEvent(
//                                    "click_item_${viewModel.dataCustomize.value!!.dataName}_done",
//                                    viewModel.dataCustomize.value!!.avatar
//                                )
                                viewModel.saveEditCharacter(this@CustomizeActivity, result.path)
                                dismissLoading()
                                withContext(Dispatchers.Main) {
                                    startIntentRightToLeft(AddCharacterActivity::class.java, result.path)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun confirmExit() {
        val dialog = ConfirmDialog(this, R.string.exit, R.string.have_not_saved_it_yet_do_you_want_to_exit)

        dialog.show()

        dialog.onYesClick = {
            handleBackLeftToRight()
        }
    }

    private fun loadPathToImage(imageView: ImageView, path: String) {
        loadImage(path, imageView, 512, false)
    }

    // Observable
    //==================================================================================================================
    private fun setupLoadData(isDataCallSuccess: Boolean) {
        if (!isDataCallSuccess) return

        launchIO(
            blockIO = {
                val dataName = intent.getStringExtra(IntentKey.AVATAR_NAME_KEY) ?: ""
                val customStatusPlay = intent.getIntExtra(IntentKey.CUSTOM_STATUS_PLAY_KEY, ValueKey.CREATE)
                viewModel.setupDataGetSuccess(dataName, customStatusPlay)
            },
            blockMain = {
                initData()
            }
        )
    }

    private fun setupNavigation(bottomNavigationList: List<NavigationModel>) {
        if (bottomNavigationList.isEmpty()) return
        val currentItemNavList = viewModel.getCurrentItemNavList()

        colorLayerAdapter.submitList(viewModel.getCurrentColorItemNavList())
        layerAdapter.submitList(currentItemNavList)
        bottomNavigationAdapter.submitList(bottomNavigationList)

        if (currentItemNavList.isNotEmpty()) {
            binding.rcvColor.smoothScrollToPosition(currentItemNavList.indexOfFirst { it.isSelected })
        }
    }

    private fun setupFlip(status: Boolean) {
        val rotation = if (status) -180f else 0f
        viewModel.imageViewList.forEachIndexed { index, view ->
            if (view != null) {
                view.rotationY = rotation
            }
        }
    }


    // Result + Permission
    //==================================================================================================================
    override fun onRestart() {
        super.onRestart()
        initNativeCollab()
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        confirmExit()
    }

    // Ads
    //==================================================================================================================
    override fun initAds() {
        initNativeCollab()
    }

    private fun initNativeCollab() {
//        Admob.getInstance().loadNativeCollapNotBanner(this, getString(R.string.native_collap_create), binding.flNativeCollab)
    }

}