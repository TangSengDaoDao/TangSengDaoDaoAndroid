package com.chat.sticker.adapter

import android.widget.Button
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.ui.Theme
import com.chat.base.utils.AndroidUtilities
import com.chat.sticker.R
import com.chat.sticker.entity.StoreEntity
import com.chat.sticker.ui.components.StickerView

/**
 * 12/30/20 3:51 PM
 * 商店
 */

class StickerStoreAdapter :
        BaseQuickAdapter<StoreEntity, BaseViewHolder>(R.layout.item_store_layout) {
    override fun convert(holder: BaseViewHolder, item: StoreEntity) {
        holder.setText(R.id.nameTv, item.title)
        holder.setText(R.id.descTv, item.desc)
        val stickerView: StickerView = holder.getView(R.id.stickerView)
        stickerView.showSticker(item.cover_lim, "", AndroidUtilities.dp(35f), true)

        val addBtn = holder.getView<Button>(R.id.addBtn)
        if (item.status != 0) {
            holder.setText(R.id.addBtn, R.string.str_sticker_remove)
            addBtn.background.setTint(ContextCompat.getColor(context,R.color.gary))
            holder.setTextColor(R.id.addBtn,ContextCompat.getColor(context,R.color.red))
        } else {
            holder.setTextColor(R.id.addBtn,ContextCompat.getColor(context,R.color.white))
            holder.setText(R.id.addBtn, R.string.str_sticker_add)
            addBtn.background.setTint(Theme.colorAccount)
        }

    }

}