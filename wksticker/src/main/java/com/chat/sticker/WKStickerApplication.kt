package com.chat.sticker

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.chat.base.WKBaseApplication
import com.chat.base.config.WKConfig
import com.chat.base.config.WKConstants
import com.chat.base.config.WKSharedPreferencesUtil
import com.chat.base.endpoint.EndpointCategory
import com.chat.base.endpoint.EndpointHandler
import com.chat.base.endpoint.EndpointManager
import com.chat.base.endpoint.EndpointSID
import com.chat.base.endpoint.entity.ChatChooseContacts
import com.chat.base.endpoint.entity.ChatItemPopupMenu
import com.chat.base.endpoint.entity.ChatToolBarMenu
import com.chat.base.endpoint.entity.ChooseChatMenu
import com.chat.base.endpoint.entity.DBMenu
import com.chat.base.endpoint.entity.InitInputPanelMenu
import com.chat.base.endpoint.entity.MsgConfig
import com.chat.base.endpoint.entity.SearchChatEditStickerMenu
import com.chat.base.endpoint.entity.SendTextMenu
import com.chat.base.endpoint.entity.StickerViewMenu
import com.chat.base.msg.IConversationContext
import com.chat.base.msg.model.WKGifContent
import com.chat.base.msgitem.WKContentType
import com.chat.base.msgitem.WKMsgItemViewManager
import com.chat.base.net.HttpResponseCode
import com.chat.base.utils.AndroidUtilities
import com.chat.base.utils.DispatchQueuePool
import com.chat.base.utils.WKFileUtils
import com.chat.base.utils.WKToastUtils
import com.chat.base.views.CommonAnim
import com.chat.base.views.FullyGridLayoutManager
import com.chat.base.views.NoEventRecycleView
import com.chat.sticker.adapter.SearchStickerAdapter
import com.chat.sticker.db.StickerDBManager
import com.chat.sticker.entity.Sticker
import com.chat.sticker.entity.StickerCategory
import com.chat.sticker.entity.StickerDetail
import com.chat.sticker.msg.EmojiContent
import com.chat.sticker.msg.EmojiProvider
import com.chat.sticker.msg.GifProvider
import com.chat.sticker.msg.StickerContent
import com.chat.sticker.msg.StickerFormat
import com.chat.sticker.msg.StickerProvider
import com.chat.sticker.service.StickerModel
import com.chat.sticker.ui.StickerStoreDetailActivity
import com.chat.sticker.ui.components.EmojiView
import com.google.android.material.snackbar.Snackbar
import com.xinbida.wukongim.WKIM
import com.xinbida.wukongim.entity.WKChannel
import com.xinbida.wukongim.entity.WKMsg
import com.xinbida.wukongim.entity.WKMsgSetting
import com.xinbida.wukongim.entity.WKSendOptions
import com.xinbida.wukongim.msgmodel.WKMessageContent
import java.io.File
import java.lang.ref.WeakReference
import java.util.Objects

/**
 * 12/30/20 2:50 PM
 * 表情商店
 */
class WKStickerApplication {

    private object SingletonInstance {
        val INSTANCE = WKStickerApplication()
    }

    companion object {
        val instance: WKStickerApplication
            get() = SingletonInstance.INSTANCE
    }

    var dispatchQueuePool = DispatchQueuePool(3)
    val stickerGridSize = (AndroidUtilities.getScreenWidth() - AndroidUtilities.dp(50f)) / 5
    var stickerDirPath = ""
    private var initPanelMenu: InitInputPanelMenu? = null

