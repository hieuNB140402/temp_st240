package com.meskiep.vaithat.dialog.text

import android.app.Activity
import android.graphics.Bitmap
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseDialog
import com.meskiep.vaithat.core.extension.gone
import com.meskiep.vaithat.core.extension.invisible
import com.meskiep.vaithat.core.extension.setFont
import com.meskiep.vaithat.core.extension.showKeyboard
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.core.extension.visible
import com.meskiep.vaithat.core.helper.BitmapHelper
import com.meskiep.vaithat.core.utils.DataLocal
import com.meskiep.vaithat.data.model.SelectedModel
import com.meskiep.vaithat.databinding.DialogTextBinding
import com.meskiep.vaithat.dialog.ChooseColorDialog

class TextDialog(val context: Activity) : BaseDialog<DialogTextBinding>(context, isTextDialog = true) {
    override val layoutId: Int = R.layout.dialog_text
    override val isCancelOnTouchOutside: Boolean
        get() = false
    override val isCancelableByBack: Boolean
        get() = false

    private val textFontAdapter by lazy { TextFontDialogAdapter() }
    private val textColorAdapter by lazy { TextColorDialogAdapter() }

    var textFontList: ArrayList<SelectedModel> = arrayListOf()
    var textColorList: ArrayList<SelectedModel> = arrayListOf()

    var onDoneClick: ((Bitmap?) -> Unit) = {}

    override fun initView() {
        initRcv()
        coerceEditText()
    }

    override fun initAction() {
        binding.apply {
            btnDone.tap { handleDone() }
            btnFontTab.tap { handleFontTab() }
            btnColorTab.tap { handleTextColorTab() }
        }

        handleTextChange()
        handleRcv()


    }

    // Init
    //==================================================================================================================
    private fun initRcv() = with(binding) {
        rcvTextColor.apply {
            adapter = textColorAdapter
            itemAnimator = null
        }

        rcvFont.apply {
            adapter = textFontAdapter
            itemAnimator = null
        }

        textColorList.addAll(DataLocal.getTextColorDefault(context))
        textFontList.addAll(DataLocal.getTextFontDefault())

        updateTextColorSelected(1)
        updateTextFontSelected(0)
    }

    // Handle
    //==================================================================================================================
    private fun handleRcv() {
        textColorAdapter.apply {
            onTextColorClick = { color, position -> updateTextColorSelected(position) }
            onChooseColorClick = { handleChooseColor() }
        }
        textFontAdapter.onTextFontClick = { font, position -> updateTextFontSelected(position) }
    }

    private fun updateTextColorSelected(position: Int, color: Int = 0) {
        textColorList = textColorList.map { it.copy(isSelected = false) }.toCollection(ArrayList())
        textColorList.forEachIndexed { index, model ->
            model.isSelected = index == position
        }

        textColorAdapter.submitList(textColorList)

        val finalColor = if (position != 0) {
            coerceEditText()
            textColorList[position].color
        } else {
            color
        }

        updateTextColor(finalColor)
    }

    private fun updateTextColor(color: Int){
        binding.tvGetText.setTextColor(color)
        binding.edtText.setTextColor(color)
    }
    private fun updateTextFontSelected(position: Int) {
        textFontList = textFontList.map { it.copy(isSelected = false) }.toCollection(ArrayList())
        textFontList.forEachIndexed { index, model ->
            model.isSelected = index == position
        }
        textFontAdapter.submitList(textFontList)

        binding.tvGetText.setFont(textFontList[position].color)
        binding.edtText.setFont(textFontList[position].color)
    }

    private fun handleTextChange() {
        binding.edtText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tvGetText.text = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
                binding.btnDone.isInvisible = p0.toString().trim() == ""
            }
        })
    }

    private fun handleChooseColor() {
        val chooseColorDialog = ChooseColorDialog(context)
        chooseColorDialog.show()

        chooseColorDialog.onDoneEvent = { color ->
            updateTextColorSelected(0, color)
            context.showKeyboard(binding.edtText)
        }
    }

    private fun coerceEditText() = with(binding){
        edtText.postDelayed({
            context.showKeyboard(edtText)
        }, 500)
    }

    private fun handleDone() {
        binding.apply {
            edtText.clearFocus()
            edtText.invisible()
            tvGetText.isVisible = !TextUtils.isEmpty(edtText.text.toString().trim())
            val bitmap = BitmapHelper.getBitmapFromEditText(tvGetText)
            onDoneClick.invoke(bitmap)
            dismissDialog()
        }
    }

    private fun handleFontTab() = with(binding){
        btnFontTab.setBackgroundResource(R.drawable.bg_8_solid_yellow)
        btnColorTab.setBackgroundResource(R.color.transparent)

        rcvFont.visible()
        rcvTextColor.gone()
    }

    private fun handleTextColorTab() = with(binding){
        btnFontTab.setBackgroundResource(R.color.transparent)
        btnColorTab.setBackgroundResource(R.drawable.bg_8_solid_yellow)

        rcvFont.gone()
        rcvTextColor.visible()
    }

    // Result + Permission
    //==================================================================================================================
    override fun onDismissListener() {}


}