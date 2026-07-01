package com.axel_stein.pizzatestapp.ui.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.withClip
import coil.load

class SplashPizzaView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    companion object {
        private const val STEP_DURATION_MS = 800L
        private const val MSG_NEXT_STEP = 0xF1
    }

    private val path = Path()
    private val oval = RectF()
    private val sweepAngles = listOf(
        0f, 45f, 90f, 135f, 180f, 225f, 270f, 315f, 360f
    )

    private val currentSweepAngle: Float
        get() = sweepAngles.getOrNull(currentSweepIndex) ?: 0f

    private var currentSweepIndex = 0
    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            alphaAnimator.cancel()
            alphaAnimator.start()

            if (currentSweepIndex == sweepAngles.size) {
                currentSweepIndex = 0
            }

            currentSweepIndex++
            postInvalidateOnAnimation()

            sendEmptyMessageDelayed(MSG_NEXT_STEP, STEP_DURATION_MS)
        }
    }

    private val layerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val layerBounds = RectF()

    private val alphaAnimator = ValueAnimator.ofInt(0, 255).apply {
        duration = STEP_DURATION_MS
        addUpdateListener {
            layerPaint.alpha = it.animatedValue as Int
            postInvalidateOnAnimation()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        load("file:///android_asset/splash.png")
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == VISIBLE) {
            handler.sendEmptyMessageDelayed(MSG_NEXT_STEP, STEP_DURATION_MS)
            alphaAnimator.start()
        } else {
            handler.removeCallbacksAndMessages(null)
            alphaAnimator.cancel()
        }
    }

    private fun setupPath(path: Path, sweepAngle: Float) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = width / 2f

        oval.set(
            cx - radius,
            cy - radius,
            cx + radius,
            cy + radius
        )

        path.reset()
        if (sweepAngle >= 360f) {
            path.addOval(oval, Path.Direction.CW)
        } else {
            path.moveTo(cx, cy)

            path.arcTo(oval, 0f, sweepAngle, false)
            path.close()
        }
    }

    private fun drawAlphaLayer(canvas: Canvas, block: Canvas.() -> Unit) {
        setupPath(path, currentSweepAngle + 45f)

        layerBounds.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.saveLayer(layerBounds, layerPaint)

        canvas.withClip(path, block)
        canvas.restore()
    }

    override fun onDraw(canvas: Canvas) {
        drawAlphaLayer(canvas) {
            super.onDraw(canvas)
        }

        setupPath(path, currentSweepAngle)
        canvas.withClip(path) {
            super.onDraw(canvas)
        }
    }
}