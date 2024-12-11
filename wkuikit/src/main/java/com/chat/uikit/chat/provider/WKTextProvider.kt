package com.chat.uikit.chat.provider

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.provider.ContactsContract
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.emoji2.widget.EmojiTextView
import com.chat.base.WKBaseApplication
import com.chat.base.act.WKWebViewActivity
import com.chat.base.config.WKApiConfig
import com.chat.base.emoji.EmojiManager
import com.chat.base.emoji.MoonUtil
import com.chat.base.endpoint.EndpointManager
import com.chat.base.endpoint.EndpointSID
import com.chat.base.endpoint.entity.CanReactionMenu
import com.chat.base.endpoint.entity.ChatChooseContacts
import com.chat.base.endpoint.entity.ChatItemPopupMenu
import com.chat.base.endpoint.entity.ChooseChatMenu
import com.chat.base.endpoint.entity.MsgConfig
import com.chat.base.entity.BottomSheetItem
import com.chat.base.glide.GlideUtils
import com.chat.base.msg.ChatAdapter
import com.chat.base.msg.model.WKGifContent
import com.chat.base.msgitem.WKChatBaseProvider
import com.chat.base.msgitem.WKChatIteMsgFromType
import com.chat.base.msgitem.WKContentType
import com.chat.base.msgitem.WKUIChatMsgItemEntity
import com.chat.base.ui.components.AlignImageSpan
import com.chat.base.ui.components.AvatarView
import com.chat.base.ui.components.NormalClickableContent
import com.chat.base.ui.components.NormalClickableSpan
import com.chat.base.utils.LayoutHelper
import com.chat.base.utils.SoftKeyboardUtils
import com.chat.base.utils.StringUtils
import com.chat.base.utils.WKDialogUtils
import com.chat.base.utils.WKPermissions
import com.chat.base.utils.WKPermissions.IPermissionResult
import com.chat.base.utils.WKToastUtils
import com.chat.base.views.BubbleLayout
import com.chat.uikit.R
import com.chat.uikit.user.UserDetailActivity
import com.google.android.material.snackbar.Snackbar
import com.xinbida.wukongim.WKIM
import com.xinbida.wukongim.entity.WKChannel
import com.xinbida.wukongim.entity.WKChannelType
import com.xinbida.wukongim.entity.WKMsg
import com.xinbida.wukongim.entity.WKMsgSetting
import com.xinbida.wukongim.entity.WKSendOptions
import com.xinbida.wukongim.msgmodel.WKImageContent
import com.xinbida.wukongim.msgmodel.WKTextContent
import java.io.File
import java.util.Objects
import kotlin.math.abs


open class WKTextProvider : WKChatBaseProvider() {
    override fun getChatViewItem(parentView: ViewGroup, from: WKChatIteMsgFromType): View? {
        return LayoutInflater.from(context).inflate(R.layout.chat_item_text, parentView, false)
    }

    override fun setData(
        adapterPosition: Int,
        parentView: View,
        uiChatMsgItemEntity: WKUIChatMsgItemEntity,
        from: WKChatIteMsgFromType
    ) {
//        val textContentLayout = parentView.findViewById<View>(R.id.textContentLayout)
        //   val linkView = parentView.findViewById<LinearLayout>(R.id.linkView)
        val contentTv = parentView.findViewById<EmojiTextView>(R.id.contentTv)
        val receivedTextNameTv = parentView.findViewById<TextView>(R.id.receivedTextNameTv)
        //val msgTimeView = parentView.findViewById<View>(R.id.msgTimeView)


        val contentTvLayout = parentView.findViewById<BubbleLayout>(R.id.contentTvLayout)

        val contentLayout = parentView.findViewById<LinearLayout>(R.id.contentLayout)

        //replyLayout.layoutParams.width = getViewWidth(from, uiChatMsgItemEntity)
        // 这里要指定文本宽度 - padding的距离
//        textContentLayout.layoutParams.width = getViewWidth(from, uiChatMsgItemEntity)
//        val bgType = getMsgBgType(
//            uiChatMsgItemEntity.previousMsg, uiChatMsgItemEntity.wkMsg, uiChatMsgItemEntity.nextMsg
//        )
        resetCellBackground(parentView, uiChatMsgItemEntity, from)
//        if (textContentLayout.layoutParams.width < msgTimeView.layoutParams.width) {
//            textContentLayout.layoutParams.width = msgTimeView.layoutParams.width
//        }
        val textColor: Int
        if (from == WKChatIteMsgFromType.SEND) {
            contentTv.setBackgroundResource(R.drawable.send_chat_text_bg)
            contentLayout.gravity = Gravity.END
            receivedTextNameTv.visibility = View.GONE
            textColor = ContextCompat.getColor(context, R.color.colorDark)
        } else {
            contentTv.setBackgroundResource(R.drawable.received_chat_text_bg)
            setFromName(uiChatMsgItemEntity, from, receivedTextNameTv)
            contentLayout.gravity = Gravity.START
            textColor = ContextCompat.getColor(context, R.color.receive_text_color)
        }
        contentTv.setTextColor(textColor)
        contentTv.text = uiChatMsgItemEntity.displaySpans
        contentTv.movementMethod = LinkMovementMethod.getInstance()
//        val preText =  PrecomputedTextCompat.create(
//            uiChatMsgItemEntity.displaySpans,
//            TextViewCompat.getTextMetricsParams(contentTv)
//        )
//
//        TextViewCompat.setPrecomputedText(contentTv, preText)
//
//
//        fun AppCompatTextView.setTextFuture(charSequence: CharSequence){
//            this.setTextFuture(PrecomputedTextCompat.getTextFuture(
//                charSequence,
//                TextViewCompat.getTextMetricsParams(this),
//                null
//            ))
//        }
//
//        contentTv.setTextFuture(uiChatMsgItemEntity.displaySpans)

        // 链接识别
//        val urls = StringUtils.getStrUrls(contentTv.text.toString())
//        if (urls.size > 0) {
//            showLinkInfo(uiChatMsgItemEntity, msgTimeView, linkView, from, urls[urls.size - 1])
//        } else {
//            linkView.visibility = View.GONE
//            msgTimeView.visibility = View.VISIBLE
//        }

        //setSelectableTextHelper(contentTv,0,true)
        selectText(contentTv, contentTvLayout, uiChatMsgItemEntity)
        if (uiChatMsgItemEntity.wkMsg.baseContentMsgModel.reply != null && uiChatMsgItemEntity.wkMsg.baseContentMsgModel.reply.payload != null) {
            replyView(contentTvLayout, from, uiChatMsgItemEntity)
        }
    }

