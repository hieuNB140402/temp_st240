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
import com.meskiep.vaithat.core.extension.checkInternet
import com.meskiep.vaithat.core.extension.checkPermissions
import com.meskiep.vaithat.core.extension.eLog
import com.meskiep.vaithat.core.extension.goToSettings
import com.meskiep.vaithat.core.extension.gone
import com.meskiep.vaithat.core.extension.invisible
import com.meskiep.vaithat.core.extension.launchIO
import com.meskiep.vaithat.core.extension.margin
import com.meskiep.vaithat.core.extension.requestPermission
import com.meskiep.vaithat.core.extension.select
import com.meskiep.vaithat.core.extension.setImageWithOption
import com.meskiep.vaithat.core.extension.setTextWithOption
import com.meskiep.vaithat.core.extension.shareImagesPaths
import com.meskiep.vaithat.core.extension.startIntentWithClearTop
import com.meskiep.vaithat.core.extension.strings
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.core.extension.visible
import com.meskiep.vaithat.core.helper.AnimationHelper
import com.meskiep.vaithat.core.helper.UnitHelper
import com.meskiep.vaithat.core.share.whatsapp.WhatsappSharingActivity
import com.meskiep.vaithat.core.utils.key.IntentKey
import com.meskiep.vaithat.core.utils.key.RequestKey
import com.meskiep.vaithat.core.utils.key.ValueKey
import com.meskiep.vaithat.core.utils.state.DeleteState
import com.meskiep.vaithat.core.utils.state.HandleState
import com.meskiep.vaithat.core.utils.state.ShareState
import com.meskiep.vaithat.data.model.MyCreationModel
import com.meskiep.vaithat.databinding.ActivityMyCreationBinding
import com.meskiep.vaithat.dialog.ConfirmDialog
import com.meskiep.vaithat.dialog.CreateNameDialog
import com.meskiep.vaithat.ui.customize.CustomizeActivity
import com.meskiep.vaithat.ui.home.HomeActivity
import com.meskiep.vaithat.ui.permission.PermissionViewModel
import com.meskiep.vaithat.ui.view.ViewActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.collections.isNotEmpty
import kotlin.jvm.java

@AndroidEntryPoint
class MyCreationActivity : WhatsappSharingActivity<ActivityMyCreationBinding>() {
    private val viewModel: MyCreationViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()
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
                launch { viewModel.editList.collect { list -> setupEditCreationType(list) } }
                launch { viewModel.viewList.collect { list -> setupViewType(list) } }

                launch { viewModel.downloadState.collect { state -> handleDownloadState(state) } }
                launch { viewModel.isShowSelection.collect { status -> setupIsLongClick(status) } }

