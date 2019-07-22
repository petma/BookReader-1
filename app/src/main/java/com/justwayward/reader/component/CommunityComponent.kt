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

import com.justwayward.reader.ui.activity.BookDiscussionDetailActivity
import com.justwayward.reader.ui.activity.BookHelpDetailActivity
import com.justwayward.reader.ui.activity.BookReviewDetailActivity
import com.justwayward.reader.ui.fragment.BookDiscussionFragment
import com.justwayward.reader.ui.fragment.BookHelpFragment
import com.justwayward.reader.ui.fragment.BookReviewFragment
import com.justwayward.reader.ui.fragment.GirlBookDiscussionFragment

import dagger.Component

/**
 * @author yuyh.
 * @date 16/9/2.
 */
@Component(dependencies = [AppComponent::class])
interface CommunityComponent {

    fun inject(fragment: BookDiscussionFragment): BookDiscussionFragment

    fun inject(activity: BookDiscussionDetailActivity): BookDiscussionDetailActivity

    fun inject(fragment: BookReviewFragment): BookReviewFragment

    fun inject(activity: BookReviewDetailActivity): BookReviewDetailActivity

    fun inject(fragment: BookHelpFragment): BookHelpFragment

    fun inject(activity: BookHelpDetailActivity): BookHelpDetailActivity

    fun inject(fragment: GirlBookDiscussionFragment): GirlBookDiscussionFragment
}