    private var searchStickerRecyclerView: NoEventRecycleView? = null
    private var adapter: SearchStickerAdapter? = null
    private var page = 1
    private var searchStickerMenu: SearchChatEditStickerMenu? = null
    fun init() {
        //初始化数据库信息
        EndpointManager.getInstance().setMethod(
            "stickers",
            EndpointCategory.wkDBMenus
        ) { DBMenu("stickers_sql") }
        //登录后重新获取表情分类
        EndpointManager.getInstance().setMethod("", EndpointCategory.loginMenus) {
            WKSharedPreferencesUtil.getInstance()
                .putBooleanWithUID("isRefreshStickerCategory", true)
            WKSharedPreferencesUtil.getInstance()
                .putBooleanWithUID("isRefreshCustomerStickerCategory", true)
            fetchSticker()
//            initStickerGridData();
            getEmoji()
            null
        }

        val appModule = WKBaseApplication.getInstance().getAppModuleWithSid("sticker")
        if (!WKBaseApplication.getInstance().appModuleIsInjection(appModule)) return

        val stickerDir = Objects.requireNonNull<File>(
            WKBaseApplication.getInstance().context.getExternalFilesDir("stickerCache")
        ).absolutePath + "/"
        WKFileUtils.getInstance().createFileDir(stickerDir)
        stickerDirPath =
            Objects.requireNonNull<File>(
                WKBaseApplication.getInstance().context.getExternalFilesDir(
                    "wkStickers"
                )
            ).absolutePath + "/"
        WKFileUtils.getInstance().createFileDir(stickerDirPath)
        // 清空缓存
        Thread { WKFileUtils.delFileOrFolder(File(stickerDir)) }.start()

        //注册消息item
        WKIM.getInstance().msgManager.registerContentMsg(WKGifContent::class.java)
        WKIM.getInstance().msgManager.registerContentMsg(EmojiContent::class.java)
        WKIM.getInstance().msgManager.registerContentMsg(StickerContent::class.java)
        // 注册长按类型
        EndpointManager.getInstance()
            .setMethod(EndpointCategory.msgConfig + WKContentType.WK_VECTOR_STICKER) {
                MsgConfig(
                    true
                )
            }
        EndpointManager.getInstance()
            .setMethod(EndpointCategory.msgConfig + WKContentType.WK_EMOJI_STICKER) { MsgConfig(true) }
        EndpointManager.getInstance()
            .setMethod(EndpointCategory.msgConfig + WKContentType.WK_GIF) { MsgConfig(true) }
        // 注册消息item
        WKMsgItemViewManager.getInstance()
            .addChatItemViewProvider(WKContentType.WK_GIF, GifProvider())
        WKMsgItemViewManager.getInstance()
            .addChatItemViewProvider(
                WKContentType.WK_EMOJI_STICKER,
                EmojiProvider()
            )
        WKMsgItemViewManager.getInstance()
            .addChatItemViewProvider(
                WKContentType.WK_VECTOR_STICKER,
                StickerProvider()
            )

        EndpointManager.getInstance().setMethod(
            "is_register_sticker"
        ) { true }
        EndpointManager.getInstance()
            .setMethod("", EndpointCategory.wkChatPopupItem, 29, object : EndpointHandler {
                override fun invoke(`object`: Any?): Any? {
                    val mMsg = `object` as WKMsg
                    if (mMsg.type == WKContentType.WK_VECTOR_STICKER) {
                        return ChatItemPopupMenu(
                            R.mipmap.msg_sticker,
                            WKBaseApplication.getInstance().context.getString(R.string.show_collections),
                            object : ChatItemPopupMenu.IPopupItemClick {
                                override fun onClick(
                                    mMsg: WKMsg,
                                    iConversationContext: IConversationContext
                                ) {
                                    val vectorStickerContent =
                                        mMsg.baseContentMsgModel as StickerContent
                                    val intent =
                                        Intent(
                                            iConversationContext.chatActivity,
                                            StickerStoreDetailActivity::class.java
                                        )
                                    intent.putExtra("category", vectorStickerContent.category)
                                    iConversationContext.chatActivity.startActivity(intent)
                                }
                            })
                    }
                    return null
                }
            })
        EndpointManager.getInstance()
            .setMethod("", EndpointCategory.wkChatPopupItem, 30, object : EndpointHandler {
                override fun invoke(`object`: Any?): Any? {
                    val msg = `object` as WKMsg
                    when (msg.type) {
                        WKContentType.WK_VECTOR_STICKER, WKContentType.WK_GIF -> {
                            val path: String = if (msg.type == WKContentType.WK_GIF) {
                                val gifModel =
                                    msg.baseContentMsgModel as WKGifContent
                                gifModel.url
                            } else {
                                val stickerModel =
                                    msg.baseContentMsgModel as StickerContent
                                stickerModel.url
                            }
                            val sticker = StickerDBManager.instance.getCustomerWithPath(path)
                            var resourceID = R.mipmap.msg_fave
                            var isAdd = true
                            var text =
                                WKBaseApplication.getInstance().context.getString(R.string.str_sticker_add)
                            if (sticker != null && !TextUtils.isEmpty(sticker.path) && sticker.path == path) {
                                resourceID = R.mipmap.msg_unfave
                                text =
                                    WKBaseApplication.getInstance().context.getString(R.string.delete_from_favorites)
                                isAdd = false
                            }
                            return ChatItemPopupMenu(
                                resourceID,
                                text,
                                object : ChatItemPopupMenu.IPopupItemClick {
                                    override fun onClick(
                                        mMsg: WKMsg,
                                        iConversationContext: IConversationContext
                                    ) {

                                        if (isAdd) {
                                            var url = ""
                                            var format = ""
                                            var placeholder = ""
                                            var category = ""
                                            var width = 0
                                            var height = 0
                                            if (mMsg.type == WKContentType.WK_GIF) {
                                                val gifModel =
                                                    mMsg.baseContentMsgModel as WKGifContent
                                                url = gifModel.url
                                                width = gifModel.width
                                                height = gifModel.height
                                            } else if (mMsg.type == WKContentType.WK_VECTOR_STICKER) {
                                                val stickerModel =
                                                    mMsg.baseContentMsgModel as StickerContent
                                                url = stickerModel.url
                                                format = StickerFormat.lim
                                                placeholder = stickerModel.placeholder
                                                category = stickerModel.category
                                                width = 200
                                                height = 200
                                            }
                                            StickerModel().addSticker(
                                                url,
                                                width,
                                                height,
                                                format,
                                                placeholder,
                                                category
                                            ) { code1, msg1 ->
                                                if (code1 == HttpResponseCode.success.toInt()) {
                                                    EndpointManager.getInstance()
                                                        .invoke(
                                                            "refresh_custom_sticker",
                                                            null
                                                        )
                                                    val viewGroup =
                                                        (iConversationContext.chatActivity as Activity).findViewById<View>(
                                                            android.R.id.content
                                                        ).rootView as ViewGroup
                                                    Snackbar.make(
                                                        viewGroup,
                                                        iConversationContext.chatActivity.getString(
                                                            R.string.str_sticker_added
                                                        ),
                                                        2000
                                                    )
                                                        .show()
                                                } else WKToastUtils.getInstance()
                                                    .showToastNormal(msg1)
                                            }
                                        } else {
                                            val paths = ArrayList<String>()
                                            paths.add(path)
                                            StickerModel().deleteSticker(
                                                paths
                                            ) { code, msg ->
                                                if (code == HttpResponseCode.success.toInt()) {
                                                    EndpointManager.getInstance()
                                                        .invoke(
                                                            "refresh_custom_sticker",
                                                            null
                                                        )
                                                    val viewGroup =
                                                        (iConversationContext.chatActivity as Activity).findViewById<View>(
                                                            android.R.id.content
                                                        ).rootView as ViewGroup
                                                    Snackbar.make(
                                                        viewGroup,
                                                        iConversationContext.chatActivity.getString(
                                                            R.string.deleted_from_favorites
                                                        ),
                                                        2000
                                                    )
                                                        .show()
                                                } else {
                                                    WKToastUtils.getInstance().showToast(msg)
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        WKContentType.WK_EMOJI_STICKER -> {
                            return ChatItemPopupMenu(
                                R.mipmap.msg_copy,
                                WKBaseApplication.getInstance().context.getString(R.string.copy),
                                object : ChatItemPopupMenu.IPopupItemClick {
                                    override fun onClick(
                                        mMsg: WKMsg,
                                        iConversationContext: IConversationContext
                                    ) {
                                        val content = mMsg.baseContentMsgModel.getDisplayContent()
                                        val str: ClipData = ClipData.newPlainText("Label", content)
                                        val cm: ClipboardManager =
                                            iConversationContext.chatActivity.getSystemService(
                                                Context.CLIPBOARD_SERVICE
                                            ) as ClipboardManager
                                        cm.setPrimaryClip(str)
                                        WKToastUtils.getInstance()
                                            .showToastNormal(
                                                iConversationContext.chatActivity.getString(
                                                    R.string.copyed
                                                )
                                            )
                                    }
                                })
                        }

                        else -> {
                            return null
                        }
                    }
                }

            })
        addListener()
    }

    private fun addListener() {

        fetchSticker()
//        initStickerGridData();


        // 获取表情View
        EndpointManager.getInstance().setMethod("get_sticker_view", object : EndpointHandler {
            override fun invoke(`object`: Any?): View? {
                if (`object` != null) {
                    val stickerView: StickerViewMenu = `object` as StickerViewMenu
                    return getStickerView(stickerView.conversationContext)
                }
                return null
            }

        })
        // 注册聊天工具栏
        EndpointManager.getInstance().setMethod(
            EndpointCategory.wkChatToolBar + "_sticker",
            EndpointCategory.wkChatToolBar,
            100
        ) { `object` ->
            val conversationContext = `object` as IConversationContext
            //  iConversationContext = conversationContext
            contextWeakRefresh = WeakReference(conversationContext)
            val view = getStickerView(conversationContext)
            ChatToolBarMenu(
                "chat_toolbar_sticker",
                R.mipmap.icon_chat_toolbar_sticker,
                R.mipmap.icon_chat_toolbar_sticker,
                view
            ) { _, _ ->
            }
        }

        EndpointManager.getInstance().setMethod(
            "text_to_emoji_sticker"
        ) { `object` ->
            val sendTextMenu = `object` as SendTextMenu
            val sticker: Sticker =
                StickerDBManager.instance.getEmojiSticker(sendTextMenu.text)
            if (!TextUtils.isEmpty(sticker.path)) {
                val emojiStickerContent =
                    EmojiContent()
                emojiStickerContent.placeholder = sticker.placeholder
                emojiStickerContent.url = sticker.path
                emojiStickerContent.content = sticker.searchable_word
                sendTextMenu.iConversationContext.sendMessage(emojiStickerContent)
                true
            } else {
                false
            }
        }
        EndpointManager.getInstance().setMethod("initInputPanel") { `object` ->
            initPanelMenu = `object` as InitInputPanelMenu
            initSearchStickerView(
                initPanelMenu!!.bottomView,
                initPanelMenu!!.frameLayout,
                initPanelMenu!!.iConversationContext.get()!!
            )
        }
        EndpointManager.getInstance().setMethod("hide_search_chat_edit_view") {
            if (searchStickerRecyclerView?.visibility == View.VISIBLE) {
                CommonAnim.getInstance().hideTop2Bottom(searchStickerRecyclerView)
            }
        }

        EndpointManager.getInstance().setMethod("search_chat_edit_content") { `object` ->
            this.searchStickerMenu = `object` as SearchChatEditStickerMenu
            searchStickerRecyclerView?.scrollToPosition(0)

            if (!TextUtils.isEmpty(searchStickerMenu!!.content)) {
                StickerModel().search(
                    searchStickerMenu!!.content,
                    1,
                    object : StickerModel.ISearchListener {
                        override fun onResult(code: Int, msg: String, list: List<Sticker>) {
                            if (list.isNotEmpty()) {
                                adapter!!.setList(ArrayList())
                                if (page == 1) {
                                    adapter!!.setList(list)
                                } else {
                                    adapter!!.addData(list)
                                }
                                resetData()
                                if (searchStickerRecyclerView?.visibility == GONE) {
                                    CommonAnim.getInstance()
                                        .showBottom2Top(searchStickerRecyclerView)
                                    var h = WKConstants.getKeyboardHeight()
                                    if (h == 0) {
                                        h = AndroidUtilities.dp(350f)
                                    }
                                    adapter!!.headerLayout!!.layoutParams.height =
                                        AndroidUtilities.getScreenHeight() - h
                                    searchStickerRecyclerView?.setHeaderViewY(adapter!!.headerLayout!!.layoutParams.height.toFloat())
                                }
                            }
                        }
                    })
            } else {
                CommonAnim.getInstance().hideTop2Bottom(searchStickerRecyclerView)
            }
        }
    }

    fun sendMsg(message: WKMessageContent) {
        if (contextWeakRefresh != null && contextWeakRefresh!!.get() != null)
            contextWeakRefresh!!.get()!!.sendMessage(message)
    }

    private var contextWeakRefresh: WeakReference<IConversationContext>? = null
    private fun initSearchStickerView(
        bottomView: View,
        parentView: FrameLayout,
        iConversationContext: IConversationContext
    ) {
        searchStickerRecyclerView = NoEventRecycleView(iConversationContext.chatActivity)
        val headerView = View(iConversationContext.chatActivity)
        headerView.setBackgroundColor(
            ContextCompat.getColor(
                iConversationContext.chatActivity,
                R.color.transparent
            )
        )
        searchStickerRecyclerView!!.layoutManager = FullyGridLayoutManager(
            iConversationContext.chatActivity, 5
        )
        searchStickerRecyclerView!!.setView(bottomView, headerView)
        searchStickerRecyclerView!!.addOnScrollListener(searchStickerRecyclerView!!.onScrollListener)
        adapter = SearchStickerAdapter()
        searchStickerRecyclerView!!.adapter = adapter
        adapter!!.addHeaderView(headerView)
        parentView.addView(searchStickerRecyclerView)

        var h = WKConstants.getKeyboardHeight()
        if (h == 0) {
            h = AndroidUtilities.dp(350f)
        }
        adapter!!.headerLayout!!.layoutParams.height =
            AndroidUtilities.getScreenHeight() - h
        searchStickerRecyclerView?.setHeaderViewY(adapter!!.headerLayout!!.layoutParams.height.toFloat())

        searchStickerRecyclerView!!.visibility = GONE
        adapter!!.addChildClickViewIds(R.id.imageView, R.id.stickerView)
        adapter!!.setOnItemClickListener { _, v, position ->
            val searchSticker = adapter!!.data[position]
            if (searchSticker.isNull) return@setOnItemClickListener
            if (v.id == R.id.imageView) {
                val gifContent = WKGifContent()
                gifContent.height = searchSticker.height
                gifContent.width = searchSticker.width
                gifContent.url = searchSticker.path
                gifContent.format = "gif"
                iConversationContext.sendMessage(gifContent)
            } else {
                if (searchSticker.category == "emoji") {
                    val emojiSticker =
                        EmojiContent()
                    emojiSticker.placeholder = searchSticker.placeholder
                    emojiSticker.url = searchSticker.path
                    emojiSticker.content = searchSticker.searchable_word
                    iConversationContext.sendMessage(emojiSticker)
                } else {
                    val vectorSticker =
                        StickerContent()
                    vectorSticker.url = searchSticker.path
                    vectorSticker.placeholder = searchSticker.placeholder
                    vectorSticker.category = searchSticker.category
                    vectorSticker.content = searchSticker.searchable_word
                    iConversationContext.sendMessage(vectorSticker)
                }
            }
            CommonAnim.getInstance().hideTop2Bottom(searchStickerRecyclerView)
            searchStickerMenu!!.iResult.onResult()
        }
    }

    fun resetData() {
        val num = adapter!!.data.size % 5
        if (num != 0) {
            var count = 5 - num
            while (count > 0) {
                val sticker = Sticker()
                sticker.isNull = true
                adapter!!.addData(sticker)
                count--
            }
        }
    }

    private fun getEmoji() {
        StickerModel().fetchStickerWithCategory("emoji",
            object : StickerModel.IStickerDetailListener {
                override fun onResult(
                    code: Int,
                    msg: String,
                    stickerDetail: StickerDetail?
                ) {
                    if (code == HttpResponseCode.success.toInt()
                        && stickerDetail != null && stickerDetail.list.isNotEmpty()
                    ) {
                        Thread {
                            StickerDBManager.instance.addSticker(stickerDetail.list)
                        }.start()

                    }
                }
            })
    }

//    private fun initStickerGridData() {
//        if (!TextUtils.isEmpty(WKConfig.getInstance().token)) {
//            GlobalScope.launch(Dispatchers.IO) {
//                val customList: List<Sticker> =
//                    StickerDBManager.instance.getUserCustomSticker()
//                for (sticker in customList) {
//                    if (!TextUtils.isEmpty(sticker.format) && sticker.format == StickerFormat.lim) {
//
//                    }
//                }
//
//                val categories = StickerDBManager.instance.getUserStickerCategory()
//                for (category in categories) {
//                    val stickers: List<Sticker> =
//                        StickerDBManager.instance.getStickerWithCategory(category.category)
//                    for (sticker in stickers) {
//                        if (!TextUtils.isEmpty(sticker.format) && sticker.format == StickerFormat.lim) {
//
//                        }
//                    }
//
//                }
//            }
//        }
//    }

    private fun fetchSticker() {
        //每次打开都获取一次表情分类直到获取成功
        if (!TextUtils.isEmpty(WKConfig.getInstance().token)) {
            val isRefreshStickerCategory =
                WKSharedPreferencesUtil.getInstance().getBooleanWithUID("isRefreshStickerCategory")
            if (isRefreshStickerCategory) {
                StickerModel().fetchCategoryList(object : StickerModel.IStickerCategoryListener {
                    override fun onResult(code: Int, msg: String, list: List<StickerCategory>) {
                        if (code == HttpResponseCode.success.toInt()) {
                            WKSharedPreferencesUtil.getInstance()
                                .putBoolean("isRefreshStickerCategory", false)
                        }
                    }
                })
            }
        }

        //刷新自定义表情
        if (!TextUtils.isEmpty(WKConfig.getInstance().token)) {
            val isRefreshCustomerStickerCategory =
                WKSharedPreferencesUtil.getInstance()
                    .getBooleanWithUID("isRefreshCustomerStickerCategory")
            if (isRefreshCustomerStickerCategory) {
                StickerModel().fetchUserCustomSticker(object : StickerModel.IStickersListener {
                    override fun onResult(code: Int, msg: String, list: List<Sticker>) {
                        if (code == HttpResponseCode.success.toInt()) {
                            WKSharedPreferencesUtil.getInstance()
                                .putBoolean("isRefreshCustomerStickerCategory", false)
                        }
                    }
                })
            }
        }
    }

    fun getStickerView(conversationContext: IConversationContext): View {
        val parentView = LinearLayout(conversationContext.chatActivity)
        val emojiView = EmojiView(conversationContext.chatActivity)
        parentView.addView(emojiView)
        return parentView
    }


    fun forwardMessage(context: Context, mMsg: WKMsg) {
        var mMessageContent =
            mMsg.baseContentMsgModel
        if (mMsg.remoteExtra != null && mMsg.remoteExtra.contentEditMsgModel != null) {
            mMessageContent = mMsg.remoteExtra.contentEditMsgModel
        }
        val chooseChatMenu =
            ChooseChatMenu(
                ChatChooseContacts { channelList: List<WKChannel>? ->
                    if (!channelList.isNullOrEmpty()) {
                        for (mChannel in channelList) {
                            var msgContent =
                                mMsg.baseContentMsgModel
                            if (mMsg.remoteExtra != null && mMsg.remoteExtra.contentEditMsgModel != null) {
                                msgContent =
                                    mMsg.remoteExtra.contentEditMsgModel
                            }
                            msgContent.mentionAll = 0
                            msgContent.mentionInfo = null
                            val option = WKSendOptions()
                            option.setting.receipt = mChannel.receipt
                            WKIM.getInstance().msgManager.sendWithOptions(
                                msgContent,
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
                mMessageContent
            )
        EndpointManager.getInstance()
            .invoke(EndpointSID.showChooseChatView, chooseChatMenu)

    }
}
