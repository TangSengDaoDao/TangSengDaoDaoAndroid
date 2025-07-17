package com.chat.sticker.entity

/**
 * 12/30/20 3:12 PM
 * 表情
 */
class Sticker : Comparable<Sticker> {
    override fun compareTo(other: Sticker): Int {
        return this.sort_num.compareTo(other.sort_num)
    }

    var path: String = ""
    var width: Int = 0
    var height: Int = 0
    var sort_num: Int = 0//排序编号
    var category: String = ""
    var title: String = ""
    var placeholder: String = ""// 占位图
    var format: String = ""// 格式
    var searchable_word: String = ""

    //本地字段
    var localPath: String = ""
    var showManager = false
    var isSelected = false
    var isNull = false
}