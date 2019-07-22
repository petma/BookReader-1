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
import android.content.Intent

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseCommuniteActivity
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.ui.fragment.BookDiscussionFragment
import com.justwayward.reader.view.SelectionLayout

import butterknife.Bind

/**
 * 综合讨论区
 */
class BookDiscussionActivity : BaseCommuniteActivity() {

    private var mIsDiscussion: Boolean = false

    @Bind(R.id.slOverall)
    internal var slOverall: SelectionLayout? = null

    override val layoutId: Int
        get() = R.layout.activity_community_book_discussion

    protected override val tabList: List<List<String>>
        get() = list1

    override fun setupActivityComponent(appComponent: AppComponent?) {

    }

    override fun initToolBar() {
        mIsDiscussion = intent.getBooleanExtra(INTENT_DIS, false)
        if (mIsDiscussion) {
            mCommonToolbar!!.title = "综合讨论区"
        } else {
            mCommonToolbar!!.title = "原创区"
        }
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {
        super.initDatas()
    }

    override fun configViews() {
        val fragment = BookDiscussionFragment.newInstance(if (mIsDiscussion) "ramble" else "original")
        supportFragmentManager.beginTransaction().replace(R.id.fragmentCO, fragment).commit()
    }

    companion object {

        private val INTENT_DIS = "isDis"

        fun startActivity(context: Context, isDiscussion: Boolean) {
            context.startActivity(Intent(context, BookDiscussionActivity::class.java)
                    .putExtra(INTENT_DIS, isDiscussion))
        }
    }
}
