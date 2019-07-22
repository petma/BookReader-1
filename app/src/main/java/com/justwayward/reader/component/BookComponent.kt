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

import com.justwayward.reader.ui.activity.BookDetailActivity
import com.justwayward.reader.ui.activity.BookSourceActivity
import com.justwayward.reader.ui.activity.BooksByTagActivity
import com.justwayward.reader.ui.activity.ReadActivity
import com.justwayward.reader.ui.activity.SearchActivity
import com.justwayward.reader.ui.activity.SearchByAuthorActivity
import com.justwayward.reader.ui.fragment.BookDetailDiscussionFragment
import com.justwayward.reader.ui.fragment.BookDetailReviewFragment

import dagger.Component

@Component(dependencies = [AppComponent::class])
interface BookComponent {
    fun inject(activity: BookDetailActivity): BookDetailActivity

    fun inject(activity: ReadActivity): ReadActivity

    fun inject(activity: BookSourceActivity): BookSourceActivity

    fun inject(activity: BooksByTagActivity): BooksByTagActivity

    fun inject(activity: SearchActivity): SearchActivity

    fun inject(activity: SearchByAuthorActivity): SearchByAuthorActivity

    fun inject(fragment: BookDetailReviewFragment): BookDetailReviewFragment

    fun inject(fragment: BookDetailDiscussionFragment): BookDetailDiscussionFragment
}