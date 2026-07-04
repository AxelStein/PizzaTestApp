package com.axel_stein.pizzatestapp.ui.components

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.animation.doOnEnd

class SwapTextLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val textView: TextView = TextView(context, attrs, defStyleAttr)
    private val nextTextView: TextView = TextView(context, attrs, defStyleAttr)
    private var valueAnimator: ValueAnimator? = null

    init {
        val lp = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        lp.gravity = Gravity.CENTER_HORIZONTAL

        textView.layoutParams = lp
        nextTextView.layoutParams = lp

        addView(textView)
        addView(nextTextView)
    }

    fun setText(text: String?) {
        valueAnimator?.cancel()

        nextTextView.text = text
        nextTextView.post {
            val prevX = textView.x
            val targetX = nextTextView.x

            valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                addUpdateListener {
                    val v = it.animatedValue as Float
                    textView.translationX = (targetX - prevX) * v
                    nextTextView.alpha = v
                    textView.alpha = 1f - v
                }
                doOnEnd {
                    valueAnimator = null
                    nextTextView.alpha = 0f
                    textView.translationX = 0f
                    textView.alpha = 1f
                    textView.text = text
                }
                start()
            }
        }
    }
}