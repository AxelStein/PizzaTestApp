package com.axel_stein.pizzatestapp.ext

import android.widget.ImageView
import coil.ImageLoader
import coil.imageLoader
import coil.load
import coil.request.ImageRequest

inline fun ImageView.loadAsset(
    name: String,
    imageLoader: ImageLoader = context.imageLoader,
    builder: ImageRequest.Builder.() -> Unit = {}
) {
    load("file:///android_asset/${name}", imageLoader, builder)
}