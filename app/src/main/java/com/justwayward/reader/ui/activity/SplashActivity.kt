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

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.TextView

import com.justwayward.reader.R

import butterknife.Bind
import butterknife.ButterKnife

class SplashActivity : AppCompatActivity() {

    @Bind(R.id.tvSkip)
    internal var tvSkip: TextView? = null

    private var flag = false
    private var runnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        ButterKnife.bind(this)

        runnable = Runnable { goHome() }

        tvSkip!!.postDelayed(runnable, 2000)

        tvSkip!!.setOnClickListener { goHome() }
    }

    @Synchronized
    private fun goHome() {
        if (!flag) {
            flag = true
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        flag = true
        tvSkip!!.removeCallbacks(runnable)
        ButterKnife.unbind(this)
    }
}
