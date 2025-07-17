package com.chat.sticker.entity

/**
 * 12/31/20 10:57 AM
 * 表情tab
 */
class StickerTab(
    iconResource: Int,
    icon: String,
    coverTgs: String,
    category: String,
    title: String
) {
    var iconResource: Int = iconResource
    var icon: String = icon
    var coverTgs: String = coverTgs
    var category: String = category
    var title: String = title
    var isChecked = false
}