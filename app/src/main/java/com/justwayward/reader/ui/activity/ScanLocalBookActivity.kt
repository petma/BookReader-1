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

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseActivity
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.Recommend
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.manager.CollectionsManager
import com.justwayward.reader.manager.EventManager
import com.justwayward.reader.ui.easyadapter.RecommendAdapter
import com.justwayward.reader.utils.AppUtils
import com.justwayward.reader.utils.FileUtils
import com.justwayward.reader.view.recyclerview.EasyRecyclerView
import com.justwayward.reader.view.recyclerview.adapter.RecyclerArrayAdapter

import java.io.File
import java.util.ArrayList

import butterknife.Bind

/**
 * 扫描本地书籍
 *
 * @author yuyh.
 * @date 2016/10/9.
 */
class ScanLocalBookActivity : BaseActivity(), RecyclerArrayAdapter.OnItemClickListener {

    @Bind(R.id.recyclerview)
    internal var mRecyclerView: EasyRecyclerView? = null

    private var mAdapter: RecommendAdapter? = null

    override val layoutId: Int
        get() = R.layout.activity_scan_local_book

    override fun setupActivityComponent(appComponent: AppComponent?) {

    }

    override fun initToolBar() {
        mCommonToolbar!!.title = "扫描本地书籍"
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {}

    override fun configViews() {
        mRecyclerView!!.setLayoutManager(LinearLayoutManager(this))
        mRecyclerView!!.setItemDecoration(ContextCompat.getColor(this, R.color.common_divider_narrow), 1, 0, 0)

        mAdapter = RecommendAdapter(this)
        mAdapter!!.setOnItemClickListener(this)
        mRecyclerView!!.setAdapterWithProgress(mAdapter)

        queryFiles()
    }

    private fun queryFiles() {
        val projection = arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.SIZE)

        // cache
        val bookpath = FileUtils.createRootPath(AppUtils.appContext)

        // 查询后缀名为txt与pdf，并且不位于项目缓存中的文档
        val cursor = contentResolver.query(
                Uri.parse("content://media/external/file"),
                projection,
                MediaStore.Files.FileColumns.DATA + " not like ? and ("
                        + MediaStore.Files.FileColumns.DATA + " like ? or "
                        + MediaStore.Files.FileColumns.DATA + " like ? or "
                        + MediaStore.Files.FileColumns.DATA + " like ? or "
                        + MediaStore.Files.FileColumns.DATA + " like ? )",
                arrayOf("%$bookpath%", "%" + Constant.SUFFIX_TXT, "%" + Constant.SUFFIX_PDF, "%" + Constant.SUFFIX_EPUB, "%" + Constant.SUFFIX_CHM), null)

        if (cursor != null && cursor.moveToFirst()) {
            val idindex = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
            val dataindex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
            val sizeindex = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
            val list = ArrayList<Recommend.RecommendBooks>()


            do {
                val path = cursor.getString(dataindex)

                val dot = path.lastIndexOf("/")
                var name = path.substring(dot + 1)
                if (name.lastIndexOf(".") > 0)
                    name = name.substring(0, name.lastIndexOf("."))

                val books = Recommend.RecommendBooks()
                books._id = name
                books.path = path
                books.title = name
                books.isFromSD = true
                books.lastChapter = FileUtils.formatFileSizeToString(cursor.getLong(sizeindex))

                list.add(books)
            } while (cursor.moveToNext())

            cursor.close()

            mAdapter!!.addAll(list)
        } else {
            mAdapter!!.clear()
        }
    }

    override fun onItemClick(position: Int) {
        val books = mAdapter!!.getItem(position)

        if (books.path.endsWith(Constant.SUFFIX_TXT)) {
            // TXT
            AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage(String.format(getString(
                            R.string.book_detail_is_joined_the_book_shelf), books.title))
                    .setPositiveButton("确定") { dialog, which ->
                        // 拷贝到缓存目录
                        FileUtils.fileChannelCopy(File(books.path),
                                File(FileUtils.getChapterPath(books._id, 1)))
                        // 加入书架
                        if (CollectionsManager.instance!!.add(books)) {
                            mRecyclerView!!.showTipViewAndDelayClose(String.format(getString(
                                    R.string.book_detail_has_joined_the_book_shelf), books.title))
                            // 通知
                            EventManager.refreshCollectionList()
                        } else {
                            mRecyclerView!!.showTipViewAndDelayClose("书籍已存在")
                        }
                        dialog.dismiss()
                    }.setNegativeButton("取消") { dialog, which -> dialog.dismiss() }.show()
        } else if (books.path.endsWith(Constant.SUFFIX_PDF)) {
            // PDF
            ReadPDFActivity.start(this, books.path)
        } else if (books.path.endsWith(Constant.SUFFIX_EPUB)) {
            // EPub
            ReadEPubActivity.start(this, books.path)
        } else if (books.path.endsWith(Constant.SUFFIX_CHM)) {
            // CHM
            ReadCHMActivity.start(this, books.path)
        }
    }

    companion object {

        fun startActivity(context: Context) {
            context.startActivity(Intent(context, ScanLocalBookActivity::class.java))
        }
    }
}
