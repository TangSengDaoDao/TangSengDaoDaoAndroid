package com.chat.advanced

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.chat.advanced.entity.ChatBgKeys
import com.chat.advanced.msg.ScreenshotContent
import com.chat.advanced.msg.ScreenshotProvider
import com.chat.advanced.service.AdvancedModel
import com.chat.advanced.ui.ChatBgListActivity
import com.chat.advanced.ui.MsgRemindActivity
import com.chat.advanced.ui.ReadMsgMembersActivity
import com.chat.advanced.ui.search.ChatImgActivity
import com.chat.advanced.ui.search.ChatWithDateActivity
import com.chat.advanced.ui.search.RecordActivity
import com.chat.advanced.ui.search.SearchAllMembersActivity
import com.chat.advanced.utils.ReactionAnimation
import com.chat.advanced.utils.ReactionStickerUtils
import com.chat.advanced.utils.ScreenShotListenManager
import com.chat.base.WKBaseApplication
import com.chat.base.config.WKApiConfig
import com.chat.base.config.WKConfig
import com.chat.base.config.WKConstants
import com.chat.base.config.WKSharedPreferencesUtil
import com.chat.base.endpoint.EndpointCategory
import com.chat.base.endpoint.EndpointHandler
import com.chat.base.endpoint.EndpointManager
import com.chat.base.endpoint.EndpointSID
import com.chat.base.endpoint.entity.CanReactionMenu
import com.chat.base.endpoint.entity.ChatBgItemMenu
import com.chat.base.endpoint.entity.ChatItemPopupMenu
import com.chat.base.endpoint.entity.ChatSettingCellMenu
import com.chat.base.endpoint.entity.EditImgMenu
import com.chat.base.endpoint.entity.EditMsgMenu
import com.chat.base.endpoint.entity.MsgReactionMenu
import com.chat.base.endpoint.entity.OtherLoginViewMenu
import com.chat.base.endpoint.entity.ReadMsgDetailMenu
import com.chat.base.endpoint.entity.ReadMsgMenu
import com.chat.base.endpoint.entity.SearchChatContentMenu
import com.chat.base.endpoint.entity.SetChatBgMenu
import com.chat.base.endpoint.entity.ShowMsgReactionMenu
import com.chat.base.endpoint.entity.WKSendMsgMenu
import com.chat.base.glide.GlideUtils
import com.chat.base.msg.IConversationContext
import com.chat.base.msgitem.ReactionSticker
import com.chat.base.msgitem.WKContentType
import com.chat.base.msgitem.WKMsgItemViewManager
import com.chat.base.net.HttpResponseCode
import com.chat.base.ui.Theme
import com.chat.base.ui.components.SwitchView
import com.chat.base.utils.AndroidUtilities
import com.chat.base.utils.ImageUtils
import com.chat.base.utils.SvgHelper
import com.chat.base.utils.UserUtils
import com.chat.base.utils.WKFileUtils
import com.chat.base.utils.WKTimeUtils
import com.chat.base.utils.WKToastUtils
import com.chat.base.utils.singleclick.SingleClickUtil
import com.xinbida.wukongim.WKIM
import com.xinbida.wukongim.entity.WKChannel
import com.xinbida.wukongim.entity.WKChannelExtras
import com.xinbida.wukongim.entity.WKChannelType
import com.xinbida.wukongim.entity.WKMsg
import com.xinbida.wukongim.message.type.WKSendMsgResult
import com.xinbida.wukongim.msgmodel.WKImageContent
import java.io.File
import kotlin.math.abs


class WKAdvancedApplication private constructor() {
    //截屏监听
    private var screenShotListenManager: ScreenShotListenManager? = null

    private object SingletonInstance {
        val INSTANCE = WKAdvancedApplication()
    }

    companion object {
        val instance: WKAdvancedApplication
            get() = SingletonInstance.INSTANCE
    }

