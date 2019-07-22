package com.justwayward.reader.bean.support

import java.io.Serializable

/**
 * @author yuyh.
 * @date 2016/11/18.
 */
class BookMark : Serializable {

    var chapter: Int = 0

    var title: String? = null

    var startPos: Int = 0

    var endPos: Int = 0

    var desc = ""
}
