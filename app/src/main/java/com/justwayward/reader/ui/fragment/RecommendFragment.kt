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
package com.justwayward.reader.ui.fragment

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.os.AsyncTask
import androidx.appcompat.app.AlertDialog
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseRVFragment
import com.justwayward.reader.bean.BookMixAToc
import com.justwayward.reader.bean.Recommend
import com.justwayward.reader.bean.support.DownloadMessage
import com.justwayward.reader.bean.support.DownloadProgress
import com.justwayward.reader.bean.support.DownloadQueue
import com.justwayward.reader.bean.support.RefreshCollectionListEvent
import com.justwayward.reader.bean.support.UserSexChooseFinishedEvent
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerMainComponent
import com.justwayward.reader.manager.CollectionsManager
import com.justwayward.reader.service.DownloadBookService
import com.justwayward.reader.ui.activity.BookDetailActivity
import com.justwayward.reader.ui.activity.MainActivity
import com.justwayward.reader.ui.activity.ReadActivity
import com.justwayward.reader.ui.contract.RecommendContract
import com.justwayward.reader.ui.easyadapter.RecommendAdapter
import com.justwayward.reader.ui.presenter.RecommendPresenter
import com.justwayward.reader.view.recyclerview.adapter.RecyclerArrayAdapter

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.util.ArrayList

import butterknife.Bind
import butterknife.OnClick

class RecommendFragment : BaseRVFragment<RecommendPresenter, Recommend.RecommendBooks>(), RecommendContract.View, RecyclerArrayAdapter.OnItemLongClickListener {

    @Bind(R.id.llBatchManagement)
    internal var llBatchManagement: LinearLayout? = null
    @Bind(R.id.tvSelectAll)
    internal var tvSelectAll: TextView? = null
    @Bind(R.id.tvDelete)
    internal var tvDelete: TextView? = null

    private var isSelectAll = false

    private val chaptersList = ArrayList<BookMixAToc.mixToc.Chapters>()

    override val layoutResId: Int
        get() = R.layout.fragment_recommend

    private val isForeground: Boolean
        get() {
            val am = activity!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val list = am.getRunningTasks(1)
            if (list != null && list.size > 0) {
                val cpn = list[0].topActivity
                if (MainActivity::class.java.name.contains(cpn.className)) {
                    return true
                }
            }

            return false
        }

    override fun initDatas() {
        EventBus.getDefault().register(this)
    }

    override fun configViews() {
        initAdapter(RecommendAdapter::class.java, true, false)
        mAdapter!!.setOnItemLongClickListener(this)
        mAdapter!!.addFooter(object : RecyclerArrayAdapter.ItemView {
            override fun onCreateView(parent: ViewGroup): View {
                return LayoutInflater.from(activity).inflate(R.layout.foot_view_shelf, parent, false)
            }

            override fun onBindView(headerView: View) {
                headerView.setOnClickListener { (activity as MainActivity).setCurrentItem(2) }
            }
        })
        mRecyclerView!!.emptyView!!.findViewById<View>(R.id.btnToAdd).setOnClickListener { (activity as MainActivity).setCurrentItem(2) }
        onRefresh()
    }

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerMainComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun showRecommendList(list: List<Recommend.RecommendBooks>) {
        mAdapter!!.clear()
        mAdapter!!.addAll(list)
        //推荐列表默认加入收藏
        for (bean in list) {
            //TODO 此处可优化：批量加入收藏->加入前需先判断是否收藏过
            CollectionsManager.instance!!.add(bean)
        }
    }

