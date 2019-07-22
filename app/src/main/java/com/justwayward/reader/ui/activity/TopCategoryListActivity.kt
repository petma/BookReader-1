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
package com.justwayward.reader.ui.activity

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseActivity
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.CategoryList
import com.justwayward.reader.common.OnRvItemClickListener
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerFindComponent
import com.justwayward.reader.ui.adapter.TopCategoryListAdapter
import com.justwayward.reader.ui.contract.TopCategoryListContract
import com.justwayward.reader.ui.presenter.TopCategoryListPresenter
import com.justwayward.reader.view.SupportGridItemDecoration

import java.util.ArrayList

import javax.inject.Inject

import butterknife.Bind

/**
 * Created by lfh on 2016/8/30.
 */
class TopCategoryListActivity : BaseActivity(), TopCategoryListContract.View {

    @Bind(R.id.rvMaleCategory)
    internal var mRvMaleCategory: RecyclerView? = null
    @Bind(R.id.rvFemaleCategory)
    internal var mRvFeMaleCategory: RecyclerView? = null

    @Inject
    internal var mPresenter: TopCategoryListPresenter? = null

    private var mMaleCategoryListAdapter: TopCategoryListAdapter? = null
    private var mFemaleCategoryListAdapter: TopCategoryListAdapter? = null
    private val mMaleCategoryList = ArrayList<CategoryList.MaleBean>()
    private val mFemaleCategoryList = ArrayList<CategoryList.MaleBean>()

    override val layoutId: Int
        get() = R.layout.activity_top_category_list

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerFindComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun initToolBar() {
        mCommonToolbar!!.title = getString(R.string.category)
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {

    }

    override fun configViews() {
        showDialog()
        mRvMaleCategory!!.setHasFixedSize(true)
        mRvMaleCategory!!.layoutManager = GridLayoutManager(this, 3)
        mRvMaleCategory!!.addItemDecoration(SupportGridItemDecoration(this))
        mRvFeMaleCategory!!.setHasFixedSize(true)
        mRvFeMaleCategory!!.layoutManager = GridLayoutManager(this, 3)
        mRvFeMaleCategory!!.addItemDecoration(SupportGridItemDecoration(this))
        mMaleCategoryListAdapter = TopCategoryListAdapter(mContext, mMaleCategoryList, ClickListener(Constant.Gender.MALE))
        mFemaleCategoryListAdapter = TopCategoryListAdapter(mContext, mFemaleCategoryList, ClickListener(Constant.Gender.FEMALE))
        mRvMaleCategory!!.adapter = mMaleCategoryListAdapter
        mRvFeMaleCategory!!.adapter = mFemaleCategoryListAdapter

        mPresenter!!.attachView(this)
        mPresenter!!.getCategoryList()
    }


    override fun showCategoryList(data: CategoryList) {
        mMaleCategoryList.clear()
        mFemaleCategoryList.clear()
        mMaleCategoryList.addAll(data.male!!)
        mFemaleCategoryList.addAll(data.female!!)
        mMaleCategoryListAdapter!!.notifyDataSetChanged()
        mFemaleCategoryListAdapter!!.notifyDataSetChanged()
    }

    override fun showError() {

    }

    override fun complete() {
        dismissDialog()
    }

    internal inner class ClickListener(@param:Constant.Gender private val gender: String) : OnRvItemClickListener<CategoryList.MaleBean> {

        override fun onItemClick(view: View, position: Int, data: CategoryList.MaleBean) {
            SubCategoryListActivity.startActivity(mContext, data.name, gender)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mPresenter != null) {
            mPresenter!!.detachView()
        }
    }
}
