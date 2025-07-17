package com.chat.sticker.adapter

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.config.WKApiConfig
import com.chat.base.glide.GlideRequestOptions
import com.chat.base.utils.AndroidUtilities
import com.chat.sticker.R
import com.chat.sticker.entity.Sticker
import com.chat.sticker.msg.StickerFormat
import com.chat.sticker.ui.components.StickerView

/**
 * 12/30/20 4:56 PM
 * 搜索表情
 */
class SearchStickerAdapter : BaseQuickAdapter<Sticker, BaseViewHolder>(R.layout.item_search_sticker_layout) {
    override fun convert(holder: BaseViewHolder, item: Sticker) {

        val width = (AndroidUtilities.getScreenWidth() - AndroidUtilities.dp( 90f)) / 4
        val imageView: ImageView = holder.getView(R.id.imageView)
        imageView.layoutParams.width = width
        imageView.layoutParams.height = imageView.layoutParams.width

        if (item.isNull) {
            holder.setVisible(R.id.imageView, true)
            holder.setGone(R.id.stickerView, true)
        } else {
            val stickerView: StickerView = holder.getView(R.id.stickerView)
            if (!TextUtils.isEmpty(item.format) && item.format == StickerFormat.lim) {
                imageView.visibility = View.GONE
                stickerView.visibility = View.VISIBLE
                stickerView.showSticker(item.path, item.placeholder, width, true)
            } else {
                imageView.visibility = View.VISIBLE
                stickerView.visibility = View.GONE
                Glide.with(context).asGif().load(WKApiConfig.getShowUrl(item.path))
                        .apply(GlideRequestOptions.getInstance().normalRequestOption()).into(imageView)
            }
        }

        when (holder.bindingAdapterPosition ) {
            1 -> {
                holder.setBackgroundResource(R.id.contentLayout, R.drawable.left_bg)
            }
            5 -> {
                holder.setBackgroundResource(R.id.contentLayout, R.drawable.right_bg)
            }
            else -> {
                holder.setBackgroundColor(R.id.contentLayout, ContextCompat.getColor(context, R.color.layoutColor))
            }
        }
    }

}