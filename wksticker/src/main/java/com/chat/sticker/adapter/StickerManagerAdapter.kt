package com.chat.sticker.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.utils.AndroidUtilities
import com.chat.sticker.R
import com.chat.sticker.entity.StickerCategory
import com.chat.sticker.ui.components.StickerView

/**
 * 1/4/21 5:23 PM
 * 管理表情
 */
class StickerManagerAdapter : BaseQuickAdapter<StickerCategory, BaseViewHolder>(R.layout.item_sticker_manger_layout) {
    override fun convert(holder: BaseViewHolder, item: StickerCategory) {
        holder.setText(R.id.titleTv, item.title)
        val stickerIv: StickerView = holder.getView(R.id.stickerView)
        stickerIv.showSticker(item.cover_lim, "", AndroidUtilities.dp(35f), true)
    }

}