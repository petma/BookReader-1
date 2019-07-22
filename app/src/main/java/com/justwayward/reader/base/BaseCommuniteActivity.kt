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
package com.justwayward.reader.base

import com.justwayward.reader.R
import com.justwayward.reader.bean.support.SelectionEvent
import com.justwayward.reader.view.SelectionLayout

import org.greenrobot.eventbus.EventBus

import java.util.ArrayList


/**
 * @author yuyh.
 * @date 16/9/2.
 */
abstract class BaseCommuniteActivity : BaseActivity(), SelectionLayout.OnSelectListener {

    @Bind(R.id.slOverall)
    internal var slOverall: SelectionLayout? = null

    protected var list: List<List<String>>

    protected var list1: List<List<String>> = object : ArrayList<List<String>>() {
        init {
            add(object : ArrayList<String>() {
                init {
                    add("全部")
                    add("精品")
                }
            })
            add(object : ArrayList<String>() {
                init {
                    add("默认排序")
                    add("最新发布")
                    add("最多评论")
                }
            })
        }
    }

    protected var list2: List<List<String>> = object : ArrayList<List<String>>() {
        init {
            add(object : ArrayList<String>() {
                init {
                    add("全部")
                    add("精品")
                }
            })
            add(object : ArrayList<String>() {
                init {
                    add("全部类型")
                    add("玄幻奇幻")
                    add("武侠仙侠")
                    add("都市异能")
                    add("历史军事")
                    add("游戏竞技")
                    add("科幻灵异")
                    add("穿越架空")
                    add("豪门总裁")
                    add("现代言情")
                    add("古代言情")
                    add("幻想言情")
                    add("耽美同人")
                }
            })
            add(object : ArrayList<String>() {
                init {
                    add("默认排序")
                    add("最新发布")
                    add("最多评论")
                    add("最有用的")
                }
            })
        }
    }

    @Constant.Distillate
    private var distillate = Constant.Distillate.ALL
    @Constant.BookType
    private var type = Constant.BookType.ALL
    @Constant.SortType
    private var sort = Constant.SortType.DEFAULT

    protected abstract val tabList: List<List<String>>

    override fun initDatas() {
        list = tabList
        if (slOverall != null) {
            slOverall!!.setData(*list.toTypedArray<List<*>>())
            slOverall!!.setOnSelectListener(this)
        }
    }

    override fun onSelect(index: Int, position: Int, title: String) {
        when (index) {
            0 -> when (position) {
                0 -> distillate = Constant.Distillate.ALL
                1 -> distillate = Constant.Distillate.DISTILLATE
                else -> {
                }
            }
            1 -> if (list.size == 2) {
                sort = Constant.sortTypeList[position]
            } else if (list.size == 3) {
                type = Constant.bookTypeList[position]
            }
            2 -> sort = Constant.sortTypeList[position]
            else -> {
            }
        }

        EventBus.getDefault().post(SelectionEvent(distillate, type, sort))
    }
}