    private var mSelectableTextHelper: SelectTextHelper? = null
    var selectText: String? = null
    private fun selectText(
        textView: TextView,
        fullLayout: View,
        uiChatMsgItemEntity: WKUIChatMsgItemEntity
    ) {
//        textMsgBean = msgBean
        val menu = EndpointManager.getInstance()
            .invoke("favorite_item", uiChatMsgItemEntity.wkMsg)
        var favoritePopupMenu: ChatItemPopupMenu? = null
        if (menu != null) {
            favoritePopupMenu = menu as ChatItemPopupMenu
        }

        val builder = SelectTextHelper.Builder(textView, fullLayout) // 放你的textView到这里！！
            .setCursorHandleColor(ContextCompat.getColor(context, R.color.colorAccent)) // 游标颜色
            .setCursorHandleSizeInDp(22f) // 游标大小 单位dp
            .setSelectedColor(
                ContextCompat.getColor(
                    context,
                    R.color.color_text_select_cursor
                )
            ) // 选中文本的颜色
            .setSelectAll(true) // 初次选中是否全选 default true
            .setScrollShow(false) // 滚动时是否继续显示 default true
            .setSelectedAllNoPop(true) // 已经全选无弹窗，设置了监听会回调 onSelectAllShowCustomPop 方法
            .setMagnifierShow(true) // 放大镜 default true
            .setSelectTextLength(2)// 首次选中文本的长度 default 2
            .setPopDelay(100)// 弹窗延迟时间 default 100毫秒
            .setFlame(uiChatMsgItemEntity.wkMsg.flame)
            .setIsShowPinnedMessage(if (uiChatMsgItemEntity.isShowPinnedMessage) 1 else 0)
            .addItem(R.mipmap.msg_copy,
                R.string.copy,
                object : SelectTextHelper.Builder.onSeparateItemClickListener {
                    override fun onClick() {
                        EndpointManager.getInstance().invoke("chat_activity_touch", null)
                        // mSelectableTextHelper?.reset()
                        val cm =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val mClipData = ClipData.newPlainText("Label", selectText)
                        cm.setPrimaryClip(mClipData)
                        WKToastUtils.getInstance()
                            .showToastNormal(context.getString(R.string.copyed))
                    }
                }).addItem(
                R.mipmap.msg_forward,
                R.string.base_forward,
                object : SelectTextHelper.Builder.onSeparateItemClickListener {
                    override fun onClick() {
                        EndpointManager.getInstance().invoke("chat_activity_touch", null)
                        if (TextUtils.isEmpty(selectText)) return
                        val textContent = WKTextContent(selectText)
                        val chooseChatMenu =
                            ChooseChatMenu(
                                ChatChooseContacts { channelList: List<WKChannel>? ->
                                    if (!channelList.isNullOrEmpty()) {
                                        for (mChannel in channelList) {
                                            textContent.mentionAll = 0
                                            textContent.mentionInfo = null
                                            val option = WKSendOptions()
                                            option.setting.receipt = mChannel.receipt
                                            WKIM.getInstance().msgManager.sendWithOptions(
                                                textContent,
                                                mChannel, option
                                            )
                                        }
                                        val viewGroup =
                                            (context as Activity).findViewById<View>(android.R.id.content)
                                                .rootView as ViewGroup
                                        Snackbar.make(
                                            viewGroup,
                                            context.getString(com.chat.base.R.string.str_forward),
                                            1000
                                        )
                                            .setAction(
                                                ""
                                            ) { }
                                            .show()
                                    }
                                },
                                textContent
                            )
                        EndpointManager.getInstance()
                            .invoke(EndpointSID.showChooseChatView, chooseChatMenu)
                    }

                }).setPopSpanCount(3) // 设置操作弹窗每行个数 default 5
        mSelectableTextHelper = builder.build()
//            .setPopStyle(
//                R.drawable.shape_color_4c4c4c_radius_8 /*操作弹窗背*/, R.mipmap.ic_arrow /*箭头图片*/
//            ) // 设置操作弹窗背景色、箭头图片
        if (favoritePopupMenu != null) {
            builder.addItem(
                favoritePopupMenu.imageResource,
                favoritePopupMenu.text,
                object : SelectTextHelper.Builder.onSeparateItemClickListener {
                    override fun onClick() {
                        EndpointManager.getInstance().invoke("chat_activity_touch", null)

                        if (!TextUtils.isEmpty(selectText)) {
                            val mMsg = WKMsg()
                            mMsg.type = WKContentType.WK_TEXT
                            mMsg.baseContentMsgModel = WKTextContent(selectText)
                            mMsg.from = uiChatMsgItemEntity.wkMsg.from
                            mMsg.channelID = uiChatMsgItemEntity.wkMsg.channelID
                            mMsg.channelType = uiChatMsgItemEntity.wkMsg.channelType
                            if (uiChatMsgItemEntity.wkMsg.remoteExtra != null && uiChatMsgItemEntity.wkMsg.remoteExtra.contentEditMsgModel != null) {
                                mMsg.remoteExtra.contentEditMsgModel = WKTextContent(selectText)
                            }
                            val chatAdapter = getAdapter() as ChatAdapter
                            uiChatMsgItemEntity.wkMsg.baseContentMsgModel.content = selectText
                            favoritePopupMenu.iPopupItemClick.onClick(
                                mMsg,
                                chatAdapter.conversationContext
                            )
                        }
                    }
                })
        }

        mSelectableTextHelper!!.setSelectListener(object : SelectTextHelper.OnSelectListener {
            override fun onClick(v: View?, originalContent: String?) {
            }


            /**
             * 长按回调
             */
            override fun onLongClick(v: View, local: FloatArray) {
                // showPopup(messageContent,textView,local)
            }

            override fun onTextSelected(content: String?) {
                selectText = content
            }


            /**
             * 弹窗关闭回调
             */
            override fun onDismiss() {}
            override fun onClickLink(clickableContent: NormalClickableSpan) {
                if (clickableContent.clickableContent.type == NormalClickableContent.NormalClickableTypes.URL) {
                    val intent = Intent(
                        context, WKWebViewActivity::class.java
                    )
                    intent.putExtra("url", clickableContent.clickableContent.content)
                    context.startActivity(intent)
                } else if (clickableContent.clickableContent.type == NormalClickableContent.NormalClickableTypes.Remind) {
                    val uid: String
                    var groupNo = ""
                    if (clickableContent.clickableContent.content.contains("|")) {
                        uid = clickableContent.clickableContent.content.split("|")[0]
                        groupNo = clickableContent.clickableContent.content.split("|")[1]
                    } else {
                        uid = clickableContent.clickableContent.content
                    }
                    val intent = Intent(context, UserDetailActivity::class.java)
                    intent.putExtra("uid", uid)
                    if (!TextUtils.isEmpty(groupNo)) intent.putExtra("groupID", groupNo)
                    context.startActivity(intent)
                } else {
                    val content = clickableContent.clickableContent.content
                    if (StringUtils.isMobile(content)) {
                        val chatAdapter = getAdapter() as ChatAdapter
                        chatAdapter.hideSoftKeyboard()
                        val list = ArrayList<BottomSheetItem>()
                        list.add(
                            BottomSheetItem(
                                context.getString(R.string.copy),
                                R.mipmap.msg_copy,
                                object : BottomSheetItem.IBottomSheetClick {
                                    override fun onClick() {
                                        val cm =
                                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val mClipData = ClipData.newPlainText("Label", content)
                                        cm.setPrimaryClip(mClipData)
                                        WKToastUtils.getInstance()
                                            .showToastNormal(context.getString(R.string.copyed))
                                    }
                                })
                        )
                        list.add(
                            BottomSheetItem(
                                context.getString(R.string.call),
                                R.mipmap.msg_calls,
                                object : BottomSheetItem.IBottomSheetClick {
                                    override fun onClick() {
                                        val desc = String.format(
                                            context.getString(R.string.call_phone_permissions_desc),
                                            context.getString(R.string.app_name)
                                        );
                                        WKPermissions.getInstance().checkPermissions(
                                            object : IPermissionResult {
                                                override fun onResult(result: Boolean) {
                                                    if (result) {
                                                        val intent =
                                                            Intent(
                                                                Intent.ACTION_CALL,
                                                                Uri.parse("tel:$content")
                                                            )
                                                        context.startActivity(intent)
                                                    }
                                                }

                                                override fun clickResult(isCancel: Boolean) {

                                                }
                                            },
                                            chatAdapter.conversationContext.chatActivity,
                                            desc,
                                            Manifest.permission.CALL_PHONE
                                        )

                                    }
                                })
                        )
                        list.add(
                            BottomSheetItem(
                                context.getString(R.string.add_to_phone_book),
                                R.mipmap.msg_contacts,
                                object : BottomSheetItem.IBottomSheetClick {
                                    override fun onClick() {

                                        val addIntent = Intent(
                                            Intent.ACTION_INSERT,
                                            Uri.withAppendedPath(
                                                Uri.parse("content://com.android.contacts"),
                                                "contacts"
                                            )
                                        )
                                        addIntent.type = "vnd.android.cursor.dir/person"
                                        addIntent.type = "vnd.android.cursor.dir/contact"
                                        addIntent.type = "vnd.android.cursor.dir/raw_contact"
                                        addIntent.putExtra(
                                            ContactsContract.Intents.Insert.NAME,
                                            ""
                                        )
                                        addIntent.putExtra(
                                            ContactsContract.Intents.Insert.PHONE,
                                            content
                                        )
                                        context.startActivity(addIntent)

                                    }
                                })
                        )
                        list.add(
                            BottomSheetItem(
                                context.getString(R.string.str_search),
                                R.mipmap.ic_ab_search,
                                object : BottomSheetItem.IBottomSheetClick {
                                    override fun onClick() {
                                        if (uiChatMsgItemEntity.iLinkClick != null)
                                            uiChatMsgItemEntity.iLinkClick.onShowSearchUser(
                                                content
                                            )
                                    }
                                })
                        )
//                        val phoneTips = String.format(
//                            context.getString(R.string.phone_tips),
//                            context.getString(R.string.app_name)
//                        )
                        val displaySpans = SpannableStringBuilder()
                        displaySpans.append(content)
                        displaySpans.setSpan(
                            StyleSpan(Typeface.BOLD), 0,
                            content.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        displaySpans.setSpan(
                            ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)), 0,
                            content.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        WKDialogUtils.getInstance()
                            .showBottomSheet(context, displaySpans, false, list)
                        return
                    }
                    if (StringUtils.isEmail(content)) {
                        val displaySpans = SpannableStringBuilder()
                        displaySpans.append(content)
                        displaySpans.setSpan(
                            StyleSpan(Typeface.BOLD), 0,
                            content.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        displaySpans.setSpan(
                            ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)), 0,
                            content.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        val list = ArrayList<BottomSheetItem>()
                        list.add(
                            BottomSheetItem(
                                context.getString(R.string.copy),
                                R.mipmap.msg_copy,
                                object : BottomSheetItem.IBottomSheetClick {
                                    override fun onClick() {
                                        val cm =
                                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val mClipData = ClipData.newPlainText("Label", content)
                                        cm.setPrimaryClip(mClipData)
                                        WKToastUtils.getInstance()
                                            .showToastNormal(context.getString(R.string.copyed))
                                    }
                                })
                        )
                        list.add(
                            BottomSheetItem(
                                context.getString(R.string.send_email),
                                R.mipmap.msg2_email,
                                object : BottomSheetItem.IBottomSheetClick {
                                    override fun onClick() {
                                        val uri = Uri.parse("mailto:$content")
                                        val email = arrayOf(content)
                                        val intent = Intent(Intent.ACTION_SENDTO, uri)
                                        intent.putExtra(Intent.EXTRA_CC, email) // 抄送人
                                        intent.putExtra(Intent.EXTRA_SUBJECT, "") // 主题
                                        intent.putExtra(Intent.EXTRA_TEXT, "") // 正文
                                        context.startActivity(Intent.createChooser(intent, ""))
                                    }
                                })
                        )
                        list.add(
                            BottomSheetItem(
                                context.getString(R.string.str_search),
                                R.mipmap.ic_ab_search,
                                object : BottomSheetItem.IBottomSheetClick {
                                    override fun onClick() {
                                        if (uiChatMsgItemEntity.iLinkClick != null)
                                            uiChatMsgItemEntity.iLinkClick.onShowSearchUser(
                                                content
                                            )
                                        // if (iLinkClick != null) iLinkClick.onShowSearchUser(content)
                                    }
                                })
                        )
                        WKDialogUtils.getInstance()
                            .showBottomSheet(context, displaySpans, false, list)
                        return
                    }
                }
            }


            /**
             * 全选显示自定义弹窗回调
             */
            override fun onSelectAllShowCustomPop(local: FloatArray) {
                showPopup(uiChatMsgItemEntity, textView, local)
            }

            /**
             * 重置回调
             */
            override fun onReset() {
            }

            /**
             * 解除自定义弹窗回调
             */
            override fun onDismissCustomPop() {
            }

            /**
             * 是否正在滚动回调
             */
            override fun onScrolling() {
            }
        })


    }

