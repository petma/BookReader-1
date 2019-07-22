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
package com.justwayward.reader.ui.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.justwayward.reader.R
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.RankingList
import com.justwayward.reader.common.OnRvItemClickListener
import com.yuyh.easyadapter.glide.GlideCircleTransform

/**
 * @author yuyh.
 * @date 16/9/1.
 */
class TopRankAdapter(private val mContext: Context, private val groupArray: List<RankingList.MaleBean>, private val childArray: List<List<RankingList.MaleBean>>) : BaseExpandableListAdapter() {
    private val inflater: LayoutInflater

    private var listener: OnRvItemClickListener<RankingList.MaleBean>? = null

    init {
        inflater = LayoutInflater.from(mContext)
    }

    override fun getGroupCount(): Int {
        return groupArray.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return childArray[groupPosition].size
    }

    override fun getGroup(groupPosition: Int): Any {
        return groupArray[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return childArray[groupPosition][childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View, parent: ViewGroup): View {
        val group = inflater.inflate(R.layout.item_top_rank_group, null)

        val ivCover = group.findViewById<View>(R.id.ivRankCover) as ImageView
        if (!TextUtils.isEmpty(groupArray[groupPosition].cover)) {
            Glide.with(mContext).load(Constant.IMG_BASE_URL + groupArray[groupPosition].cover!!).placeholder(R.drawable.avatar_default)
                    .transform(GlideCircleTransform(mContext)).into(ivCover)
            group.setOnClickListener {
                if (listener != null) {
                    listener!!.onItemClick(group, groupPosition, groupArray[groupPosition])
                }
            }
        } else {
            ivCover.setImageResource(R.drawable.ic_rank_collapse)
        }

        val tvName = group.findViewById<View>(R.id.tvRankGroupName) as TextView
        tvName.text = groupArray[groupPosition].title

        val ivArrow = group.findViewById<View>(R.id.ivRankArrow) as ImageView
        if (childArray[groupPosition].size > 0) {
            if (isExpanded) {
                ivArrow.setImageResource(R.drawable.rank_arrow_up)
            } else {
                ivArrow.setImageResource(R.drawable.rank_arrow_down)
            }
        } else {
            ivArrow.visibility = View.GONE
        }
        return group
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View, parent: ViewGroup): View {
        val child = inflater.inflate(R.layout.item_top_rank_child, null)

        val tvName = child.findViewById<View>(R.id.tvRankChildName) as TextView
        tvName.text = childArray[groupPosition][childPosition].title

        child.setOnClickListener { listener!!.onItemClick(child, childPosition, childArray[groupPosition][childPosition]) }
        return child
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    fun setItemClickListener(listener: OnRvItemClickListener<RankingList.MaleBean>) {
        this.listener = listener
    }
}
