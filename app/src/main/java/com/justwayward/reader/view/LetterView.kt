package com.justwayward.reader.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView

/**
 * @author yuyh.
 * @date 17/1/30.
 */

class LetterView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TextView(context, attrs, defStyleAttr) {


    private var mPaintBackground: Paint? = null
    private var mPaintText: Paint? = null
    private var mRect: Rect? = null

    private var text: String? = null

    private var charHash: Int = 0

    init {

        init()

        setText(getText().toString())
    }

    private fun init() {
        mPaintText = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaintText!!.color = Color.WHITE

        mPaintBackground = Paint(Paint.ANTI_ALIAS_FLAG)

        mRect = Rect()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec) // 宽高相同
    }

    override fun onDraw(canvas: Canvas) {
        if (null != text) {
            // 画圆
            canvas.drawCircle((width / 2).toFloat(), (width / 2).toFloat(), (width / 2).toFloat(), mPaintBackground!!)
            // 写字
            mPaintText!!.textSize = (width / 2).toFloat()
            mPaintText!!.strokeWidth = 3f
            mPaintText!!.getTextBounds(text, 0, 1, mRect)
            // 垂直居中
            val fontMetrics = mPaintText!!.fontMetricsInt
            val baseline = (measuredHeight - fontMetrics.bottom - fontMetrics.top) / 2
            // 左右居中
            mPaintText!!.textAlign = Paint.Align.CENTER
            canvas.drawText(text!!, (width / 2).toFloat(), baseline.toFloat(), mPaintText!!)
        }
    }

    fun setText(content: String) {
        var content = content
        if (TextUtils.isEmpty(content)) {
            content = " "
        }
        this.text = content.toCharArray()[0].toString()
        this.text = text!!.toUpperCase()
        charHash = this.text!!.hashCode()
        val color = colors[charHash % colors.size]
        mPaintBackground!!.color = color
        invalidate()
    }

    companion object {

        // 颜色画板集
        private val colors = intArrayOf(-0xe54364, -0xe95f7b, -0xe3bf1, -0xc63ee, -0xd1338f, -0xd851a0, -0x1981de, -0x2cac00, -0xcb6725, -0xd67f47, -0x18b3c4, -0x3fc6d5, -0x64a64a, -0x71bb53, -0x423c39, -0xcbb6a2, -0xd3c1b0, -0x6a5a5a, -0x807373, -0x137841, -0x278f53, -0x9687b, -0x645c82, -0x4b6dab, -0x4b6dab)
    }
}
