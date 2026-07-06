package com.axel_stein.pizzatestapp.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PointF
import android.graphics.drawable.Drawable
import com.axel_stein.pizzatestapp.ext.dpToPx

class ArcDrawable(private val context: Context) : Drawable() {
    private val path = Path()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    var fillColor: Int
        get() = paint.color
        set(value) { paint.color = value }

    fun createArcPathFromPoints(pM: PointF, width: Float) {
        val endY = pM.y - 65f.dpToPx(context)

        path.reset()
        path.lineTo(0f, endY)

        val controlX = width / 2f
        val controlY = 2 * pM.y - endY

        path.quadTo(
            controlX,
            controlY,
            width,
            endY
        )

        path.lineTo(width, 0f)
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