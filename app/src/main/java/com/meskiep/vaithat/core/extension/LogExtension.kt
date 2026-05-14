package com.meskiep.vaithat.core.extension

import android.content.Context
import android.util.Log

fun dLog(content: String) {
    Log.d("nbhieu", "================================================================================================\n$content")
}

fun eLog(content: String) {
    Log.e("nbhieu", "================================================================================================\n$content")
}

fun iLog(content: String) {
    Log.i("nbhieu", "================================================================================================\n$content")
}

fun wLog(content: String) {
    Log.w("nbhieu", "================================================================================================\n$content")
}