package com.chat.sticker.entity

/**
 * 2021/8/4 14:46
 * 表情详情
 */
class StickerDetail {
    var category: String = ""
    var cover: String = ""
    var cover_lim: String = ""
    var title: String = ""
    var desc: String = ""
    var added: Boolean = false
    lateinit var list: List<Sticker>
}