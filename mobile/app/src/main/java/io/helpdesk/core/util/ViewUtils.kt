package io.helpdesk.core.util

import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade

fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun ImageView.loadImage(url: String?, @DrawableRes placeholder: Int, @DrawableRes error: Int) {
    GlideApp.with(this)
        .asBitmap()
        .load(url)
        .placeholder(placeholder)
        .error(error)
        .transition(withCrossFade())
        .into(this)
}