    private fun showPopup(uiChatMsgItemEntity: WKUIChatMsgItemEntity, v: View, local: FloatArray) {
        val mMsgConfig: MsgConfig = getMsgConfig(uiChatMsgItemEntity.wkMsg.type)
        var isShowReaction = false
        val `object` = EndpointManager.getInstance()
            .invoke(
                "is_show_reaction",
                CanReactionMenu(uiChatMsgItemEntity.wkMsg, mMsgConfig)
            )
        if (`object` != null) {
            isShowReaction = `object` as Boolean
        }
        if (uiChatMsgItemEntity.wkMsg.flame == 1) isShowReaction = false
        val finalIsShowReaction = isShowReaction
        showChatPopup(
            uiChatMsgItemEntity.wkMsg,
            v,
            local,
            finalIsShowReaction,
            getPopupList(uiChatMsgItemEntity.wkMsg)
        )
    }

    //    private fun setSelectableTextHelper(
//        textView: TextView?,
//        position: Int,
//        isEmoji: Boolean
//    ) {
//       val selectableTextHelper = SelectTextHelper.Builder(textView)
//            .setCursorHandleColor(
//                context.getColor(R.color.blue)
//            )
//            .setCursorHandleSizeInDp(16f)
//            .setSelectedColor(
//                context.getColor(R.color.blue)
//            )
//            .setSelectAll(true)
//            .setIsEmoji(isEmoji)
//            .setScrollShow(false)
//            .setSelectedAllNoPop(true)
//            .setMagnifierShow(false)
//            .build()
//        selectableTextHelper.setSelectListener(object : SelectTextHelper.OnSelectListener {
//            override fun onClick(v: View) {}
//            override fun onLongClick(v: View) {}
//            override fun onTextSelected(content: CharSequence) {
//                val selectedText = content.toString()
//               // msg.setSelectText(selectedText)
////                if (onItemClickListener != null) {
////                    onItemClickListener.onTextSelected(msgArea, position, msg)
////                }
//            }
//
//            override fun onDismiss() {
////                msg.setSelectText(msg.getExtra())
//            }
//
//            override fun onClickUrl(url: String) {}
//            override fun onSelectAllShowCustomPop() {}
//            override fun onReset() {
////                msg.setSelectText(null)
////                msg.setSelectText(msg.getExtra())
//            }
//
//            override fun onDismissCustomPop() {}
//            override fun onScrolling() {}
//        })
//    }
    override val itemViewType: Int
        get() = WKContentType.WK_TEXT


