package com.chat.sticker.entity

import com.chad.library.adapter.base.entity.MultiItemEntity

class StickerUI(
    var viewType: Int,
    var title: String,
    val category: String,
    sticker: Sticker?,
) :
    MultiItemEntity {
    var sticker: Sticker? = null
    override val itemType: Int
        get() = viewType

    init {
        this.sticker = sticker
    }
}