    fun init() {
        val appModule = WKBaseApplication.getInstance().getAppModuleWithSid("advanced")
        if (!WKBaseApplication.getInstance().appModuleIsInjection(appModule)) {
            return
        }
        initReactionSticker()
//        WKImageEditorApplication.getInstance().init()
        WKIM.getInstance().msgManager.registerContentMsg(ScreenshotContent::class.java)
        WKMsgItemViewManager.getInstance()
            .addChatItemViewProvider(WKContentType.screenshot, ScreenshotProvider())
        WKIM.getInstance().cmdManager.addCmdListener("advanced_application") { cmd ->
            if (cmd != null && cmd.cmdKey == "syncMessageReaction") {
                if (cmd.paramJsonObject.has("channel_id") && cmd.paramJsonObject.has("channel_type")) {
                    val channelId: String = cmd.paramJsonObject.optString("channel_id")
                    val channelType: Byte = cmd.paramJsonObject.optInt("channel_type").toByte()
                    AdvancedModel.instance.syncReaction(channelId, channelType)
                }
            }
        }
        EndpointManager.getInstance()
            .setMethod("advancedModule", EndpointSID.sendMessage) { `object` ->
                if (`object` is WKSendMsgMenu) {
                    `object`.option.setting.receipt = `object`.channel.receipt
                }
            }

        EndpointManager.getInstance().setMethod(EndpointSID.openChatPage) { `object` ->
            if (`object` is WKChannel && !TextUtils.isEmpty(`object`.channelID)) {
                AdvancedModel.instance.syncReaction(`object`.channelID, `object`.channelType)
            }
            null
        }
        EndpointManager.getInstance().setMethod("stop_screen_shot") {
            stopScreenShotListen()
        }
        EndpointManager.getInstance().setMethod(
            "start_screen_shot"
        ) { `object` ->
            val iConversationContext = `object` as IConversationContext
            startScreenShotListen(iConversationContext)
            null
        }
        EndpointManager.getInstance().setMethod(
            "msg_remind_view"
        ) { `object` ->
            val chatSettingCellMenu = `object` as ChatSettingCellMenu
            getMsgItemRemindView(chatSettingCellMenu)
        }
        EndpointManager.getInstance().setMethod(
            "find_msg_view"
        ) { `object` ->
            val chatSettingCellMenu = `object` as ChatSettingCellMenu
            getFindMsgView(chatSettingCellMenu)
        }

        EndpointManager.getInstance().setMethod(
            "msg_receipt_view"
        ) { `object` ->
            val chatSettingCellMenu = `object` as ChatSettingCellMenu
            getMsgReceiptView(chatSettingCellMenu)
        }


        // 搜索聊天图片
//        EndpointManager.getInstance().setMethod(
//            "str_search_chat_img", EndpointCategory.wkSearchChatContent, 96
//        ) {
//            SearchChatContentMenu(
//                WKBaseApplication.getInstance().context.getString(R.string.image)
//            ) { channelID: String?, channelType: Byte ->
//                val intent = Intent(
//                    WKBaseApplication.getInstance().context,
//                    ChatImgActivity::class.java
//                )
//                intent.putExtra("channel_id", channelID)
//                intent.putExtra("channel_type", channelType)
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                WKBaseApplication.getInstance().context.startActivity(intent)
//            }
//        }
//        // 按日期查找聊天内容
//        EndpointManager.getInstance().setMethod(
//            "str_search_chat_for_date", EndpointCategory.wkSearchChatContent, 100
//        ) {
//            SearchChatContentMenu(
//                WKBaseApplication.getInstance().context.getString(R.string.str_search_for_date)
//            ) { channelID: String?, channelType: Byte ->
//                val intent = Intent(
//                    WKBaseApplication.getInstance().context,
//                    ChatWithDateActivity::class.java
//                )
//                intent.putExtra("channel_id", channelID)
//                intent.putExtra("channel_type", channelType)
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                WKBaseApplication.getInstance().context.startActivity(intent)
//            }
//        }
//        // 搜索群成员
//        EndpointManager.getInstance().setMethod(
//            "str_search_group_member", EndpointCategory.wkSearchChatContent, 101
//        ) { `object`: Any ->
//            val mChannel = `object` as WKChannel
//            if (mChannel.channelType == WKChannelType.GROUP) {
//                return@setMethod SearchChatContentMenu(
//                    WKBaseApplication.getInstance().context
//                        .getString(R.string.str_find_group_member)
//                ) { channelID: String?, channelType: Byte ->
//                    val intent = Intent(
//                        WKBaseApplication.getInstance().context,
//                        SearchAllMembersActivity::class.java
//                    )
//                    intent.putExtra("channelID", channelID)
//                    intent.putExtra("channelType", channelType)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    WKBaseApplication.getInstance().context.startActivity(intent)
//                }
//            } else {
//                return@setMethod null
//            }
//        }

        EndpointManager.getInstance()
            .setMethod(
                "message_edit",
                EndpointCategory.wkChatPopupItem,
                1,
                object : EndpointHandler {
                    override fun invoke(`object`: Any?): Any? {
                        val mMsg = `object` as WKMsg
                        if (mMsg.type == WKContentType.WK_TEXT) {
                            if (!TextUtils.isEmpty(mMsg.fromUID) && mMsg.fromUID.equals(
                                    WKConfig.getInstance().uid
                                ) && mMsg.status == WKSendMsgResult.send_success && WKTimeUtils.getInstance().currentSeconds - mMsg.timestamp < 60 * 60 * 24
                            ) {
                                val popupMenu = ChatItemPopupMenu(
                                    R.mipmap.msg_edit,
                                    WKBaseApplication.getInstance().context.getString(R.string.str_edit),
                                    object : ChatItemPopupMenu.IPopupItemClick {
                                        override fun onClick(
                                            mMsg: WKMsg,
                                            iConversationContext: IConversationContext
                                        ) {
                                            iConversationContext.showEdit(mMsg)
                                        }

                                    })
                                popupMenu.tag = "text_message_edit"
                                return popupMenu
                            }
                        } else if (mMsg.type == WKContentType.WK_IMAGE) {
                            val popupMenu = ChatItemPopupMenu(
                                R.mipmap.msg_edit,
                                WKBaseApplication.getInstance().context.getString(R.string.str_edit),
                                object : ChatItemPopupMenu.IPopupItemClick {
                                    override fun onClick(
                                        mMsg: WKMsg,
                                        iConversationContext: IConversationContext
                                    ) {
                                        val imgMsgModel = mMsg.baseContentMsgModel as
                                                WKImageContent
                                        var showUrl: String
                                        if (!TextUtils.isEmpty(imgMsgModel.localPath)) {
                                            showUrl = imgMsgModel.localPath
                                            val file = File(showUrl)
                                            if (!file.exists() || file.length() == 0L) {
                                                //如果本地文件被删除就显示网络图片
                                                showUrl = WKApiConfig.getShowUrl(imgMsgModel.url)
                                            }
                                        } else {
                                            showUrl = WKApiConfig.getShowUrl(imgMsgModel.url)
                                        }
                                        editImg(showUrl, iConversationContext.chatActivity)
                                    }
                                })
                            popupMenu.tag = "image_message_edit"
                            return popupMenu
                        }
                        return null
                    }

                })

        EndpointManager.getInstance().setMethod(
            "editMsg"
        ) { `object` ->
            val menu = `object` as
                    EditMsgMenu
            editImg(menu.url, menu.context)
            null
        }

        EndpointManager.getInstance().setMethod(
            "show_receipt"
        ) { `object` ->
            val mMsg = `object` as WKMsg
            mMsg.setting.receipt == 1 && mMsg.remoteExtra.readedCount > 0 && mMsg.channelType == WKChannelType.GROUP && !TextUtils.isEmpty(
                mMsg.fromUID
            ) && mMsg.fromUID == WKConfig.getInstance().uid
        }

        EndpointManager.getInstance().setMethod(
            "is_show_reaction"
        ) { `object` ->
            val menu = `object` as CanReactionMenu
            val mMsg = menu.mMsg
            val config = menu.config
            var isShowReaction = true
            if (mMsg.status != WKSendMsgResult.send_success || TextUtils.isEmpty(mMsg.messageID) || !config.isCanShowReaction) {
                isShowReaction = false
            }
            if (mMsg.channelType == WKChannelType.PERSONAL) {
                if (UserUtils.getInstance()
                        .checkFriendRelation(mMsg.channelID) || UserUtils.getInstance()
                        .checkBlacklist(mMsg.channelID)
                ) {
                    isShowReaction = false
                }
            }
            if (mMsg.channelType == WKChannelType.GROUP) {
                if (!UserUtils.getInstance().checkInGroupOk(
                        mMsg.channelID,
                        WKConfig.getInstance().uid
                    ) || UserUtils.getInstance()
                        .checkGroupBlacklist(mMsg.channelID, WKConfig.getInstance().uid)
                ) {
                    isShowReaction = false
                }
            }
            isShowReaction
        }

        // 查看消息已读未读详情
        EndpointManager.getInstance().setMethod("show_msg_read_detail") { `object`: Any? ->
            if (`object` is ReadMsgDetailMenu) {
                val intent = Intent(
                    WKBaseApplication.getInstance().context,
                    ReadMsgMembersActivity::class.java
                )
                intent.putExtra("message_id", `object`.messageID)
                intent.putExtra(
                    "group_no",
                    `object`.iConversationContext.chatChannelInfo.channelID
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                WKBaseApplication.getInstance().context.startActivity(intent)
            }
            null
        }


        EndpointManager.getInstance().setMethod(
            "other_login_view"
        ) { `object` ->
            val otherLoginView = `object` as OtherLoginViewMenu
            getOtherLoginView(otherLoginView.context, otherLoginView.parentViewGroup)
        }

        EndpointManager.getInstance().setMethod("get_wx_token") { `object` ->
            val code = `object` as String
            AdvancedModel.instance.wxLogin(code)
            null
        }

        EndpointManager.getInstance().setMethod(
            "set_chat_bg_view"
        ) { `object` ->
            val menu = `object` as ChatBgItemMenu
            getChatBgView(menu)
            null
        }

        EndpointManager.getInstance().setMethod("set_chat_bg")
        { `object` ->
            val menu = `object` as SetChatBgMenu
            setChatBG(menu)
            null
        }


        EndpointManager.getInstance().setMethod(
            "reaction_sticker"
        ) {
            reactionStickers
        }
        EndpointManager.getInstance().setMethod("stop_reaction_animation") {
            ReactionAnimation.stop()
        }
        //消息回应监听
        EndpointManager.getInstance().setMethod("wk_msg_reaction") { `object`: Any? ->
            if (`object` is MsgReactionMenu) {
//                ReactionAnimation.show(
//                    `object`.chatAdapter.context,
//                    `object`.chatAdapter,
//                    `object`.emoji,
//                    `object`.location,
//                    `object`.wkMsg
//                )
                var isAdded = false
                if (`object`.wkMsg.reactionList != null && `object`.wkMsg.reactionList.isNotEmpty()) {
                    for (reaction in `object`.wkMsg.reactionList) {
                        if (reaction.emoji == `object`.emoji && reaction.uid == WKConfig.getInstance().uid) {
                            isAdded = true
                            break
                        }
                    }
                }
                if (!isAdded) {
                    ReactionStickerUtils.showAnimation = `object`.emoji
                }

                AdvancedModel.instance.reactionsMsg(
                    `object`.wkMsg.channelID,
                    `object`.wkMsg.channelType,
                    `object`.wkMsg.messageID,
                    `object`.emoji
                )
            }
            null
        }
        EndpointManager.getInstance().setMethod("refresh_msg_reaction") { `object` ->
            val menu = `object` as ShowMsgReactionMenu
            ReactionStickerUtils.refreshMsgReactionsData(
                menu.parentView,
                menu.chatAdapter,
                menu.from,
                menu.list
            )
            null
        }
        EndpointManager.getInstance().setMethod(
            "show_msg_reaction"
        ) { `object` ->
            val menu = `object` as ShowMsgReactionMenu
            ReactionStickerUtils.setMsgReactionsData(
                menu.parentView,
                menu.chatAdapter,
                menu.from,
                menu.list
            )
            null
        }

        EndpointManager.getInstance().setMethod(
            "read_msg"
        ) { `object` ->
            val menu = `object` as ReadMsgMenu
            AdvancedModel.instance.readMsg(
                menu.channelID, menu.channelType, menu.msgIds
            )
            null
        }

    }

    private fun setChatBG(menu: SetChatBgMenu) {
        val channel =
            WKIM.getInstance().channelManager.getChannel(menu.channelID, menu.channelType)
        if (channel?.localExtra == null) return
        val urlObject = channel.localExtra[ChatBgKeys.chatBgUrl]
        var url = ""
        if (urlObject != null && urlObject is String) {
            url = urlObject
        }
        val themePref = Theme.getTheme()
        val isDark = if (themePref != Theme.DARK_MODE && themePref != Theme.LIGHT_MODE) {
            Theme.isSystemDarkMode(menu.backGroundIV.context)
        } else {
            themePref == Theme.DARK_MODE
        }

        if (TextUtils.isEmpty(url)) {
//            val userInfoEntity = WKConfig.getInstance().userInfo
            val overallURL =
                WKSharedPreferencesUtil.getInstance().getSPWithUID(ChatBgKeys.chatBgUrl)
            val overallIsDeleted =
                WKSharedPreferencesUtil.getInstance().getIntWithUID(ChatBgKeys.chatBgIsDeleted)
            if (overallIsDeleted == 1) return

            if (!TextUtils.isEmpty(overallURL)) {
                val isSvg =
                    WKSharedPreferencesUtil.getInstance().getIntWithUID(ChatBgKeys.chatBgIsSvg)
                val colorStr = WKSharedPreferencesUtil.getInstance()
                    .getSPWithUID(if (isDark) ChatBgKeys.chatBgColorDark else ChatBgKeys.chatBgColorLight)
                var colors = intArrayOf()
                if (!TextUtils.isEmpty(colorStr)) {
                    val colorArr = colorStr.split(",")
                    colors = intArrayOf(
                        Color.parseColor("#" + colorArr[0]),
                        Color.parseColor("#" + colorArr[1]),
                        Color.parseColor("#" + colorArr[2]),
                        Color.parseColor("#" + colorArr[3])
                    )
//                    colors[0] = Color.parseColor("#" + colorArr[0])
//                    colors[1] = Color.parseColor("#" + colorArr[1])
//                    colors[2] = Color.parseColor("#" + colorArr[2])
//                    colors[3] = Color.parseColor("#" + colorArr[3])
                }
//                if (isDark){
//                    colors = intArrayOf(
//                        Color.parseColor("#252D3A"),
//                        Color.parseColor("#252D3A"),
//                        Color.parseColor("#252D3A"),
//                        Color.parseColor("#252D3A")
//                    )
//                }
//                val colorIndex = WKSharedPreferencesUtil.getInstance()
//                    .getInt(userInfoEntity.uid + "_" + WKChannelCustomerExtras.chatBgColorIndex)
                val gradientAngle = WKSharedPreferencesUtil.getInstance().getIntWithUID(
                    ChatBgKeys.chatBgGradientAngle
                )
                val showPattern = WKSharedPreferencesUtil.getInstance()
                    .getIntWithUID(ChatBgKeys.chatBgShowPattern)
                //userInfoEntity.chat_bg_show_pattern
                val path = WKConstants.chatBgCacheDir + overallURL.replace("/", "_")
                val file = File(path)
                if (file.exists()) {
                    if (isSvg == 1)
                        setSvgBG(
                            colors,
                            path,
                            showPattern,
                            gradientAngle,
                            menu.rootLayout,
                            menu.backGroundIV,
                            isDark
                        )
                    else {
                        val isBlurred = WKSharedPreferencesUtil.getInstance()
                            .getIntWithUID(ChatBgKeys.chatBgIsBlurred)
                        menu.blurView.visibility =
                            if (isBlurred == 1) View.VISIBLE else View.GONE
                        GlideUtils.getInstance()
                            .showImg(menu.backGroundIV.context, path, menu.backGroundIV)
                    }
                } else {
                    // 下载文件
                    downloadBG(menu.backGroundIV.context, overallURL, path) {
                        if (isSvg == 1)
                            setSvgBG(
                                colors,
                                path,
                                showPattern,
                                gradientAngle,
                                menu.rootLayout,
                                menu.backGroundIV,
                                isDark
                            )
                        else {
                            val isBlurred = WKSharedPreferencesUtil.getInstance()
                                .getIntWithUID(ChatBgKeys.chatBgIsBlurred)
                            menu.blurView.visibility =
                                if (isBlurred == 1) View.VISIBLE else View.GONE
                            GlideUtils.getInstance()
                                .showImg(menu.backGroundIV.context, path, menu.backGroundIV)
                        }
                    }
                }
            } else {
                // 设置系统默认背景
                if (isDark) {
                    menu.backGroundIV.setBackgroundResource(R.mipmap.ic_chat_bg_dark)
                } else {
                    menu.backGroundIV.setBackgroundResource(R.mipmap.ic_chat_bg)
                }
            }
//            else {
//                val drawable = GradientDrawable(
//                    Theme.getGradientOrientation(0),
//                    Theme.defaultColorsDark[0]
//                )
//                menu.rootLayout.background = drawable
//                val patternColor = AndroidUtilities.getPatternColor(
//                    Theme.defaultColorsDark[0][0],
//                    Theme.defaultColorsDark[0][1],
//                    Theme.defaultColorsDark[0][2],
//                    Theme.defaultColorsDark[0][3]
//                )
//                val svgBitmap = SvgHelper.getBitmap(
//                    R.raw.def,
//                    AndroidUtilities.getScreenWidth(),
//                    AndroidUtilities.getScreenHeight(),
//                    patternColor
//                )
//                menu.backGroundIV.setImageBitmap(svgBitmap)
//            }
        } else {
            var colors = intArrayOf()
            var isSvg = 0
//            var colorIndex = 0
            var gradientAngle = 0
            var showPattern = 0
            var isBlurred = 0
            var isDeleted = 0
            val isDeletedObject = channel.localExtra[ChatBgKeys.chatBgIsDeleted]
            val isSvgObject = channel.localExtra[ChatBgKeys.chatBgIsSvg]
//            val colorIndexObject = channel.localExtra[WKChannelCustomerExtras.chatBgColorIndex]
            val gradientAngleObject = channel.localExtra[ChatBgKeys.chatBgIsSvg]
            val showPatternObject = channel.localExtra[ChatBgKeys.chatBgShowPattern]
            val isBlurredObject = channel.localExtra[ChatBgKeys.chatBgIsBlurred]
            val colorObject =
                channel.localExtra[if (isDark) ChatBgKeys.chatBgColorDark else ChatBgKeys.chatBgColorLight]
            if (colorObject != null) {
                val colorsStr = colorObject as String
                if (!TextUtils.isEmpty(colorsStr)) {
                    val colorArr = colorsStr.split(",")
                    colors = intArrayOf(
                        Color.parseColor("#" + colorArr[0]),
                        Color.parseColor("#" + colorArr[1]),
                        Color.parseColor("#" + colorArr[2]),
                        Color.parseColor("#" + colorArr[3])
                    )
                }
            }
            if (isDeletedObject != null) isDeleted = isDeletedObject as Int
            if (isDeleted == 1) return
            if (isSvgObject != null) isSvg = isSvgObject as Int
//            if (colorIndexObject != null) colorIndex = colorIndexObject as Int
            if (gradientAngleObject != null) gradientAngle = gradientAngleObject as Int
            if (showPatternObject != null) showPattern = showPatternObject as Int
            if (isBlurredObject != null) isBlurred = isBlurredObject as Int
            val path = WKConstants.chatBgCacheDir + url.replace("/", "_")
            val file = File(path)
            if (file.exists()) {
                if (isSvg == 1)
                    setSvgBG(
                        colors,
                        path,
                        showPattern,
                        gradientAngle,
                        menu.rootLayout,
                        menu.backGroundIV,
                        isDark
                    )
                else {
                    menu.blurView.visibility =
                        if (isBlurred == 1) View.VISIBLE else View.GONE
                    GlideUtils.getInstance()
                        .showImg(menu.backGroundIV.context, path, menu.backGroundIV)
                }
            } else {
                // 下载文件
                downloadBG(menu.backGroundIV.context, url, path) {
                    if (isSvg == 1)
                        setSvgBG(
                            colors,
                            path,
                            showPattern,
                            gradientAngle,
                            menu.rootLayout,
                            menu.backGroundIV,
                            isDark
                        )
                    else {
                        menu.blurView.visibility =
                            if (isBlurred == 1) View.VISIBLE else View.GONE
                        GlideUtils.getInstance()
                            .showImg(menu.backGroundIV.context, path, menu.backGroundIV)
                    }
                }
            }
        }
    }

    private fun downloadBG(
        context: Context,
        url: String,
        savePath: String,
        result: () -> Unit
    ) {
        Glide.with(context)
            .downloadOnly()
            .load(WKApiConfig.getShowUrl(url))
            .listener(object : RequestListener<File?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<File?>,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: File,
                    model: Any,
                    target: Target<File?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    if (resource.exists()) {
                        WKFileUtils.getInstance()
                            .fileCopy(resource.absolutePath, savePath)
                        result()
                    }
                    return false
                }
            })
            .preload()
    }

    private fun setSvgBG(
        colors: IntArray,
        path: String,
        showPattern: Int,
        gradientAngle: Int,
        rootLayout: View,
        imageView: ImageView,
        isDark: Boolean
    ) {
        if (isDark) {
            val index = abs(path.hashCode()) % Theme.defaultColorsDark.size
            val drawable = GradientDrawable(
                Theme.getGradientOrientation(gradientAngle),
                Theme.defaultColorsDark[index]
            )
            rootLayout.background = drawable
        } else {
            if (colors.isNotEmpty()) {
                val drawable = GradientDrawable(
                    Theme.getGradientOrientation(gradientAngle),
                    colors
                )
                rootLayout.background = drawable
            }
        }
//        val colors1 = intArrayOf(
//            Color.parseColor("#5f4167"),
//            Color.parseColor("#5f4167"),
//            Color.parseColor("#252D3A"),
//            Color.parseColor("#252D3A")
//        )
////        if (colors.isNotEmpty()) {
//            val drawable = GradientDrawable(
//                Theme.getGradientOrientation(gradientAngle),
//                colors1
//            )
//            rootLayout.background = drawable
////        }

        if (showPattern == 1 && colors.isNotEmpty() && colors.size == 4) {
            val patternColor = AndroidUtilities.getPatternColor(
                colors[0],
                colors[1],
                colors[2],
                colors[3],
            )
            val svgBitmap = SvgHelper.getBitmap(
                File(path),
                AndroidUtilities.getScreenWidth(),
                AndroidUtilities.getScreenHeight(),
                patternColor
            )

            imageView.setImageBitmap(svgBitmap)
        } else {
            imageView.setImageBitmap(null)
        }

    }


    /**
     * 监听
     */
    private fun startScreenShotListen(iConversationContext: IConversationContext) {
        screenShotListenManager =
            ScreenShotListenManager.newInstance(iConversationContext.chatActivity)
        if (screenShotListenManager != null) {
            screenShotListenManager!!.setListener {
                if (!iConversationContext.isShowChatActivity)
                    return@setListener
                val channel = iConversationContext.chatChannelInfo
                var screenshot = 0
                if (channel?.remoteExtraMap != null && channel.remoteExtraMap.containsKey(
                        WKChannelExtras.screenshot
                    )
                ) {
                    val `object` =
                        channel.remoteExtraMap[WKChannelExtras.screenshot]
                    if (`object` != null) screenshot = `object` as Int
                }
                if (screenshot == 1) {
                    val screenshotContent = ScreenshotContent()
                    screenshotContent.fromuid = WKConfig.getInstance().uid
                    screenshotContent.fromname = WKConfig.getInstance().userName
                    iConversationContext.sendMessage(screenshotContent)
                }
            }
            screenShotListenManager!!.startListen()
        }

    }


    private fun stopScreenShotListen() {
        if (screenShotListenManager != null) {
            screenShotListenManager!!.stopListen()
            screenShotListenManager = null
        }
    }

    private fun getMsgItemRemindView(chatSettingCellMenu: ChatSettingCellMenu): View {
        val context = chatSettingCellMenu.parentLayout.context
        val channelID = chatSettingCellMenu.channelID
        val channelType = chatSettingCellMenu.channelType
        val view = LayoutInflater.from(context)
            .inflate(R.layout.msg_item_remind_layout, chatSettingCellMenu.parentLayout, false)
        val remindLayout = view.findViewById<View>(R.id.remindLayout)
        SingleClickUtil.onSingleClick(remindLayout) {
            val intent = Intent(context, MsgRemindActivity::class.java)
            intent.putExtra("channelID", channelID)
            intent.putExtra("channelType", channelType)
            context.startActivity(intent)
        }
        val chatBgLayout = view.findViewById<View>(R.id.chatBgLayout)
        chatBgLayout.visibility = View.VISIBLE
        SingleClickUtil.onSingleClick(chatBgLayout) {
            val intent = Intent(context, ChatBgListActivity::class.java)
            intent.putExtra("channelID", channelID)
            intent.putExtra("channelType", channelType)
            context.startActivity(intent)
        }
        return view
    }

    private fun getFindMsgView(chatSettingCellMenu: ChatSettingCellMenu): View {
        val context = chatSettingCellMenu.parentLayout.context
        val channelID = chatSettingCellMenu.channelID
        val channelType = chatSettingCellMenu.channelType
        val view = LayoutInflater.from(context)
            .inflate(R.layout.msg_item_remind_layout, chatSettingCellMenu.parentLayout, false)
        val textView = view.findViewById<TextView>(R.id.titleCenterTv)
        textView.setText(R.string.str_find_chat_content)
        SingleClickUtil.onSingleClick(view) {
            val intent = Intent(context, RecordActivity::class.java)
            intent.putExtra("channel_id", channelID)
            intent.putExtra("channel_type", channelType)
            context.startActivity(intent)
        }
        return view
    }

    private fun getMsgReceiptView(chatSettingCellMenu: ChatSettingCellMenu): View {
        val context = chatSettingCellMenu.parentLayout.context
        val channelID = chatSettingCellMenu.channelID
        val channelType = chatSettingCellMenu.channelType
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_msg_receipt_layout, chatSettingCellMenu.parentLayout, false)
        val switchView = view.findViewById<SwitchView>(R.id.receiptSwitchView)
