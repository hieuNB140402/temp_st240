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
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseActivity
import com.meskiep.vaithat.core.extension.gone
import com.meskiep.vaithat.core.extension.handleBackLeftToRight
import com.meskiep.vaithat.core.extension.hideNavigation
import com.meskiep.vaithat.core.extension.hideSoftKeyboard
import com.meskiep.vaithat.core.extension.launchIO
import com.meskiep.vaithat.core.extension.loadImage
import com.meskiep.vaithat.core.extension.openImagePicker
import com.meskiep.vaithat.core.extension.setBackgroundWithOption
import com.meskiep.vaithat.core.extension.setFont
import com.meskiep.vaithat.core.extension.setImageWithOption
import com.meskiep.vaithat.core.extension.setTextWithOption
import com.meskiep.vaithat.core.extension.strings
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.core.extension.visible
import com.meskiep.vaithat.core.helper.BitmapHelper
import com.meskiep.vaithat.core.helper.UnitHelper
import com.meskiep.vaithat.core.utils.DataLocal
import com.meskiep.vaithat.core.utils.key.IntentKey
import com.meskiep.vaithat.core.utils.key.RequestKey
import com.meskiep.vaithat.core.utils.key.ValueKey
import com.meskiep.vaithat.core.utils.state.SaveState
import com.meskiep.vaithat.data.model.draw.Draw
import com.meskiep.vaithat.data.model.draw.DrawableDraw
import com.meskiep.vaithat.databinding.ActivityAddCharacterBinding
import com.meskiep.vaithat.dialog.ChooseColorDialog
import com.meskiep.vaithat.dialog.ConfirmDialog
import com.meskiep.vaithat.listener.listenerdraw.OnDrawListener
import com.meskiep.vaithat.ui.add_character.adapter.AddCharacterBackgroundColorAdapter
import com.meskiep.vaithat.ui.add_character.adapter.AddCharacterBackgroundImageAdapter
import com.meskiep.vaithat.ui.add_character.adapter.AddCharacterSpeechAdapter
import com.meskiep.vaithat.ui.add_character.adapter.AddCharacterStickerAdapter
import com.meskiep.vaithat.ui.add_character.adapter.AddCharacterTextColorAdapter
import com.meskiep.vaithat.ui.add_character.adapter.AddCharacterTextFontAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.collections.forEachIndexed
import kotlin.getValue

class AddCharacterActivity : BaseActivity<ActivityAddCharacterBinding>() {
    private val viewModel: AddCharacterViewModel by viewModels()
    private val backgroundImageAdapter by lazy { AddCharacterBackgroundImageAdapter() }
    private val backgroundColorAdapter by lazy { AddCharacterBackgroundColorAdapter() }
    private val stickerAdapter by lazy { AddCharacterStickerAdapter() }
    private val speechAdapter by lazy { AddCharacterSpeechAdapter() }
    private val textFontAdapter by lazy { AddCharacterTextFontAdapter(this) }
    private val textColorAdapter by lazy { AddCharacterTextColorAdapter(this) }
    private val buttonNavigationList by lazy {
        arrayListOf(
            binding.btnBackground,
            binding.btnSticker,
            binding.btnSpeech,
            binding.btnText,
        )
    }

