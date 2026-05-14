package com.meskiep.vaithat.dialog

import android.app.Activity
import android.text.TextUtils
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.base.BaseDialog
import com.meskiep.vaithat.core.extension.select
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.core.helper.StringHelper
import com.meskiep.vaithat.core.helper.UnitHelper
import com.meskiep.vaithat.databinding.DialogInstructionBinding

class InstructionDialog(val context: Activity) :
    BaseDialog<DialogInstructionBinding>(context, isAnim = true) {
    override val layoutId: Int = R.layout.dialog_instruction
    override val isCancelOnTouchOutside: Boolean
        get() = false
    override val isCancelableByBack: Boolean
        get() = false

    override fun initView() {
        initText()
    }

    override fun initAction() {
        binding.btnClose.tap { dismissDialog() }
    }

    override fun onDismissListener() {}

    private fun initText() {
        binding.apply {
            tvTitle.select()

            val allText = TextUtils.concat(
                createColoredText(R.string.cosplay_mode, R.color.yellow_FBE46E),
                " ",
                createColoredText(R.string.players_are_randomly_given),
                " ",
                createColoredText(R.string.one_reference_image, R.color.yellow_FBE46E),
                " ",
                createColoredText(R.string.to_follow_your_task_is_to_recreate_that_character_as_accurately_as_possible_by_choosing_the_right_every_correct_match_increases_your),
                " ",
                createColoredText(R.string.percentage_score, R.color.yellow_FBE46E),
                " ",
                createColoredText(R.string.the_closer_your_character_looks_to_the_random_image_the_higher_your_final_score_will_be_pay_attention_to_small_details_to_achieve_the_best_cosplay_result)
            )

            tvDescription.apply {
                text = allText
                setStroke(UnitHelper.pxToDpFloat(context, 2f), context.getColor(R.color.green_003B50))
            }
        }
    }

    private fun createColoredText(
        @androidx.annotation.StringRes textRes: Int,
        @androidx.annotation.ColorRes colorRes: Int = R.color.white,
        font: Int = R.font.cookies
    ) = StringHelper.changeColor(context, context.getString(textRes), colorRes, font)
}