                launch { viewModel.isLastItem.collect { isLastItem -> changeImageActionBarRight(isLastItem) } }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnActionBarLeft.tap { startIntentWithClearTop(HomeActivity::class.java) }
                btnActionBarRight.tap { viewModel.handleSelectAll() }
                btnActionBarNextToRight.tap { handleDelete() }
            }
            bottomBar.apply {
                btnLeft.tap(2000) { handlePrepareAnotherAction(ValueKey.SHARE_ANOTHER_APP) }
                btnRight.tap { handlePrepareAnotherAction(ValueKey.DOWNLOAD_TO_EXTERNAL) }
            }
            btnEditCreation.tap { viewModel.setTypeStatus(ValueKey.EDIT_CREATION) }
            btnViewCreation.tap { viewModel.setTypeStatus(ValueKey.VIEW_CREATION) }

            btnTelegram.tap { handlePrepareAnotherAction(ValueKey.ADD_TELEGRAM) }
            btnWhatsapp.tap { handlePrepareAnotherAction(ValueKey.ADD_WHATSAPP) }
        }
        handleRcv()
    }

    // Init
    //==================================================================================================================
    override fun initActionBar() {
        binding.actionBar.apply {

            btnActionBarLeft.setImageWithOption(R.drawable.ic_back)
            tvCenter.setTextWithOption(strings(R.string.my_creation))

            btnActionBarNextToRight.setImageResource(R.drawable.ic_my_creation_delete)
            btnActionBarRight.setImageResource(R.drawable.ic_my_creation_unselect_all)

            btnActionBarNextToRight.margin("right", -6)
            tvCenter.margin("horizontal", 32)
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
    private fun handleDelete(thumbPath: String = "") {
        val dialog = ConfirmDialog(this, R.string.delete, R.string.are_you_sure_want_to_delete_this_item)
        dialog.show()
        dialog.onYesClick = {
            launchIO(
                blockIO = { viewModel.deleteMyCreation(this, thumbPath) },
                blockMain = { state ->
                    when (state) {
                        DeleteState.Empty -> showToast(R.string.please_select_an_item)
                        DeleteState.Success -> viewModel.resetGetMyCreation(this)
                        is DeleteState.Failure -> eLog("handleDelete: ${state.error}")
                    }
                }
            )
        }
    }

    fun changeImageActionBarRight(isLastItem: Boolean) {
        val res = if (isLastItem) R.drawable.ic_my_creation_selected_all else R.drawable.ic_my_creation_unselect_all
        binding.actionBar.btnActionBarRight.setImageResource(res)
    }

    private fun resetData() {
        viewModel.resetGetMyCreation(this)
    }

    private fun handlePrepareAnotherAction(typeShare: Int) {
        launchIO(
            blockIO = { viewModel.getItemSelectedState() },
            blockMain = { state ->
                when (state) {
                    ShareState.Empty -> showToast(R.string.please_select_an_item)
                    is ShareState.Success -> {
                        when (typeShare) {
                            ValueKey.SHARE_ANOTHER_APP -> shareImagesPaths(state.thumbPathList)
                            ValueKey.ADD_TELEGRAM -> viewModel.addToTelegram(this, state.thumbPathList)
                            ValueKey.ADD_WHATSAPP -> handleAddToWhatsApp(state.thumbPathList)
                            ValueKey.DOWNLOAD_TO_EXTERNAL -> checkStoragePermission(state.thumbPathList)
                        }
                    }
                }
            }
        )
    }

    private fun handleAddToWhatsApp(thumbPathList: List<String>) {

        if (thumbPathList.size < 3) {
            showToast(R.string.at_least_3_photos_are_needed)
            return
        }

        if (thumbPathList.size > 30) {
            showToast(R.string.maximum_30_images)
            return
        }

        val dialog = CreateNameDialog(this)
        dialog.show()

        dialog.onYesClick = { packageName ->
            viewModel.addToWhatsapp(this, packageName, thumbPathList) { stickerPack ->
                if (stickerPack != null) {
                    addToWhatsapp(stickerPack) {
                        resetData()
                    }
                }
            }
        }
    }

    private fun handleRcv() {
        editAdapter.apply {
            onItemClick = { model -> handleItemClick(model.thumbPath, ValueKey.EDIT_CREATION) }
            onItemSelectClick = { position -> viewModel.touchSelectMyCreation(position) }
            onItemEditClick = { model -> checkInternet { handleEditClick(model.thumbPath) } }
            onItemDeleteClick = { model -> handleDelete(model.thumbPath) }
            onItemLongClick = { position -> viewModel.showSelectMyCreation(position) }
        }

        viewAdapter.apply {
            onItemClick = { model -> handleItemClick(model.thumbPath, ValueKey.VIEW_CREATION) }
            onItemSelectClick = { position -> viewModel.touchSelectMyCreation(position) }
            onItemDeleteClick = { model -> handleDelete(model.thumbPath) }
            onItemLongClick = { position -> viewModel.showSelectMyCreation(position) }
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

    private fun handleEditClick(thumbPath: String) {
        launchIO(
            blockIO = { viewModel.getDataNameAndFileNameInternalByThumbPath(thumbPath) },
            blockMain = { dataName, fileNameInternal ->
                val intent = Intent(this@MyCreationActivity, CustomizeActivity::class.java)
                intent.apply {
                    intent.putExtra(IntentKey.AVATAR_NAME_KEY, dataName)
                    intent.putExtra(IntentKey.CUSTOM_STATUS_PLAY_KEY, ValueKey.EDIT)
                    intent.putExtra(IntentKey.PATH_EDIT_KEY, fileNameInternal)
                }
                val option = AnimationHelper.intentAnimRL(this)
                startActivity(intent, option.toBundle())
            }
        )
    }

    private fun handleItemClick(pathImage: String, type: Int) {
        val intent = Intent(this, ViewActivity::class.java)
        intent.apply {
            putExtra(IntentKey.PATH_KEY, pathImage)
            putExtra(IntentKey.VIEW_TYPE_KEY, ValueKey.VIEW_TYPE)
            putExtra(IntentKey.MY_CREATION_VIEW_KEY, type)
        }
        val option = AnimationHelper.intentAnimRL(this)
        startActivity(intent, option.toBundle())
    }

    private fun checkStoragePermission(thumbPathList: List<String> = emptyList()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            handleDownload()
        } else {
            val perms = permissionViewModel.getStoragePermissions()
            if (checkPermissions(perms)) {
                handleDownload(thumbPathList)
            } else if (permissionViewModel.needGoToSettings(sharePreference, true)) {
                goToSettings()
            } else {
                requestPermission(perms, RequestKey.STORAGE_PERMISSION_CODE)
            }
        }
    }

    private fun handleDownload(thumbPathList: List<String> = emptyList()) {
        viewModel.downloadThumbPathToExternal(this, thumbPathList)
    }


    // Observable
    //==================================================================================================================
    private fun setupTypeSelected(type: Int) {
        if (type == -1) return

        binding.apply {
            val strokeWith = UnitHelper.dpToPx(this@MyCreationActivity, 2f)
            val backgroundSelected = R.drawable.bg_100_button_focus_app_medium
            val backgroundUnselect = R.drawable.bg_100_button_unfocus_app_medium

            val textAndStrokeColorSelected = R.color.white
            val textAndStrokeColorUnselect = R.color.green_003B50

            val strokeColorUnselect = R.color.transparent

            if (type == ValueKey.EDIT_CREATION) {
                rcvEditCreation.visible()
                rcvViewCreation.gone()

                btnEditCreation.setBackgroundResource(backgroundSelected)

                tvEditCreation.apply {
                    setTextColor(getColor(textAndStrokeColorSelected))
                    setStroke(strokeWith, getColor(textAndStrokeColorUnselect))
                }

                btnViewCreation.setBackgroundResource(backgroundUnselect)

                tvViewCreation.apply {
                    setTextColor(getColor(textAndStrokeColorUnselect))
                    setStroke(strokeWith, getColor(strokeColorUnselect))
                }

                layoutNoItem.isVisible = viewModel.editListIsEmpty()
            } else {
                rcvEditCreation.gone()
                rcvViewCreation.visible()

                btnEditCreation.setBackgroundResource(backgroundUnselect)

                tvEditCreation.apply {
                    setTextColor(getColor(textAndStrokeColorUnselect))
                    setStroke(strokeWith, getColor(strokeColorUnselect))
                }

                btnViewCreation.setBackgroundResource(backgroundSelected)

                tvViewCreation.apply {
                    setTextColor(getColor(textAndStrokeColorSelected))
                    setStroke(strokeWith, getColor(textAndStrokeColorUnselect))
                }

                layoutNoItem.isVisible = viewModel.viewListIsEmpty()
            }
            resetData()
        }
    }

    private suspend fun handleDownloadState(state: HandleState) {
        when (state) {
            HandleState.LOADING -> showLoading()

            HandleState.SUCCESS -> {
                dismissLoading()
                showToast(R.string.download_success)
            }

            else -> {
                dismissLoading()
                showToast(R.string.an_error_occurred)
            }
        }
    }

    private fun setupEditCreationType(list: List<MyCreationModel>) {
        editAdapter.submitList(list)



        lifecycleScope.launch {
            dismissLoading()
            binding.layoutNoItem.isVisible = list.isEmpty() && viewModel.isEditState()
        }
    }

    private fun setupViewType(list: List<MyCreationModel>) {
        viewAdapter.submitList(list)

        binding.layoutNoItem.isVisible = list.isEmpty() && !viewModel.isEditState()
    }

    private fun setupIsLongClick(isShow: Boolean) {
        binding.apply {
            lnlBottomTop.isVisible = viewModel.isEditState()
            imvBlur.visible()

            if (!isShow) {
                actionBar.apply {
                    btnActionBarRight.invisible()
                    btnActionBarNextToRight.invisible()
                }
                lnlDownShare.gone()

                if (viewModel.isEditState()) {
                    if (viewModel.editListIsEmpty()) {
                        lnlBottomTop.gone()
                        imvBlur.gone()
                    }
                } else {
                    imvBlur.gone()
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
        viewModel.resetGetMyCreation(this)
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