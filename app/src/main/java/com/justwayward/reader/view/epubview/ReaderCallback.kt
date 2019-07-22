package com.justwayward.reader.view.epubview

/**
 * @author yuyh.
 * @date 2016/12/15.
 */
interface ReaderCallback {

    fun getPageHref(position: Int): String

    fun toggleToolBarVisible()

    fun hideToolBarIfVisible()


}