//        var seekBarView: SeekBarView? = null

        val channel = WKIM.getInstance().channelManager.getChannel(
            channelID,
            channelType
        )
        if (channel != null) {
            switchView.isChecked = channel.receipt == 1
        }
        switchView.setOnCheckedChangeListener { buttonView, isChecked ->
            run {
                if (buttonView.isPressed) {
                    if (channelType == WKChannelType.PERSONAL) {
                        AdvancedModel.instance
                            .updateUserSetting(
                                channelID, "receipt", if (isChecked) 1 else 0
                            ) { code: Int, msg: String? ->
                                if (code != HttpResponseCode.success.toInt()) {
                                    switchView.isChecked = !isChecked
                                    WKToastUtils.getInstance().showToast(msg)
                                }
                            }
                    }
                } else {
                    AdvancedModel.instance.updateGroupSetting(
                        channelID,
                        "receipt",
                        if (isChecked) 1 else 0
                    ) { code: Int, msg: String? ->
                        if (code != HttpResponseCode.success.toInt()) {
                            switchView.isChecked = !isChecked
                            WKToastUtils.getInstance().showToast(msg)
                        }
                    }
                }
            }
        }
        return view
    }

    fun editImg(path: String, context: Context) {
        if (path.startsWith("http") || path.startsWith("HTTP")) {
            Glide.with(context)
                .asBitmap()
                .load(path)
                .into(object : CustomTarget<Bitmap?>(SIZE_ORIGINAL, SIZE_ORIGINAL) {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {
                        ImageUtils.getInstance().saveBitmap(
                            context, resource, true
                        ) { path -> gotEdit(context, path) }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        } else {
            gotEdit(context, path)
        }
    }

    private fun gotEdit(context: Context, path: String) {
        EndpointManager.getInstance().invoke("edit_img", EditImgMenu(
            context, true, path, null, -1
        ) { _: Bitmap?, _: String? -> })
    }

    private val reactionStickers: ArrayList<ReactionSticker> = ArrayList()
    private fun initReactionSticker() {
        reactionStickers.add(ReactionSticker("like", R.raw.like_small))
        reactionStickers.add(ReactionSticker("bad", R.raw.bad_small))
        reactionStickers.add(ReactionSticker("love", R.raw.love_small))
        reactionStickers.add(ReactionSticker("fire", R.raw.fire_small))
        reactionStickers.add(ReactionSticker("celebrate", R.raw.celebrate_small))
        reactionStickers.add(ReactionSticker("happy", R.raw.happy_small))
        reactionStickers.add(ReactionSticker("haha", R.raw.haha_small))
        reactionStickers.add(ReactionSticker("terrified", R.raw.terrified_small))
        reactionStickers.add(ReactionSticker("depressed", R.raw.depressed_small))
        reactionStickers.add(ReactionSticker("shit", R.raw.shit_small))
        reactionStickers.add(ReactionSticker("vomit", R.raw.vomit_small))
    }

    private fun getChatBgView(
        menu: ChatBgItemMenu
    ) {
        val view =
            LayoutInflater.from(menu.activity)
                .inflate(R.layout.chat_bg_layout, menu.parentView, false)
        val chatBgLayout = view.findViewById<View>(R.id.chatBgLayout)
        SingleClickUtil.onSingleClick(chatBgLayout) {
            val intent = Intent(menu.activity, ChatBgListActivity::class.java)
            intent.putExtra("channelID", menu.channelID)
            intent.putExtra("channelType", menu.channelType)
            menu.activity.startActivity(intent)
        }
        menu.parentView.removeAllViews()
        menu.parentView.addView(view)
    }

    private fun getOtherLoginView(context: Context, parentView: ViewGroup): View {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.other_login_type_layout, parentView, false)
        val wxLoginLayout =
            view.findViewById<View>(R.id.wxLoginLayout)
        SingleClickUtil.onSingleClick(wxLoginLayout) {
        }
        view.findViewById<View>(R.id.phoneLoginLayout).setOnClickListener {
        }
        parentView.removeAllViews()
        parentView.addView(view)
        return view
    }

}