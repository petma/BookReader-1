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
package com.justwayward.reader.view

import android.content.Context
import android.media.MediaPlayer
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView

import java.util.Timer
import java.util.TimerTask

/**
 * 打字效果TextView
 *
 * @author yuyh.
 * @date 16/4/10.
 */
class TypeTextView : TextView {
    private var mContext: Context? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var mShowTextString: String? = null
    private var mTypeTimer: Timer? = null
    private var mOnTypeViewListener: OnTypeViewListener? = null
    private var mTypeTimeDelay = TYPE_TIME_DELAY // 打字间隔

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initTypeTextView(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initTypeTextView(context)
    }

    constructor(context: Context) : super(context) {
        initTypeTextView(context)
    }

    fun setOnTypeViewListener(onTypeViewListener: OnTypeViewListener) {
        mOnTypeViewListener = onTypeViewListener
    }

    @JvmOverloads
    fun start(textString: String, typeTimeDelay: Int = TYPE_TIME_DELAY) {
        if (TextUtils.isEmpty(textString) || typeTimeDelay < 0) {
            return
        }
        post {
            mShowTextString = textString
            mTypeTimeDelay = typeTimeDelay
            text = ""
            startTypeTimer()
            if (null != mOnTypeViewListener) {
                mOnTypeViewListener!!.onTypeStart()
            }
        }
    }

    fun stop() {
        stopTypeTimer()
        stopAudio()
    }

    private fun initTypeTextView(context: Context) {
        mContext = context
    }

    private fun startTypeTimer() {
        stopTypeTimer()
        mTypeTimer = Timer()
        mTypeTimer!!.schedule(TypeTimerTask(), mTypeTimeDelay.toLong())
    }

    private fun stopTypeTimer() {
        if (null != mTypeTimer) {
            mTypeTimer!!.cancel()
            mTypeTimer = null
        }
    }

    private fun startAudioPlayer() {
        stopAudio()
        //TODO playAudio(R.raw.type_in);
    }

    private fun playAudio(audioResId: Int) {
        try {
            stopAudio()
            mMediaPlayer = MediaPlayer.create(mContext, audioResId)
            mMediaPlayer!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun stopAudio() {
        if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    internal inner class TypeTimerTask : TimerTask() {
        override fun run() {
            post {
                if (text.toString().length < mShowTextString!!.length) {
                    text = mShowTextString!!.substring(0, text.toString().length + 1)
                    startAudioPlayer()
                    startTypeTimer()
                } else {
                    stopTypeTimer()
                    if (null != mOnTypeViewListener) {
                        mOnTypeViewListener!!.onTypeOver()
                    }
                }
            }
        }
    }

    interface OnTypeViewListener {
        fun onTypeStart()

        fun onTypeOver()
    }

    companion object {
        private val TYPE_TIME_DELAY = 80
    }
}