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
package com.justwayward.reader.view

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.PopupWindow

import com.justwayward.reader.R

/**
 * @author yuyh.
 * @date 16/9/5.
 */
class LoginPopupWindow(private val mActivity: Activity) : PopupWindow(), View.OnTouchListener {

    private val mContentView: View

    private val qq: ImageView
    private val weibo: ImageView
    private val wechat: ImageView

    internal var listener: LoginTypeListener? = null


    init {
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT

        mContentView = LayoutInflater.from(mActivity).inflate(R.layout.layout_login_popup_window, null)
        contentView = mContentView

        qq = mContentView.findViewById<View>(R.id.ivQQ) as ImageView
        weibo = mContentView.findViewById<View>(R.id.ivWeibo) as ImageView
        wechat = mContentView.findViewById<View>(R.id.ivWechat) as ImageView

        qq.setOnTouchListener(this)
        weibo.setOnTouchListener(this)
        wechat.setOnTouchListener(this)

        isFocusable = true
        isOutsideTouchable = true
        setBackgroundDrawable(ColorDrawable(Color.parseColor("#00000000")))

        animationStyle = R.style.LoginPopup

        setOnDismissListener { lighton() }
    }

    private fun scale(v: View, isDown: Boolean) {
        if (v.id == qq.id || v.id == weibo.id || v.id == wechat.id) {
            if (isDown) {
                val testAnim = AnimationUtils.loadAnimation(mActivity, R.anim.scale_down)
                v.startAnimation(testAnim)
            } else {
                val testAnim = AnimationUtils.loadAnimation(mActivity, R.anim.scale_up)
                v.startAnimation(testAnim)
            }
        }
        if (!isDown && listener != null) {
            when (v.id) {
                R.id.ivQQ -> listener!!.onLogin(qq, "QQ")
            }

            qq.postDelayed({ dismiss() }, 500)

        }
    }

    private fun lighton() {
        val lp = mActivity.window.attributes
        lp.alpha = 1.0f
        mActivity.window.attributes = lp
    }

    private fun lightoff() {
        val lp = mActivity.window.attributes
        lp.alpha = 0.3f
        mActivity.window.attributes = lp
    }

    override fun showAsDropDown(anchor: View, xoff: Int, yoff: Int) {
        lightoff()
        super.showAsDropDown(anchor, xoff, yoff)
    }

    override fun showAtLocation(parent: View, gravity: Int, x: Int, y: Int) {
        lightoff()
        super.showAtLocation(parent, gravity, x, y)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> scale(v, true)
            MotionEvent.ACTION_UP -> scale(v, false)
        }
        return false
    }

    interface LoginTypeListener {

        fun onLogin(view: ImageView, type: String)
    }

    fun setLoginTypeListener(listener: LoginTypeListener) {
        this.listener = listener
    }

}
