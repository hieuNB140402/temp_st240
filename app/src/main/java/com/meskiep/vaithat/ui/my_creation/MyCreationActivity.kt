package com.meskiep.vaithat.ui.my_creation

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.extension.checkPermissions
import com.meskiep.vaithat.core.extension.eLog
import com.meskiep.vaithat.core.extension.goToSettings
import com.meskiep.vaithat.core.extension.gone
import com.meskiep.vaithat.core.extension.hideNavigation
import com.meskiep.vaithat.core.extension.invisible
import com.meskiep.vaithat.core.extension.launchIO
import com.meskiep.vaithat.core.extension.requestPermission
import com.meskiep.vaithat.core.extension.select
import com.meskiep.vaithat.core.extension.setImageWithOption
import com.meskiep.vaithat.core.extension.setTextWithOption
import com.meskiep.vaithat.core.extension.shareImagesPaths
import com.meskiep.vaithat.core.extension.startIntentWithClearTop
import com.meskiep.vaithat.core.extension.strings
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.core.extension.visible
import com.meskiep.vaithat.core.helper.InternetHelper
import com.meskiep.vaithat.core.helper.LanguageHelper
import com.meskiep.vaithat.core.helper.UnitHelper
import com.meskiep.vaithat.core.share.whatsapp.WhatsappSharingActivity
import com.meskiep.vaithat.core.utils.key.IntentKey
import com.meskiep.vaithat.core.utils.key.RequestKey
import com.meskiep.vaithat.core.utils.key.ValueKey
import com.meskiep.vaithat.core.utils.state.DeleteState
import com.meskiep.vaithat.core.utils.state.HandleState
import com.meskiep.vaithat.data.app.DataViewModel
import com.meskiep.vaithat.data.model.MyCreationModel
import com.meskiep.vaithat.data.model.custom.CustomizeModel
import com.meskiep.vaithat.databinding.ActivityMyCreationBinding
import com.meskiep.vaithat.dialog.ConfirmDialog
import com.meskiep.vaithat.ui.customize.CustomizeActivity
import com.meskiep.vaithat.ui.home.HomeActivity
import com.meskiep.vaithat.ui.permission.PermissionViewModel
import com.meskiep.vaithat.ui.view.ViewActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.isNotEmpty
import kotlin.jvm.java

@AndroidEntryPoint
class MyCreationActivity : WhatsappSharingActivity<ActivityMyCreationBinding>() {
    private val viewModel: MyCreationViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()
    private val editAdapter by lazy { MyCreationAdapter(this, ValueKey.EDIT_CREATION) }
    private val viewAdapter by lazy { MyCreationAdapter(this, ValueKey.VIEW_CREATION) }

    override fun setViewBinding(): ActivityMyCreationBinding {
        return ActivityMyCreationBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        lifecycleScope.launch { showLoading() }
        initRcv()
        viewModel.setTypeStatus(ValueKey.EDIT_CREATION)
        resetData()
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.typeSelected.collect { type -> setupTypeSelected(type) } }
                launch { viewModel.downloadState.collect { state -> handleDownloadState(state) } }
                launch { viewModel.isShowSelection.collect { status -> setupIsLongClick(status) } }

                launch { viewModel.editList.collect { list -> setupEditCreationType(list) } }
                launch { viewModel.viewList.collect { list -> setupViewType(list) } }

//                launch { dataViewModel.allData.collect { list -> setupGetData(list) } }
                launch { viewModel.isLastItem.collect { status -> changeImageActionBarRight(status) } }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnActionBarLeft.tap { startIntentWithClearTop(HomeActivity::class.java) }
                btnActionBarRight.tap { handleSelectAll() }
                btnActionBarNextToRight.tap { handleDelete() }
            }
//            bottomBar.apply {
//                btnBottomBarLeft.tap(2000) { handleShare() }
//                btnBottomBarRight.tap { checkStoragePermission() }
//            }
            btnEditCreation.tap { viewModel.setTypeStatus(ValueKey.EDIT_CREATION) }
            btnViewCreation.tap { viewModel.setTypeStatus(ValueKey.VIEW_CREATION) }

