package com.chat.sticker.adapter

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.text.TextUtils
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.config.WKApiConfig
import com.chat.base.glide.GlideUtils
import com.chat.base.ui.Theme
import com.chat.base.utils.AndroidUtilities
import com.chat.sticker.R
import com.chat.sticker.entity.StickerTab
import com.chat.sticker.ui.components.StickerView

class SickerTabAdapter :
    BaseQuickAdapter<StickerTab, BaseViewHolder>(R.layout.item_sticker_tab) {
    override fun convert(holder: BaseViewHolder, item: StickerTab, payloads: List<Any>) {
        super.convert(holder, item, payloads)
        val stickerTab = payloads[0] as StickerTab
        holder.setVisible(R.id.bottomView, stickerTab.isChecked)
        val stickerView: StickerView = holder.getView(R.id.stickerView)
        val imageView = stickerView.imageView
        if (stickerTab.iconResource != 0) {
            if (stickerTab.isChecked) {
                imageView.colorFilter =
                    PorterDuffColorFilter(Theme.colorAccount, PorterDuff.Mode.MULTIPLY)
            } else
                imageView.colorFilter =
                    PorterDuffColorFilter(-0x7d746c, PorterDuff.Mode.MULTIPLY)
        }
    }

    override fun convert(holder: BaseViewHolder, item: StickerTab) {
        val stickerView: StickerView = holder.getView(R.id.stickerView)
        val imageView = stickerView.imageView
        if (item.iconResource != 0) {
            if (item.isChecked) {
                imageView.colorFilter =
                    PorterDuffColorFilter(Theme.colorAccount, PorterDuff.Mode.MULTIPLY)
            } else
                Theme.setColorFilter(context, imageView, R.color.popupTextColor)
            imageView.setImageResource(item.iconResource)
        } else {
            if (!TextUtils.isEmpty(item.coverTgs)) {
                stickerView.showSticker(
                    item.coverTgs,
                    "",
                    AndroidUtilities.dp(30f),
                    true
                )
            } else {
                GlideUtils.getInstance()
                    .showImg(context, WKApiConfig.getShowUrl(item.icon), imageView)
            }

        }
        holder.getView<View>(R.id.bottomView).setBackgroundColor(Theme.colorAccount)
        holder.setVisible(R.id.bottomView, item.isChecked)
    }

    fun setSelect(index: Int) {
        var isRefresh = true
        for (i in data.indices) {
            if (data[i].isChecked) {
                if (i == index) {
                    isRefresh = false
                    break
                }
                data[i].isChecked = false
                notifyItemChanged(i, data[i])
                break
            }
        }
        if (isRefresh) {
            data[index].isChecked = true
            notifyItemChanged(index, data[index])
            recyclerView.smoothScrollToPosition(index)
        }
    }

}