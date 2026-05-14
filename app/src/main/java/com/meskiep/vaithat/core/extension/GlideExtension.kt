package com.meskiep.vaithat.core.extension

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.facebook.shimmer.ShimmerDrawable
import com.meskiep.vaithat.core.utils.DataLocal
import java.io.File


fun loadImage(path: String, imageView: ImageView, isLoadShimmer: Boolean = true) {
    val shimmerDrawable = ShimmerDrawable().apply {
        setShimmer(DataLocal.shimmer)
    }
    if (isLoadShimmer){
        Glide.with(imageView.context).load(path).placeholder(shimmerDrawable).error(shimmerDrawable).into(imageView)
    }else{
        Glide.with(imageView.context).load(path).into(imageView)
    }
}

fun loadImage(path: Int, imageView: ImageView, isLoadShimmer: Boolean = true) {
    val shimmerDrawable = ShimmerDrawable().apply {
        setShimmer(DataLocal.shimmer)
    }
    if (isLoadShimmer){
        Glide.with(imageView.context).load(path).placeholder(shimmerDrawable).error(shimmerDrawable).into(imageView)
    }else{
        Glide.with(imageView.context).load(path).into(imageView)
    }
}

fun loadImage(path: Any, imageView: ImageView, onShowLoading: (() -> Unit)? = null, onDismissLoading: (() -> Unit)? = null){
    onShowLoading?.invoke()
    Glide.with(imageView.context).load(path).listener(object : RequestListener<Drawable>{
        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
            onDismissLoading?.invoke()
            return false
        }

        override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable?>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
            onDismissLoading?.invoke()
            return false
        }
    }).into(imageView)
}

@SuppressLint("CheckResult")
fun ImageView.loadImageFromFile(path: String) {
    val file = File(path)
    val request = Glide.with(context)
        .load(file)

    request.signature(ObjectKey(file.lastModified()))

    request.into(this)
}

fun loadThumbnail(view: ImageView, url: String){
    val file = File(url)

    Glide.with(view.context)
        .asBitmap()
        .load(file)
        .frame(1000000)
        .into(view)
}