package com.meskiep.vaithat.ui.emoji_maker

import android.graphics.Bitmap
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseActivity
import com.meskiep.vaithat.core.extension.gone
import com.meskiep.vaithat.core.extension.launchIO
import com.meskiep.vaithat.core.extension.setBackgroundWithOption
import com.meskiep.vaithat.core.extension.setImageWithOption
import com.meskiep.vaithat.core.extension.setImageWithOptionAndState
import com.meskiep.vaithat.core.extension.setState
import com.meskiep.vaithat.core.extension.setTextWithOption
import com.meskiep.vaithat.core.extension.strings
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.core.extension.visible
import com.meskiep.vaithat.core.helper.DragBottomSheetHelper
import com.meskiep.vaithat.core.helper.UnitHelper
import com.meskiep.vaithat.data.model.SortEmojiLayerModel
import com.meskiep.vaithat.data.model.draw.Draw
import com.meskiep.vaithat.data.model.draw.DrawableDraw
import com.meskiep.vaithat.databinding.ActivityEmojiMakerBinding
import com.meskiep.vaithat.dialog.text.TextDialog
import com.meskiep.vaithat.listener.listenerdraw.OnDrawListener
import com.meskiep.vaithat.ui.emoji_maker.adapter.SortEmojiLayerAdapter
import com.meskiep.vaithat.ui.emoji_maker.viewModel.EmojiMakerViewModel
import kotlinx.coroutines.launch

class EmojiMakerActivity : BaseActivity<ActivityEmojiMakerBinding>() {
    private val viewModel: EmojiMakerViewModel by viewModels()

    private val sortEmojiLayerAdapter by lazy { SortEmojiLayerAdapter() }
    override fun setViewBinding(): ActivityEmojiMakerBinding {
        return ActivityEmojiMakerBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        binding.rcvLayerSort.apply {
            adapter = sortEmojiLayerAdapter
            itemAnimator = null
        }
        initDrawView()
        setupDragBottom()
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            launch { viewModel.bitmapBush.collect { bitmap -> setupGetBrushBitmap(bitmap) } }
        }
    }

    override fun viewListener() {
        binding.apply {
            btnBrush.tap { handleStartBrush() }
            btnText.tap { handleAddText() }
            btnUndo.tap { drawView.undo() }
            btnRedo.tap { drawView.redo() }
        }
    }


    // Init
    //==================================================================================================================
    override fun initActionBar() {
        binding.actionBar.btnActionBarLeft.setImageWithOption(R.drawable.ic_back)
        setStateActionBar(false)
    }

    private fun setStateActionBar(isEnable: Boolean) = with(binding.actionBar) {
        val textColor = if (isEnable) R.color.white else R.color.gray_B2717171
        val strokeColor = if (isEnable) R.color.green_003B50 else R.color.transparent
        val strokeWidth = UnitHelper.pxToDpFloat(this@EmojiMakerActivity, 2f)

        actionBar.apply {
            btnActionBarCenter.setImageWithOptionAndState(isEnable, R.drawable.ic_reset_enable, R.drawable.ic_reset_disable)
            btnActionBarRightText.setBackgroundWithOption(
                isEnable,
                R.drawable.bg_focus_very_short,
                R.drawable.bg_unfocus_very_short
            )
            tvActionBarRightText.apply {
                setTextWithOption(strings(R.string.save))
                setTextColor(getColor(textColor))
                setStroke(strokeWidth, getColor(strokeColor))
            }
        }
    }

    private fun initDrawView() {
        binding.drawView.apply {
            setConstrained(true)
            setLocked(false)
            setOnDrawListener(object : OnDrawListener {
                override fun onAddedDraw(draw: Draw) {
                    checkUndoRedo()
                }

                override fun onClickedDraw(draw: Draw) {
                    checkUndoRedo()
                }

                override fun onDeletedDraw(draw: Draw) {
                    checkUndoRedo()
                }

                override fun onDragFinishedDraw(draw: Draw) {
                    checkUndoRedo()
                }

                override fun onTouchedDownDraw(draw: Draw) {
                    checkUndoRedo()
                }

                override fun onZoomFinishedDraw(draw: Draw) {
                    checkUndoRedo()
                }

                override fun onFlippedDraw(draw: Draw) {
                    checkUndoRedo()
                }

                override fun onDoubleTappedDraw(draw: Draw) {
                    checkUndoRedo()
                }

                override fun onHideOptionIconDraw() {
                    checkUndoRedo()
                }

                override fun onUndoDeleteDraw(draw: List<Draw?>) {
                    checkUndoRedo()
                }

                override fun onUndoUpdateDraw(draw: List<Draw?>) {
                    checkUndoRedo()
                }

                override fun onUndoDeleteAll() {
                    checkUndoRedo()
                }

                override fun onRedoAll() {
                    checkUndoRedo()
                }

                override fun onReplaceDraw(draw: Draw) {
                    checkUndoRedo()
                }

                override fun onEditText(draw: DrawableDraw) {
                    checkUndoRedo()
                }

                override fun onReplace(draw: Draw) {
                    checkUndoRedo()
                }
            })
        }

        checkUndoRedo()
    }

    private fun setupDragBottom() {
        val helper = DragBottomSheetHelper(
            bottomView = binding.flLayerSort,
            dragView = binding.vDrag
        )

        helper.setup()
        helper.expand()
        binding.btnLayer.tap { helper.expand() }

    }

    // Handle
    //==================================================================================================================
    private fun handleStartBrush() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frmBrush, BrushFragment())
            .addToBackStack(null)
            .commit()

        binding.frmBrush.visible()
    }

    private fun addDrawable(path: String, bitmapResult: Bitmap? = null) {
        launchIO(
            blockIO = {
                bitmapResult ?: Glide.with(this@EmojiMakerActivity).load(path).submit().get().toBitmap()
            },
            blockMain = { bitmap ->
                val drawableEmoji = viewModel.loadDrawableEmoji(this@EmojiMakerActivity, bitmap)
                drawableEmoji.let { binding.drawView.addDraw(it) }
            }
        )
    }

    private fun handleAddText() {
        val dialog = TextDialog(this)
        dialog.show()
        dialog.onDoneClick = { bitmap -> addDrawable("", bitmap) }
    }

    private fun checkUndoRedo() {
        binding.apply {
            btnUndo.setState(
                drawView.undoList.isNotEmpty(),
                R.drawable.ic_emoji_maker_undo_enable,
                R.drawable.ic_emoji_maker_undo_not_enable
            )

            btnRedo.setState(
                drawView.redoList.isNotEmpty(),
                R.drawable.ic_emoji_maker_redo_enable,
                R.drawable.ic_emoji_maker_redo_not_enable
            )

            val isEnable = binding.drawView.drawList.isNotEmpty()

            setStateActionBar(isEnable)
            updateDrawList()
        }
    }

    fun updateDrawList() {
        val list = binding.drawView.drawList
        val sortEmojiLayerModelList = list.map {
            SortEmojiLayerModel(
                drawableDraw = it
            )
        }
        sortEmojiLayerAdapter.submitList(sortEmojiLayerModelList)

    }

    // Observable
    //==================================================================================================================
    private fun setupGetBrushBitmap(bitmap: Bitmap?) {
        if (bitmap == null) return
        binding.frmBrush.gone()

        addDrawable("", bitmap)
    }
    // Result + Permission
    //==================================================================================================================

    // Ads
    //==================================================================================================================

}