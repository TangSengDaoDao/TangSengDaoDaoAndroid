package com.chat.sticker.adapter

import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.config.WKApiConfig
import com.chat.base.glide.GlideRequestOptions
import com.chat.base.ui.Theme
import com.chat.base.ui.components.CheckBox
import com.chat.base.utils.AndroidUtilities
import com.chat.sticker.R
import com.chat.sticker.entity.Sticker
import com.chat.sticker.msg.StickerFormat
import com.chat.sticker.ui.components.StickerView

/**
 * 1/3/21 9:21 PM
 * 添加自定义表情
 */
class AddCustomStickerAdapter(
    var width: Int
) : BaseQuickAdapter<Sticker, BaseViewHolder>(R.layout.item_add_custom_sticker_layout) {

    override fun convert(holder: BaseViewHolder, item: Sticker, payloads: List<Any>) {
        super.convert(holder, item, payloads)
        val sticker = payloads[0] as Sticker
        if (sticker.showManager) {
            val checkBox: CheckBox = holder.getView(viewId = R.id.checkBox)
            checkBox.setChecked(sticker.isSelected, true)
            checkBox.setDrawBackground(true)
        }
    }

    override fun convert(holder: BaseViewHolder, item: Sticker) {
        val stickerView: StickerView = holder.getView(R.id.stickerView)
        val contentLayout: FrameLayout = holder.getView(R.id.contentLayout)
        contentLayout.layoutParams.width = width
        contentLayout.layoutParams.height = width
        val checkBox: CheckBox = holder.getView(viewId = R.id.checkBox)
        if (item.path == "addCustom") {
            holder.setGone(R.id.addIV, false)
            holder.setGone(R.id.stickerView, true)
            holder.setGone(R.id.checkBox, true)

        } else {
            holder.setGone(R.id.addIV, true)
            holder.setGone(R.id.stickerView, false)
            holder.setGone(R.id.checkBox, false)
            if (item.format == StickerFormat.lim) {
                stickerView.showSticker(item.path, item.placeholder, item.width, true)
            } else {
                val showURL: String = if (!TextUtils.isEmpty(item.localPath)) {
                    item.localPath
                } else WKApiConfig.getShowUrl(item.path)
                Glide.with(context).asGif().load(showURL)
                    .apply(GlideRequestOptions.getInstance().normalRequestOption())
                    .into(stickerView.imageView)
                stickerView.imageView.layoutParams.width = width
                stickerView.imageView.layoutParams.height = width
            }
        }
        stickerView.imageView.scaleType = ImageView.ScaleType.CENTER
        if (item.showManager) {
            checkBox.visibility = View.VISIBLE
        } else checkBox.visibility = View.GONE

        checkBox.setResId(context, R.mipmap.round_check2)
        checkBox.setDrawBackground(true)
        checkBox.setHasBorder(true)
        checkBox.setStrokeWidth(AndroidUtilities.dp(2f))
        checkBox.setBorderColor(ContextCompat.getColor(context, R.color.white))
        checkBox.setSize(24)
        checkBox.setColor(
            Theme.colorAccount,
            ContextCompat.getColor(context, R.color.white)
        )
        checkBox.setChecked(item.isSelected, true)
    }

}