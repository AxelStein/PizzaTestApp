package com.axel_stein.pizzatestapp.ui.components

import android.R.attr.width
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sqrt

class ArcDrawable : Drawable() {
    private val path = Path()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    var fillColor: Int
        get() = paint.color
        set(value) { paint.color = value }

    fun createArcPathFromPoints(pS: PointF, pM: PointF, pL: PointF) {
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

    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }
}