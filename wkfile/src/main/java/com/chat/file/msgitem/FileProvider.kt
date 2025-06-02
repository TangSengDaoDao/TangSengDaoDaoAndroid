package com.chat.file.msgitem

import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.chat.base.msgitem.*
import com.chat.base.net.ud.WKProgressManager
import com.chat.base.utils.StringUtils
import com.chat.base.utils.singleclick.SingleClickUtil
import com.chat.base.views.BubbleLayout
import com.chat.file.ChatFileActivity
import com.chat.file.R
import java.util.*

class FileProvider : WKChatBaseProvider() {

    override fun getChatViewItem(parentView: ViewGroup, from: WKChatIteMsgFromType): View? {
        return LayoutInflater.from(context).inflate(R.layout.chat_item_file, parentView, false)

    }

    override fun setData(
        adapterPosition: Int,
        parentView: View,
        uiChatMsgItemEntity: WKUIChatMsgItemEntity,
        from: WKChatIteMsgFromType
    ) {
        val fileView = parentView.findViewById<LinearLayout>(R.id.fileView)
        fileView.layoutParams.width = getViewWidth(from, uiChatMsgItemEntity)
        val nameTv = parentView.findViewById<TextView>(R.id.nameTv)
        val typeTv = parentView.findViewById<TextView>(R.id.typeTv)
        val sizeTv = parentView.findViewById<TextView>(R.id.sizeTv)
        val progressBar = parentView.findViewById<ProgressBar>(R.id.progress)
        resetCellBackground(parentView, uiChatMsgItemEntity, from)
        val fileContent = uiChatMsgItemEntity.wkMsg.baseContentMsgModel as FileContent
        if (!TextUtils.isEmpty(fileContent.name)) {
            nameTv.text = fileContent.name
            if (fileContent.name.contains(".")) {
                val type = fileContent.name.substring(fileContent.name.lastIndexOf(".") + 1)
                if (!TextUtils.isEmpty(type)) typeTv.text =
                    type.uppercase(Locale.getDefault()) else typeTv.setText(R.string.str_file_unknown_file)
            } else typeTv.setText(R.string.str_file_unknown_file)
        }
        sizeTv.text = StringUtils.sizeFormatNum2String(fileContent.size)
        val contentLayout = parentView.findViewById<BubbleLayout>(R.id.contentLayout)
        SingleClickUtil.onSingleClick(contentLayout) {
            val intent = Intent(context, ChatFileActivity::class.java)
            intent.putExtra("clientMsgNo", uiChatMsgItemEntity.wkMsg.clientMsgNO)
            context.startActivity(intent)
        }
        //设置上传进度
        if (TextUtils.isEmpty(fileContent.url)) {
            WKProgressManager.instance.registerProgress(uiChatMsgItemEntity.wkMsg.clientSeq,
                object : WKProgressManager.IProgress {
                    override fun onProgress(tag: Any?, progress: Int) {
                        if (tag is Long) {
                            if (tag == uiChatMsgItemEntity.wkMsg.clientSeq) {
                                progressBar.progress = progress
                                if (progress >= 100) {
                                    progressBar.visibility = View.INVISIBLE
                                } else progressBar.visibility = View.VISIBLE
                            }
                        }
                    }

                    override fun onSuccess(tag: Any?, path: String?) {
                        progressBar.visibility = View.INVISIBLE
                        if (tag != null) {
                            WKProgressManager.instance.unregisterProgress(tag)
                        }
                    }

                    override fun onFail(tag: Any?, msg: String?) {
                    }

                })
        } else progressBar.visibility = View.INVISIBLE
    }

    override val itemViewType: Int
        get() = WKContentType.WK_FILE

    override fun resetCellBackground(
        parentView: View,
        uiChatMsgItemEntity: WKUIChatMsgItemEntity,
        from: WKChatIteMsgFromType
    ) {
        super.resetCellBackground(parentView, uiChatMsgItemEntity, from)
        val contentLayout = parentView.findViewById<BubbleLayout>(R.id.contentLayout)
        val bgType = getMsgBgType(
            uiChatMsgItemEntity.previousMsg,
            uiChatMsgItemEntity.wkMsg,
            uiChatMsgItemEntity.nextMsg
        )
        contentLayout.setAll(bgType, from, WKContentType.WK_FILE)
    }

    override fun resetCellListener(
        position: Int,
        parentView: View,
        uiChatMsgItemEntity: WKUIChatMsgItemEntity,
        from: WKChatIteMsgFromType
    ) {
        super.resetCellListener(position, parentView, uiChatMsgItemEntity, from)
        val contentLayout = parentView.findViewById<BubbleLayout>(R.id.contentLayout)
        addLongClick(contentLayout, uiChatMsgItemEntity)
    }
}