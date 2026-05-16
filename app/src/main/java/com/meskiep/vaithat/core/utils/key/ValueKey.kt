package com.meskiep.vaithat.core.utils.key

object ValueKey {
    // Internal
    const val DOWNLOAD_ALBUM = "Emoji & Sticker Maker"
    // Image from Customize
    const val CHARACTER_CUSTOMIZE_ALBUM = "Character Customize"
    // Folder parent
    const val DATA_CHARACTER_ALBUM = "Data Character"
    const val EDIT_CHARACTER_ALBUM = "Edit Character"
    val DATA_FILE_API_INTERNAL by lazy { "data_api.json" }


    //============================================================================================================================
    // Home
    const val CREATION_EMOJI = 0
    const val EMOJI_MAKER = 1
    const val COSPLAY_EMOJI = 2
    const val MY_CREATION = 3

    //============================================================================================================================
    // Customize
    const val RANDOM_LAYER = "RANDOM_LAYER"
    const val NONE_LAYER = "NONE_LAYER"

    const val CREATE = 0
    const val EDIT = 1
    const val COSPLAY = 2

    //============================================================================================================================
    const val SIZE_PROGRESS_DEFAULT = 50

    //============================================================================================================================
    // Add Character
    const val IMAGE_BACKGROUND = 0
    const val COLOR_BACKGROUND = 1

    const val BACKGROUND_NAVIGATION = 0
    const val STICKER_NAVIGATION = 1
    const val SPEECH_NAVIGATION = 2
    const val TEXT_NAVIGATION = 3

    //============================================================================================================================
    // View
    const val VIEW_TYPE = 0
    const val SUCCESS_TYPE = 1

    const val EDIT_CREATION = 0
    const val VIEW_CREATION = 1

    //============================================================================================================================
    // Share
    const val RANDOM_IDENTIFIER_FOR_ADDING_STICKER_TO_WHATSAPP =
        "RANDOM_IDENTIFIER_FOR_WHATSAPP_STICKER"
}