    private fun shotTipsMsg(mTextContent: WKTextContent) {
        var clientMsgNo = mTextContent.reply.message_id
        val mMsg =
            WKIM.getInstance().msgManager.getWithMessageID(mTextContent.reply.message_id)
        if (mMsg != null) {
            clientMsgNo = mMsg.clientMsgNO
        }
        (Objects.requireNonNull(getAdapter()) as ChatAdapter).showTipsMsg(clientMsgNo)
    }

//    private fun showLinkInfo(
//        uiChatMsgItemEntity: WKUIChatMsgItemEntity,
//        msgTimeStatusView: View,
//        parentView: LinearLayout,
//        from: WKChatIteMsgFromType,
//        url: String
//    ) {
//        uiChatMsgItemEntity.isUpdateStatus = false
//        val linkView = LayoutInflater.from(context)
//            .inflate(R.layout.chat_text_link_desc_layout, parentView, false)
//        val msgTimeView = linkView.findViewById<View>(R.id.msgTimeView)
//        setMsgTimeAndStatus(uiChatMsgItemEntity, msgTimeView, from)
//        val titleTv = linkView.findViewById<TextView>(R.id.linkTitleTv)
//        val nameTv = linkView.findViewById<TextView>(R.id.linkNameTv)
//        val contentTv = linkView.findViewById<TextView>(R.id.linkContentTv)
//        val logoIv = linkView.findViewById<AppCompatImageView>(R.id.linkLogoIv)
//        val coverIv = linkView.findViewById<AppCompatImageView>(R.id.linkCoverIv)
//        if (from == WKChatIteMsgFromType.SEND) {
//            contentTv.setTextColor(ContextCompat.getColor(context, R.color.send_text_color))
//            nameTv.setTextColor(ContextCompat.getColor(context, R.color.send_text_color))
//            titleTv.setTextColor(ContextCompat.getColor(context, R.color.send_text_color))
//        } else {
//            contentTv.setTextColor(ContextCompat.getColor(context, R.color.receive_text_color))
//            nameTv.setTextColor(ContextCompat.getColor(context, R.color.receive_text_color))
//            titleTv.setTextColor(ContextCompat.getColor(context, R.color.receive_text_color))
//        }
//        val jsonStr = WKSharedPreferencesUtil.getInstance().getSP(url)
//        var jsonObject: JSONObject? = null
//        try {
//            if (!TextUtils.isEmpty(jsonStr)) jsonObject = JSONObject(jsonStr)
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
//        if (jsonObject == null) {
//            parentView.visibility = View.GONE
//            msgTimeStatusView.visibility = View.VISIBLE
//        } else {
//            val title = jsonObject.optString("title")
//            val content = jsonObject.optString("content")
//            val coverURL = jsonObject.optString("coverURL")
//            val logo = jsonObject.optString("logo")
//            if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(content)) {
//                titleTv.text = title
//                contentTv.text = content
//                Glide.with(context).asBitmap().load(logo)
//                    .into(object : CustomTarget<Bitmap?>(SIZE_ORIGINAL, SIZE_ORIGINAL) {
//                        override fun onResourceReady(
//                            resource: Bitmap, transition: Transition<in Bitmap?>?
//                        ) {
//                            logoIv.visibility = View.VISIBLE
//                            logoIv.setImageBitmap(resource)
//                        }
//
//                        override fun onLoadCleared(placeholder: Drawable?) {}
//                        override fun onLoadFailed(errorDrawable: Drawable?) {
//                            super.onLoadFailed(errorDrawable)
//                            logoIv.visibility = View.GONE
//                        }
//                    })
//                // GlideUtils.getInstance().showImg(getContext(), logo, logoIv);
//                if (!TextUtils.isEmpty(coverURL)) {
//                    // GlideUtils.getInstance().showImg(getContext(), coverURL.replaceAll(" ", ""), coverIv);
//                    Glide.with(context).asBitmap().load(coverURL.replace(" ".toRegex(), ""))
//                        .into(object : CustomTarget<Bitmap?>(SIZE_ORIGINAL, SIZE_ORIGINAL) {
//                            override fun onResourceReady(
//                                resource: Bitmap, transition: Transition<in Bitmap?>?
//                            ) {
//                                coverIv.visibility = View.VISIBLE
//                                coverIv.setImageBitmap(resource)
//                            }
//
//                            override fun onLoadCleared(placeholder: Drawable?) {
//
//                            }
//
//                            override fun onLoadFailed(errorDrawable: Drawable?) {
//                                super.onLoadFailed(errorDrawable)
//                                coverIv.visibility = View.GONE
//                            }
//
//                        })
//                } else coverIv.visibility = View.GONE
//                val strings = url.split("\\.").toTypedArray()
//                if (strings.size > 1) {
//                    val stringBuffer = StringBuffer()
//                    for (i in 1 until strings.size) {
//                        if (!TextUtils.isEmpty(stringBuffer)) stringBuffer.append(".")
//                        stringBuffer.append(strings[i])
//                    }
//                    nameTv.text = stringBuffer
//                }
//                parentView.removeAllViews()
//                parentView.addView(linkView)
//                parentView.visibility = View.VISIBLE
//                msgTimeStatusView.visibility = View.GONE
//            } else {
//                parentView.visibility = View.GONE
//                msgTimeStatusView.visibility = View.VISIBLE
//            }
//        }
//    }

