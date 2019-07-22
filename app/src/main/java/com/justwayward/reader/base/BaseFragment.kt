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
package com.justwayward.reader.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.justwayward.reader.ReaderApplication
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.view.loadding.CustomDialog

import butterknife.ButterKnife

abstract class BaseFragment : Fragment() {

    protected var parentView: View
    protected var activity: FragmentActivity? = null
    protected var inflater: LayoutInflater

    protected var mContext: Context? = null

    private var dialog: CustomDialog? = null

    @get:LayoutRes
    abstract val layoutResId: Int

    val supportActivity: FragmentActivity?
        get() = super.getActivity()

    val applicationContext: Context?
        get() = if (this.activity == null)
            if (getActivity() == null)
                null
            else
                getActivity()!!
                        .applicationContext
        else
            this.activity!!.applicationContext

    protected abstract fun setupActivityComponent(appComponent: AppComponent?)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        parentView = inflater.inflate(layoutResId, container, false)
        activity = supportActivity
        mContext = activity
        this.inflater = inflater
        return parentView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
        setupActivityComponent(ReaderApplication.getsInstance()!!.appComponent)
        attachView()
        initDatas()
        configViews()
    }

    abstract fun attachView()

    abstract fun initDatas()

    /**
     * 对各种控件进行设置、适配、填充数据
     */
    abstract fun configViews()

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        this.activity = activity as FragmentActivity?
    }

    override fun onDetach() {
        super.onDetach()
        this.activity = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ButterKnife.unbind(this)
    }

    protected override fun getLayoutInflater(): LayoutInflater {
        return inflater
    }

    fun getDialog(): CustomDialog {
        if (dialog == null) {
            dialog = CustomDialog.instance(getActivity())
            dialog!!.setCancelable(false)
        }
        return dialog
    }

    fun hideDialog() {
        if (dialog != null)
            dialog!!.hide()
    }

    fun showDialog() {
        getDialog().show()
    }

    fun dismissDialog() {
        if (dialog != null) {
            dialog!!.dismiss()
            dialog = null
        }
    }

    protected fun gone(vararg views: View) {
        if (views != null && views.size > 0) {
            for (view in views) {
                if (view != null) {
                    view.visibility = View.GONE
                }
            }
        }
    }

    protected fun visible(vararg views: View) {
        if (views != null && views.size > 0) {
            for (view in views) {
                if (view != null) {
                    view.visibility = View.VISIBLE
                }
            }
        }

    }

    protected fun isVisible(view: View): Boolean {
        return view.visibility == View.VISIBLE
    }


}