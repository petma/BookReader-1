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
package com.justwayward.reader.view.recyclerview.swipe

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.DecelerateInterpolator
import android.view.animation.Transformation
import android.widget.AbsListView
import android.widget.FrameLayout

class SwipeRefreshLayout
/**
 * Constructor that is called when inflating SwipeRefreshLayout from XML.
 *
 * @param context
 * @param attrs
 */
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private var mTarget: View? = null // the target of the gesture
    private var mListener: OnRefreshListener? = null
    private var mRefreshing = false
    private val mTouchSlop: Int
    private var mTotalDragDistance = -1f
    private val mMediumAnimationDuration: Int
    private var mCurrentTargetOffsetTop: Int = 0
    // Whether or not the starting offset has been determined.
    private var mOriginalOffsetCalculated = false

    private var mInitialMotionY: Float = 0.toFloat()
    private var mInitialDownY: Float = 0.toFloat()
    private var mIsBeingDragged: Boolean = false
    private var mActivePointerId = INVALID_POINTER
    // Whether this item is scaled up rather than clipped
    private var mScale: Boolean = false

    // Target is returning to its start offset because it was cancelled or a
    // refresh was triggered.
    private var mReturningToStart: Boolean = false
    private val mDecelerateInterpolator: DecelerateInterpolator

    private var mCircleView: CircleImageView? = null
    private var mCircleViewIndex = -1

    protected var mFrom: Int = 0

    private var mStartingScale: Float = 0.toFloat()

    protected var mOriginalOffsetTop: Int = 0

    private var mProgress: MaterialProgressDrawable? = null

    private var mScaleAnimation: Animation? = null

    private var mScaleDownAnimation: Animation? = null

    private var mAlphaStartAnimation: Animation? = null

    private var mAlphaMaxAnimation: Animation? = null

    private var mScaleDownToStartAnimation: Animation? = null

    private var mSpinnerFinalOffset: Float = 0.toFloat()

    private var mNotify: Boolean = false

    private var mCircleWidth: Int = 0

    private var mCircleHeight: Int = 0

    // Whether the client has set a custom starting position;
    private var mUsingCustomStart: Boolean = false

    private val mRefreshListener = object : AnimationListener {
        override fun onAnimationStart(animation: Animation) {}

        override fun onAnimationRepeat(animation: Animation) {}

        override fun onAnimationEnd(animation: Animation) {
            if (mRefreshing) {
                // Make sure the progress view is fully visible
                mProgress!!.alpha = MAX_ALPHA
                mProgress!!.start()
                if (mNotify) {
                    if (mListener != null) {
                        mListener!!.onRefresh()
                    }
                }
            } else {
                mProgress!!.stop()
                mCircleView!!.visibility = View.GONE
                setColorViewAlpha(MAX_ALPHA)
                // Return the circle to its start position
                if (mScale) {
                    setAnimationProgress(0f /* animation complete and view is hidden */)
                } else {
                    setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCurrentTargetOffsetTop,
                            true /* requires update */)
                }
            }
            mCurrentTargetOffsetTop = mCircleView!!.top
        }
    }

    /**
     * Pre API 11, alpha is used to make the progress circle appear instead of scale.
     */
    private val isAlphaUsedForScale: Boolean
        get() = android.os.Build.VERSION.SDK_INT < 11

    /**
     * @return Whether the SwipeRefreshWidget is actively showing refresh
     * progress.
     */
    /**
     * Notify the widget that refresh state has changed. Do not call this when
     * refresh is triggered by a swipe gesture.
     *
     * @param refreshing Whether or not the view should show refresh progress.
     */
    // scale and show
    /* requires update *//* notify */ var isRefreshing: Boolean
        get() = mRefreshing
        set(refreshing) = if (refreshing && mRefreshing != refreshing) {
            mRefreshing = refreshing
            var endTarget = 0
            if (!mUsingCustomStart) {
                endTarget = (mSpinnerFinalOffset + mOriginalOffsetTop).toInt()
            } else {
                endTarget = mSpinnerFinalOffset.toInt()
            }
            setTargetOffsetTopAndBottom(endTarget - mCurrentTargetOffsetTop,
                    true)
            mNotify = false
            startScaleUpAnimation(mRefreshListener)
        } else {
            setRefreshing(refreshing, false)
        }

    /**
     * Get the diameter of the progress circle that is displayed as part of the
     * swipe to refresh layout. This is not valid until a measure pass has
     * completed.
     *
     * @return Diameter in pixels of the progress circle view.
     */
    val progressCircleDiameter: Int
        get() = if (mCircleView != null) mCircleView!!.measuredHeight else 0

    private val mAnimateToCorrectPosition = object : Animation() {
        public override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            var targetTop = 0
            var endTarget = 0
            if (!mUsingCustomStart) {
                endTarget = (mSpinnerFinalOffset - Math.abs(mOriginalOffsetTop)).toInt()
            } else {
                endTarget = mSpinnerFinalOffset.toInt()
            }
            targetTop = mFrom + ((endTarget - mFrom) * interpolatedTime).toInt()
            val offset = targetTop - mCircleView!!.top
            setTargetOffsetTopAndBottom(offset, false /* requires update */)
            mProgress!!.setArrowScale(1 - interpolatedTime)
        }
    }

    private val mAnimateToStartPosition = object : Animation() {
        public override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            moveToStart(interpolatedTime)
        }
    }

    private fun setColorViewAlpha(targetAlpha: Int) {
        mCircleView!!.background.alpha = targetAlpha
        mProgress!!.alpha = targetAlpha
    }

    /**
     * The refresh indicator starting and resting position is always positioned
     * near the top of the refreshing content. This position is a consistent
     * location, but can be adjusted in either direction based on whether or not
     * there is a toolbar or actionbar present.
     *
     * @param scale Set to true if there is no view at a higher z-order than
     * where the progress spinner is set to appear.
     * @param start The offset in pixels from the top of this view at which the
     * progress spinner should appear.
     * @param end The offset in pixels from the top of this view at which the
     * progress spinner should come to rest after a successful swipe
     * gesture.
     */
    fun setProgressViewOffset(scale: Boolean, start: Int, end: Int) {
        mScale = scale
        mCircleView!!.visibility = View.GONE
        mCurrentTargetOffsetTop = start
        mOriginalOffsetTop = mCurrentTargetOffsetTop
        mSpinnerFinalOffset = end.toFloat()
        mUsingCustomStart = true
        mCircleView!!.invalidate()
    }

    /**
     * The refresh indicator resting position is always positioned near the top
     * of the refreshing content. This position is a consistent location, but
     * can be adjusted in either direction based on whether or not there is a
     * toolbar or actionbar present.
     *
     * @param scale Set to true if there is no view at a higher z-order than
     * where the progress spinner is set to appear.
     * @param end The offset in pixels from the top of this view at which the
     * progress spinner should come to rest after a successful swipe
     * gesture.
     */
    fun setProgressViewEndTarget(scale: Boolean, end: Int) {
        mSpinnerFinalOffset = end.toFloat()
        mScale = scale
        mCircleView!!.invalidate()
    }

    /**
     * One of DEFAULT, or LARGE.
     */
    fun setSize(size: Int) {
        if (size != MaterialProgressDrawable.LARGE && size != MaterialProgressDrawable.DEFAULT) {
            return
        }
        val metrics = resources.displayMetrics
        if (size == MaterialProgressDrawable.LARGE) {
            mCircleWidth = (CIRCLE_DIAMETER_LARGE * metrics.density).toInt()
            mCircleHeight = mCircleWidth
        } else {
            mCircleWidth = (CIRCLE_DIAMETER * metrics.density).toInt()
            mCircleHeight = mCircleWidth
        }
        // force the bounds of the progress circle inside the circle view to
        // update by setting it to null before updating its size and then
        // re-setting it
        mCircleView!!.setImageDrawable(null)
        mProgress!!.updateSizes(size)
        mCircleView!!.setImageDrawable(mProgress)
    }

    init {

        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

        mMediumAnimationDuration = resources.getInteger(
                android.R.integer.config_mediumAnimTime)

        setWillNotDraw(false)
        mDecelerateInterpolator = DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR)

        val a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS)
        isEnabled = a.getBoolean(0, true)
        a.recycle()

        val metrics = resources.displayMetrics
        mCircleWidth = (CIRCLE_DIAMETER * metrics.density).toInt()
        mCircleHeight = (CIRCLE_DIAMETER * metrics.density).toInt()

        createProgressView()
        ViewCompat.setChildrenDrawingOrderEnabled(this, true)
        // the absolute offset has to take into account that the circle starts at an offset
        mSpinnerFinalOffset = DEFAULT_CIRCLE_TARGET * metrics.density
        mTotalDragDistance = mSpinnerFinalOffset

        requestDisallowInterceptTouchEvent(true)
    }

    override fun getChildDrawingOrder(childCount: Int, i: Int): Int {
        return if (mCircleViewIndex < 0) {
            i
        } else if (i == childCount - 1) {
            // Draw the selected child last
            mCircleViewIndex
        } else if (i >= mCircleViewIndex) {
            // Move the children after the selected child earlier one
            i + 1
        } else {
            // Keep the children before the selected child the same
            i
        }
    }

    private fun createProgressView() {
        mCircleView = CircleImageView(context, CIRCLE_BG_LIGHT, (CIRCLE_DIAMETER / 2).toFloat())
        mProgress = MaterialProgressDrawable(context, this)
        mProgress!!.setBackgroundColor(CIRCLE_BG_LIGHT)
        mCircleView!!.setImageDrawable(mProgress)
        mCircleView!!.visibility = View.GONE
        addView(mCircleView)
    }

    /**
     * Set the listener to be notified when a refresh is triggered via the swipe
     * gesture.
     */
    fun setOnRefreshListener(listener: OnRefreshListener) {
        mListener = listener
    }

    private fun startScaleUpAnimation(listener: AnimationListener?) {
        mCircleView!!.visibility = View.VISIBLE
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            // Pre API 11, alpha is used in place of scale up to show the
            // progress circle appearing.
            // Don't adjust the alpha during appearance otherwise.
            mProgress!!.alpha = MAX_ALPHA
        }
        mScaleAnimation = object : Animation() {
            public override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                setAnimationProgress(interpolatedTime)
            }
        }
        mScaleAnimation!!.duration = mMediumAnimationDuration.toLong()
        if (listener != null) {
            mCircleView!!.setAnimationListener(listener)
        }
        mCircleView!!.clearAnimation()
        mCircleView!!.startAnimation(mScaleAnimation)
    }

    /**
     * Pre API 11, this does an alpha animation.
     * @param progress
     */
    private fun setAnimationProgress(progress: Float) {
        if (isAlphaUsedForScale) {
            setColorViewAlpha((progress * MAX_ALPHA).toInt())
        } else {
            ViewCompat.setScaleX(mCircleView!!, progress)
            ViewCompat.setScaleY(mCircleView!!, progress)
        }
    }

    private fun setRefreshing(refreshing: Boolean, notify: Boolean) {
        if (mRefreshing != refreshing) {
            mNotify = notify
            ensureTarget()
            mRefreshing = refreshing
            if (mRefreshing) {
                animateOffsetToCorrectPosition(mCurrentTargetOffsetTop, mRefreshListener)
            } else {
                startScaleDownAnimation(mRefreshListener)
            }
        }
    }

    private fun startScaleDownAnimation(listener: AnimationListener?) {
        mScaleDownAnimation = object : Animation() {
            public override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                setAnimationProgress(1 - interpolatedTime)
            }
        }
        mScaleDownAnimation!!.duration = SCALE_DOWN_DURATION.toLong()
        mCircleView!!.setAnimationListener(listener)
        mCircleView!!.clearAnimation()
        mCircleView!!.startAnimation(mScaleDownAnimation)
    }

    private fun startProgressAlphaStartAnimation() {
        mAlphaStartAnimation = startAlphaAnimation(mProgress!!.alpha, STARTING_PROGRESS_ALPHA)
    }

    private fun startProgressAlphaMaxAnimation() {
        mAlphaMaxAnimation = startAlphaAnimation(mProgress!!.alpha, MAX_ALPHA)
    }

    private fun startAlphaAnimation(startingAlpha: Int, endingAlpha: Int): Animation? {
        // Pre API 11, alpha is used in place of scale. Don't also use it to
        // show the trigger point.
        if (mScale && isAlphaUsedForScale) {
            return null
        }
        val alpha = object : Animation() {
            public override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                mProgress!!.alpha = (startingAlpha + (endingAlpha - startingAlpha) * interpolatedTime).toInt()
            }
        }
        alpha.duration = ALPHA_ANIMATION_DURATION.toLong()
        // Clear out the previous animation listeners.
        mCircleView!!.setAnimationListener(null)
        mCircleView!!.clearAnimation()
        mCircleView!!.startAnimation(alpha)
        return alpha
    }


    @Deprecated("Use {@link #setProgressBackgroundColorSchemeResource(int)}")
    fun setProgressBackgroundColor(colorRes: Int) {
        setProgressBackgroundColorSchemeResource(colorRes)
    }

    /**
     * Set the background color of the progress spinner disc.
     *
     * @param colorRes Resource id of the color.
     */
    fun setProgressBackgroundColorSchemeResource(colorRes: Int) {
        setProgressBackgroundColorSchemeColor(resources.getColor(colorRes))
    }

    /**
     * Set the background color of the progress spinner disc.
     *
     * @param color
     */
    fun setProgressBackgroundColorSchemeColor(color: Int) {
        mCircleView!!.setBackgroundColor(color)
        mProgress!!.setBackgroundColor(color)
    }


    @Deprecated("Use {@link #setColorSchemeResources(int...)}")
    fun setColorScheme(vararg colors: Int) {
        setColorSchemeResources(*colors)
    }

    /**
     * Set the color resources used in the progress animation from color resources.
     * The first color will also be the color of the bar that grows in response
     * to a user swipe gesture.
     *
     * @param colorResIds
     */
    fun setColorSchemeResources(vararg colorResIds: Int) {
        val res = resources
        val colorRes = IntArray(colorResIds.size)
        for (i in colorResIds.indices) {
            colorRes[i] = res.getColor(colorResIds[i])
        }
        setColorSchemeColors(*colorRes)
    }

    /**
     * Set the colors used in the progress animation. The first
     * color will also be the color of the bar that grows in response to a user
     * swipe gesture.
     *
     * @param colors
     */
    fun setColorSchemeColors(vararg colors: Int) {
        ensureTarget()
        mProgress!!.setColorSchemeColors(*colors)
    }

    private fun ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (mTarget == null) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child.javaClass.isAssignableFrom(RecyclerView::class.java)) {
                    mTarget = child
                    break
                }
            }
        }
    }

    /**
     * Set the distance to trigger a sync in dips
     *
     * @param distance
     */
    fun setDistanceToTriggerSync(distance: Int) {
        mTotalDragDistance = distance.toFloat()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val width = measuredWidth
        val height = measuredHeight
        if (childCount == 0) {
            return
        }
        if (mTarget == null) {
            ensureTarget()
        }
        if (mTarget == null) {
            return
        }
        val child = mTarget
        val childLeft = paddingLeft
        val childTop = paddingTop
        val childWidth = width - paddingLeft - paddingRight
        val childHeight = height - paddingTop - paddingBottom
        child!!.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
        val circleWidth = mCircleView!!.measuredWidth
        val circleHeight = mCircleView!!.measuredHeight
        mCircleView!!.layout(width / 2 - circleWidth / 2, mCurrentTargetOffsetTop,
                width / 2 + circleWidth / 2, mCurrentTargetOffsetTop + circleHeight)
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mTarget == null) {
            ensureTarget()
        }
        if (mTarget == null) {
            return
        }
        mTarget!!.measure(View.MeasureSpec.makeMeasureSpec(
                measuredWidth - paddingLeft - paddingRight,
                View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(
                measuredHeight - paddingTop - paddingBottom, View.MeasureSpec.EXACTLY))
        mCircleView!!.measure(View.MeasureSpec.makeMeasureSpec(mCircleWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(mCircleHeight, View.MeasureSpec.EXACTLY))
        if (!mUsingCustomStart && !mOriginalOffsetCalculated) {
            mOriginalOffsetCalculated = true
            mOriginalOffsetTop = -mCircleView!!.measuredHeight
            mCurrentTargetOffsetTop = mOriginalOffsetTop
        }
        mCircleViewIndex = -1
        // Get the index of the circleview.
        for (index in 0 until childCount) {
            if (getChildAt(index) === mCircleView) {
                mCircleViewIndex = index
                break
            }
        }
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    fun canChildScrollUp(): Boolean {
        //        //For make it can work when my recycler view is in Gone.
        //        return false;
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget is AbsListView) {
                val absListView = mTarget as AbsListView?
                return absListView!!.childCount > 0 && (absListView.firstVisiblePosition > 0 || absListView.getChildAt(0)
                        .top < absListView.paddingTop)
            } else {
                return ViewCompat.canScrollVertically(mTarget!!, -1) || mTarget!!.scrollY > 0
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget!!, -1)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        ensureTarget()

        val action = MotionEventCompat.getActionMasked(ev)
        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false
        }
        if (!isEnabled || mReturningToStart || canChildScrollUp() || mRefreshing) {
            // Fail fast if we're not in a state where a swipe is possible
            return false
        }
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCircleView!!.top, true)
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0)
                mIsBeingDragged = false
                val initialDownY = getMotionEventY(ev, mActivePointerId)
                if (initialDownY == -1f) {
                    return false
                }
                mInitialDownY = initialDownY
            }

            MotionEvent.ACTION_MOVE -> {
                if (mActivePointerId == INVALID_POINTER) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but don't have an active pointer id.")
                    return false
                }

                val y = getMotionEventY(ev, mActivePointerId)
                if (y == -1f) {
                    return false
                }
                val yDiff = y - mInitialDownY
                if (yDiff > mTouchSlop && !mIsBeingDragged) {
                    mInitialMotionY = mInitialDownY + mTouchSlop
                    mIsBeingDragged = true
                    mProgress!!.alpha = STARTING_PROGRESS_ALPHA
                }
            }

            MotionEventCompat.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mIsBeingDragged = false
                mActivePointerId = INVALID_POINTER
            }
        }
        return mIsBeingDragged
    }

    private fun getMotionEventY(ev: MotionEvent, activePointerId: Int): Float {
        val index = MotionEventCompat.findPointerIndex(ev, activePointerId)
        return if (index < 0) {
            -1f
        } else MotionEventCompat.getY(ev, index)
    }

    override fun requestDisallowInterceptTouchEvent(b: Boolean) {
        // Nope.
        //Why Nope?
        super.requestDisallowInterceptTouchEvent(b)
    }

    private fun isAnimationRunning(animation: Animation?): Boolean {
        return animation != null && animation.hasStarted() && !animation.hasEnded()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(ev)

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false
        }

        if (!isEnabled || mReturningToStart || canChildScrollUp()) {
            // Fail fast if we're not in a state where a swipe is possible
            return false
        }

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0)
                mIsBeingDragged = false
            }

            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId)
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.")
                    return false
                }

                val y = MotionEventCompat.getY(ev, pointerIndex)
                val overscrollTop = (y - mInitialMotionY) * DRAG_RATE
                if (mIsBeingDragged) {

                    mProgress!!.showArrow(true)
                    val originalDragPercent = overscrollTop / mTotalDragDistance
                    if (originalDragPercent < 0) {
                        return false
                    }
                    val dragPercent = Math.min(1f, Math.abs(originalDragPercent))
                    val adjustedPercent = Math.max(dragPercent - .4, 0.0).toFloat() * 5 / 3
                    val extraOS = Math.abs(overscrollTop) - mTotalDragDistance
                    val slingshotDist = if (mUsingCustomStart)
                        mSpinnerFinalOffset - mOriginalOffsetTop
                    else
                        mSpinnerFinalOffset
                    val tensionSlingshotPercent = Math.max(0f,
                            Math.min(extraOS, slingshotDist * 2) / slingshotDist)
                    val tensionPercent = (tensionSlingshotPercent / 4 - Math.pow(
                            (tensionSlingshotPercent / 4).toDouble(), 2.0)).toFloat() * 2f
                    val extraMove = slingshotDist * tensionPercent * 2f

                    val targetY = mOriginalOffsetTop + (slingshotDist * dragPercent + extraMove).toInt()
                    // where 1.0f is a full circle
                    if (mCircleView!!.visibility != View.VISIBLE) {
                        mCircleView!!.visibility = View.VISIBLE
                    }
                    if (!mScale) {
                        ViewCompat.setScaleX(mCircleView!!, 1f)
                        ViewCompat.setScaleY(mCircleView!!, 1f)
                    }
                    if (overscrollTop < mTotalDragDistance) {
                        if (mScale) {
                            setAnimationProgress(overscrollTop / mTotalDragDistance)
                        }
                        if (mProgress!!.alpha > STARTING_PROGRESS_ALPHA && !isAnimationRunning(mAlphaStartAnimation)) {
                            // Animate the alpha
                            startProgressAlphaStartAnimation()
                        }
                        val strokeStart = adjustedPercent * .8f
                        mProgress!!.setStartEndTrim(0f, Math.min(MAX_PROGRESS_ANGLE, strokeStart))
                        mProgress!!.setArrowScale(Math.min(1f, adjustedPercent))
                    } else {
                        if (mProgress!!.alpha < MAX_ALPHA && !isAnimationRunning(mAlphaMaxAnimation)) {
                            // Animate the alpha
                            startProgressAlphaMaxAnimation()
                        }
                    }
                    val rotation = (-0.25f + .4f * adjustedPercent + tensionPercent * 2) * .5f
                    mProgress!!.setProgressRotation(rotation)
                    setTargetOffsetTopAndBottom(targetY - mCurrentTargetOffsetTop,
                            true /* requires update */)
                }
            }
            MotionEventCompat.ACTION_POINTER_DOWN -> {
                val index = MotionEventCompat.getActionIndex(ev)
                mActivePointerId = MotionEventCompat.getPointerId(ev, index)
            }

            MotionEventCompat.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (mActivePointerId == INVALID_POINTER) {
                    if (action == MotionEvent.ACTION_UP) {
                        Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.")
                    }
                    return false
                }
                val pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId)
                val y = MotionEventCompat.getY(ev, pointerIndex)
                val overscrollTop = (y - mInitialMotionY) * DRAG_RATE
                mIsBeingDragged = false
                if (overscrollTop > mTotalDragDistance) {
                    setRefreshing(true, true /* notify */)
                } else {
                    // cancel refresh
                    mRefreshing = false
                    mProgress!!.setStartEndTrim(0f, 0f)
                    var listener: AnimationListener? = null
                    if (!mScale) {
                        listener = object : AnimationListener {

                            override fun onAnimationStart(animation: Animation) {}

                            override fun onAnimationEnd(animation: Animation) {
                                if (!mScale) {
                                    startScaleDownAnimation(null)
                                }
                            }

                            override fun onAnimationRepeat(animation: Animation) {}

                        }
                    }
                    animateOffsetToStartPosition(mCurrentTargetOffsetTop, listener)
                    mProgress!!.showArrow(false)
                }
                mActivePointerId = INVALID_POINTER
                return false
            }
        }
        return true
    }

    private fun animateOffsetToCorrectPosition(from: Int, listener: AnimationListener?) {
        mFrom = from
        mAnimateToCorrectPosition.reset()
        mAnimateToCorrectPosition.duration = ANIMATE_TO_TRIGGER_DURATION.toLong()
        mAnimateToCorrectPosition.interpolator = mDecelerateInterpolator
        if (listener != null) {
            mCircleView!!.setAnimationListener(listener)
        }
        mCircleView!!.clearAnimation()
        mCircleView!!.startAnimation(mAnimateToCorrectPosition)
    }

    private fun animateOffsetToStartPosition(from: Int, listener: AnimationListener?) {
        if (mScale) {
            // Scale the item back down
            startScaleDownReturnToStartAnimation(from, listener)
        } else {
            mFrom = from
            mAnimateToStartPosition.reset()
            mAnimateToStartPosition.duration = ANIMATE_TO_START_DURATION.toLong()
            mAnimateToStartPosition.interpolator = mDecelerateInterpolator
            if (listener != null) {
                mCircleView!!.setAnimationListener(listener)
            }
            mCircleView!!.clearAnimation()
            mCircleView!!.startAnimation(mAnimateToStartPosition)
        }
    }

    private fun moveToStart(interpolatedTime: Float) {
        var targetTop = 0
        targetTop = mFrom + ((mOriginalOffsetTop - mFrom) * interpolatedTime).toInt()
        val offset = targetTop - mCircleView!!.top
        setTargetOffsetTopAndBottom(offset, false /* requires update */)
    }

    private fun startScaleDownReturnToStartAnimation(from: Int,
                                                     listener: AnimationListener?) {
        mFrom = from
        if (isAlphaUsedForScale) {
            mStartingScale = mProgress!!.alpha.toFloat()
        } else {
            mStartingScale = ViewCompat.getScaleX(mCircleView!!)
        }
        mScaleDownToStartAnimation = object : Animation() {
            public override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                val targetScale = mStartingScale + -mStartingScale * interpolatedTime
                setAnimationProgress(targetScale)
                moveToStart(interpolatedTime)
            }
        }
        mScaleDownToStartAnimation!!.duration = SCALE_DOWN_DURATION.toLong()
        if (listener != null) {
            mCircleView!!.setAnimationListener(listener)
        }
        mCircleView!!.clearAnimation()
        mCircleView!!.startAnimation(mScaleDownToStartAnimation)
    }

    private fun setTargetOffsetTopAndBottom(offset: Int, requiresUpdate: Boolean) {
        mCircleView!!.bringToFront()
        mCircleView!!.offsetTopAndBottom(offset)
        mCurrentTargetOffsetTop = mCircleView!!.top
        if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
            invalidate()
        }
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = MotionEventCompat.getActionIndex(ev)
        val pointerId = MotionEventCompat.getPointerId(ev, pointerIndex)
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex)
        }
    }

    companion object {
        // Maps to ProgressBar.Large style
        val LARGE = MaterialProgressDrawable.LARGE
        // Maps to ProgressBar default style
        val DEFAULT = MaterialProgressDrawable.DEFAULT

        private val LOG_TAG = SwipeRefreshLayout::class.java.simpleName

        private val MAX_ALPHA = 255
        private val STARTING_PROGRESS_ALPHA = (.3f * MAX_ALPHA).toInt()

        private val CIRCLE_DIAMETER = 40
        private val CIRCLE_DIAMETER_LARGE = 56

        private val DECELERATE_INTERPOLATION_FACTOR = 2f
        private val INVALID_POINTER = -1
        private val DRAG_RATE = .5f

        // Max amount of circle that can be filled by progress during swipe gesture,
        // where 1.0 is a full circle
        private val MAX_PROGRESS_ANGLE = .8f

        private val SCALE_DOWN_DURATION = 150

        private val ALPHA_ANIMATION_DURATION = 300

        private val ANIMATE_TO_TRIGGER_DURATION = 200

        private val ANIMATE_TO_START_DURATION = 200

        // Default background for the progress spinner
        private val CIRCLE_BG_LIGHT = -0x50506
        // Default offset in dips from the top of the view to where the progress spinner should stop
        private val DEFAULT_CIRCLE_TARGET = 64
        private val LAYOUT_ATTRS = intArrayOf(android.R.attr.enabled)
    }

}
/**
 * Simple constructor to use when creating a SwipeRefreshLayout from code.
 *
 * @param context
 */
