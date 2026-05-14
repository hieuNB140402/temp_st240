package com.meskiep.vaithat.core.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.viewbinding.ViewBinding
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.helper.LanguageHelper


abstract class BaseDialog<VB : ViewBinding>(
    context: Context,
    val isAnim: Boolean = false,
    val isTextDialog: Boolean = false,
) : Dialog(context, R.style.TransparentDialog) {

    protected lateinit var binding: VB
    abstract val layoutId: Int
    abstract val isCancelOnTouchOutside: Boolean
    abstract val isCancelableByBack: Boolean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.setLocale(context)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DataBindingUtil.inflate(LayoutInflater.from(context), layoutId, null, false)
        setContentView(binding.root)

        setCancelable(isCancelableByBack)
        setCanceledOnTouchOutside(isCancelOnTouchOutside)

        if (isAnim) {
            window?.attributes?.windowAnimations = R.style.DialogZoomAnimation
        }

        if (isTextDialog){
            window?.apply {
                // 1. Ép Window chiếm toàn bộ màn hình vật lý
                setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
        }else{
            setupWindow()
        }

        initView()
        initAction()
    }

    private fun setupWindow() {
        window?.apply {
            // 1. Ép Window chiếm toàn bộ màn hình vật lý
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            // 2. Quan trọng: Cho phép vẽ vào vùng tai thỏ và tràn qua StatusBar
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }

            // 3. Flags để layout không bị giới hạn bởi StatusBar
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            // 4. Giữ cho StatusBar hiển thị nhưng trong suốt để Dialog hiện bên dưới
            statusBarColor = Color.TRANSPARENT

            setGravity(Gravity.CENTER)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setDecorFitsSystemWindows(true) // Cực kỳ quan trọng trên Android 11+
            } else {
                // Chỉ dùng các flag LAYOUT, không dùng flag HIDE để không mất StatusBar
                decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        onDismissListener()
    }

    abstract fun initView()
    abstract fun initAction()
    abstract fun onDismissListener()

    fun dismissDialog() {
        this.dismiss()
    }
}
