package com.meskiep.vaithat.ui.emoji_maker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseFragment
import com.meskiep.vaithat.core.extension.gone
import com.meskiep.vaithat.core.extension.setBackgroundWithOption
import com.meskiep.vaithat.core.extension.setState
import com.meskiep.vaithat.core.extension.setTextWithOption
import com.meskiep.vaithat.core.extension.strings
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.core.extension.visible
import com.meskiep.vaithat.core.helper.UnitHelper
import com.meskiep.vaithat.core.utils.DataLocal
import com.meskiep.vaithat.databinding.FragmentBrushBinding
import com.meskiep.vaithat.dialog.ChooseColorDialog
import com.meskiep.vaithat.dialog.text.TextColorDialogAdapter
import com.meskiep.vaithat.ui.emoji_maker.viewModel.BrushViewModel
import com.meskiep.vaithat.ui.emoji_maker.viewModel.EmojiMakerViewModel
import com.raed.rasmview.brushtool.data.Brush
import com.raed.rasmview.brushtool.data.BrushesRepository

class BrushFragment : BaseFragment<FragmentBrushBinding>() {
    private val viewModel: BrushViewModel by viewModels()
    private val emojiViewModel: EmojiMakerViewModel by activityViewModels()
    private val colorAdapter by lazy { TextColorDialogAdapter() }


    override fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentBrushBinding {
        return FragmentBrushBinding.inflate(inflater)
    }

    override fun initView() {
        initRcv()
        setupBrush()
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarRightText.tap { handleDone() }
            btnBrush.tap { handleBrushSelect() }
            btnEraser.tap { handleEraserSelect() }

            rasmView.rasmContext.state.addOnStateChangedListener {
                btnUndo.setState(
                    rasmView.rasmContext.state.canCallUndo(),
                    R.drawable.ic_emoji_maker_undo_enable,
                    R.drawable.ic_emoji_maker_undo_not_enable
                )
                btnRedo.setState(
                    rasmView.rasmContext.state.canCallRedo(),
                    R.drawable.ic_emoji_maker_redo_enable,
                    R.drawable.ic_emoji_maker_redo_not_enable
                )
            }

            btnUndo.tap { rasmView.rasmContext.state.undo() }
            btnRedo.tap { rasmView.rasmContext.state.redo() }
        }

        colorAdapter.apply {
            onTextColorClick = { color, position -> updateTextColorSelected(position, color) }
            onChooseColorClick = { handleChooseColor() }
        }

    }

    // Init
    //==================================================================================================================
    override fun initActionBar() {
        val context = (activity as EmojiMakerActivity)

        binding.actionBar.apply {
            btnActionBarRightText.setBackgroundWithOption(R.drawable.bg_focus_very_short)
            tvActionBarRightText.apply {
                setTextWithOption(context.strings(R.string.done))
                setTextColor(context.getColor(R.color.white))
                setStroke(
                    UnitHelper.pxToDpFloat(context, 2f),
                    context.getColor(R.color.green_003B50)
                )
            }
        }
    }

    private fun initRcv() {
        val context = (activity as EmojiMakerActivity)

        binding.rcvColor.apply {
            adapter = colorAdapter
            itemAnimator = null
        }
        viewModel.updateTextColorList(DataLocal.getTextColorDefault(context))
        updateTextColorSelected(1, viewModel.brushColorDefault)
    }

    // Handle
    //==================================================================================================================
    private fun setupBrush() {
        val context = (activity as EmojiMakerActivity)
        binding.apply {
            sbSize.setProgress((viewModel.brushSizeDefault * 100).toInt())

            rasmView.rasmContext.apply {
                setBackgroundColor(context.getColor(R.color.transparent))
                brushConfig = BrushesRepository(resources).get(Brush.Pen)
                brushConfig.size = viewModel.brushSizeDefault
                brushColor = viewModel.brushColorDefault
            }
            handleSeekbarListener()
        }
    }

    private fun handleSeekbarListener() = with(binding) {
        sbSize.onSizeChanged = { progress ->
            val value = maxOf(2f, progress.toFloat())

            val size = value / 100f
            rasmView.rasmContext.brushConfig.size = size
            viewModel.brushSizeDefault = size
        }
    }

    private fun handleBrushSelect() = with(binding) {
        val context = (activity as EmojiMakerActivity)
        val colorSelected = context.getColor(R.color.yellow_FFD364)
        val colorUnselect = context.getColor(R.color.transparent)

        rasmView.rasmContext.apply {
            brushConfig = BrushesRepository(resources).get(Brush.Pen)
            brushConfig.size = viewModel.brushSizeDefault
        }
        btnBrush.setImageResource(R.drawable.ic_emoji_maker_brush_selected)
        flBrush.setBackgroundColor(colorSelected)

        btnEraser.setImageResource(R.drawable.ic_emoji_maker_eraser_unselect)
        flEraser.setBackgroundColor(colorUnselect)

        lnlColor.visible()
    }

    private fun handleEraserSelect() = with(binding) {
        val context = (activity as EmojiMakerActivity)
        val colorSelected = context.getColor(R.color.yellow_FFD364)
        val colorUnselect = context.getColor(R.color.transparent)

        rasmView.rasmContext.apply {
            brushConfig = BrushesRepository(resources).get(Brush.HardEraser)
            brushConfig.size = viewModel.brushSizeDefault
        }

        btnBrush.setImageResource(R.drawable.ic_emoji_maker_brush_unselect)
        flBrush.setBackgroundColor(colorUnselect)

        btnEraser.setImageResource(R.drawable.ic_emoji_maker_eraser_selected)
        flEraser.setBackgroundColor(colorSelected)

        lnlColor.gone()
    }

    private fun updateTextColorSelected(position: Int, color: Int = 0) {
        val finalColor = viewModel.updateTextColorSelect(position, color)

        colorAdapter.submitList(viewModel.textColorList)
        binding.rasmView.rasmContext.brushColor = finalColor
    }

    private fun handleChooseColor() {
        val dialog = ChooseColorDialog((activity as EmojiMakerActivity))

        dialog.show()

        dialog.onDoneEvent = { color ->
            updateTextColorSelected(0, color)
        }
    }

    private fun handleDone() {
        if (binding.rasmView.rasmContext.state.canCallUndo()) {
            val bitmap = binding.rasmView.rasmContext.exportRasm()
            emojiViewModel.setBitmapBush(bitmap)
        }
        parentFragmentManager.popBackStack()
    }
    // Observable
    //==================================================================================================================

    // Result + Permission
    //==================================================================================================================

    // Ads
    //==================================================================================================================
}