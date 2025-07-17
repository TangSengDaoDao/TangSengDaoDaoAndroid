package com.chat.sticker.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.emoji.EmojiColorPickerWindow
import com.chat.base.emoji.EmojiManager
import com.chat.base.endpoint.EndpointManager
import com.chat.base.ui.components.FilterImageView
import com.chat.base.utils.AndroidUtilities
import com.chat.sticker.R
import com.chat.sticker.entity.EmojiEntity


/**
 * 12/31/20 11:26 AM
 * emoji表情
 */
class EmojiAdapter(
    var width: Int,
    private val popup: EmojiColorPickerWindow
) :
    BaseMultiItemQuickAdapter<EmojiEntity, BaseViewHolder>() {
    init {
        addItemType(0, R.layout.item_emoji_layout)
        addItemType(1, R.layout.item_emoji_text_layout)
        addItemType(2, R.layout.item_empty_layout)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun convert(holder: BaseViewHolder, item: EmojiEntity) {
        if (item.itemType == 0) {
            val imageView: FilterImageView = holder.getView(R.id.emojiIv)
            // imageView.setEmojiTag(item.entry.tag)
            imageView.setAllCorners(0)
            imageView.strokeWidth = 0f
            val size = (width - AndroidUtilities.dp(80f)) / 8
            val layoutParams = LinearLayout.LayoutParams(
                size,
                size
            )
            layoutParams.setMargins(
                AndroidUtilities.dp(5f),
                AndroidUtilities.dp(5f),
                AndroidUtilities.dp(5f),
                AndroidUtilities.dp(5f)
            )
            imageView.layoutParams = layoutParams
            imageView.setImageDrawable(
                EmojiManager.getInstance().getDrawable(context, item.entry.text)
            )
            imageView.setOnTouchListener { _, event ->
                if (event?.action == MotionEvent.ACTION_UP ) {
                    popup.dismiss()
                }
                false
            }
            imageView.setOnLongClickListener { v ->
                if (item.entry.tag.contains("default")) {
                    val location = IntArray(2)
                    val l = IntArray(2)
                    v.getLocationOnScreen(l)
                    val distanceToRight: Int =
                        v.resources.displayMetrics.widthPixels - (l[0] + v.width)
                    l[0] = distanceToRight
                    popup.setEmoji(item.entry.tag.replace("_default", "_color"))
                    popup.selection = 0
                    var x = 0
                    var x1 = 0
                    val popupWidth: Int = popup.popupWidth
                    val popupHeight: Int = popup.popupHeight
                    if (!popup.isCompound) {
                        x =
                            32 * popup.selection + AndroidUtilities.dp(4 * popup.selection - 1f)
                    }
                    x1 = x
                    if (location[0] - x < AndroidUtilities.dp(5f)) {
                        x += (location[0] - x) - AndroidUtilities.dp(5f)
                    } else if (location[0] - x + popupWidth > AndroidUtilities.displaySize.x - AndroidUtilities.dp(
                            5f
                        )
                    ) {
                        x += (location[0] - x + popupWidth) - (AndroidUtilities.displaySize.x - AndroidUtilities.dp(
                            5f
                        ))
                    }

                    x1 += if (l[0] > popupWidth) {
                        - AndroidUtilities.dp(5f)
                    } else {
                        popupWidth - l[0] - v.width - AndroidUtilities.dp(5f)
                    }
                    val xOffset = -x
                    val yOffset = if (v.top < 0) v.top else 0
                    popup.setupArrow(
                        (AndroidUtilities.dp((if (AndroidUtilities.isTablet()) 30 else 22).toFloat()) + x1 + AndroidUtilities.dpf2(
                            0.5f
                        )).toInt()
                    )
                    popup.isFocusable = true
                    popup.showAsDropDown(
                        v,
                        xOffset,
                        -v.measuredHeight - popupHeight + (v.measuredHeight - size) / 2 - yOffset
                    )
                    EndpointManager.getInstance().invoke("emoji_color_popup_show", true)
                    popup.setOnSelectionUpdateListener { arg, arg2 ->
                        Log.e("选中", "$arg")
                    }
                    true
                }
                false
            }
        } else if (item.itemType == 1) {
            val titleTv = holder.getView<TextView>(R.id.titleCenterTv)
            titleTv.text = item.entry.text
            titleTv.typeface = AndroidUtilities.getTypeface("fonts/rmedium.ttf")
//            titleTv.setTextColor(0x7d746c)
        }
    }

}