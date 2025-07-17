package com.chat.sticker.msg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chat.base.msgitem.WKChatBaseProvider
import com.chat.base.msgitem.WKChatIteMsgFromType
import com.chat.base.msgitem.WKContentType
import com.chat.base.msgitem.WKUIChatMsgItemEntity
import com.chat.base.utils.AndroidUtilities
import com.chat.sticker.R
import com.chat.sticker.ui.components.StickerView
import com.xinbida.wukongim.message.type.WKSendMsgResult

class EmojiProvider : WKChatBaseProvider() {
    override fun getChatViewItem(parentView: ViewGroup, from: WKChatIteMsgFromType): View? {
        return LayoutInflater.from(context)
            .inflate(R.layout.chat_item_emoji_sticker_layout, parentView, false)
    }

    override fun setData(
        adapterPosition: Int,
        parentView: View,
        uiChatMsgItemEntity: WKUIChatMsgItemEntity,
        from: WKChatIteMsgFromType
    ) {
        val emojiStickerContent =
            uiChatMsgItemEntity.wkMsg.baseContentMsgModel as EmojiContent
        val stickerView = parentView.findViewById<StickerView>(R.id.stickerView)
        stickerView.showSticker(
            emojiStickerContent.url,
            emojiStickerContent.placeholder,
            AndroidUtilities.dp(120f),
            false,
            uiChatMsgItemEntity.wkMsg.status != WKSendMsgResult.send_success
        )
        addLongClick(stickerView, uiChatMsgItemEntity)
        stickerView.setOnClickListener {
            stickerView.restart()

        }
    }

    override val itemViewType: Int
        get() = WKContentType.WK_EMOJI_STICKER


    override fun resetCellListener(
        position: Int,
        parentView: View,
        uiChatMsgItemEntity: WKUIChatMsgItemEntity,
        from: WKChatIteMsgFromType
    ) {
        super.resetCellListener(position, parentView, uiChatMsgItemEntity, from)
        val stickerView = parentView.findViewById<StickerView>(R.id.stickerView)
        addLongClick(stickerView, uiChatMsgItemEntity)
    }

}