    private val iconNavigationList by lazy {
        arrayListOf(
            binding.imvBackground,
            binding.imvSticker,
            binding.imvSpeech,
            binding.imvText,
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
            btnActionBarLeft.setImageWithOption(R.drawable.ic_back)
            btnActionBarCenter.setImageWithOption(R.drawable.ic_reset)
            btnActionBarRightText.setBackgroundWithOption(R.drawable.bg_focus_very_short)
            tvActionBarRightText.apply {
                setTextWithOption(strings(R.string.save))
                setTextColor(getColor(R.color.white))
                setStroke(
                    UnitHelper.pxToDpFloat(this@AddCharacterActivity, 2f),
                    getColor(R.color.green_003B50)
                )
            }
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
        launchIO(
            blockIO = {
                showLoading()
                viewModel.loadDataDefault(this@AddCharacterActivity)
                viewModel.updatePathDefault(intent.getStringExtra(IntentKey.INTENT_KEY) ?: "")
                addDrawable(viewModel.pathDefault, true)
            },
            blockMain = {
                viewModel.setTypeNavigation(ValueKey.BACKGROUND_NAVIGATION)
                viewModel.setTypeBackground(ValueKey.IMAGE_BACKGROUND)

                submitBackgroundImage()
                submitBackgroundColor()

                stickerAdapter.submitList(viewModel.stickerList)
                speechAdapter.submitList(viewModel.speechList)

                submitTextFont()
                submitTextColor()

                dismissLoading()
            }
        )
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
        launchIO(
            blockIO = {
                if (bitmapText == null) {
                    Glide.with(this@AddCharacterActivity).load(path)
                        .signature(ObjectKey(File(path).lastModified())).submit().get()
                        .toBitmap()
                } else {
                    bitmapText
                }
            },
            blockMain = { bitmap ->
                val drawableEmoji = viewModel.loadDrawableEmoji(this@AddCharacterActivity, bitmap, isCharacter)
                drawableEmoji.let { binding.drawView.addDraw(it) }
            }
        )
    }

    private fun confirmExit() {
        viewModel.setIsFocusEditText(false)
        val dialog = ConfirmDialog(this, R.string.exit, R.string.have_not_saved_it_yet_do_you_want_to_exit)
        dialog.show()

        dialog.onYesClick = {
//            showInterAll { finish() }
            handleBackLeftToRight()
        }
    }

    private fun confirmReset() {
        viewModel.setIsFocusEditText(false)

        val dialog = ConfirmDialog(this, R.string.reset, R.string.do_you_want_to_reset_all)
        dialog.show()

        dialog.onYesClick = {
            launchIO(
                blockIO = {
                    showLoading()
                    viewModel.loadDataDefault(this@AddCharacterActivity)
                    viewModel.resetDraw()
                },
                blockMain = {
                    binding.apply {
                        drawView.removeAllDraw()
                        Glide.with(this@AddCharacterActivity).clear(imvBackgroundExport)
                        imvBackgroundExport.setBackgroundColor(getColor(R.color.transparent))

                        edtText.apply {
                            setText("")
                            setFont(viewModel.getTextFontDefault())
                            setTextColor(viewModel.getTextColorDefault())
                        }

                    }
                    addDrawable(viewModel.pathDefault, true)

                    submitBackgroundImage()
                    submitBackgroundColor()

                    submitTextFont()
                    submitTextColor()

                    dismissLoading()
                }
            )

        }
    }

    private fun handleSetBackgroundImage(path: String, position: Int) {
        launchIO(
            blockIO = { viewModel.updateBackgroundImageSelected(position) },
            blockMain = {
                binding.imvBackgroundExport.setBackgroundColor(getColor(R.color.transparent))
                loadImage(path, binding.imvBackgroundExport, false)

                submitBackgroundImage()
                submitBackgroundColor()
            }
        )
    }

    private fun handleChooseColor(isTextColor: Boolean = false) {
        val dialog = ChooseColorDialog(this)

        dialog.show()

        dialog.onDoneEvent = { color ->
            if (!isTextColor) {
                handleSetBackgroundColor(color, 0)
            } else {
                handleTextColorClick(color, 0)
            }
        }
    }

    private fun handleSpeech(path: String) {
//        val dialog = DialogSpeech(this, path)
//        dialog.show()
//        dialog.onDoneClick = { bitmap ->
//            dialog.dismiss()
//            hideNavigation(true)
//            if (bitmap != null) {
//                addDrawable("", false, bitmap)
//            }
//        }
    }

    private fun handleSetBackgroundColor(color: Int, position: Int) {
        launchIO(
            blockIO = { viewModel.updateBackgroundColorSelected(position) },
            blockMain = {
                binding.apply {
                    Glide.with(this@AddCharacterActivity).clear(imvBackgroundExport)
                    imvBackgroundExport.setBackgroundColor(color)
                }

                submitBackgroundImage()
                submitBackgroundColor()
            }
        )
    }

    private fun handleFontClick(font: Int, position: Int) {
        binding.apply {
            viewModel.updateTextFontSelected(position)

            edtText.setFont(font)
            tvGetText.setFont(font)

            submitTextFont()
        }
    }

    private fun handleTextColorClick(color: Int, position: Int) {
        binding.apply {
            viewModel.updateTextColorSelected(position)

            edtText.setTextColor(color)
            tvGetText.setTextColor(color)

            submitTextColor()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun handleDoneText() {
        viewModel.setIsFocusEditText(false)

        binding.apply {
            if (edtText.text.toString().trim() == "") {
                showToast(R.string.null_edt)
            } else {
                tvGetText.text = edtText.text.toString().trim()
                val bitmap = BitmapHelper.getBitmapFromEditText(tvGetText)
                val drawableEmoji = viewModel.loadDrawableEmoji(this@AddCharacterActivity, bitmap, isText = true)
                binding.drawView.addDraw(drawableEmoji)

                // Reset
                val font = viewModel.getTextFontDefault()
                val color = viewModel.getTextColorDefault()

                edtText.apply {
                    text = null
                    setFont(font)
                    setTextColor(color)
                }

                tvGetText.apply {
                    text = ""
                    setFont(font)
                    setTextColor(color)
                }

                viewModel.updateTextFontSelected(0)
                viewModel.updateTextColorSelected(1)

                submitTextFont()
                submitTextColor()
            }
        }
    }

    private fun clearFocus() {
        binding.drawView.hideSelect()
    }

    private fun handleSave() {
        binding.apply {
//            clearFocus()
//            lifecycleScope.launch(Dispatchers.IO) {
//                showLoading()
//                delay(200)
//                viewModel.saveImageFromView(this@AddCharacterActivity, flSave).collect { result ->
//                    when (result) {
//                        is SaveState.Loading -> showLoading()
//
//                        is SaveState.Error -> {
//                            dismissLoading(true)
//                            withContext(Dispatchers.Main) {
//                                showToast(R.string.save_failed_please_try_again)
//                            }
//                        }
//
//                        is SaveState.Success -> {
//                            val intent = Intent(this@AddCharacterActivity, ViewActivity::class.java)
//                            intent.putExtra(IntentKey.INTENT_KEY, result.path)
//                            intent.putExtra(IntentKey.STATUS_KEY, ValueKey.MY_DESIGN)
//                            intent.putExtra(IntentKey.TYPE_KEY, ValueKey.TYPE_SUCCESS)
//                            val options = ActivityOptions.makeCustomAnimation(
//                                this@AddCharacterActivity, R.anim.slide_in_right, R.anim.slide_out_left
//                            )
//                            dismissLoading(true)
//                            withContext(Dispatchers.Main) {
//                                showInterAll { startActivity(intent, options.toBundle()) }
//                            }
//                        }
//                    }
//                }
//            }
        }
    }

    private fun submitBackgroundImage() {
        backgroundImageAdapter.submitList(viewModel.backgroundImageList)
    }

    private fun submitBackgroundColor() {
        backgroundColorAdapter.submitList(viewModel.backgroundColorList)
    }

    private fun submitTextFont(){
        textFontAdapter.submitList(viewModel.textFontList)
    }

    private fun submitTextColor(){
        textColorAdapter.submitList(viewModel.textColorList)
    }

    // Observable
    //==================================================================================================================
    private fun setupTypeNavigation(type: Int) {
        if (type == -1) return

        iconNavigationList.forEachIndexed { index, button ->
            val (res, status, backgroundColor) = if (index == type) {
                Triple(DataLocal.addCharacterBottomNavigationSelected[index], true, R.color.yellow_FFD364)
            } else {
                Triple(DataLocal.addCharacterBottomNavigationUnselect[index], false, R.color.transparent)
            }

            button.setImageResource(res)

            buttonNavigationList[index].setBackgroundColor(getColor(backgroundColor))
            layoutNavigationList[index].isVisible = status
        }
    }

    private fun setupTypeBackground(type: Int) {
        binding.apply {
            when (type) {
                ValueKey.IMAGE_BACKGROUND -> {
                    rcvBackgroundImage.visible()
                    rcvBackgroundColor.gone()

                    btnBackgroundImage.setBackgroundResource(R.drawable.bg_100_button_focus_app_medium)

                    tvBackgroundImage.apply {
                        setTextColor(getColor(R.color.white))
                        setStroke(UnitHelper.dpToPx(this@AddCharacterActivity, 2f), getColor(R.color.green_003B50))
                    }


                    btnBackgroundColor.setBackgroundResource(R.drawable.bg_100_button_unfocus_app_medium)

                    tvBackgroundColor.apply {
                        setTextColor(getColor(R.color.green_003B50))
                        setStroke(UnitHelper.dpToPx(this@AddCharacterActivity, 2f), getColor(R.color.transparent))
                    }
                    submitBackgroundImage()
                }

                ValueKey.COLOR_BACKGROUND -> {
                    rcvBackgroundImage.gone()
                    rcvBackgroundColor.visible()

                    btnBackgroundImage.setBackgroundResource(R.drawable.bg_100_button_unfocus_app_medium)

                    tvBackgroundImage.apply {
                        setTextColor(getColor(R.color.green_003B50))
                        setStroke(UnitHelper.dpToPx(this@AddCharacterActivity, 2f), getColor(R.color.transparent))
                    }

                    btnBackgroundColor.setBackgroundResource(R.drawable.bg_100_button_focus_app_medium)

                    tvBackgroundColor.apply {
                        setTextColor(getColor(R.color.white))
                        setStroke(UnitHelper.dpToPx(this@AddCharacterActivity, 2f), getColor(R.color.green_003B50))
                    }

                    submitBackgroundColor()

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
                hideNavigation()
            }
        }
    }

    // Result + Permission
    //==================================================================================================================
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
//        Admob.getInstance().loadNativeCollapNotBanner(this, getString(R.string.native_collap_bg), binding.flNativeCollab)
    }

    override fun initAds() {
        initNativeCollab()
    }
}