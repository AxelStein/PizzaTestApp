package com.axel_stein.pizzatestapp.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.withClip
import com.axel_stein.pizzatestapp.ext.loadAsset

class SplashPizzaView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    companion object {
        private const val STEP_DURATION_MS = 150L
        private const val MSG_NEXT_STEP = 0xF1
    }

    private val path = Path()
    private val oval = RectF()
    private val sweepAngles = listOf(0f, 45f, 90f, 135f, 180f, 225f, 270f, 315f, 360f)

    private val currentSweepAngle: Float
        get() = sweepAngles.getOrNull(currentSweepIndex) ?: 0f

    private var currentSweepIndex = 0
    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == MSG_NEXT_STEP) {
                currentSweepIndex++
                postInvalidateOnAnimation()

                sendEmptyMessageDelayed(MSG_NEXT_STEP, STEP_DURATION_MS)

                if (currentSweepIndex == sweepAngles.size) {
                    if (!isAppeared) {
                        animateDisappear()
                        return
                    } else {
                        currentSweepIndex = 0
                    }
                }
            }
        }
    }

    var isAppeared: Boolean = false
        set(value) {
            if (field == value) return
            field = value

            if (value) {
                animate().cancel()

                currentSweepIndex = 0
                scaleX = 1f
                scaleY = 1f
                alpha = 1f
                handler.sendEmptyMessageDelayed(MSG_NEXT_STEP, STEP_DURATION_MS)
                postInvalidateOnAnimation()
            } else if (currentSweepIndex == 0) {
                animateDisappear()
            }
        }

    var onDisappeared: () -> Unit = {}

    private fun animateDisappear() {
        handler.removeCallbacksAndMessages(null)
        currentSweepIndex = sweepAngles.lastIndex
        postInvalidateOnAnimation()

        onDisappeared()

        animate()
            .setDuration(100)
            .scaleX(0f)
            .scaleY(0f)
            .alpha(0f)
            .start()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        loadAsset("splash.png")
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

    override fun onDraw(canvas: Canvas) {
        setupPath(path, currentSweepAngle)
        canvas.withClip(path) {
            super.onDraw(canvas)
        }
    }
}