    override fun showBookToc(bookId: String, list: List<BookMixAToc.mixToc.Chapters>) {
        chaptersList.clear()
        chaptersList.addAll(list)
        DownloadBookService.post(DownloadQueue(bookId, list, 1, list.size))
        dismissDialog()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun downloadMessage(msg: DownloadMessage) {
        mRecyclerView!!.setTipViewText(msg.message)
        if (msg.isComplete) {
            mRecyclerView!!.hideTipView(2200)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showDownProgress(progress: DownloadProgress) {
        mRecyclerView!!.setTipViewText(progress.message)
    }

    override fun onItemClick(position: Int) {
        if (isVisible(llBatchManagement))
        //批量管理时，屏蔽点击事件
            return
        ReadActivity.startActivity(activity!!, mAdapter!!.getItem(position), mAdapter!!.getItem(position).isFromSD)
    }

    override fun onItemLongClick(position: Int): Boolean {
        //批量管理时，屏蔽长按事件
        if (isVisible(llBatchManagement)) return false
        showLongClickDialog(position)
        return false
    }

    /**
     * 显示长按对话框
     *
     * @param position
     */
    private fun showLongClickDialog(position: Int) {
        val isTop = CollectionsManager.instance!!.isTop(mAdapter!!.getItem(position)._id)
        val items: Array<String>
        val listener: DialogInterface.OnClickListener
        if (mAdapter!!.getItem(position).isFromSD) {
            items = resources.getStringArray(R.array.recommend_item_long_click_choice_local)
            listener = DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    0 ->
                        //置顶、取消置顶
                        CollectionsManager.instance!!.top(mAdapter!!.getItem(position)._id, !isTop)
                    1 -> {
                        //删除
                        val removeList = ArrayList<Recommend.RecommendBooks>()
                        removeList.add(mAdapter!!.getItem(position))
                        showDeleteCacheDialog(removeList)
                    }
                    2 ->
                        //批量管理
                        showBatchManagementLayout()
                    else -> {
                    }
                }
                dialog.dismiss()
            }
        } else {
            items = resources.getStringArray(R.array.recommend_item_long_click_choice)
            listener = DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    0 ->
                        //置顶、取消置顶
                        CollectionsManager.instance!!.top(mAdapter!!.getItem(position)._id, !isTop)
                    1 ->
                        //书籍详情
                        BookDetailActivity.startActivity(activity!!,
                                mAdapter!!.getItem(position)._id)
                    2 ->
                        //移入养肥区
                        mRecyclerView!!.showTipViewAndDelayClose("正在拼命开发中...")
                    3 ->
                        //缓存全本
                        if (mAdapter!!.getItem(position).isFromSD) {
                            mRecyclerView!!.showTipViewAndDelayClose("本地文件不支持该选项哦")
                        } else {
                            showDialog()
                            mPresenter!!.getTocList(mAdapter!!.getItem(position)._id)
                        }
                    4 -> {
                        //删除
                        val removeList = ArrayList<Recommend.RecommendBooks>()
                        removeList.add(mAdapter!!.getItem(position))
                        showDeleteCacheDialog(removeList)
                    }
                    5 ->
                        //批量管理
                        showBatchManagementLayout()
                    else -> {
                    }
                }
                dialog.dismiss()
            }
        }
        if (isTop) items[0] = getString(R.string.cancle_top)
        AlertDialog.Builder(activity!!)
                .setTitle(mAdapter!!.getItem(position).title)
                .setItems(items, listener)
                .setNegativeButton(null, null)
                .create().show()
    }

    /**
     * 显示删除本地缓存对话框
     *
     * @param removeList
     */
    private fun showDeleteCacheDialog(removeList: List<Recommend.RecommendBooks>) {
        val selected = booleanArrayOf(true)
        AlertDialog.Builder(activity!!)
                .setTitle(activity!!.getString(R.string.remove_selected_book))
                .setMultiChoiceItems(arrayOf(activity!!.getString(R.string.delete_local_cache)), selected
                ) { dialog, which, isChecked -> selected[0] = isChecked }
                .setPositiveButton(activity!!.getString(R.string.confirm)) { dialog, which ->
                    dialog.dismiss()
                    object : AsyncTask<String, String, String>() {
                        override fun onPreExecute() {
                            super.onPreExecute()
                            showDialog()
                        }

                        override fun doInBackground(vararg params: String): String? {
                            CollectionsManager.instance!!.removeSome(removeList, selected[0])
                            return null
                        }

                        override fun onPostExecute(s: String) {
                            super.onPostExecute(s)
                            mRecyclerView!!.showTipViewAndDelayClose("成功移除书籍")
                            for (bean in removeList) {
                                mAdapter!!.remove(bean)
                            }
                            if (isVisible(llBatchManagement)) {
                                //批量管理完成后，隐藏批量管理布局并刷新页面
                                goneBatchManagementAndRefreshUI()
                            }
                            hideDialog()
                        }
                    }.execute()
                }
                .setNegativeButton(activity!!.getString(R.string.cancel), null)
                .create().show()
    }

    /**
     * 隐藏批量管理布局并刷新页面
     */
    fun goneBatchManagementAndRefreshUI() {
        if (mAdapter == null) return
        gone(llBatchManagement)
        for (bean in mAdapter!!.allData) {
            bean.showCheckBox = false
        }
        mAdapter!!.notifyDataSetChanged()
    }

    /**
     * 显示批量管理布局
     */
    private fun showBatchManagementLayout() {
        visible(llBatchManagement)
        for (bean in mAdapter!!.allData) {
            bean.showCheckBox = true
        }
        mAdapter!!.notifyDataSetChanged()
    }


    @OnClick(R.id.tvSelectAll)
    fun selectAll() {
        isSelectAll = !isSelectAll
        tvSelectAll!!.text = if (isSelectAll) activity!!.getString(R.string.cancel_selected_all) else activity!!.getString(R.string.selected_all)
        for (bean in mAdapter!!.allData) {
            bean.isSeleted = isSelectAll
        }
        mAdapter!!.notifyDataSetChanged()
    }

    @OnClick(R.id.tvDelete)
    fun delete() {
        val removeList = ArrayList<Recommend.RecommendBooks>()
        for (bean in mAdapter!!.allData) {
            if (bean.isSeleted) removeList.add(bean)
        }
        if (removeList.isEmpty()) {
            mRecyclerView!!.showTipViewAndDelayClose(activity!!.getString(R.string.has_not_selected_delete_book))
        } else {
            showDeleteCacheDialog(removeList)
        }
    }

    override fun onRefresh() {
        super.onRefresh()
        val stack = Throwable().stackTrace


        var hasRefBookShelfInCallStack = false
        var isMRefresh = false
        for (i in stack.indices) {
            val ste = stack[i]
            if (ste.methodName == "pullSyncBookShelf") {
                hasRefBookShelfInCallStack = true
            }
            if (ste.methodName == "onAnimationEnd" && ste.fileName == "SwipeRefreshLayout.java") {
                isMRefresh = true
            }
        }

        if (!hasRefBookShelfInCallStack && isMRefresh) {
            (activity as MainActivity).pullSyncBookShelf()
            return
        }


        gone(llBatchManagement)
        val data = CollectionsManager.instance!!.collectionListBySort
        mAdapter!!.clear()
        mAdapter!!.addAll(data)
        //不加下面这句代码会导致，添加本地书籍的时候，部分书籍添加后直接崩溃
        //报错：Scrapped or attached views may not be recycled. isScrap:false isAttached:true
        mAdapter!!.notifyDataSetChanged()
        mRecyclerView!!.setRefreshing(false)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun RefreshCollectionList(event: RefreshCollectionListEvent) {
        mRecyclerView!!.setRefreshing(true)
        onRefresh()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun UserSexChooseFinished(event: UserSexChooseFinishedEvent) {
        //首次进入APP，选择性别后，获取推荐列表
        mPresenter!!.getRecommendList()
    }

    override fun showError() {
        loaddingError()
        dismissDialog()
    }

    override fun complete() {
        mRecyclerView!!.setRefreshing(false)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!userVisibleHint) {
            goneBatchManagementAndRefreshUI()
        }
    }

    override fun onResume() {
        super.onResume()
        //这样监听返回键有个缺点就是没有拦截Activity的返回监听，如果有更优方案可以改掉
        view!!.isFocusableInTouchMode = true
        view!!.requestFocus()
        view!!.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                if (isVisible(llBatchManagement)) {
                    goneBatchManagementAndRefreshUI()
                    return@OnKeyListener true
                }
            }
            false
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

}
