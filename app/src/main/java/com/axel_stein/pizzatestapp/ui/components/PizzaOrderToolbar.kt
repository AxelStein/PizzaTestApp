package com.axel_stein.pizzatestapp.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import com.axel_stein.pizzatestapp.databinding.LayoutPizzaOrderToolbarBinding

class PizzaOrderToolbar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutPizzaOrderToolbarBinding.inflate(
        LayoutInflater.from(context),
        this,
        false
    )

    var shouldAppear = false
        set(value) {
            if (field == value) return
            field = value
            if (value) {
                animateAppearance()
            }
        }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addView(binding.root)
    }

    private fun animateAppearance() {
        binding.btnNavigateUp.animate()
            .translationX(0f)
            .setInterpolator(OvershootInterpolator())
            .start()

        binding.btnFavorite.animate()
            .translationX(0f)
            .setInterpolator(OvershootInterpolator())
            .start()

        binding.nameLayout.animate()
            .translationY(0f)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    fun setPizzaName(name: String?) {
        binding.pizzaName.setText(name)
    }
}