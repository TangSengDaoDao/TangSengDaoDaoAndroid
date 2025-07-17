package com.chat.sticker.entity

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.chat.base.emoji.EmojiEntry

class EmojiEntity(var entry: EmojiEntry, override val itemType: Int) : MultiItemEntity {
}