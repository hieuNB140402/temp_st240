package com.meskiep.vaithat.ui.add_character

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.create.babycute.babymaker.R
import com.create.babycute.babymaker.core.base.BaseActivity
import com.create.babycute.babymaker.core.extension.gone
import com.create.babycute.babymaker.core.extension.hideNavigation
import com.create.babycute.babymaker.core.extension.hideSoftKeyboard
import com.create.babycute.babymaker.core.extension.loadImage
import com.create.babycute.babymaker.core.extension.openImagePicker
import com.create.babycute.babymaker.core.extension.setFont
import com.create.babycute.babymaker.core.extension.setImageActionBar
import com.create.babycute.babymaker.core.extension.showInterAll
import com.create.babycute.babymaker.core.extension.tap
import com.create.babycute.babymaker.core.extension.visible
import com.create.babycute.babymaker.core.helper.BitmapHelper
import com.create.babycute.babymaker.core.helper.UnitHelper
import com.create.babycute.babymaker.core.utils.DataLocal
import com.create.babycute.babymaker.core.utils.key.IntentKey
import com.create.babycute.babymaker.core.utils.key.RequestKey
import com.create.babycute.babymaker.core.utils.key.ValueKey
import com.create.babycute.babymaker.core.utils.state.SaveState
import com.create.babycute.babymaker.data.model.draw.Draw
import com.create.babycute.babymaker.data.model.draw.DrawableDraw
import com.create.babycute.babymaker.databinding.ActivityAddCharacterBinding
import com.create.babycute.babymaker.dialog.ChooseColorDialog
import com.create.babycute.babymaker.dialog.ConfirmDialog
import com.create.babycute.babymaker.dialog.DialogSpeech
import com.create.babycute.babymaker.listener.listenerdraw.OnDrawListener
import com.create.babycute.babymaker.ui.add_character.AddCharacterViewModel
import com.meskiep.vaithat.ui.add_character.adapter.AddCharacterBackgroundColorAdapter
import com.meskiep.vaithat.ui.add_character.adapter.AddCharacterBackgroundImageAdapter
import com.meskiep.vaithat.ui.add_character.adapter.AddCharacterSpeechAdapter
import com.meskiep.vaithat.ui.add_character.adapter.AddCharacterStickerAdapter
import com.meskiep.vaithat.ui.add_character.adapter.AddCharacterTextColorAdapter
import com.meskiep.vaithat.ui.add_character.adapter.AddCharacterTextFontAdapter
import com.create.babycute.babymaker.ui.permission.PermissionViewModel
import com.create.babycute.babymaker.ui.view.ViewActivity
import com.lvt.ads.util.Admob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.collections.forEachIndexed
import kotlin.collections.get
import kotlin.getValue
import kotlin.text.get

class AddCharacterActivity : BaseActivity<ActivityAddCharacterBinding>() {
    private val viewModel: AddCharacterViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()
    private val backgroundImageAdapter by lazy { AddCharacterBackgroundImageAdapter() }
    private val backgroundColorAdapter by lazy { AddCharacterBackgroundColorAdapter() }
    private val stickerAdapter by lazy { AddCharacterStickerAdapter() }
    private val speechAdapter by lazy { AddCharacterSpeechAdapter() }
    private val textFontAdapter by lazy { AddCharacterTextFontAdapter(this) }
    private val textColorAdapter by lazy { AddCharacterTextColorAdapter() }
    private val buttonNavigationList by lazy {
        arrayListOf(
            binding.btnBackground,
            binding.btnSticker,
            binding.btnSpeech,
            binding.btnText,
        )
    }
    private val layoutNavigationList by lazy {
        arrayListOf(
            binding.lnlBackground,
            binding.lnlSticker,
            binding.lnlSpeech,
            binding.lnlText,
        )
    }

    override fun setViewBinding(): ActivityAddCharacterBinding {
        return ActivityAddCharacterBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        viewModel.layoutParams = binding.flFunction.layoutParams as ViewGroup.MarginLayoutParams
        initRcv()
        initDrawView()
        initData()
    }

