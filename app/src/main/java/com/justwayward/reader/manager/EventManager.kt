package com.justwayward.reader.manager

import com.justwayward.reader.bean.support.RefreshCollectionIconEvent
import com.justwayward.reader.bean.support.RefreshCollectionListEvent
import com.justwayward.reader.bean.support.SubEvent

import org.greenrobot.eventbus.EventBus

/**
 * @author yuyh.
 * @date 17/1/30.
 */

object EventManager {

    fun refreshCollectionList() {
        EventBus.getDefault().post(RefreshCollectionListEvent())
    }

    fun refreshCollectionIcon() {
        EventBus.getDefault().post(RefreshCollectionIconEvent())
    }

    fun refreshSubCategory(minor: String, type: String) {
        EventBus.getDefault().post(SubEvent(minor, type))
    }

}
