package com.axel_stein.pizzatestapp.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.animation.OvershootInterpolator
import androidx.appcompat.widget.AppCompatImageView
import com.axel_stein.pizzatestapp.domain.model.PizzaSize
import com.axel_stein.pizzatestapp.ext.findActivity
import com.axel_stein.pizzatestapp.ui.components.zoomy.ZoomTouchHandler

class PizzaImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    var pizzaSize: PizzaSize? = null
        set(value) {
            val setup = field == null
            if (field == value) return
            field = value

            if (value == null) return

            if (setup) {
                scaleX = value.scale
                scaleY = value.scale
                return
            }

            animate().cancel()
            animate()
                .scaleX(value.scale)
                .scaleY(value.scale)
                .setInterpolator(OvershootInterpolator())
                .setDuration(400)
                .start()
        }

    private val PizzaSize.scale: Float
        get() = when (this) {
            PizzaSize.Small -> 0.7f
            PizzaSize.Medium -> 0.9f
            PizzaSize.Large -> 1f
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        context.findActivity()?.let {
            ZoomTouchHandler.Builder(it)
                .setTarget(this)
                .register()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        setOnTouchListener(null)
    }
}