    override fun dataObservable() {
        binding.apply {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch { viewModel.typeNavigation.collect { type -> setupTypeNavigation(type) } }
                    launch { viewModel.typeBackground.collect { type -> setupTypeBackground(type) } }
                    launch { viewModel.isFocusEditText.collect { status -> setupFocusEditText(status) } }
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnActionBarLeft.tap { confirmExit() }
                btnActionBarCenter.tap { confirmReset() }
                btnActionBarRight.tap { handleSave() }
            }
            btnBackgroundImage.tap { viewModel.setTypeBackground(ValueKey.IMAGE_BACKGROUND) }
            btnBackgroundColor.tap { viewModel.setTypeBackground(ValueKey.COLOR_BACKGROUND) }
            btnBackground.tap { viewModel.setTypeNavigation(ValueKey.BACKGROUND_NAVIGATION) }
            btnSticker.tap { viewModel.setTypeNavigation(ValueKey.STICKER_NAVIGATION) }
            btnSpeech.tap { viewModel.setTypeNavigation(ValueKey.SPEECH_NAVIGATION) }
            btnText.tap { viewModel.setTypeNavigation(ValueKey.TEXT_NAVIGATION) }

            edtText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    binding.tvGetText.text = p0.toString()
                }

                override fun afterTextChanged(p0: Editable?) {}
            })
            edtText.setOnEditorActionListener { textView, i, keyEvent ->
                if (i == EditorInfo.IME_ACTION_DONE) {
                    viewModel.setIsFocusEditText(false)
                    true
                } else {
                    false
                }
            }
            edtText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    viewModel.setIsFocusEditText(true)
                } else {
                    viewModel.setIsFocusEditText(false)
                }
            }
            btnDoneText.tap { handleDoneText() }

            main.tap {
                viewModel.setIsFocusEditText(false)
                clearFocus()
            }

            backgroundImageAdapter.apply {
                onAddImageClick = { openImagePicker() }
                onBackgroundImageClick = { path, position -> handleSetBackgroundImage(path, position) }
            }

            backgroundColorAdapter.apply {
                onChooseColorClick = { handleChooseColor() }
                onBackgroundColorClick = { color, position -> handleSetBackgroundColor(color, position) }
            }

            stickerAdapter.onItemClick = { path -> addDrawable(path) }

            speechAdapter.onItemClick = { path -> handleSpeech(path) }

            textFontAdapter.onTextFontClick = { font, position -> handleFontClick(font, position) }

            textColorAdapter.apply {
                onChooseColorClick = { handleChooseColor(true) }
                onTextColorClick = { color, position -> handleTextColorClick(color, position) }
            }
        }
    }

    // Init
    //==================================================================================================================
    override fun initActionBar() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.ic_back)
            setImageActionBar(btnActionBarCenter, R.drawable.ic_reset)
            setImageActionBar(btnActionBarRight, R.drawable.ic_save)
        }
    }

    private fun initRcv() {
        binding.apply {
            rcvBackgroundImage.apply {
                adapter = backgroundImageAdapter
                itemAnimator = null
            }

            rcvBackgroundColor.apply {
                adapter = backgroundColorAdapter
                itemAnimator = null
            }

            rcvSticker.apply {
                adapter = stickerAdapter
                itemAnimator = null
            }

            rcvSpeech.apply {
                adapter = speechAdapter
                itemAnimator = null
            }

            rcvFont.apply {
                adapter = textFontAdapter
                itemAnimator = null
            }

            rcvTextColor.apply {
                adapter = textColorAdapter
                itemAnimator = null
            }
        }
    }

    private fun initData() {
        lifecycleScope.launch(Dispatchers.IO) {
            showLoading()
            viewModel.loadDataDefault(this@AddCharacterActivity)
            viewModel.updatePathDefault(intent.getStringExtra(IntentKey.INTENT_KEY) ?: "")
            addDrawable(viewModel.pathDefault, true)

            withContext(Dispatchers.Main) {
                viewModel.setTypeNavigation(ValueKey.BACKGROUND_NAVIGATION)
                viewModel.setTypeBackground(ValueKey.IMAGE_BACKGROUND)
                backgroundImageAdapter.submitList(viewModel.backgroundImageList)
                backgroundColorAdapter.submitList(viewModel.backgroundColorList)
                stickerAdapter.submitList(viewModel.stickerList)
                speechAdapter.submitList(viewModel.speechList)
                textFontAdapter.submitListReset(viewModel.textFontList)
                textColorAdapter.submitListReset(viewModel.textColorList)
                dismissLoading(true)
            }
        }
    }

    private fun initDrawView() {
        binding.drawView.apply {
            setConstrained(true)
            setLocked(false)
            setOnDrawListener(object : OnDrawListener {
                override fun onAddedDraw(draw: Draw) {
                    viewModel.updateCurrentCurrentDraw(draw)
                    viewModel.addDrawView(draw)
                    viewModel.setIsFocusEditText(false)
                }

                override fun onClickedDraw(draw: Draw) {
                    viewModel.setIsFocusEditText(false)
                }

                override fun onDeletedDraw(draw: Draw) {
                    viewModel.deleteDrawView(draw)
                    viewModel.setIsFocusEditText(false)
                }

                override fun onDragFinishedDraw(draw: Draw) {
                    viewModel.setIsFocusEditText(false)
                }

                override fun onTouchedDownDraw(draw: Draw) {
                    viewModel.updateCurrentCurrentDraw(draw)
                    viewModel.setIsFocusEditText(false)
                }

                override fun onZoomFinishedDraw(draw: Draw) {}

                override fun onFlippedDraw(draw: Draw) {
                    viewModel.setIsFocusEditText(false)
                }

                override fun onDoubleTappedDraw(draw: Draw) {}

                override fun onHideOptionIconDraw() {}

                override fun onUndoDeleteDraw(draw: List<Draw?>) {}

                override fun onUndoUpdateDraw(draw: List<Draw?>) {}

                override fun onUndoDeleteAll() {}

                override fun onRedoAll() {}

                override fun onReplaceDraw(draw: Draw) {}

                override fun onEditText(draw: DrawableDraw) {}

                override fun onReplace(draw: Draw) {}
            })
        }
    }


    // Handle
    //==================================================================================================================

    private fun addDrawable(path: String, isCharacter: Boolean = false, bitmapText: Bitmap? = null) {
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmapDefault = if (bitmapText == null) Glide.with(this@AddCharacterActivity).load(path).signature(
                ObjectKey(
                    File(
                        path
                    ).lastModified()
                )
            ).submit().get().toBitmap() else bitmapText
            val drawableEmoji = viewModel.loadDrawableEmoji(this@AddCharacterActivity, bitmapDefault, isCharacter)

            withContext(Dispatchers.Main) {
                drawableEmoji.let { binding.drawView.addDraw(it) }
            }
        }
    }

    private fun confirmExit() {
        viewModel.setIsFocusEditText(false)
        val dialog = ConfirmDialog(this, R.string.exit, R.string.do_you_want_to_exit)
        dialog.show()

        fun dismissDialog() {
            dialog.dismiss()
            hideNavigation(true)
        }
        dialog.onNoClick = {
            dismissDialog()
        }
        dialog.onYesClick = {
            dismissDialog()
            showInterAll { finish() }
        }
    }

    private fun confirmReset() {
        viewModel.setIsFocusEditText(false)
        val dialog = ConfirmDialog(this, R.string.reset, R.string.do_you_want_to_reset)
        dialog.show()

        fun dismissDialog() {
            dialog.dismiss()
            hideNavigation(true)
        }

        dialog.onNoClick = {
            dismissDialog()
        }

        dialog.onYesClick = {
            dismissDialog()
            lifecycleScope.launch {
                showLoading()
                withContext(Dispatchers.IO) {
                    viewModel.loadDataDefault(this@AddCharacterActivity)
                    viewModel.resetDraw()
                }
                binding.drawView.removeAllDraw()
                binding.imvBackground.setImageBitmap(null)
                binding.imvBackground.setBackgroundColor(getColor(R.color.transparent))
                binding.edtText.setText("")
                binding.edtText.setFont(viewModel.textFontList.first().color)
                binding.edtText.setTextColor(viewModel.textColorList[1].color)
                addDrawable(viewModel.pathDefault, true)
                backgroundImageAdapter.submitList(viewModel.backgroundImageList)
                backgroundColorAdapter.submitList(viewModel.backgroundColorList)
                stickerAdapter.submitList(viewModel.stickerList)
                speechAdapter.submitList(viewModel.speechList)
                textFontAdapter.submitListReset(viewModel.textFontList)
                textColorAdapter.submitListReset(viewModel.textColorList)
                dismissLoading(true)
                showInterAll()
            }
        }
    }

    private fun handleSetBackgroundImage(path: String, position: Int) {
        binding.imvBackground.setBackgroundColor(getColor(R.color.transparent))
        loadImage(path, binding.imvBackground, false)
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.updateBackgroundImageSelected(position)
            withContext(Dispatchers.Main) {
                backgroundColorAdapter.resetCurrentSelected()
                backgroundImageAdapter.submitItem(position, viewModel.backgroundImageList)
            }
        }
    }


    private fun handleChooseColor(isTextColor: Boolean = false) {
        val dialog = ChooseColorDialog(this)

        dialog.show()

        fun dismissDialog() {
            dialog.dismiss()
            hideNavigation(true)
        }

        dialog.onCloseEvent = {
            dismissDialog()
        }

        dialog.onDoneEvent = { color ->
            dismissDialog()
            if (!isTextColor) {
                handleSetBackgroundColor(color, 0)
            } else {
                handleTextColorClick(color, 0)
            }
        }
    }

    private fun handleSpeech(path: String) {
        val dialog = DialogSpeech(this, path)
        dialog.show()
        dialog.onDoneClick = { bitmap ->
            dialog.dismiss()
            hideNavigation(true)
            if (bitmap != null) {
                addDrawable("", false, bitmap)
            }
        }
    }

    private fun handleSetBackgroundColor(color: Int, position: Int) {
        binding.apply {
            imvBackground.setImageBitmap(null)
            imvBackground.setBackgroundColor(color)
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.updateBackgroundColorSelected(position)
                withContext(Dispatchers.Main) {
                    backgroundImageAdapter.resetCurrentSelected()
                    backgroundColorAdapter.submitItem(position, viewModel.backgroundColorList)
                }
            }
        }
    }

    private fun handleFontClick(font: Int, position: Int) {
        binding.apply {
            edtText.setFont(font)
            tvGetText.setFont(font)
            viewModel.updateTextFontSelected(position)
            textFontAdapter.submitItem(position, viewModel.textFontList)
        }
    }

    private fun handleTextColorClick(color: Int, position: Int) {
        binding.apply {
            edtText.setTextColor(color)
            tvGetText.setTextColor(color)
            viewModel.updateTextColorSelected(position)
            textColorAdapter.submitItem(position, viewModel.textColorList)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun handleDoneText() {
        viewModel.setIsFocusEditText(false)
        binding.apply {
            if (edtText.text.toString().trim() == "") {
                showToast(getString(R.string.null_edt))
            } else {
                tvGetText.text = edtText.text.toString().trim()
                val bitmap = BitmapHelper.getBitmapFromEditText(tvGetText)
                val drawableEmoji = viewModel.loadDrawableEmoji(this@AddCharacterActivity, bitmap, isText = true)
                binding.drawView.addDraw(drawableEmoji)

                // Reset
                val font = viewModel.textFontList.first().color
                val color = viewModel.textColorList[1].color

                edtText.text = null
                edtText.setFont(font)
                edtText.setTextColor(color)

                viewModel.updateTextFontSelected(0)
                viewModel.updateTextColorSelected(1)

                textFontAdapter.submitListReset(viewModel.textFontList)
                textColorAdapter.submitListReset(viewModel.textColorList)

                tvGetText.text = ""
                tvGetText.setFont(font)
                tvGetText.setTextColor(color)
            }
        }
    }

    private fun clearFocus() {
        binding.drawView.hideSelect()
    }

    private fun handleSave() {
        binding.apply {
            clearFocus()
            lifecycleScope.launch(Dispatchers.IO) {
                showLoading()
                delay(200)
                viewModel.saveImageFromView(this@AddCharacterActivity, flSave).collect { result ->
                    when (result) {
                        is SaveState.Loading -> showLoading()

                        is SaveState.Error -> {
                            dismissLoading(true)
                            withContext(Dispatchers.Main) {
                                showToast(R.string.save_failed_please_try_again)
                            }
                        }

                        is SaveState.Success -> {
                            val intent = Intent(this@AddCharacterActivity, ViewActivity::class.java)
                            intent.putExtra(IntentKey.INTENT_KEY, result.path)
                            intent.putExtra(IntentKey.STATUS_KEY, ValueKey.MY_DESIGN)
                            intent.putExtra(IntentKey.TYPE_KEY, ValueKey.TYPE_SUCCESS)
                            val options = ActivityOptions.makeCustomAnimation(
                                this@AddCharacterActivity, R.anim.slide_in_right, R.anim.slide_out_left
                            )
                            dismissLoading(true)
                            withContext(Dispatchers.Main) {
                                showInterAll { startActivity(intent, options.toBundle()) }
                            }
                        }
                    }
                }
            }
        }
    }

    // Observable
    //==================================================================================================================
    private fun setupTypeNavigation(type: Int) {
        if (type == -1) return
        buttonNavigationList.forEachIndexed { index, button ->
            val (res, status) = if (index == type) {
                DataLocal.bottomNavigationSelected[index] to true
            } else {
                DataLocal.bottomNavigationNotSelect[index] to false
            }

            button.setImageResource(res)
            layoutNavigationList[index].isVisible = status
        }
    }

    private fun setupTypeBackground(type: Int) {
        binding.apply {
            when (type) {
                ValueKey.IMAGE_BACKGROUND -> {
                    rcvBackgroundImage.visible()
                    rcvBackgroundColor.gone()
                    btnBackgroundImage.apply {
                        setBackgroundResource(R.drawable.bg_100_stroke_white_solid_pink)
                    }
                    btnBackgroundColor.apply {
                        setBackgroundResource(R.drawable.bg_100_stroke_white)
                    }
                    backgroundImageAdapter.submitList(viewModel.backgroundImageList)
                }

                ValueKey.COLOR_BACKGROUND -> {
                    rcvBackgroundImage.gone()
                    rcvBackgroundColor.visible()
                    btnBackgroundImage.apply {
                        setBackgroundResource(R.drawable.bg_100_stroke_white)
                    }
                    btnBackgroundColor.apply {
                        setBackgroundResource(R.drawable.bg_100_stroke_white_solid_pink)
                    }
                    backgroundColorAdapter.submitList(viewModel.backgroundColorList)
                }

                else -> return
            }
        }
    }

    private fun setupFocusEditText(status: Boolean) {
        binding.apply {
            if (status) {
                viewModel.layoutParams.topMargin = UnitHelper.dpToPx(this@AddCharacterActivity, -170)
                flFunction.layoutParams = viewModel.layoutParams
            } else {
                viewModel.layoutParams.topMargin = viewModel.originalMarginBottom
                flFunction.layoutParams = viewModel.layoutParams
                hideSoftKeyboard()
                edtText.clearFocus()
                hideNavigation(true)
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
                openImagePicker()
            } else {
                permissionViewModel.updateStorageGranted(sharePreference, false)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestKey.PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            handleSetBackgroundImage(selectedImageUri.toString(), 0)
        }
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        if (viewModel.isFocusEditText.value) {
            viewModel.setIsFocusEditText(false)
        } else {
            confirmExit()
        }
    }

    override fun onRestart() {
        super.onRestart()
        initNativeCollab()
    }

    // Ads
    //==================================================================================================================
    fun initNativeCollab() {
        Admob.getInstance().loadNativeCollapNotBanner(this, getString(R.string.native_collap_bg), binding.flNativeCollab)
    }

    override fun initAds() {
        initNativeCollab()
    }
}