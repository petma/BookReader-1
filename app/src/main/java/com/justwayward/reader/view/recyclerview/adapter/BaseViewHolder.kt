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
package com.justwayward.reader.view.recyclerview.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Checkable
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.yuyh.easyadapter.glide.GlideCircleTransform
import com.yuyh.easyadapter.glide.GlideRoundTransform

/**
 * M为这个itemView对应的model。
 * 使用RecyclerArrayAdapter就一定要用这个ViewHolder。
 * 这个ViewHolder将ItemView与Adapter解耦。
 * 推荐子类继承第二个构造函数。并将子类的构造函数设为一个ViewGroup parent。
 * 然后这个ViewHolder就完全独立。adapter在new的时候只需将parentView传进来。View的生成与管理由ViewHolder执行。
 * 实现setData来实现UI修改。Adapter会在onCreateViewHolder里自动调用。
 *
 *
 * 在一些特殊情况下，只能在setData里设置监听。
 *
 * @param <M>
</M> */
abstract class BaseViewHolder<M> : RecyclerView.ViewHolder {

    protected var holder: BaseViewHolder<M>

    val layoutId: Int
    protected var mContext: Context? = null
    /**
     * 获取item布局
     *
     * @return
     */
    var itemView: View? = null
        private set

    private val mViews = SparseArray<View>()

    protected val context: Context
        get() = if (mContext == null) mContext = itemView.getContext() else mContext

    constructor(itemView: View) : super(itemView) {
        holder = this
        this.itemView = itemView
        mContext = this.itemView!!.getContext()
    }

    constructor(parent: ViewGroup, @LayoutRes res: Int) : super(LayoutInflater.from(parent.context).inflate(res, parent, false)) {
        holder = this
        this.itemView = itemView
        layoutId = res
        mContext = this.itemView!!.getContext()
    }

    open fun setData(item: M) {

    }

    protected fun <T : View> `$`(@IdRes id: Int): T {
        return itemView.findViewById(id)
    }

    fun <V : View> getView(viewId: Int): V {
        var view: View? = mViews.get(viewId)
        if (view == null) {
            view = this.itemView!!.findViewById(viewId)
            mViews.put(viewId, view)
        }
        return view as V?
    }

    fun setOnItemViewClickListener(listener: View.OnClickListener): BaseViewHolder<*> {
        this.itemView!!.setOnClickListener(listener)
        return this
    }

    fun setText(viewId: Int, value: String): BaseViewHolder<*> {
        val view = getView<TextView>(viewId)
        view.text = value
        return this
    }

    fun setTextColor(viewId: Int, color: Int): BaseViewHolder<*> {
        val view = getView<TextView>(viewId)
        view.setTextColor(color)
        return this
    }

    fun setTextColorRes(viewId: Int, colorRes: Int): BaseViewHolder<*> {
        val view = getView<TextView>(viewId)
        view.setTextColor(ContextCompat.getColor(mContext!!, colorRes))
        return this
    }

    fun setImageResource(viewId: Int, imgResId: Int): BaseViewHolder<*> {
        val view = getView<ImageView>(viewId)
        view.setImageResource(imgResId)
        return this
    }

    fun setBackgroundColor(viewId: Int, color: Int): BaseViewHolder<*> {
        val view = getView<View>(viewId)
        view.setBackgroundColor(color)
        return this
    }

    fun setBackgroundColorRes(viewId: Int, colorRes: Int): BaseViewHolder<*> {
        val view = getView<View>(viewId)
        view.setBackgroundResource(colorRes)
        return this
    }

    fun setImageDrawable(viewId: Int, drawable: Drawable?): BaseViewHolder<*> {
        val view = getView<ImageView>(viewId)
        view.setImageDrawable(drawable)
        return this
    }

    fun setImageDrawableRes(viewId: Int, drawableRes: Int): BaseViewHolder<*> {
        val drawable = ContextCompat.getDrawable(mContext!!, drawableRes)
        return setImageDrawable(viewId, drawable)
    }

    fun setImageUrl(viewId: Int, imgUrl: String): BaseViewHolder<*> {
        val view = getView<ImageView>(viewId)
        Glide.with(mContext).load(imgUrl).into(view)
        return this
    }

    fun setImageUrl(viewId: Int, imgUrl: String, placeHolderRes: Int): BaseViewHolder<*> {
        val view = getView<ImageView>(viewId)
        Glide.with(mContext).load(imgUrl).placeholder(placeHolderRes).into(view)
        return this
    }

    fun setCircleImageUrl(viewId: Int, imgUrl: String, placeHolderRes: Int): BaseViewHolder<*> {
        val view = getView<ImageView>(viewId)
        Glide.with(mContext).load(imgUrl).placeholder(placeHolderRes).transform(GlideCircleTransform(mContext)).into(view)
        return this
    }

    fun setRoundImageUrl(viewId: Int, imgUrl: String, placeHolderRes: Int): BaseViewHolder<*> {
        val view = getView<ImageView>(viewId)
        Glide.with(mContext).load(imgUrl).placeholder(placeHolderRes).transform(GlideRoundTransform(mContext)).into(view)
        return this
    }

    fun setImageBitmap(viewId: Int, imgBitmap: Bitmap): BaseViewHolder<*> {
        val view = getView<ImageView>(viewId)
        view.setImageBitmap(imgBitmap)
        return this
    }

    fun setVisible(viewId: Int, visible: Boolean): BaseViewHolder<*> {
        val view = getView<View>(viewId)
        view.visibility = if (visible) View.VISIBLE else View.GONE
        return this
    }

    fun setVisible(viewId: Int, visible: Int): BaseViewHolder<*> {
        val view = getView<View>(viewId)
        view.visibility = visible
        return this
    }

    fun setTag(viewId: Int, tag: Any): BaseViewHolder<*> {
        val view = getView<View>(viewId)
        view.tag = tag
        return this
    }

    fun setTag(viewId: Int, key: Int, tag: Any): BaseViewHolder<*> {
        val view = getView<View>(viewId)
        view.setTag(key, tag)
        return this
    }

    fun setChecked(viewId: Int, checked: Boolean): BaseViewHolder<*> {
        val view = getView<View>(viewId)
        view.setChecked(checked)
        return this
    }

    fun setAlpha(viewId: Int, value: Float): BaseViewHolder<*> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getView<View>(viewId).alpha = value
        } else {
            val alpha = AlphaAnimation(value, value)
            alpha.duration = 0
            alpha.fillAfter = true
            getView<View>(viewId).startAnimation(alpha)
        }
        return this
    }

    fun setTypeface(viewId: Int, typeface: Typeface): BaseViewHolder<*> {
        val view = getView<TextView>(viewId)
        view.typeface = typeface
        view.paintFlags = view.paintFlags or Paint.SUBPIXEL_TEXT_FLAG
        return this
    }

    fun setTypeface(typeface: Typeface, vararg viewIds: Int): BaseViewHolder<*> {
        for (viewId in viewIds) {
            val view = getView<TextView>(viewId)
            view.typeface = typeface
            view.paintFlags = view.paintFlags or Paint.SUBPIXEL_TEXT_FLAG
        }
        return this
    }

    fun setOnClickListener(viewId: Int, listener: View.OnClickListener): BaseViewHolder<*> {
        val view = getView<View>(viewId)
        view.setOnClickListener(listener)
        return this
    }

}