package com.chat.sticker.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.ui.Theme
import com.chat.base.utils.AndroidUtilities
import com.chat.sticker.R
import com.chat.sticker.entity.StickerCategory
import com.chat.sticker.ui.components.StickerView

/**
 * 1/4/21 6:06 PM
 * 排序
 */
class StickerReorderAdapter :
    BaseQuickAdapter<StickerCategory, BaseViewHolder>(R.layout.item_sticker_reorder_layout),
    DraggableModule {
    override fun convert(holder: BaseViewHolder, item: StickerCategory) {
        holder.setText(R.id.titleTv, item.title)
        val stickerIv: StickerView = holder.getView(R.id.stickerView)
        stickerIv.showSticker(item.cover_lim, "", AndroidUtilities.dp(35f), true)
        val imageView = holder.getView<ImageView>(R.id.sortIV)
        Theme.setColorFilter(context, imageView, R.color.popupTextColor)
    }

}