    override fun resetCellListener(
        position: Int,
        parentView: View,
        uiChatMsgItemEntity: WKUIChatMsgItemEntity,
        from: WKChatIteMsgFromType
    ) {
        super.resetCellListener(position, parentView, uiChatMsgItemEntity, from)
//        val linkView = parentView.findViewById<LinearLayout>(R.id.linkView)
//        if (linkView != null && linkView.childCount > 0) {
//            val msgTimeView = linkView.getChildAt(0)
//            setMsgTimeAndStatus(uiChatMsgItemEntity, msgTimeView, from)
//        }
    }

    override fun resetCellBackground(
        parentView: View,
        uiChatMsgItemEntity: WKUIChatMsgItemEntity,
        from: WKChatIteMsgFromType
    ) {
        super.resetCellBackground(parentView, uiChatMsgItemEntity, from)
        val contentTvLayout = parentView.findViewById<BubbleLayout>(R.id.contentTvLayout)
        val textContentLayout = parentView.findViewById<View>(R.id.textContentLayout)
        val msgTimeView = parentView.findViewById<View>(R.id.msgTimeView)
        // 这里要指定文本宽度 - padding的距离
        if (textContentLayout == null || msgTimeView == null) {
            return
        }
        textContentLayout.layoutParams.width = getViewWidth(from, uiChatMsgItemEntity)
        val bgType = getMsgBgType(
            uiChatMsgItemEntity.previousMsg,
            uiChatMsgItemEntity.wkMsg,
            uiChatMsgItemEntity.nextMsg
        )
        contentTvLayout.setAll(bgType, from, WKContentType.WK_TEXT)
        if (textContentLayout.layoutParams.width < msgTimeView.layoutParams.width) {
            textContentLayout.layoutParams.width = msgTimeView.layoutParams.width
        }
    }

