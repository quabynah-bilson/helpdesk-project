package io.helpdesk.core.util

import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.bumptech.glide.request.target.Target
import io.helpdesk.R

fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun ImageView.loadImage(
    url: String?,
    @DrawableRes placeholder: Int = R.drawable.avatar_circular_clip,
    @DrawableRes error: Int = R.drawable.ic_account
) {
    GlideApp.with(this.context)
        .asBitmap()
        .load(url)
        .placeholder(placeholder)
        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
        .error(error)
        .transition(withCrossFade())
        .circleCrop()
        .into(this)
}