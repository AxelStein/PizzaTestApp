package com.axel_stein.pizzatestapp.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import com.axel_stein.pizzatestapp.R
import com.axel_stein.pizzatestapp.databinding.FragmentPizzaOrderBinding
import com.axel_stein.pizzatestapp.ui.components.zoomy.ZoomTouchListener
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sqrt

class PizzaOrderLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ZoomTouchListener {

    private lateinit var binding: FragmentPizzaOrderBinding
    private val path = Path()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    var shouldAppear = false
        set(value) {
            if (field != value) {
                field = value

                if (value) {
                    animateAppearance()
                }
            }
        }

    private fun animateAppearance() = binding.run {
        listOf(pizzaSizeLayout, tvDescription)
            .forEach {
                it.animate()
                    .translationY(0f)
                    .setInterpolator(OvershootInterpolator())
                    .start()
            }

        quantityStepper.animate()
            .alpha(1f)
            .start()

        pagerProductImages.animate()
            .alpha(1f)
            .start()

        zoomHintLayout.animate()
            .alpha(1f)
            .start()
    }

    init {
        setWillNotDraw(false)
        paint.color = ContextCompat.getColor(context, R.color.highlight)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = FragmentPizzaOrderBinding.bind(this)
        doOnNextLayout {
            if (!shouldAppear) {
                val parentHeight = height.toFloat()
                binding.run {
                    pizzaSizeLayout.translationY = parentHeight - pizzaSizeLayout.top
                    tvDescription.translationY = parentHeight - tvDescription.top
                }
                calcArc()
            }
        }
    }

    private fun calcArc() {
        createArcPath(
            getFabCenter(findViewById(R.id.fabSizeSmall)),
            getFabCenter(findViewById(R.id.fabSizeMedium)),
            getFabCenter(findViewById(R.id.fabSizeLarge)),
        )
    }

    fun getFabCenter(child: View): PointF {
        val childLocation = IntArray(2)
        child.getLocationOnScreen(childLocation)

        val relativeX = (childLocation[0]).toFloat()
        val relativeY = (childLocation[1]).toFloat()

        val centerX = relativeX + (child.width / 2f)
        val centerY = relativeY + (child.height / 2f) - binding.pizzaSizeLayout.translationY

        return PointF(centerX, centerY)
    }

    private fun createArcPath(pS: PointF, pM: PointF, pL: PointF) {
        path.reset()

        val viewWidth = width.toFloat()

        val d = 2 * (pS.x * (pM.y - pL.y) + pM.x * (pL.y - pS.y) + pL.x * (pS.y - pM.y))
        if (kotlin.math.abs(d) < 0.0001f) {
            return
        }

        val sSq = pS.x * pS.x + pS.y * pS.y
        val mSq = pM.x * pM.x + pM.y * pM.y
        val lSq = pL.x * pL.x + pL.y * pL.y

        val centerX = (sSq * (pM.y - pL.y) + mSq * (pL.y - pS.y) + lSq * (pS.y - pM.y)) / d
        val centerY = (sSq * (pL.x - pM.x) + mSq * (pS.x - pL.x) + lSq * (pM.x - pS.x)) / d

        val radius = hypot(pS.x - centerX, pS.y - centerY)

        val leftPoint = PointF()
        val underRootLeft = (radius * radius) - (centerX * centerX)
        leftPoint.y = if (underRootLeft >= 0) {
            centerY + sqrt(underRootLeft)
        } else {
            pS.y
        }

        val rightPoint = PointF(viewWidth, leftPoint.y)

        var startAngle = Math.toDegrees(
            atan2(
                (leftPoint.y - centerY).toDouble(),
                (leftPoint.x - centerX).toDouble()
            )
        ).toFloat()

        var endAngle = Math.toDegrees(
            atan2(
                (rightPoint.y - centerY).toDouble(),
                (rightPoint.x - centerX).toDouble()
            )
        ).toFloat()

        if (startAngle < 0) startAngle += 360f
        if (endAngle < 0) endAngle += 360f

        var sweepAngle = endAngle - startAngle
        if (sweepAngle > 0) {
            sweepAngle -= 360f
        }

        val oval = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        path.moveTo(0f, 0f)
        path.lineTo(0f, leftPoint.y)
        path.arcTo(oval, startAngle, sweepAngle, false)

        path.lineTo(rightPoint.x, 0f)
        path.close()
    }

    override fun onZoomStarted() = binding.run {}

    override fun onImageScaled(progress: Float) = binding.run {
        val parentHeight = height.toFloat()

        toolbar.translationY = -(toolbar.height * progress)

        listOf(quantityStepper, tvDescription, pizzaSizeLayout).forEach { v ->
            v.translationY = (parentHeight - v.top) * progress
        }
    }

    override fun onZoomEnded() = binding.run {
        toolbar.animateResetTranslationY()
        quantityStepper.animateResetTranslationY()
        pizzaSizeLayout.animateResetTranslationY()
        tvDescription.animateResetTranslationY()
    }

    private fun View.animateResetTranslationY() {
        animate()
            .translationY(0f)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (shouldAppear) {
            canvas.drawPath(path, paint)
        }
    }
}