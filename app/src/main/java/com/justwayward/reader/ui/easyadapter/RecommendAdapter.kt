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
package com.justwayward.reader.ui.easyadapter

import android.content.Context
import android.text.TextUtils
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton

import com.justwayward.reader.R
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.Recommend
import com.justwayward.reader.manager.SettingManager
import com.justwayward.reader.utils.FileUtils
import com.justwayward.reader.utils.FormatUtils
import com.justwayward.reader.view.recyclerview.adapter.BaseViewHolder
import com.justwayward.reader.view.recyclerview.adapter.RecyclerArrayAdapter

import java.text.NumberFormat

/**
 * @author yuyh.
 * @date 2016/9/7.
 */
class RecommendAdapter(context: Context) : RecyclerArrayAdapter<Recommend.RecommendBooks>(context) {

    override fun OnCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return object : BaseViewHolder<Recommend.RecommendBooks>(parent, R.layout.item_recommend_list) {
            override fun setData(item: Recommend.RecommendBooks) {
                super.setData(item)
                var latelyUpdate = ""
                if (!TextUtils.isEmpty(FormatUtils.getDescriptionTimeFromDateString(item.updated))) {
                    latelyUpdate = FormatUtils.getDescriptionTimeFromDateString(item.updated) + ":"
                }

                holder.setText(R.id.tvRecommendTitle, item.title)
                        .setText(R.id.tvLatelyUpdate, latelyUpdate)
                        .setText(R.id.tvRecommendShort, item.lastChapter)
                        .setVisible(R.id.ivTopLabel, item.isTop)
                        .setVisible(R.id.ckBoxSelect, item.showCheckBox)
                        .setVisible(R.id.ivUnReadDot, FormatUtils.formatZhuiShuDateString(item.updated)
                                .compareTo(item.recentReadingTime) > 0)

                if (item.path != null && item.path.endsWith(Constant.SUFFIX_PDF)) {
                    holder.setImageResource(R.id.ivRecommendCover, R.drawable.ic_shelf_pdf)
                } else if (item.path != null && item.path.endsWith(Constant.SUFFIX_EPUB)) {
                    holder.setImageResource(R.id.ivRecommendCover, R.drawable.ic_shelf_epub)
                } else if (item.path != null && item.path.endsWith(Constant.SUFFIX_CHM)) {
                    holder.setImageResource(R.id.ivRecommendCover, R.drawable.ic_shelf_chm)
                } else if (item.isFromSD) {
                    holder.setImageResource(R.id.ivRecommendCover, R.drawable.ic_shelf_txt)
                    val fileLen = FileUtils.getChapterFile(item._id, 1).length()
                    if (fileLen > 10) {
                        val progress = SettingManager.instance!!.getReadProgress(item._id)[2].toDouble() / fileLen
                        val fmt = NumberFormat.getPercentInstance()
                        fmt.maximumFractionDigits = 2
                        holder.setText(R.id.tvRecommendShort, "当前阅读进度：" + fmt.format(progress))
                    }
                } else if (!SettingManager.instance!!.isNoneCover) {
                    holder.setRoundImageUrl(R.id.ivRecommendCover, Constant.IMG_BASE_URL + item.cover!!,
                            R.drawable.cover_default)
                } else {
                    holder.setImageResource(R.id.ivRecommendCover, R.drawable.cover_default)
                }

                val ckBoxSelect = holder.getView<CheckBox>(R.id.ckBoxSelect)
                ckBoxSelect.isChecked = item.isSeleted
                ckBoxSelect.setOnCheckedChangeListener { buttonView, isChecked -> item.isSeleted = isChecked }
            }
        }
    }

}
