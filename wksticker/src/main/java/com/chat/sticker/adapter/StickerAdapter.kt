package com.chat.sticker.adapter

import android.text.TextUtils
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.config.WKApiConfig
import com.chat.base.emoji.MoonUtil
import com.chat.base.glide.GlideUtils
import com.chat.sticker.R
import com.chat.sticker.entity.Sticker
import com.chat.sticker.msg.StickerFormat
import com.chat.sticker.ui.components.StickerView

/**
 * 1/4/21 3:45 PM
 * 表情
 */
class StickerAdapter(var width: Int) :
    BaseQuickAdapter<Sticker, BaseViewHolder>(R.layout.item_sticker_layout) {
    var visibility: Boolean = true
//    override fun convert(holder: BaseViewHolder, item: Sticker, payloads: List<Any>) {
//        super.convert(holder, item, payloads)
//        val stickerView: StickerView = holder.getView(R.id.stickerView)
//        if (stickerView.drawable != null) {
//            if (visibility)
//                stickerView.drawable.start()
//            else stickerView.drawable.stop()
//        }
//    }

    override fun convert(holder: BaseViewHolder, item: Sticker) {
        val stickerView: StickerView = holder.getView(R.id.stickerView)
        if (!TextUtils.isEmpty(item.searchable_word)) {
            holder.setGone(R.id.emojiTv, false)
            MoonUtil.identifyFaceExpression(
                context,
                holder.getView(R.id.emojiTv),
                item.searchable_word,
                MoonUtil.SMALL_SCALE
            )
        } else {
            holder.setGone(R.id.emojiTv, true)
        }
        if (!TextUtils.isEmpty(item.format) && item.format == StickerFormat.lim) {
            stickerView.showSticker(item.path, item.placeholder, width,  true, visibility)
        } else {
            GlideUtils.getInstance()
                .showGif(
                    context,
                    WKApiConfig.getShowUrl(item.path),
                    holder.getView(R.id.imageView),
                    null
                )
        }
        holder.setGone(
            R.id.imageView,
            !TextUtils.isEmpty(item.format) && item.format == StickerFormat.lim
        )
        holder.setGone(
            R.id.stickerView,
            TextUtils.isEmpty(item.format) || item.format == StickerFormat.gif
        )
        holder.getView<ImageView>(R.id.imageView).layoutParams.width = width
        holder.getView<ImageView>(R.id.imageView).layoutParams.height = width
        holder.setText(R.id.descTv, item.title)
    }
}