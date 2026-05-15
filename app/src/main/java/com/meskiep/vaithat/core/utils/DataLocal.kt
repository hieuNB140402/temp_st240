package com.meskiep.vaithat.core.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.meskiep.vaithat.R
import com.meskiep.vaithat.data.model.IntroModel
import com.meskiep.vaithat.data.model.LanguageModel
import com.meskiep.vaithat.data.model.SelectedModel
import com.facebook.shimmer.Shimmer
import com.meskiep.vaithat.core.utils.key.ValueKey
import com.meskiep.vaithat.data.model.HomeFeatureModel


object DataLocal {
    val shimmer = Shimmer.AlphaHighlightBuilder().setDuration(1800).setBaseAlpha(0.7f).setHighlightAlpha(0.6f)
        .setDirection(Shimmer.Direction.LEFT_TO_RIGHT).setAutoStart(true).build()

    var lastClickTime = 0L

    var isFailBaseURL = false

    fun getLanguageList(): ArrayList<LanguageModel> {
        return arrayListOf(
            LanguageModel("hi", "Hindi", R.drawable.ic_flag_hindi),
            LanguageModel("es", "Spanish", R.drawable.ic_flag_spanish),
            LanguageModel("fr", "French", R.drawable.ic_flag_french),
            LanguageModel("en", "English", R.drawable.ic_flag_english),
            LanguageModel("pt", "Portuguese", R.drawable.ic_flag_portugeese),
            LanguageModel("de", "German", R.drawable.ic_flag_germani),
            LanguageModel("in", "Indonesian", R.drawable.ic_flag_indo)
        )
    }

    val itemIntroList = listOf(
        IntroModel(R.drawable.img_intro_1, R.string.title_1),
        IntroModel(R.drawable.img_intro_2, R.string.title_2),
        IntroModel(R.drawable.img_intro_3, R.string.title_3),
    )


    fun getBackgroundColorDefault(context: Context): ArrayList<SelectedModel> {
        return arrayListOf(
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_1)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_2)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_3)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_4)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_5)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_6)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_7)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_8)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_9)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_10)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_11)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_12)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_13)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_14)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_15)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_16)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_17)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_18)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_19)),
        )
    }

    val addCharacterBottomNavigationUnselect = arrayListOf(
        R.drawable.ic_add_character_background_unselect,
        R.drawable.ic_add_character_sticker_unselect,
        R.drawable.ic_add_character_speech_unselect,
        R.drawable.ic_add_character_text_unselect,
    )

    val addCharacterBottomNavigationSelected = arrayListOf(
        R.drawable.ic_add_character_background_selected,
        R.drawable.ic_add_character_sticker_selected,
        R.drawable.ic_add_character_speech_selected,
        R.drawable.ic_add_character_text_selected,
    )

    fun getTextFontDefault(): ArrayList<SelectedModel> {
        return arrayListOf(
            SelectedModel(path = "Roboto", color = R.font.roboto_regular, isSelected = true),
            SelectedModel(path = "Aldrich", color = R.font.aldrich),
            SelectedModel(path = "Brush", color = R.font.brush_script),
            SelectedModel(path = "Nova", color = R.font.nova_script),
            SelectedModel(path = "Carattere", color = R.font.carattere),
            SelectedModel(path = "Digital Numbers", color = R.font.digital_numbers),
            SelectedModel(path = "Dynalight", color = R.font.dynalight),
            SelectedModel(path = "Edwardian", color = R.font.edwardian_script_itc),
            SelectedModel(path = "Vni Ongdo", color = R.font.vni_ongdo)
        )
    }

    fun getTextColorDefault(context: Context): ArrayList<SelectedModel> {
        return arrayListOf(
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_9)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.black), isSelected = true),
            SelectedModel(color = ContextCompat.getColor(context, R.color.white)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_19)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_2)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_3)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_4)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_5)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_6)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_7)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_8))
        )
    }

    fun getHomeFeatureList(): ArrayList<HomeFeatureModel> {
        return arrayListOf(
            HomeFeatureModel(
                R.drawable.img_home_thumb_1,
                R.drawable.img_home_bg_1,
                R.string.create_emoji,
                ValueKey.CREATION_EMOJI
            ),
            HomeFeatureModel(
                R.drawable.img_home_thumb_2,
                R.drawable.img_home_bg_2,
                R.string.emoji_maker,
                ValueKey.EMOJI_MAKER
            ),
            HomeFeatureModel(
                R.drawable.img_home_thumb_3,
                R.drawable.img_home_bg_3,
                R.string.cosplay_emoji,
                ValueKey.COSPLAY_EMOJI
            ),
            HomeFeatureModel(
                R.drawable.img_home_thumb_4,
                R.drawable.img_home_bg_4,
                R.string.my_creation,
                ValueKey.MY_CREATION
            ),
        )
    }

}
