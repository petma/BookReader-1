package com.justwayward.reader.bean.user

class HtmlBook {

    var name: String? = null
    var bookid: String? = null
    var size: String? = null

    constructor() {}

    constructor(name: String, bookid: String, size: String) {
        this.name = name
        this.size = size
        this.bookid = bookid
    }
}
