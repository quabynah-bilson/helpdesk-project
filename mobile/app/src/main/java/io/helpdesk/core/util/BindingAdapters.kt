package io.helpdesk.core.util

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("imageUrl", "circleCrop", "placeholder", "error")
fun loadImage(
    view: ImageView,
    url: String?,
    circleCrop: Boolean = false,
    placeholder: Drawable?,
    error: Drawable?,
) {
    GlideApp.with(view).asBitmap().load(url)
        .placeholder(placeholder)
        .error(error).apply {
            if (circleCrop) circleCrop()
        }
        .into(view)
}