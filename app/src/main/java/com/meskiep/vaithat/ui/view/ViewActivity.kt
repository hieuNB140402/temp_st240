package com.meskiep.vaithat.ui.view


import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseActivity
import com.meskiep.vaithat.core.extension.animateZoom
import com.meskiep.vaithat.core.extension.animateZoomIn
import com.meskiep.vaithat.core.extension.checkPermissions
import com.meskiep.vaithat.core.extension.dLog
import com.meskiep.vaithat.core.extension.goToSettings
import com.meskiep.vaithat.core.extension.gone
import com.meskiep.vaithat.core.extension.handleBackLeftToRight
import com.meskiep.vaithat.core.extension.invisible
import com.meskiep.vaithat.core.extension.launchIO
import com.meskiep.vaithat.core.extension.loadImage
import com.meskiep.vaithat.core.extension.loadImageFromFile
import com.meskiep.vaithat.core.extension.margin
import com.meskiep.vaithat.core.extension.requestPermission
import com.meskiep.vaithat.core.extension.setImageWithOption
import com.meskiep.vaithat.core.extension.setTextWithOption
import com.meskiep.vaithat.core.extension.shareImagesPaths
import com.meskiep.vaithat.core.extension.showErrorDialog
import com.meskiep.vaithat.core.extension.startIntentRightToLeft
import com.meskiep.vaithat.core.extension.startIntentWithClearTop
import com.meskiep.vaithat.core.extension.strings
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.core.extension.visible
import com.meskiep.vaithat.core.helper.MediaHelper
import com.meskiep.vaithat.core.utils.key.IntentKey
import com.meskiep.vaithat.core.utils.key.RequestKey
import com.meskiep.vaithat.core.utils.key.ValueKey
import com.meskiep.vaithat.core.utils.state.HandleState
import com.meskiep.vaithat.data.app.DataViewModel
import com.meskiep.vaithat.data.model.custom.CustomizeModel
import com.meskiep.vaithat.databinding.ActivityViewBinding
import com.meskiep.vaithat.dialog.ConfirmDialog
import com.meskiep.vaithat.ui.home.HomeActivity
import com.meskiep.vaithat.ui.my_creation.MyCreationActivity
import com.meskiep.vaithat.ui.permission.PermissionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.isNotEmpty
import kotlin.jvm.java

@AndroidEntryPoint
class ViewActivity : BaseActivity<ActivityViewBinding>() {
    private val viewModel: ViewViewModel by viewModels()

    //    private val myAlbumViewModel: MyCreationViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()

