package com.chat.sticker.adapter

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.config.WKApiConfig
import com.chat.base.glide.GlideRequestOptions
import com.chat.base.utils.AndroidUtilities
import com.chat.base.utils.singleclick.SingleClickUtil
import com.chat.sticker.R
import com.chat.sticker.WKStickerApplication
import com.chat.sticker.entity.Sticker
import com.chat.sticker.entity.StickerUI
import com.chat.sticker.service.StickerModel
import com.chat.sticker.ui.CustomStickerActivity
import com.chat.sticker.ui.components.StickerView
import java.io.File

class StickersGridAdapter : BaseMultiItemQuickAdapter<StickerUI, BaseViewHolder>() {
    init {
        addItemType(2, R.layout.item_sticker_grid_layout)
        addItemType(1, R.layout.item_sticker_grid_layout)
        addItemType(0, R.layout.item_sticker_title_layout)
    }

    override fun convert(holder: BaseViewHolder, item: StickerUI) {
        if (item.viewType == 0) {
            val addIV = holder.getView<AppCompatImageView>(R.id.addIV)
            val textView = holder.getView<TextView>(R.id.titleTv)
            textView.typeface = AndroidUtilities.getTypeface("fonts/rmedium.ttf")
            textView.setTextColor(-0x7d746c)
            addIV.colorFilter = PorterDuffColorFilter(
                -0x7d746c, PorterDuff.Mode.MULTIPLY
            )
            if (item.category == "favorite") {
                addIV.visibility = View.VISIBLE
                textView.setText(R.string.str_add_custom_sticker)
                SingleClickUtil.onSingleClick(addIV) {
                    val intent = Intent(context, CustomStickerActivity::class.java)
                    context.startActivity(intent)
                }
            } else {
                addIV.visibility = View.GONE
                textView.text = item.title
            }
        } else if (item.viewType == 1) {
            val stickerView = holder.getView<StickerView>(R.id.stickerView)
            stickerView.showSticker(
                item.sticker!!.path,
                item.sticker!!.placeholder,
                WKStickerApplication.instance.stickerGridSize,
                true
            )
        } else {
            val stickerView = holder.getView<StickerView>(R.id.stickerView)
            val imageView = stickerView.imageView
            imageView.layoutParams.width = WKStickerApplication.instance.stickerGridSize
            imageView.layoutParams.height = WKStickerApplication.instance.stickerGridSize
//            dispatchQueuePool.execute {
            val showURL: String
            val file = File(StickerModel().getLocalPath(item.sticker!!.path))
            if (file.exists()) showURL = file.absolutePath else {
                showURL = WKApiConfig.getShowUrl(item.sticker!!.path)
                StickerModel().download(showURL, file.absolutePath)
            }
            AndroidUtilities.runOnUIThread {
                Glide.with(context).asGif().load(showURL)
                    .apply(GlideRequestOptions.getInstance().normalRequestOption())
                    .into(imageView)
            }
            if (!file.exists()) {
                val list: MutableList<Sticker> = ArrayList()
                list.add(item.sticker!!)
                StickerModel().downloadCustomerSticker(list)
            }
//            }

        }
    }

}