    override fun resetFromName(
        position: Int,
        parentView: View,
        uiChatMsgItemEntity: WKUIChatMsgItemEntity,
        from: WKChatIteMsgFromType
    ) {
        val receivedTextNameTv = parentView.findViewById<TextView>(R.id.receivedTextNameTv)
        setFromName(uiChatMsgItemEntity, from, receivedTextNameTv)
    }

    override fun refreshReply(
        adapterPosition: Int,
        parentView: View,
        uiChatMsgItemEntity: WKUIChatMsgItemEntity,
        from: WKChatIteMsgFromType
    ) {
        super.refreshReply(adapterPosition, parentView, uiChatMsgItemEntity, from)
        val textModel = uiChatMsgItemEntity.wkMsg.baseContentMsgModel as WKTextContent
        val replyContentRevokedTv = parentView.findViewWithTag<View>("replyRevokedTv")
        val replyContentLayout = parentView.findViewWithTag<View>("replyContentLayout")
        if (replyContentRevokedTv == null || replyContentLayout == null)
            return
        if (textModel.reply != null) {
            if (uiChatMsgItemEntity.wkMsg.baseContentMsgModel.reply.revoke == 1) {
                replyContentRevokedTv.visibility = View.VISIBLE
                replyContentLayout.visibility = View.GONE
            } else {
                val replyIV = parentView.findViewWithTag<AppCompatImageView>("replyIV")
                val replyTV = parentView.findViewWithTag<AppCompatTextView>("replyTV")
                if (replyIV != null && replyTV != null) {
                    showReplyContent(textModel, replyIV, replyTV)
                }
            }
        }
    }

