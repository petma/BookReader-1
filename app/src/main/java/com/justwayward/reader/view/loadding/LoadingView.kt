/**
 * Copyright 2016 JustWayward Team
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.justwayward.reader.view.loadding

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator

import com.justwayward.reader.R


class LoadingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    var bounds: Rect? = null
    private var mRing: Ring? = null

    private var animator: Animator? = null
    private var animatorSet: AnimatorSet? = null
    private var mIsAnimatorCancel = false

    private var interpolator: Interpolator? = null
    //the ring's RectF
    private val mTempBounds = RectF()
    //绘制半圆的paint
    private var mPaint: Paint? = null
    private val DEFAULT_COLOR = -0xc46621
    private var mAnimationStarted = false

    private var mRotation = 0f

    var color: Int
        get() = mRing!!.color
        set(color) {
            mRing!!.color = color
            mPaint!!.color = color
        }

    /**
     * Listen the animatorSet and the IncrementAnimator;
     */
    internal var animatorListener: Animator.AnimatorListener = object : Animator.AnimatorListener {

        override fun onAnimationStart(animation: Animator) {

        }

        override fun onAnimationEnd(animation: Animator) {
            if (mIsAnimatorCancel) return
            if (animation is ValueAnimator) {
                mRing!!.sweeping = mRing!!.sweep
            } else if (animation is AnimatorSet) {
                mRing!!.restore()
                animatorSet!!.start()
            }
        }

        override fun onAnimationCancel(animation: Animator) {

        }

        override fun onAnimationRepeat(animation: Animator) {

        }
    }

    init {
        mRing = Ring()
        bounds = Rect()
        mPaint = Paint()
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeWidth = mRing!!.strokeWidth

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.LoadingView, 0, 0)
            color = a.getInt(R.styleable.LoadingView_loadding_color, DEFAULT_COLOR)
            setRingStyle(a.getInt(R.styleable.LoadingView_ring_style, RING_STYLE_SQUARE))
            setProgressStyle(a.getInt(R.styleable.LoadingView_progress_style, PROGRESS_STYLE_MATERIAL))
            setStrokeWidth(a.getDimension(R.styleable.LoadingView_ring_width, dp2px(STROKE_WIDTH)))
            setCenterRadius(a.getDimension(R.styleable.LoadingView_ring_radius, dp2px(CENTER_RADIUS)))
            a.recycle()
        }
    }

    /**
     * set the ring strokeWidth
     *
     * @param stroke
     */
    fun setStrokeWidth(stroke: Float) {
        mRing!!.strokeWidth = stroke
        mPaint!!.strokeWidth = stroke
    }

    fun setCenterRadius(radius: Float) {
        mRing!!.ringCenterRadius = radius
    }

    fun setRingStyle(style: Int) {
        when (style) {
            RING_STYLE_SQUARE -> mPaint!!.strokeCap = Paint.Cap.SQUARE
            RING_STYLE_ROUND -> mPaint!!.strokeCap = Paint.Cap.ROUND
        }
    }

    /**
     * set the animator's interpolator
     *
     * @param style
     */
    fun setProgressStyle(style: Int) {
        when (style) {
            PROGRESS_STYLE_MATERIAL -> interpolator = FastOutSlowInInterpolator()
            PROGRESS_STYLE_LINEAR -> interpolator = LinearInterpolator()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec)
        val width = dp2px(CIRCLE_DIAMETER.toFloat()).toInt()
        val height = dp2px(CIRCLE_DIAMETER.toFloat()).toInt()
        if (widthSpecMode == View.MeasureSpec.AT_MOST && heightSpecMode == View.MeasureSpec.AT_MOST) {
            setMeasuredDimension(width, height)
        } else if (widthSpecMode == View.MeasureSpec.AT_MOST) {
            setMeasuredDimension(width, heightSpecSize)
        } else if (heightSpecMode == View.MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, height)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val ring = mRing
        ring!!.setInsets(w, h)
        bounds!!.set(0, 0, w, h)
    }

    private fun buildAnimator() {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(ANIMATOR_DURATION)
        valueAnimator.repeatCount = -1
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener {
            mRotation = valueAnimator.animatedValue as Float
            invalidate()
        }
        animator = valueAnimator
        animatorSet = buildFlexibleAnimation()
        animatorSet!!.addListener(animatorListener)
    }

    override fun onDraw(canvas: Canvas) {
        if (!mIsAnimatorCancel) {
            val bounds = bounds
            val saveCount = canvas.save()
            canvas.rotate(mRotation * 360, bounds!!.exactCenterX(), bounds.exactCenterY())
            drawRing(canvas, bounds)
            canvas.restoreToCount(saveCount)
        } else {
            canvas.restore()
        }
    }

    /**
     * draw the ring
     *
     * @param canvas to draw the Ring
     * @param bounds the ring's rect
     */
    private fun drawRing(canvas: Canvas, bounds: Rect) {
        val arcBounds = mTempBounds
        val ring = mRing
        arcBounds.set(bounds)
        arcBounds.inset(ring!!.strokeInset, ring.strokeInset)
        canvas.drawArc(arcBounds, ring.start, ring.sweep, false, mPaint!!)
    }

    fun start() {
        if (mAnimationStarted) {
            return
        }

        if (animator == null || animatorSet == null) {
            mRing!!.reset()
            buildAnimator()
        }

        animator!!.start()
        animatorSet!!.start()
        mAnimationStarted = true
        mIsAnimatorCancel = false
    }


    fun stop() {
        mIsAnimatorCancel = true
        if (animator != null) {
            animator!!.end()
            animator!!.cancel()
        }
        if (animatorSet != null) {

            animatorSet!!.end()
            animatorSet!!.cancel()
        }
        animator = null
        animatorSet = null

        mAnimationStarted = false
        mRing!!.reset()
        mRotation = 0f
        invalidate()
    }

    /**
     * build FlexibleAnimation to control the progress
     *
     * @return Animatorset for control the progress
     */
    private fun buildFlexibleAnimation(): AnimatorSet {
        val ring = mRing
        val set = AnimatorSet()
        val increment = ValueAnimator.ofFloat(0, MAX_PROGRESS_ARC - MIN_PROGRESS_ARC).setDuration(ANIMATOR_DURATION / 2)
        increment.interpolator = LinearInterpolator()
        increment.addUpdateListener { animation ->
            val sweeping = ring!!.sweeping
            val value = animation.animatedValue as Float
            ring.sweep = sweeping + value
            invalidate()
        }
        increment.addListener(animatorListener)
        val reduce = ValueAnimator.ofFloat(0, MAX_PROGRESS_ARC - MIN_PROGRESS_ARC).setDuration(ANIMATOR_DURATION / 2)
        reduce.interpolator = interpolator
        reduce.addUpdateListener { animation ->
            val sweeping = ring!!.sweeping
            val starting = ring.starting
            val value = animation.animatedValue as Float
            ring.sweep = sweeping - value
            ring.start = starting + value
        }
        set.play(reduce).after(increment)
        return set
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            start()
        } else {
            stop()
        }
    }

    /**
     * turn dp to px
     *
     * @param dp value
     * @return result px value
     */
    private fun dp2px(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }


    override fun onSaveInstanceState(): Parcelable? {
        val parcelable = super.onSaveInstanceState()
        val state = SavedState(parcelable)
        state.ring = mRing
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(state)
        mRing = savedState.ring
    }

    internal class Ring : Parcelable {


        var strokeInset = 0f
        var strokeWidth = 0f
        var ringCenterRadius = 0f
        var start = 0f
        var end = 0f
        var sweep = 0f
        var sweeping = MIN_PROGRESS_ARC

        var starting = 0f
        var ending = 0f
        var color: Int = 0


        fun restore() {
            starting = start
            sweeping = sweep
            ending = end
        }

        fun reset() {
            end = 0f
            start = 0f
            sweeping = MIN_PROGRESS_ARC
            sweep = 0f
            starting = 0f
        }

        fun setInsets(width: Int, height: Int) {
            val minEdge = Math.min(width, height).toFloat()
            val insets: Float
            if (ringCenterRadius <= 0 || minEdge < 0) {
                insets = Math.ceil((strokeWidth / 2.0f).toDouble()).toFloat()
            } else {
                insets = minEdge / 2.0f - ringCenterRadius
            }


            strokeInset = insets
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeFloat(this.strokeInset)
            dest.writeFloat(this.strokeWidth)
            dest.writeFloat(this.ringCenterRadius)
            dest.writeFloat(this.start)
            dest.writeFloat(this.end)
            dest.writeFloat(this.sweep)
            dest.writeFloat(this.sweeping)
            dest.writeFloat(this.starting)
            dest.writeFloat(this.ending)
            dest.writeInt(this.color)
        }

        constructor() {}

        protected constructor(`in`: Parcel) {
            this.strokeInset = `in`.readFloat()
            this.strokeWidth = `in`.readFloat()
            this.ringCenterRadius = `in`.readFloat()
            this.start = `in`.readFloat()
            this.end = `in`.readFloat()
            this.sweep = `in`.readFloat()
            this.sweeping = `in`.readFloat()
            this.starting = `in`.readFloat()
            this.ending = `in`.readFloat()
            this.color = `in`.readInt()
        }

        companion object {

            val CREATOR: Parcelable.Creator<Ring> = object : Parcelable.Creator<Ring> {
                override fun createFromParcel(source: Parcel): Ring {
                    return Ring(source)
                }

                override fun newArray(size: Int): Array<Ring> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    /**
     *
     */
    internal class SavedState : View.BaseSavedState {
        var ring: Ring? = null


        constructor(superState: Parcelable) : super(superState) {}

        private constructor(`in`: Parcel) : super(`in`) {
            ring = `in`.readParcelable(Ring::class.java.classLoader)
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeParcelable(this.ring, flags)
        }

        companion object {


            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {

        //the size in wrap_content model
        private val CIRCLE_DIAMETER = 56

        private val CENTER_RADIUS = 15f
        private val STROKE_WIDTH = 3.5f

        private val MAX_PROGRESS_ARC = 300f
        private val MIN_PROGRESS_ARC = 20f

        private val ANIMATOR_DURATION: Long = 1332
        //the ring style
        internal val RING_STYLE_SQUARE = 0
        internal val RING_STYLE_ROUND = 1

        //the animator style
        internal val PROGRESS_STYLE_MATERIAL = 0
        internal val PROGRESS_STYLE_LINEAR = 1
    }
}