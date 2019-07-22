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
package com.justwayward.reader.manager

import com.justwayward.reader.R
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.support.BookMark
import com.justwayward.reader.utils.AppUtils
import com.justwayward.reader.utils.ScreenUtils
import com.justwayward.reader.utils.SharedPreferencesUtil
import com.justwayward.reader.utils.ToastUtils

import java.util.ArrayList

/**
 * @author yuyh.
 * @date 2016/9/23.
 */
class SettingManager {

    val readFontSize: Int
        get() = getReadFontSize("")

    val readBrightness: Int
        get() = ScreenUtils.screenBrightness

    private val lightnessKey: String
        get() = "readLightness"

    val readTheme: Int
        get() = if (SharedPreferencesUtil.instance!!.getBoolean(Constant.ISNIGHT, false)) {
            ThemeManager.NIGHT
        } else SharedPreferencesUtil.instance!!.getInt("readTheme", 3)

    val isVolumeFlipEnable: Boolean
        get() = SharedPreferencesUtil.instance!!.getBoolean("volumeFlip", true)

    val isAutoBrightness: Boolean
        get() = SharedPreferencesUtil.instance!!.getBoolean("autoBrightness", false)

    /**
     * 获取用户选择性别
     *
     * @return
     */
    val userChooseSex: String?
        get() = SharedPreferencesUtil.instance!!.getString("userChooseSex", Constant.Gender.MALE)

    val isUserChooseSex: Boolean
        get() = SharedPreferencesUtil.instance!!.exists("userChooseSex")

    val isNoneCover: Boolean
        get() = SharedPreferencesUtil.instance!!.getBoolean("isNoneCover", false)

    /**
     * 保存书籍阅读字体大小
     *
     * @param bookId     需根据bookId对应，避免由于字体大小引起的分页不准确
     * @param fontSizePx
     * @return
     */
    fun saveFontSize(bookId: String, fontSizePx: Int) {
        // 书籍对应
        SharedPreferencesUtil.instance!!.putInt(getFontSizeKey(bookId), fontSizePx)
    }

    /**
     * 保存全局生效的阅读字体大小
     *
     * @param fontSizePx
     */
    fun saveFontSize(fontSizePx: Int) {
        saveFontSize("", fontSizePx)
    }

    fun getReadFontSize(bookId: String): Int {
        return SharedPreferencesUtil.instance!!.getInt(getFontSizeKey(bookId), ScreenUtils.dpToPxInt(16f))
    }

    private fun getFontSizeKey(bookId: String): String {
        return "$bookId-readFontSize"
    }

    /**
     * 保存阅读界面屏幕亮度
     *
     * @param percent 亮度比例 0~100
     */
    fun saveReadBrightness(percent: Int) {
        var percent = percent
        if (percent > 100) {
            ToastUtils.showToast("saveReadBrightnessErr CheckRefs")
            percent = 100
        }
        SharedPreferencesUtil.instance!!.putInt(lightnessKey, percent)
    }

    @Synchronized
    fun saveReadProgress(bookId: String, currentChapter: Int, m_mbBufBeginPos: Int, m_mbBufEndPos: Int) {
        SharedPreferencesUtil.instance!!
                .putInt(getChapterKey(bookId), currentChapter)
                .putInt(getStartPosKey(bookId), m_mbBufBeginPos)
                .putInt(getEndPosKey(bookId), m_mbBufEndPos)
    }

    /**
     * 获取上次阅读章节及位置
     *
     * @param bookId
     * @return
     */
    fun getReadProgress(bookId: String): IntArray {
        val lastChapter = SharedPreferencesUtil.instance!!.getInt(getChapterKey(bookId), 1)
        val startPos = SharedPreferencesUtil.instance!!.getInt(getStartPosKey(bookId), 0)
        val endPos = SharedPreferencesUtil.instance!!.getInt(getEndPosKey(bookId), 0)

        return intArrayOf(lastChapter, startPos, endPos)
    }

    fun removeReadProgress(bookId: String) {
        SharedPreferencesUtil.instance!!
                .remove(getChapterKey(bookId))
                .remove(getStartPosKey(bookId))
                .remove(getEndPosKey(bookId))
    }

    private fun getChapterKey(bookId: String): String {
        return "$bookId-chapter"
    }

    private fun getStartPosKey(bookId: String): String {
        return "$bookId-startPos"
    }

    private fun getEndPosKey(bookId: String): String {
        return "$bookId-endPos"
    }


    fun addBookMark(bookId: String, mark: BookMark): Boolean {
        var marks: MutableList<BookMark>? = SharedPreferencesUtil.instance!!.getObject(getBookMarksKey(bookId), ArrayList<*>::class.java)
        if (marks != null && marks.size > 0) {
            for (item in marks) {
                if (item.chapter == mark.chapter && item.startPos == mark.startPos) {
                    return false
                }
            }
        } else {
            marks = ArrayList()
        }
        marks.add(mark)
        SharedPreferencesUtil.instance!!.putObject(getBookMarksKey(bookId), marks)
        return true
    }

    fun getBookMarks(bookId: String): List<BookMark>? {
        return SharedPreferencesUtil.instance!!.getObject(getBookMarksKey(bookId), ArrayList<*>::class.java)
    }

    fun clearBookMarks(bookId: String) {
        SharedPreferencesUtil.instance!!.remove(getBookMarksKey(bookId))
    }

    private fun getBookMarksKey(bookId: String): String {
        return "$bookId-marks"
    }

    fun saveReadTheme(theme: Int) {
        SharedPreferencesUtil.instance!!.putInt("readTheme", theme)
    }

    /**
     * 是否可以使用音量键翻页
     *
     * @param enable
     */
    fun saveVolumeFlipEnable(enable: Boolean) {
        SharedPreferencesUtil.instance!!.putBoolean("volumeFlip", enable)
    }

    fun saveAutoBrightness(enable: Boolean) {
        SharedPreferencesUtil.instance!!.putBoolean("autoBrightness", enable)
    }


    /**
     * 保存用户选择的性别
     *
     * @param sex male female
     */
    fun saveUserChooseSex(sex: String) {
        SharedPreferencesUtil.instance!!.putString("userChooseSex", sex)
    }

    fun saveNoneCover(isNoneCover: Boolean) {
        SharedPreferencesUtil.instance!!.putBoolean("isNoneCover", isNoneCover)
    }

    companion object {

        @Volatile
        private var manager: SettingManager? = null

        val instance: SettingManager?
            get() = if (manager != null) manager else manager = SettingManager()
    }
}
