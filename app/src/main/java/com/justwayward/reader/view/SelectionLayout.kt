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

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.ListPopupWindow
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView

import com.justwayward.reader.R
import com.yuyh.easyadapter.abslistview.EasyLVAdapter
import com.yuyh.easyadapter.abslistview.EasyLVHolder

import java.util.ArrayList

/**
 * @author yuyh.
 * @date 16/9/2.
 */
class SelectionLayout @JvmOverloads constructor(private val mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(mContext, attrs, defStyleAttr) {
    private val parent: LinearLayout

    private var listener: OnSelectListener? = null

    init {
        parent = this
    }

    fun setData(vararg data: List<String>) {
        if (data != null && data.size > 0) {
            for (i in data.indices) {
                val list = data[i]
                val childView = ChildView(mContext)
                val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.weight = 1f
                childView.layoutParams = params
                childView.setData(list)
                childView.tag = i
                addView(childView)
            }
        }
    }


    private fun closeAll() {
        val childCount = childCount
        for (i in 0 until childCount) {
            val childView = getChildAt(i) as ChildView
            childView.closePopWindow()
        }
    }

    internal inner class ChildView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr), View.OnClickListener, AdapterView.OnItemClickListener {

        private val layout: LinearLayout

        private var ivArrow: ImageView? = null
        private var tvTitle: TextView? = null

        private var isOpen = false

        private val data = ArrayList<String>()
        private var mListPopupWindow: ListPopupWindow? = null
        private var mAdapter: SelAdapter? = null

        var operatingAnim1 = AnimationUtils.loadAnimation(mContext, R.anim.roate_0_180)
        var operatingAnim2 = AnimationUtils.loadAnimation(mContext, R.anim.roate_180_360)
        var lin1 = LinearInterpolator()
        var lin2 = LinearInterpolator()

        init {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            layout = inflater.inflate(R.layout.view_selection, this) as LinearLayout

            initView()
        }

        private fun initView() {
            ivArrow = findViewById<View>(R.id.ivSelArrow) as ImageView
            ivArrow!!.scaleType = ImageView.ScaleType.MATRIX   //required
            tvTitle = findViewById<View>(R.id.tvSelTitle) as TextView
            setOnClickListener(this)
            operatingAnim1.interpolator = lin1
            operatingAnim1.fillAfter = true
            operatingAnim2.interpolator = lin2
            operatingAnim2.fillAfter = true
        }

        private fun setData(list: List<String>?) {
            if (list != null && !list.isEmpty()) {
                data.addAll(list)
                tvTitle!!.text = list[0]
            }
        }

        fun openPopupWindow() {
            if (mListPopupWindow == null) {
                createPopupWindow()
            }
            mListPopupWindow!!.show()
        }

        private fun createPopupWindow() {
            mListPopupWindow = ListPopupWindow(mContext)
            mAdapter = SelAdapter(mContext, data)
            mListPopupWindow!!.setAdapter(mAdapter)
            mListPopupWindow!!.width = ViewGroup.LayoutParams.MATCH_PARENT
            mListPopupWindow!!.height = ViewGroup.LayoutParams.WRAP_CONTENT
            mListPopupWindow!!.anchorView = parent.getChildAt(0)
            mListPopupWindow!!.setForceIgnoreOutsideTouch(false)
            mListPopupWindow!!.setOnItemClickListener(this)
            mListPopupWindow!!.setOnDismissListener {
                ivArrow!!.startAnimation(operatingAnim2)
                isOpen = false
            }
            mListPopupWindow!!.isModal = true
        }

        fun closePopWindow() {
            if (mListPopupWindow != null && mListPopupWindow!!.isShowing) {
                mListPopupWindow!!.dismiss()
            }
        }

        override fun onClick(v: View) {
            if (isOpen) {
                ivArrow!!.startAnimation(operatingAnim2)
                closePopWindow()
                isOpen = false
            } else {
                ivArrow!!.startAnimation(operatingAnim1)
                openPopupWindow()
                isOpen = true
            }
        }

        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            mAdapter!!.setSelPosition(position)
            tvTitle!!.text = data[position]
            if (listener != null) {
                listener!!.onSelect(this.tag as Int, position, data[position])
            }
            mListPopupWindow!!.dismiss()
        }


        internal inner class SelAdapter(context: Context, list: List<String>) : EasyLVAdapter<String>(context, list, R.layout.item_selection_view) {

            var selPosition = 0

            override fun convert(holder: EasyLVHolder, position: Int, s: String) {
                holder.setText(R.id.tvSelTitleItem, s)
                if (selPosition == position) {
                    holder.setTextColor(R.id.tvSelTitleItem, ContextCompat.getColor(mContext, R.color.light_pink))
                } else {
                    holder.setTextColor(R.id.tvSelTitleItem, ContextCompat.getColor(mContext, R.color.light_black))
                }
            }

            fun setSelPosition(position: Int) {
                selPosition = position
                notifyDataSetChanged()
            }
        }
    }

    interface OnSelectListener {
        fun onSelect(index: Int, position: Int, title: String)
    }

    fun setOnSelectListener(listener: OnSelectListener) {
        this.listener = listener
    }
}