    override fun setViewBinding(): ActivityViewBinding {
        return ActivityViewBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
//        dataViewModel.ensureData(this)
        viewModel.setImagePath(intent.getStringExtra(IntentKey.PATH_KEY) ?: "")
        viewModel.setTypeView(intent.getIntExtra(IntentKey.VIEW_TYPE_KEY, ValueKey.VIEW_TYPE))
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            launch { viewModel.typeView.collect { status -> setupTypeView(status) } }
            launch { viewModel.imagePath.collect { path -> loadImageByPath(path) } }
//            launch { dataViewModel.allData.collect { list -> setupGetData(list) } }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnActionBarLeft.tap { handleExit() }
                btnActionBarNextToRight.tap(2000) { handleNextToRightTop() }
                btnActionBarRight.tap(2000) { handleRightTop() }
            }
            bottomBar.apply {
                btnLeft.tap(2000) { handleLeftBot() }
                btnRight.tap(2000) { checkStoragePermission() }
            }
        }
    }

    // Init
    //==================================================================================================================
    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarLeft.setImageWithOption(R.drawable.ic_back)
        }
    }

    // Handle
    //==================================================================================================================
    private fun setupViewUI() {
        binding.apply {
            initNativeCollab()

            viewModel.updateStatusView(intent.getIntExtra(IntentKey.MY_CREATION_VIEW_KEY, ValueKey.EDIT_CREATION))
            actionBar.apply {
                btnActionBarRight.setImageWithOption(R.drawable.ic_view_delete)

                if (viewModel.statusView == ValueKey.EDIT_CREATION) {
                    btnActionBarNextToRight.setImageWithOption(R.drawable.ic_view_edit)
                }
            }

            flBottom.margin("bottom", 26)
            vBlur.visible()

            bottomBar.apply {
                btnLeft.setTextWithOption(strings(R.string.share))
                btnRight.setTextWithOption(strings(R.string.download))
            }
        }
    }

    private fun setupSuccessUI() {
        binding.apply {
            initNativeCollab()

            actionBar.apply {
                btnActionBarRight.setImageWithOption(R.drawable.ic_view_share)
                btnActionBarNextToRight.setImageWithOption(R.drawable.ic_view_edit)

            }

            flBottom.margin("bottom", 160)
            vBlur.gone()

            bottomBar.apply {
                btnLeft.setTextWithOption(strings(R.string.my_creation))
                btnRight.setTextWithOption(strings(R.string.download))
            }
        }
    }

    private fun handleExit() {
        if (viewModel.typeView.value == ValueKey.VIEW_TYPE) {
            handleBackLeftToRight()
        } else {
            handleBackLeftToRight()
        }
    }

    private fun handleRightTop() {
        if (viewModel.typeView.value == ValueKey.VIEW_TYPE) {
            handleDelete()
        } else {
            handleShare()
        }
    }

    private fun handleNextToRightTop() {
        if (viewModel.typeView.value == ValueKey.VIEW_TYPE) {
            handleEdit()
        } else {
            startIntentWithClearTop(HomeActivity::class.java)
        }
    }

    private fun handleShare() {
        shareImagesPaths(arrayListOf(viewModel.imagePath.value))
    }

    private fun handleDelete() {
        val dialogDelete = ConfirmDialog(this@ViewActivity, R.string.delete, R.string.are_you_sure_want_to_delete_this_item)
        dialogDelete.show()

        dialogDelete.onYesClick = {
            launchIO(
                blockIO = { viewModel.deleteItem() },
                blockMain = { handleBackLeftToRight() }
            )
        }
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
        lifecycleScope.launch {
            MediaHelper.downloadPartsToExternal(this@ViewActivity, arrayListOf(viewModel.imagePath.value)).collect { state ->
                when (state) {
                    HandleState.LOADING -> showLoading()
                    HandleState.SUCCESS -> {
                        dismissLoading()

                        withContext(Dispatchers.Main) {
                            binding.tvDownloadSuccess.animateZoom(1f, 300) {
                                lifecycleScope.launch {
                                    delay(500)
                                    binding.tvDownloadSuccess.animateZoomIn(1f, 0f, 300) {
                                        binding.tvDownloadSuccess.invisible()
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        dismissLoading()

                        withContext(Dispatchers.Main) {
                            showErrorDialog()
                        }
                    }

                }
            }
        }

    }

    private fun handleLeftBot() {
        binding.apply {
            if (viewModel.typeView.value == ValueKey.VIEW_TYPE) {
                handleShare()
            } else {
                startIntentRightToLeft(MyCreationActivity::class.java)
            }
        }
    }

    private fun handleEdit() {
//        lifecycleScope.launch(Dispatchers.IO) {
//            showLoading()
//            myAlbumViewModel.editItem(this@ViewActivity, viewModel.imagePath.value, dataViewModel.allData.value)
//            withContext(Dispatchers.Main) {
//                if (myAlbumViewModel.isApi && dataViewModel.allData.value.size <= ValueKey.POSITION_API && InternetHelper.isInternetAvailable(
//                        this@ViewActivity
//                    )
//                ) {
//                    myAlbumViewModel.isCallData = true
//                    myAlbumViewModel.pathImageSelect = viewModel.imagePath.value
//                    dataViewModel.ensureData(this@ViewActivity)
//                    return@withContext
//                } else if (myAlbumViewModel.isApi && dataViewModel.allData.value.size <= ValueKey.POSITION_API && !InternetHelper.isInternetAvailable(
//                        this@ViewActivity
//                    )
//                ) {
//                    showToast(R.string.please_check_your_internet)
//                    dismissLoading(true)
//                    return@withContext
//                }
//                dismissLoading(true)
//                myAlbumViewModel.checkDataInternet(this@ViewActivity) {
//                    val intent = Intent(this@ViewActivity, CustomizeActivity::class.java)
//                    intent.putExtra(IntentKey.INTENT_KEY, myAlbumViewModel.positionCharacter)
//                    intent.putExtra(IntentKey.STATUS_FROM_KEY, ValueKey.EDIT)
//                    val option = ActivityOptions.makeCustomAnimation(
//                        this@ViewActivity, R.anim.slide_in_right, R.anim.slide_out_left
//                    )
//                    showInterAll { startActivity(intent, option.toBundle()) }
//                }
//            }
//        }
    }

    // Observable
    //==================================================================================================================
    fun setupTypeView(status: Int) {
        when (status) {
            ValueKey.VIEW_TYPE -> setupViewUI()
            ValueKey.SUCCESS_TYPE -> setupSuccessUI()
            else -> {}
        }
    }

    fun loadImageByPath(path: String) {
        dLog("loadImageByPath: ${path}")
        if (path == "") return

        loadImage(path, binding.imvImage, false)
    }

    private fun setupGetData(list: ArrayList<CustomizeModel>) {
//        if (list.isNotEmpty()) {
//            if (myAlbumViewModel.isCallData) {
//                handleEdit()
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

    override fun onRestart() {
        super.onRestart()
//        if (viewModel.typeView.value == ValueKey.TYPE_VIEW && viewModel.statusView == ValueKey.MY_CHARACTER) {
//            loadImage()
//        }
    }

    // Ads
    //==================================================================================================================
    private fun initNativeCollab() {

//        loadNativeCollabAds(
//            getString(if (viewModel.typeView.value == ValueKey.TYPE_SUCCESS) R.string.native_collap_success else R.string.native_collap_view),
//            binding.flNativeCollab
//        )
    }
}