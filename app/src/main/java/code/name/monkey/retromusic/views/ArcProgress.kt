package code.name.monkey.retromusic.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.util.ViewUtil

class ArcProgress(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val OPEN_ANGLE = 120
    private var RADIUS = ViewUtil.convertDpToPixel(78f, resources)
    private val progressWidth = ViewUtil.convertDpToPixel(5f, resources)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = progressWidth
        strokeCap = Paint.Cap.ROUND
    }
    lateinit var rect: RectF
    var progress = 0f
        set(value) {
            field = value
            invalidate()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        RADIUS = width / 2 - progressWidth
        rect = RectF(width / 2 - RADIUS, height / 2 - RADIUS, width / 2 + RADIUS, height / 2 + RADIUS)
    }

    override fun onDraw(canvas: Canvas) {
        paint.color = resources.getColor(R.color.arc_progress_gray)
        canvas.drawArc(rect, (90 + OPEN_ANGLE / 2).toFloat(), (360 - OPEN_ANGLE).toFloat(), false, paint)
        paint.color = resources.getColor(R.color.arc_progress_color)
        canvas.drawArc(rect, (90 + OPEN_ANGLE / 2).toFloat(), progress * (360 - OPEN_ANGLE), false, paint)
    }
}