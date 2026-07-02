package com.axel_stein.pizzatestapp.ui.screens.pizza_order

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class CenterScalePageTransformer : ViewPager2.PageTransformer {
    private val minScale = 0.3f

    override fun transformPage(page: View, position: Float) {
        page.translationX = position * page.width / -2f

        when {
            position < -1 -> {
                page.scaleX = minScale
                page.scaleY = minScale
            }
            position <= 1 -> {
                val scaleFactor = minScale + (1 - abs(position)) * (1 - minScale)
                page.scaleX = scaleFactor
                page.scaleY = scaleFactor
            }
            else -> {
                page.scaleX = minScale
                page.scaleY = minScale
            }
        }
    }
}