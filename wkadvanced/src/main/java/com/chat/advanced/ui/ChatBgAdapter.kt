package com.chat.advanced.ui

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.advanced.R
import com.chat.advanced.entity.ChatBgEntity
import com.chat.base.config.WKApiConfig
import com.chat.base.glide.GlideUtils
import com.chat.base.utils.AndroidUtilities

class ChatBgAdapter(
    private val width: Int,
    private val height: Int,
    private var chatBgURL: String,
    private val isDark: Boolean
) :
    BaseQuickAdapter<ChatBgEntity, BaseViewHolder>(R.layout.item_chat_bg) {

    override fun convert(holder: BaseViewHolder, item: ChatBgEntity) {
        val imageView = holder.getView<ImageView>(R.id.imageView)
        val backIV = holder.getView<ImageView>(R.id.backIV)
        backIV.layoutParams.width = width
        backIV.layoutParams.height = height
        imageView.layoutParams.width = width
        imageView.layoutParams.height = height
        if (TextUtils.isEmpty(item.url)) {
            imageView.setImageResource(R.drawable.default_view_bg)
            backIV.visibility = View.GONE
            imageView.colorFilter =
                PorterDuffColorFilter(
                    ContextCompat.getColor(context, R.color.homeColor),
                    PorterDuff.Mode.MULTIPLY
                )
        } else {
            GlideUtils.getInstance()
                .showImg(context, WKApiConfig.getShowUrl(item.cover), imageView)
            if (item.is_svg == 1) {
                val gradientColor0: Int
                val gradientColor1: Int
                val gradientColor2: Int
                val gradientColor3: Int
                if (isDark) {
                    gradientColor0 = Color.parseColor("#" + item.dark_colors[0])
                    gradientColor1 = Color.parseColor("#" + item.dark_colors[1])
                    gradientColor2 = Color.parseColor("#" + item.dark_colors[2])
                    gradientColor3 = Color.parseColor("#" + item.dark_colors[3])
                } else {
                    gradientColor0 = Color.parseColor("#" + item.light_colors[0])
                    gradientColor1 = Color.parseColor("#" + item.light_colors[1])
                    gradientColor2 = Color.parseColor("#" + item.light_colors[2])
                    gradientColor3 = Color.parseColor("#" + item.light_colors[3])
                }
                val drawable = GradientDrawable(
                    GradientDrawable.Orientation.TR_BL,
                    intArrayOf(gradientColor0, gradientColor1, gradientColor2, gradientColor3)
                )
                backIV.setImageDrawable(drawable)
//                val color1 = Theme.defaultColorsDark[3][0]
//                val gradientColor1 = Theme.defaultColorsDark[3][1]
//                val gradientColor2 = Theme.defaultColorsDark[3][2]
//                val gradientColor3 = Theme.defaultColorsDark[3][3]
                val pco = AndroidUtilities.getPatternColor(
                    gradientColor0,
                    gradientColor1,
                    gradientColor2,
                    gradientColor3
                )
                imageView.colorFilter =
                    PorterDuffColorFilter(
                        pco,
                        PorterDuff.Mode.MULTIPLY
                    )
                backIV.visibility = View.VISIBLE
            } else {
                imageView.colorFilter = null
                backIV.visibility = View.GONE
            }
        }

        if (TextUtils.isEmpty(chatBgURL)) {
            if (TextUtils.isEmpty(item.url)) {
                holder.setGone(R.id.selectedView, false)
            } else holder.setGone(R.id.selectedView, true)
        } else {
            if (chatBgURL == item.url) {
                holder.setGone(R.id.selectedView, false)
            } else holder.setGone(R.id.selectedView, true)
        }
    }

    fun setURL(url: String) {
        chatBgURL = url
        notifyItemRangeChanged(0, data.size)
    }
}