//            btnTelegram.tap { handleAddToTelegram(viewModel.getPathSelected()) }
//            btnWhatsapp.tap { handleAddToWhatsApp(viewModel.getPathSelected()) }
        }
        handleRcv()
    }

    // Init
    //==================================================================================================================
    override fun initActionBar() {
        binding.actionBar.apply {

            btnActionBarLeft.setImageWithOption(R.drawable.ic_back)
            tvCenter.setTextWithOption(strings(R.string.my_creation))

            btnActionBarRight.setImageResource(R.drawable.ic_my_creation_delete)
            btnActionBarNextToRight.setImageResource(R.drawable.ic_my_creation_unselect_all)
        }

        binding.bottomBar.apply {
            btnLeft.setTextWithOption(strings(R.string.share))
            btnRight.setTextWithOption(strings(R.string.download))
        }
    }

    override fun initText() {
        binding.apply {
            tvWhatsapp.select()
            tvTelegram.select()
        }
    }

    private fun initRcv() {
        binding.apply {
            rcvEditCreation.apply {
                adapter = editAdapter
                itemAnimator = null
            }
            rcvViewCreation.apply {
                adapter = viewAdapter
                itemAnimator = null
            }
        }
    }

    // Handle other
    //==================================================================================================================
    private fun handleDelete() {
        launchIO(
            blockIO = { viewModel.deleteMyCreation(this) },
            blockMain = { state ->
                when (state) {
                    DeleteState.Empty -> showToast(R.string.please_select_an_item)
                    DeleteState.Success -> viewModel.setSelectionState(false)
                    is DeleteState.Failure -> eLog("handleDelete: ${state.error}")
                }
            }
        )
    }

    fun changeImageActionBarRight(isReset: Boolean) {
        val res = if (isReset) R.drawable.ic_my_creation_selected_all else R.drawable.ic_my_creation_unselect_all
        binding.actionBar.btnActionBarRight.setImageResource(res)
    }

    private fun resetData() {
        viewModel.apply {
            resetGetMyCreation(this@MyCreationActivity)
            setSelectionState(false)
        }
        changeImageActionBarRight(true)
    }

    private fun handleSelectAll() {
//        val shouldSelectAll = viewModel.selectAll()
//        changeImageActionBarRight(!shouldSelectAll)
    }

    private fun handleAddToTelegram(list: ArrayList<String>) {
//        if (list.isEmpty()) {
//            showToast(R.string.no_images_are_currently_selected)
//            return
//        }
//        viewModel.addToTelegram(this, list)
    }

    private fun handleAddToWhatsApp(list: ArrayList<String>) {
//        if (list.isEmpty()) {
//            showToast(R.string.no_images_are_currently_selected)
//            return
//        }
//        if (list.size < 3) {
//            showToast(R.string.limit_3_items)
//            return
//        }
//        if (list.size > 30) {
//            showToast(R.string.limit_30_items)
//            return
//        }
//
//        val dialog = CreateNameDialog(this)
//        LanguageHelper.setLocale(this)
//        dialog.show()
//
//        fun dismissDialog() {
//            dialog.dismiss()
//            hideNavigation(true)
//        }
//        dialog.onNoClick = {
//            dismissDialog()
//        }
//        dialog.onDismissClick = {
//            dismissDialog()
//        }
//        dialog.onYesClick = { packageName ->
//            dismissDialog()
//            viewModel.addToWhatsapp(this, packageName, list) { stickerPack ->
//                if (stickerPack != null) {
//                    addToWhatsapp(stickerPack) {
//                        resetData()
//                    }
//                }
//            }
//        }
    }

    private fun handleRcv() {
        editAdapter.apply {
//            onItemClick = { pathImage -> handleItemClick(pathImage, ValueKey.MY_CHARACTER) }
//            onItemTick = { position -> viewModel.toggleSelect(position) }
//            onEditClick = { pathInternal -> handleEditClick(pathInternal) }
//            onDeleteClick = { pathInternal -> handleDelete(arrayListOf(pathInternal)) }
//            onLongClick = { position -> handleLongClick(position) }
        }

        viewAdapter.apply {
//            onItemClick = { pathImage -> handleItemClick(pathImage, ValueKey.MY_DESIGN) }
//            onItemTick = { position -> viewModel.toggleSelect(position) }
//            onDeleteClick = { pathInternal -> handleDelete(arrayListOf(pathInternal)) }
//            onLongClick = { position -> handleLongClick(position) }
        }

        binding.rcvEditCreation.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(
                recyclerView: RecyclerView, motionEvent: MotionEvent
            ): Boolean {
                return when {
                    motionEvent.action != MotionEvent.ACTION_UP || recyclerView.findChildViewUnder(
                        motionEvent.x, motionEvent.y
                    ) != null -> false

                    else -> {
                        resetData()
                        true
                    }
                }
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
            override fun onTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent) {}
        })

        binding.rcvViewCreation.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(
                recyclerView: RecyclerView, motionEvent: MotionEvent
            ): Boolean {
                return when {
                    motionEvent.action != MotionEvent.ACTION_UP || recyclerView.findChildViewUnder(
                        motionEvent.x, motionEvent.y
                    ) != null -> false

                    else -> {
                        resetData()
                        true
                    }
                }
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
            override fun onTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent) {}
        })

    }

    private fun handleLongClick(position: Int) {
//        viewModel.showLongClick(position)
//        handleSelectList(false)
    }

    private fun handleEditClick(pathInternal: String) {
//        lifecycleScope.launch(Dispatchers.IO) {
//            showLoading()
//            viewModel.editItem(this@MyCreationActivity, pathInternal, dataViewModel.allData.value)
//            withContext(Dispatchers.Main) {
//                if (viewModel.isApi && dataViewModel.allData.value.size <= ValueKey.POSITION_API && InternetHelper.isInternetAvailable(
//                        this@MyCreationActivity
//                    )
//                ) {
//                    viewModel.isCallData = true
//                    viewModel.pathImageSelect = pathInternal
//                    dataViewModel.ensureData(this@MyCreationActivity)
//                    return@withContext
//                } else if (viewModel.isApi && dataViewModel.allData.value.size <= ValueKey.POSITION_API && !InternetHelper.isInternetAvailable(
//                        this@MyCreationActivity
//                    )
//                ) {
//                    dismissLoading(true)
//                    showToast(R.string.please_check_your_internet)
//                    return@withContext
//                }
//                dismissLoading(true)
//                viewModel.checkDataInternet(this@MyCreationActivity) {
//                    val intent = Intent(this@MyCreationActivity, CustomizeActivity::class.java)
//                    intent.putExtra(IntentKey.INTENT_KEY, viewModel.positionCharacter)
//                    intent.putExtra(IntentKey.STATUS_FROM_KEY, ValueKey.EDIT)
//                    val option = ActivityOptions.makeCustomAnimation(
//                        this@MyCreationActivity, R.anim.slide_in_right, R.anim.slide_out_left
//                    )
//                    showInterAll { startActivity(intent, option.toBundle()) }
//                }
//            }
//        }
    }

    private fun handleDelete(pathList: ArrayList<String> = arrayListOf()) {

//        val pathInternalList = if (pathList.isEmpty()) viewModel.getPathSelected() else pathList
//        if (pathInternalList.isEmpty()) {
//            showToast(R.string.please_select_an_image)
//            return
//        }
//        val dialog = ConfirmDialog(this, R.string.delete, R.string.do_you_want_to_delete)
//        LanguageHelper.setLocale(this)
//        dialog.show()
//        dialog.onNoClick = {
//            dialog.dismiss()
//            hideNavigation(true)
//        }
//        dialog.onYesClick = {
//            lifecycleScope.launch(Dispatchers.IO) {
//                viewModel.deleteItem(this@MyCreationActivity, pathInternalList)
//                withContext(Dispatchers.Main) {
//                    dialog.dismiss()
//                    hideNavigation(true)
//                    resetData()
//                }
//            }
//        }
    }

    private fun handleShare() {
//        val pathInternalList = viewModel.getPathSelected()
//        if (pathInternalList.isEmpty()) {
//            showToast(R.string.please_select_an_image)
//            return
//        }
//        shareImagesPaths(pathInternalList)
    }

    private fun handleItemClick(pathImage: String, type: Int) {
//        val intent = Intent(this, ViewActivity::class.java)
//        intent.putExtra(IntentKey.INTENT_KEY, pathImage)
//        intent.putExtra(IntentKey.TYPE_KEY, ValueKey.TYPE_VIEW)
//        intent.putExtra(IntentKey.STATUS_KEY, type)
//        val option = ActivityOptions.makeCustomAnimation(
//            this@MyCreationActivity, R.anim.slide_in_right, R.anim.slide_out_left
//        )
//        showInterAll { startActivity(intent, option.toBundle()) }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            handleDownload()
        } else {
            val perms = permissionViewModel.getStoragePermissions()
            if (checkPermissions(perms)) {
                handleDownload()
            } else if (permissionViewModel.needGoToSettings(sharePreference, true)) {
                goToSettings()
            } else {
                requestPermission(perms, RequestKey.STORAGE_PERMISSION_CODE)
            }
        }
    }

    private fun handleDownload() {
//        val pathInternalList = viewModel.getPathSelected()
//        if (pathInternalList.isEmpty()) {
//            showToast(R.string.please_select_an_image)
//            return
//        }
//        viewModel.downloadFiles(this)
    }

    private fun setupIsLongClick(isShow: Boolean) {
        binding.apply {
            lnlBottomTop.isVisible = viewModel.isEditState()

            if (!isShow) {
                actionBar.apply {
                    btnActionBarRight.invisible()
                    btnActionBarNextToRight.invisible()
                }

                if (viewModel.isEditState()) {
                    lnlDownShare.visible()

                    if (!viewModel.editListIsEmpty()) {
                        lnlBottom.visible()
                    } else {
                        lnlBottom.gone()
                    }
                } else {
                    lnlBottom.gone()
                }
            } else {
                actionBar.apply {
                    btnActionBarRight.visible()
                    btnActionBarNextToRight.visible()
                }

                lnlDownShare.visible()
                lnlBottom.visible()
            }
        }

    }

    // Observable
    //==================================================================================================================
    private fun setupTypeSelected(type: Int) {
        binding.apply {
            if (type != -1) {

                val strokeWith = UnitHelper.dpToPx(this@MyCreationActivity, 2f)

                if (type == ValueKey.EDIT_CREATION) {
                    rcvEditCreation.visible()
                    rcvViewCreation.gone()

                    btnEditCreation.setBackgroundResource(R.drawable.bg_100_button_focus_app_medium)

                    tvEditCreation.apply {
                        setTextColor(getColor(R.color.white))
                        setStroke(strokeWith, getColor(R.color.green_003B50))
                    }

                    btnViewCreation.setBackgroundResource(R.drawable.bg_100_button_unfocus_app_medium)

                    tvViewCreation.apply {
                        setTextColor(getColor(R.color.green_003B50))
                        setStroke(strokeWith, getColor(R.color.transparent))
                    }

                    layoutNoItem.isVisible = viewModel.editList.value.isEmpty()
                } else {
                    rcvEditCreation.visible()
                    rcvViewCreation.gone()

                    btnEditCreation.setBackgroundResource(R.drawable.bg_100_button_unfocus_app_medium)

                    tvEditCreation.apply {
                        setTextColor(getColor(R.color.green_003B50))
                        setStroke(strokeWith, getColor(R.color.transparent))
                    }

                    btnViewCreation.setBackgroundResource(R.drawable.bg_100_button_focus_app_medium)

                    tvViewCreation.apply {
                        setTextColor(getColor(R.color.white))
                        setStroke(strokeWith, getColor(R.color.green_003B50))
                    }

                    layoutNoItem.isVisible = viewModel.viewList.value.isEmpty()
                }
                resetData()
            }
        }
    }

    private suspend fun handleDownloadState(state: HandleState) {
//        when (state) {
//            HandleState.LOADING -> {
//                showLoading()
//            }
//
//            HandleState.SUCCESS -> {
//                dismissLoading(true)
//                showToast(R.string.download_success)
//            }
//
//            else -> {
//                dismissLoading(true)
//                showToast(R.string.download_failed_please_try_again_later)
//            }
//        }
    }

    private fun setupEditCreationType(list: List<MyCreationModel>) {
        editAdapter.submitList(list)

        binding.layoutNoItem.isVisible = list.isEmpty() && viewModel.isEditState()

        lifecycleScope.launch { dismissLoading() }
    }

    private fun setupViewType(list: List<MyCreationModel>) {
        viewAdapter.submitList(list)

        binding.layoutNoItem.isVisible = list.isEmpty() && !viewModel.isEditState()
    }

    private fun setupGetData(list: ArrayList<CustomizeModel>) {
//        if (list.isNotEmpty()) {
//            if (!viewModel.isCallData) {
//                if (viewModel.typeSelected.value != -1) {
//                    viewModel.setTypeStatus(viewModel.typeSelected.value)
//                } else {
//                    viewModel.setTypeStatus(ValueKey.MY_CHARACTER)
//                }
//            } else {
//                handleEditClick(viewModel.pathImageSelect)
//            }
//        }
    }

    // Result + Permission
    //==================================================================================================================
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RequestKey.STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                permissionViewModel.updateStorageGranted(sharePreference, true)
                handleDownload()
            } else {
                permissionViewModel.updateStorageGranted(sharePreference, false)
            }
        }
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        startIntentWithClearTop(HomeActivity::class.java)
    }

    override fun onRestart() {
        super.onRestart()
        initNativeCollab()
    }

    // Ads
    //==================================================================================================================
    fun initNativeCollab() {
//        Admob.getInstance().loadNativeCollapNotBanner(this, getString(R.string.native_collap_my_baby), binding.flNativeCollab)
    }

    override fun initAds() {
//        initNativeCollab()
//        Admob.getInstance().loadNativeAd(
//            this, getString(R.string.native_my_baby), binding.nativeAds, R.layout.ads_native_banner
//        )
    }
}