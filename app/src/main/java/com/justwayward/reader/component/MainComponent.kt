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

import com.justwayward.reader.ui.activity.MainActivity
import com.justwayward.reader.ui.activity.SettingActivity
import com.justwayward.reader.ui.activity.WifiBookActivity
import com.justwayward.reader.ui.fragment.RecommendFragment

import dagger.Component

@Component(dependencies = [AppComponent::class])
interface MainComponent {
    fun inject(activity: MainActivity): MainActivity

    fun inject(fragment: RecommendFragment): RecommendFragment

    fun inject(activity: SettingActivity): SettingActivity
    fun inject(activity: WifiBookActivity): WifiBookActivity
}