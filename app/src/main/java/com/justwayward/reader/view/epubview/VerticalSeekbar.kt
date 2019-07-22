package com.justwayward.reader.view.epubview

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewParent
import android.widget.SeekBar

import com.justwayward.reader.ui.fragment.EPubReaderFragment

/**
 * @author yuyh.
 * @date 2016/12/13.
 */
class VerticalSeekbar : SeekBar {

    private var fragment: EPubReaderFragment? = null

    private var mIsDragging: Boolean = false
    private var mTouchDownY: Float = 0.toFloat()
    private val mScaledTouchSlop: Int
    var isInScrollingContainer = false
    private var mOnSeekBarChangeListener: SeekBar.OnSeekBarChangeListener? = null
    internal var mTouchProgressOffset: Float = 0.toFloat()

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context) : super(context) {}

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        super.onSizeChanged(h, w, oldh, oldw)

    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int,
                           heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        //canvas.rotate(-90);
        //canvas.translate(-getHeight(), 0);

        canvas.rotate(90f)
        canvas.translate(0f, (-width).toFloat())
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isInScrollingContainer) {

                    mTouchDownY = event.y
                } else {
                    isPressed = true

                    invalidate()
                    onStartTrackingTouch()
                    trackTouchEvent(event)
                    attemptClaimDrag()

                    onSizeChanged(width, height, 0, 0)
                }
                if (fragment != null) {
                    fragment!!.removeCallback()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (mIsDragging) {
                    trackTouchEvent(event)

                } else {
                    val y = event.y
                    if (Math.abs(y - mTouchDownY) > mScaledTouchSlop) {
                        isPressed = true

                        invalidate()
                        onStartTrackingTouch()
                        trackTouchEvent(event)
                        attemptClaimDrag()

                    }
                }
                onSizeChanged(width, height, 0, 0)
            }

            MotionEvent.ACTION_UP -> {
                if (mIsDragging) {
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                    isPressed = false

                } else {
                    onStartTrackingTouch()
                    trackTouchEvent(event)
                    onStopTrackingTouch()

                }
                onSizeChanged(width, height, 0, 0)
                invalidate()

                if (fragment != null) {
                    fragment!!.startCallback()
                }
            }
        }
        return true

    }

    private fun trackTouchEvent(event: MotionEvent) {
        val height = height
        val top = paddingTop
        val bottom = paddingBottom
        val available = height - top - bottom

        val y = event.y.toInt()

        val scale: Float
        var progress = 0f

        // 下面是最小值
        if (y > height - bottom) {
            scale = 1.0f
        } else if (y < top) {
            scale = 0.0f
        } else {
            scale = (y - top).toFloat() / available.toFloat()
            progress = mTouchProgressOffset
        }

        val max = max
        progress += scale * max

        setProgress(progress.toInt())
        // 便于监听用户触摸改变进度
        mOnSeekBarChangeListener!!.onProgressChanged(this, progress.toInt(), true)

    }

    internal fun onStartTrackingTouch() {
        mIsDragging = true
    }

    internal fun onStopTrackingTouch() {
        mIsDragging = false
    }

    private fun attemptClaimDrag() {
        val p = parent
        p?.requestDisallowInterceptTouchEvent(true)
    }

    @Synchronized
    override fun setProgress(progress: Int) {
        super.setProgress(progress)
        onSizeChanged(width, height, 0, 0)
    }

    override fun setOnSeekBarChangeListener(l: SeekBar.OnSeekBarChangeListener) {
        mOnSeekBarChangeListener = l
        super.setOnSeekBarChangeListener(mOnSeekBarChangeListener)
    }

    fun setFragment(fragment: EPubReaderFragment) {
        this.fragment = fragment
    }
}