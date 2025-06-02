package com.chat.file.search.remote

import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chat.base.base.WKBaseActivity
import com.chat.base.config.WKConfig
import com.chat.base.entity.GlobalSearchReq
import com.chat.base.msgitem.WKContentType
import com.chat.base.net.HttpResponseCode
import com.chat.base.search.GlobalSearchModel
import com.chat.base.ui.Theme
import com.chat.base.utils.StringUtils
import com.chat.base.utils.WKReader
import com.chat.base.utils.WKTimeUtils
import com.chat.base.utils.singleclick.SingleClickUtil.determineTriggerSingleClick
import com.chat.file.ChatFileActivity
import com.chat.file.R
import com.chat.file.databinding.ActSearchChatFileLayoutBinding
import com.chat.file.msgitem.FileContent
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.xinbida.wukongim.WKIM
import com.xinbida.wukongim.entity.WKChannel
import com.xinbida.wukongim.entity.WKChannelType
import java.util.Locale

class SearchWithFileActivity : WKBaseActivity<ActSearchChatFileLayoutBinding>() {
    private var page = 1
    private var channelID: String? = null
    private var channelType: Byte = 0
    private lateinit var channel: WKChannel
    private lateinit var adapter: SearchGlobalAdapter
    override fun getViewBinding(): ActSearchChatFileLayoutBinding {
        return ActSearchChatFileLayoutBinding.inflate(layoutInflater)
    }

    override fun setTitle(titleTv: TextView) {
        titleTv.setText(R.string.str_file_file)
    }

    override fun initPresenter() {
        channelID = intent.getStringExtra("channel_id")
        channelType = intent.getByteExtra("channel_type", WKChannelType.PERSONAL)
        if (channelType == WKChannelType.PERSONAL) {
            channel = WKIM.getInstance().channelManager.getChannel(channelID, channelType)
        }

    }

    override fun initView() {
        adapter = SearchGlobalAdapter()
        initAdapter(wkVBinding.recyclerView, adapter)
    }

    override fun initListener() {
        wkVBinding.spinKit.setColor(Theme.colorAccount)
        wkVBinding.refreshLayout.setEnableRefresh(false)
        wkVBinding.refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                page++
                getData()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
            }
        })

        adapter.setOnItemClickListener { adapter1: BaseQuickAdapter<*, *>, view1: View?, position: Int ->
            determineTriggerSingleClick(
                view1!!
            ) { _: View? ->
                val entity = adapter1.data[position] as SearchGlobalFileEntity?
                if (entity != null) {
                    val intent = Intent(
                        this@SearchWithFileActivity,
                        ChatFileActivity::class.java
                    )
                    intent.putExtra("clientMsgNo", entity.msg.client_msg_no)
                    startActivity(intent)
                }
            }
        }
        getData()
    }

    private fun getData() {
        val contentType = ArrayList<Int>()
        contentType.add(WKContentType.WK_FILE)
        val req = GlobalSearchReq(
            1, "",
            channelID!!, channelType, "", "", contentType, page, 20, 0, 0
        )
        GlobalSearchModel.search(req) { code, msg, resp ->
            wkVBinding.refreshLayout.finishLoadMore()
            if (code != HttpResponseCode.success) {
                showToast(msg)
                return@search
            }

            if (resp == null || WKReader.isEmpty(resp.messages)) {
                if (page == 1) {
                    wkVBinding.noDataTv.visibility = View.VISIBLE
                } else {
                    wkVBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                }
                return@search
            }
            val uidList = ArrayList<String>()
            var channels: List<WKChannel>? = null
            if (channelType != WKChannelType.PERSONAL) {
                for (message in resp.messages) {
                    uidList.add(message.from_uid)
                }
                channels = WKIM.getInstance().channelManager.getChannels(uidList)
            }

            val list: ArrayList<SearchGlobalFileEntity> = ArrayList()
            for (message in resp.messages) {
                var fileContent: FileContent?
                val content = message.getMessageModel() ?: continue
                fileContent = content as FileContent?
                if (fileContent == null) {
                    continue
                }
                val date = WKTimeUtils.getInstance().time2YearMonth(message.timestamp * 1000)
                val time = WKTimeUtils.getInstance().time2DataDay(message.timestamp * 1000)
                val fileSize = StringUtils.sizeFormatNum2String(fileContent.size)
                val fileType: String = if (fileContent.name.contains(".")) {
                    val type: String =
                        fileContent.name.substring(fileContent.name.lastIndexOf(".") + 1)
                    if (!TextUtils.isEmpty(type)) type.uppercase(Locale.getDefault())
                    else getString(R.string.unknown_file)
                } else getString(R.string.unknown_file)

                var fromName = ""
                val fromUID = message.from_uid
                var fromAvatarCache = ""
                if (WKReader.isNotEmpty(channels)) {
                    for (channel in channels!!) {
                        if (channel.channelID == message.from_uid) {
                            fromName = if (!TextUtils.isEmpty(channel.channelRemark)) {
                                channel.channelRemark
                            } else {
                                channel.channelName
                            }
                            fromAvatarCache = channel.avatarCacheKey
                        }
                    }
                }
                if (TextUtils.isEmpty(fromName)) {
                    fromName = message.from_channel.channel_name
                }
                val entity =
                    SearchGlobalFileEntity(
                        fileContent,
                        message,
                        fileSize,
                        fileType,
                        time,
                        date,
                        fromUID,
                        fromName,
                        fromAvatarCache
                    )
                list.add(entity)
            }

            if (page == 1) {
                adapter.setList(list)
            } else {
                adapter.addData(list)
            }
        }
    }
}