    private fun replyView(
        contentLayout: BubbleLayout,
        from: WKChatIteMsgFromType,
        uiChatMsgItemEntity: WKUIChatMsgItemEntity
    ) {
        val replyLayout = LinearLayout(context)
        replyLayout.orientation = LinearLayout.HORIZONTAL
        replyLayout.setBackgroundResource(R.drawable.reply_bg)
        contentLayout.addView(
            replyLayout, 1,
            LayoutHelper.createLinear(
                LayoutHelper.WRAP_CONTENT,
                LayoutHelper.WRAP_CONTENT,
                0f,
                5f,
                0f,
                10f
            )
        )
        val lineView = View(context)
        lineView.setBackgroundResource(R.drawable.reply_line)
        replyLayout.addView(
            lineView,
            LayoutHelper.createLinear(3, LayoutHelper.MATCH_PARENT, 0f, 0f, 5f, 0f)
        )

        // revoke
        val replyContentRevokedTv = AppCompatTextView(context)
        replyLayout.addView(
            replyContentRevokedTv,
            LayoutHelper.createLinear(
                LayoutHelper.WRAP_CONTENT,
                LayoutHelper.WRAP_CONTENT,
                0f,
                10f,
                10f,
                10f
            )
        )
        replyContentRevokedTv.setTextColor(ContextCompat.getColor(context, R.color.popupTextColor))
        replyContentRevokedTv.setText(R.string.reply_msg_is_revoked)
        val size = context.resources.getDimension(R.dimen.font_size_14)
        val pSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_PX,
            size,
            contentLayout.resources.displayMetrics
        )
        replyContentRevokedTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, pSize)
        // reply content layout
        val replyContentLayout = LinearLayout(context)
        replyContentLayout.orientation = LinearLayout.VERTICAL
        replyLayout.addView(
            replyContentLayout,
            LayoutHelper.createLinear(
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.WRAP_CONTENT,
                0f,
                5f,
                5f,
                5f
            )
        )

        val userLayout = LinearLayout(context)
        replyContentLayout.addView(
            userLayout,
            LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT)
        )
        userLayout.orientation = LinearLayout.HORIZONTAL
        val avatarView = AvatarView(context)
        avatarView.setSize(20f)
        val userNameTv = AppCompatTextView(context)
        val nameSize = context.resources.getDimension(R.dimen.font_size_12)
        val namePSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_PX,
            nameSize,
            contentLayout.resources.displayMetrics
        )
        userNameTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, namePSize)
        userNameTv.setTextColor(ContextCompat.getColor(context, R.color.color999))
        userLayout.addView(
            avatarView,
            LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT)
        )
        userLayout.addView(
            userNameTv,
            LayoutHelper.createLinear(
                LayoutHelper.WRAP_CONTENT,
                LayoutHelper.WRAP_CONTENT,
                5f,
                0f,
                0f,
                0f
            )
        )
        val replyTV = AppCompatTextView(context)
        replyTV.ellipsize = TextUtils.TruncateAt.END
        replyTV.isSingleLine = true
        replyTV.setLines(1)
        replyContentLayout.addView(
            replyTV,
            LayoutHelper.createLinear(
                LayoutHelper.WRAP_CONTENT,
                LayoutHelper.WRAP_CONTENT,
                0f,
                10f,
                0f,
                0f
            )
        )
        replyTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, pSize)
        val textColor: Int = if (from == WKChatIteMsgFromType.SEND) {
            ContextCompat.getColor(context, R.color.colorDark)
        } else {
            ContextCompat.getColor(context, R.color.receive_text_color)
        }
        replyTV.setTextColor(textColor)

        val replyIV = AppCompatImageView(context)
        replyIV.scaleType = ImageView.ScaleType.CENTER
        replyContentLayout.addView(replyIV, LayoutHelper.createLinear(80, 80, 0f, 10f, 0f, 0f))

        val textModel = uiChatMsgItemEntity.wkMsg.baseContentMsgModel as WKTextContent
        val mChannel = WKIM.getInstance().channelManager.getChannel(
            textModel.reply.from_uid, WKChannelType.PERSONAL
        )
        if (mChannel != null) {
            val showName =
                if (TextUtils.isEmpty(mChannel.channelRemark)) {
                    mChannel.channelName
                } else mChannel.channelRemark
            userNameTv.text = showName
            avatarView.showAvatar(mChannel)
        }
        if (!TextUtils.isEmpty(uiChatMsgItemEntity.wkMsg.fromUID)) {
            val colors =
                WKBaseApplication.getInstance().context.resources.getIntArray(R.array.name_colors)
            val index = abs(textModel.reply.from_uid.hashCode()) % colors.size
            val myShapeDrawable = lineView.background as GradientDrawable
            myShapeDrawable.setColor(colors[index])
            userNameTv.setTextColor(colors[index])
            val bgColor = ColorUtils.setAlphaComponent(colors[index], 30)
            val bgShapeDrawable = replyLayout.background as GradientDrawable
            bgShapeDrawable.setColor(bgColor)
        }
        if (textModel.reply.revoke == 1) {
            replyContentLayout.visibility = View.GONE
            replyContentRevokedTv.visibility = View.VISIBLE
            return
        }
        replyContentRevokedTv.visibility = View.GONE
        showReplyContent(textModel, replyIV, replyTV)
        replyLayout.setOnClickListener {
            shotTipsMsg(
                textModel
            )
        }
        replyTV.setOnClickListener {
            shotTipsMsg(
                textModel
            )
        }

        replyContentRevokedTv.tag = "replyRevokedTv"
        replyIV.tag = "replyIV"
        replyTV.tag = "replyTV"
        replyContentLayout.tag = "replyContentLayout"
    }

    private fun showReplyContent(
        mTextContent: WKTextContent,
        replyIv: AppCompatImageView,
        replyTv: AppCompatTextView
    ) {
        when (mTextContent.reply.payload.type) {
            WKContentType.WK_GIF -> {
                replyIv.visibility = View.VISIBLE
                replyTv.visibility = View.GONE
                val gifContent = mTextContent.reply.payload as WKGifContent
                GlideUtils.getInstance()
                    .showGif(
                        context,
                        WKApiConfig.getShowUrl(gifContent.url),
                        replyIv,
                        null
                    )
            }

            WKContentType.WK_IMAGE -> {
                replyIv.visibility = View.VISIBLE
                replyTv.visibility = View.GONE
                val imageContent = mTextContent.reply.payload as WKImageContent
                var showUrl: String
                if (!TextUtils.isEmpty(imageContent.localPath)) {
                    showUrl = imageContent.localPath
                    val file = File(showUrl)
                    if (!file.exists()) {
                        //如果本地文件被删除就显示网络图片
                        showUrl = WKApiConfig.getShowUrl(imageContent.url)
                    }
                } else {
                    showUrl = WKApiConfig.getShowUrl(imageContent.url)
                }
                GlideUtils.getInstance().showImg(context, showUrl, replyIv)
            }

            else -> {
                replyIv.visibility = View.GONE
                replyTv.visibility = View.VISIBLE
                var content = mTextContent.reply.payload.displayContent
                if (mTextContent.reply.contentEditMsgModel != null && !TextUtils.isEmpty(
                        mTextContent.reply.contentEditMsgModel.displayContent
                    )
                ) {
                    content = mTextContent.reply.contentEditMsgModel.displayContent
                }
                if (TextUtils.isEmpty(content)) {
                    content = context.getString(R.string.base_unknow_msg)
                }
                replyTv.movementMethod = LinkMovementMethod.getInstance()
                val strUrls = StringUtils.getStrUrls(content)
                val replySpan = SpannableStringBuilder()
                replySpan.append(content)
                if (strUrls.size > 0) {
                    for (url in strUrls) {
                        if (TextUtils.isEmpty(url)) {
                            continue
                        }
                        var fromIndex = 0
                        while (fromIndex >= 0) {
                            fromIndex = content.indexOf(url, fromIndex)
                            if (fromIndex >= 0) {
                                replySpan.setSpan(
                                    StyleSpan(Typeface.BOLD),
                                    fromIndex,
                                    fromIndex + url.length,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                replySpan.setSpan(
                                    NormalClickableSpan(true,
                                        ContextCompat.getColor(context, R.color.blue),
                                        NormalClickableContent(
                                            NormalClickableContent.NormalClickableTypes.URL,
                                            url
                                        ),
                                        object : NormalClickableSpan.IClick {
                                            override fun onClick(view: View) {
                                                SoftKeyboardUtils.getInstance()
                                                    .hideSoftKeyboard(context as Activity)
                                                val intent = Intent(
                                                    context, WKWebViewActivity::class.java
                                                )
                                                intent.putExtra("url", url)
                                                context.startActivity(intent)
                                            }
                                        }),
                                    fromIndex,
                                    fromIndex + url.length,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                fromIndex += url.length
                            }
                        }
                    }
                }

                // emoji
                val matcher = EmojiManager.getInstance().pattern.matcher(content)
                while (matcher.find()) {
                    val start = matcher.start()
                    val end = matcher.end()
                    val emoji = content.substring(start, end)
                    val d = MoonUtil.getEmotDrawable(context, emoji, MoonUtil.SMALL_SCALE)
                    if (d != null) {
                        val span: AlignImageSpan =
                            object : AlignImageSpan(d, ALIGN_CENTER) {
                                override fun onClick(view: View) {}
                            }
                        replySpan.setSpan(
                            span,
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                replyTv.text = replySpan
            }
        }
    }
}