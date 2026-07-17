package ru.netology.statsview.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import ru.netology.statsview.R
import ru.netology.statsview.utils.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(
    context,
    attributeSet,
    defStyleAttr,
    defStyleRes,
) {
    private var textSize = AndroidUtils.dp(context, 20).toFloat()
    private var lineWidth = AndroidUtils.dp(context, 8)
    private var colors = emptyList<Int>()
    private var progress = 0F
    private var fillType = FillType.PARALLEL
    private var animator: ValueAnimator? = null
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    init {
        context.withStyledAttributes(attributeSet, R.styleable.StatsView) {
            textSize = getDimension(R.styleable.StatsView_android_textSize, textSize)
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth.toFloat()).toInt()
            colors = listOf(
                getColor(R.styleable.StatsView_color1, generateRandomColor()),
                getColor(R.styleable.StatsView_color2, generateRandomColor()),
                getColor(R.styleable.StatsView_color3, generateRandomColor()),
                getColor(R.styleable.StatsView_color4, generateRandomColor()),
            )
            fillType = when (getInt(R.styleable.StatsView_fillType, 0)) {
                1 -> FillType.SEQUENTIAL
                else -> FillType.PARALLEL
            }
        }
        paint.strokeWidth = lineWidth.toFloat()
    }
    var data: Pair<List<Float>, Float> = emptyList<Float>() to 0F
        set(value) {
            field = value
            animator?.cancel()
            progress = 0F
            animator = ValueAnimator.ofFloat(0F, 1F).apply {
                duration = 2000
                addUpdateListener { anim ->
                    progress = anim.animatedValue as Float
                    invalidate()
                }
                start()
            }
        }
    private var radius = 0F
    private var center = PointF()
    private var oval = RectF()
    private val textPaint = Paint().apply {
        textSize = this@StatsView.textSize
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
        )
    }

    override fun onDraw(canvas: Canvas) {
        val (values, total) = data
        if (total == 0F) {
            return
        }
        paint.color = 0xFFE0E0E0.toInt()
        canvas.drawCircle(center.x, center.y, radius, paint)

        var startAngle = -90F + 360F * progress
        values.forEachIndexed { index, datum ->
            val maxAngle = datum / total * 360F
            val angle = maxAngle * progress
            paint.color = colors.getOrElse(index) { generateRandomColor() }
            canvas.drawArc(oval, startAngle, angle, false, paint)
            startAngle += when (fillType) {
                FillType.PARALLEL -> maxAngle
                FillType.SEQUENTIAL -> angle
            }
        }

        canvas.drawText(
            "%.2f%%".format(values.sum() / total * progress * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint
        )
    }

    private enum class FillType {
        PARALLEL,
        SEQUENTIAL,
    }

    fun generateRandomColor(): Int = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}
