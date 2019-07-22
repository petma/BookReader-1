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
package com.justwayward.reader.component

import com.justwayward.reader.ui.activity.SubCategoryListActivity
import com.justwayward.reader.ui.activity.SubOtherHomeRankActivity
import com.justwayward.reader.ui.activity.SubRankActivity
import com.justwayward.reader.ui.activity.SubjectBookListActivity
import com.justwayward.reader.ui.activity.SubjectBookListDetailActivity
import com.justwayward.reader.ui.activity.TopCategoryListActivity
import com.justwayward.reader.ui.activity.TopRankActivity
import com.justwayward.reader.ui.fragment.SubCategoryFragment
import com.justwayward.reader.ui.fragment.SubRankFragment
import com.justwayward.reader.ui.fragment.SubjectFragment

import dagger.Component

/**
 * @author yuyh.
 * @date 16/9/1.
 */
@Component(dependencies = [AppComponent::class])
interface FindComponent {

    /** 分类  */
    fun inject(activity: TopCategoryListActivity): TopCategoryListActivity

    fun inject(activity: SubCategoryListActivity): SubCategoryListActivity

    fun inject(fragment: SubCategoryFragment): SubCategoryFragment

    /** 排行  */
    fun inject(activity: TopRankActivity): TopRankActivity

    fun inject(activity: SubRankActivity): SubRankActivity

    fun inject(activity: SubOtherHomeRankActivity): SubOtherHomeRankActivity

    fun inject(fragment: SubRankFragment): SubRankFragment

    /** 主题书单  */
    fun inject(subjectBookListActivity: SubjectBookListActivity): SubjectBookListActivity

    fun inject(subjectFragment: SubjectFragment): SubjectFragment

    fun inject(categoryListActivity: SubjectBookListDetailActivity): SubjectBookListDetailActivity
}
