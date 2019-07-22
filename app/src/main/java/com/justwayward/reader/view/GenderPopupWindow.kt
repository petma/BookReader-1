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
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow

import com.justwayward.reader.R
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.support.UserSexChooseFinishedEvent
import com.justwayward.reader.manager.SettingManager

import org.greenrobot.eventbus.EventBus

/**
 * Created by xiaoshu on 2016/10/9.
 * 性别选择弹窗 用户没有选择性别则进入app提示
 */
class GenderPopupWindow(private val mActivity: Activity) : PopupWindow() {
    private val mContentView: View

    private val mBtnMale: Button
    private val mBtnFemale: Button
    private val mIvClose: ImageView

    init {
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT

        mContentView = LayoutInflater.from(mActivity).inflate(R.layout.layout_gender_popup_window, null)
        contentView = mContentView
        isFocusable = true
        isOutsideTouchable = true
        isTouchable = true
        setBackgroundDrawable(ColorDrawable(Color.parseColor("#00000000")))

        animationStyle = R.style.LoginPopup

        setOnDismissListener {
            lighton()
            EventBus.getDefault().post(UserSexChooseFinishedEvent())
        }
        mBtnMale = mContentView.findViewById<View>(R.id.mBtnMale) as Button
        mBtnMale.setOnClickListener {
            SettingManager.instance!!.saveUserChooseSex(Constant.Gender.MALE)
            dismiss()
        }
        mBtnFemale = mContentView.findViewById<View>(R.id.mBtnFemale) as Button
        mBtnFemale.setOnClickListener {
            SettingManager.instance!!.saveUserChooseSex(Constant.Gender.FEMALE)
            dismiss()
        }
        mIvClose = mContentView.findViewById<View>(R.id.mIvClose) as ImageView
        mIvClose.setOnClickListener { dismiss() }
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
}
