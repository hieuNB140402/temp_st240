package com.meskiep.vaithat.core.helper

import android.content.Context
import android.content.SharedPreferences
import com.meskiep.vaithat.core.utils.key.SharePreferenceKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharePreferenceHelper(val context: Context) {
    val preferences: SharedPreferences = context.getSharedPreferences(SharePreferenceKey.SHARE_KEY, Context.MODE_PRIVATE)

    // Language
    fun getPreLanguage(): String {
        return preferences.getString(SharePreferenceKey.KEY_LANGUAGE, "") ?: ""
    }

    fun setPreLanguage(language: String) {
        val editor = preferences.edit()
        editor.putString(SharePreferenceKey.KEY_LANGUAGE, language)
        editor.apply()
    }

    // First Language
    fun getIsFirstLang(): Boolean {
        return preferences.getBoolean(SharePreferenceKey.FIRST_LANG_KEY, true)
    }

    fun setIsFirstLang(isFirstAccess: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(SharePreferenceKey.FIRST_LANG_KEY, isFirstAccess)
        editor.apply()
    }

    // Permission
    fun getIsFirstPermission(): Boolean {
        return preferences.getBoolean(SharePreferenceKey.FIRST_PERMISSION_KEY, true)
    }

    fun setIsFirstPermission(isFirstAccess: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(SharePreferenceKey.FIRST_PERMISSION_KEY, isFirstAccess)
        editor.apply()
    }

    // Rate
    fun getIsRate(context: Context): Boolean {
        return preferences.getBoolean(SharePreferenceKey.RATE_KEY, false)
    }

    fun setIsRate(isFirstAccess: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(SharePreferenceKey.RATE_KEY, isFirstAccess)
        editor.apply()
    }

    // Back
    fun setCountBack(countBack: Int) {
        val editor = preferences.edit()
        editor.putInt(SharePreferenceKey.COUNT_BACK_KEY, countBack)
        editor.apply()
    }

    fun getCountBack(): Int {
        return preferences.getInt(SharePreferenceKey.COUNT_BACK_KEY, 0)
    }

    // Storage Permission
    fun getStoragePermission(): Int {
        return preferences.getInt(SharePreferenceKey.STORAGE_KEY, 0)
    }

    fun setStoragePermission(count: Int) {
        val editor = preferences.edit()
        editor.putInt(SharePreferenceKey.STORAGE_KEY, count)
        editor.apply()
    }

    // Notification Permission
    fun getNotificationPermission(): Int {
        return preferences.getInt(SharePreferenceKey.NOTIFICATION_KEY, 0)
    }

    fun setNotificationPermission(count: Int) {
        val editor = preferences.edit()
        editor.putInt(SharePreferenceKey.NOTIFICATION_KEY, count)
        editor.apply()
    }

    // Camera Permission
    fun getCameraPermission(): Int {
        return preferences.getInt(SharePreferenceKey.CAMERA_KEY, 0)
    }

    fun setCameraPermission(count: Int) {
        val editor = preferences.edit()
        editor.putInt(SharePreferenceKey.CAMERA_KEY, count)
        editor.apply()
    }

    // Data asset
    fun getQuantityUnzipped(): MutableSet<Int> {
        val json = preferences.getString(SharePreferenceKey.QUANTITY_UNZIPPED, "[]")
        val type = object : TypeToken<MutableSet<Int>>(){}.type
        return Gson().fromJson(json, type)
    }

    fun setQuantityUnzipped(count: MutableSet<Int>) {
        val editor = preferences.edit()
        val json = Gson().toJson(count)
        editor.putString(SharePreferenceKey.QUANTITY_UNZIPPED, json)
        editor.apply()
    }

    // Version
    fun getCurrentVersion(): String {
        return preferences.getString(SharePreferenceKey.CURRENT_VERSION_KEY, "") ?: ""
    }

    fun setCurrentVersion(version: String) {
        val editor = preferences.edit()
        editor.putString(SharePreferenceKey.CURRENT_VERSION_KEY, version)
        editor.apply()
    }

    // getFirstClothes
    fun getFirstClothes(): String {
        return preferences.getString(SharePreferenceKey.PATH_IMAGE, "") ?: ""
    }

    fun setFirstClothes(path: String) {
        val editor = preferences.edit()
        editor.putString(SharePreferenceKey.PATH_IMAGE, path)
        editor.apply()
    }

}