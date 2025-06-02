package com.chat.video.search.remote

import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chat.base.act.PlayVideoActivity
import com.chat.base.base.WKBaseActivity
import com.chat.base.config.WKApiConfig
import com.chat.base.endpoint.EndpointManager
import com.chat.base.endpoint.EndpointSID
import com.chat.base.endpoint.entity.ChatViewMenu
import com.chat.base.entity.GlobalSearchReq
import com.chat.base.msgitem.WKContentType
import com.chat.base.net.HttpResponseCode
import com.chat.base.search.GlobalSearchModel
import com.chat.base.ui.Theme
import com.chat.base.utils.AndroidUtilities
import com.chat.base.utils.WKReader
import com.chat.base.utils.WKTimeUtils
import com.chat.base.utils.singleclick.SingleClickUtil.determineTriggerSingleClick
import com.chat.base.views.FullyGridLayoutManager
import com.chat.base.views.pinnedsectionitemdecoration.PinnedHeaderItemDecoration
import com.chat.video.R
import com.chat.video.databinding.ActSearchChatVideoLayoutBinding
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.xinbida.wukongim.WKIM
import com.xinbida.wukongim.entity.WKChannelType
import com.xinbida.wukongim.msgmodel.WKVideoContent

class SearchActivity : WKBaseActivity<ActSearchChatVideoLayoutBinding>() {
    private var channelID: String? = null
    private var channelType: Byte = 0
    lateinit var adapter: SearchAdapter
    private var page = 1;
    override fun getViewBinding(): ActSearchChatVideoLayoutBinding {
        return ActSearchChatVideoLayoutBinding.inflate(layoutInflater)
    }

    override fun setTitle(titleTv: TextView) {
        titleTv.setText(R.string.wk_video)
    }

    override fun initPresenter() {
        channelID = intent.getStringExtra("channel_id")
        channelType = intent.getByteExtra("channel_type", WKChannelType.PERSONAL)
    }

    override fun initView() {
        wkVBinding.spinKit.setColor(Theme.colorAccount)
        val mHeaderItemDecoration =
            PinnedHeaderItemDecoration.Builder(1).enableDivider(false).create()
        val wH = (AndroidUtilities.getScreenWidth() - AndroidUtilities.dp(6f)) / 4
        val layoutManager = FullyGridLayoutManager(this, 4)
        wkVBinding.recyclerView.layoutManager = layoutManager
        adapter = SearchAdapter(wH) { item ->
            val orderSeq = WKIM.getInstance().msgManager.getMessageOrderSeq(
                item.message.message_seq,
                channelID, channelType
            )
            EndpointManager.getInstance().invoke(
                EndpointSID.chatView,
                ChatViewMenu(
                    this@SearchActivity,
                    channelID,
                    channelType,
                    orderSeq,
                    false
                )
            )
        }
        wkVBinding.recyclerView.adapter = adapter
        wkVBinding.recyclerView.addItemDecoration(mHeaderItemDecoration)
    }

    override fun initListener() {
        wkVBinding.refreshLayout.setEnableRefresh(false)
        wkVBinding.refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                page++
                getData()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
            }
        })
        adapter.addChildClickViewIds(R.id.imageView)
        adapter.setOnItemChildClickListener { adapter1: BaseQuickAdapter<*, *>, view1: View, position: Int ->
            determineTriggerSingleClick(
                view1
            ) { _: View? ->
                val entity = adapter1.data[position] as SearchEntity?
                if (entity != null && entity.itemType == 0) {
                    val activityOptions =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this@SearchActivity,
                            Pair(
                                view1,
                                "coverIv"
                            )
                        )
                    val intent = Intent(
                        this,
                        PlayVideoActivity::class.java
                    )
                    intent.putExtra("coverImg", WKApiConfig.getShowUrl(entity.videoModel.cover))
                    intent.putExtra("url", WKApiConfig.getShowUrl(entity.videoModel.url))
                    startActivity(intent, activityOptions.toBundle())
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
            }
        }
        getData()
    }

    private fun getData() {
        val contentType = ArrayList<Int>()
        contentType.add(WKContentType.WK_VIDEO)
        val req = GlobalSearchReq(
            1, "",
            channelID!!, channelType, "", "", contentType, page, 20, 0, 0
        )
        GlobalSearchModel.search(req) { code, msg, resp ->
            wkVBinding.refreshLayout.finishRefresh()
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


            val list = ArrayList<SearchEntity>()
            for (message in resp.messages) {
                var videoModel: WKVideoContent? = null
                val model = message.getMessageModel() ?: continue
                if (model is WKVideoContent) {
                    videoModel = model
                }
                if (videoModel == null) {
                    continue
                }
                val date = WKTimeUtils.getInstance().time2YearMonth(message.timestamp * 1000)
                if (WKReader.isNotEmpty(list)) {
                    if (list[list.size - 1].date != date) {
                        val entity = SearchEntity(1)
                        entity.date = date
                        list.add(entity)
                    }
                } else {
                    val entity = SearchEntity(1)
                    entity.date = date
                    list.add(entity)
                }
                val entity = SearchEntity(0)
                entity.message = message
                entity.videoModel = videoModel
                entity.date = date
                if (videoModel.second > 0) {
                    //分
                    val minute: Int = (videoModel.second / (60)).toInt()
                    //秒
                    val second: Int = (videoModel.second % 60).toInt()
                    val showMinute = if (minute < 10) ("0$minute") else minute.toString() + ""
                    val showSecond = if (second < 10) ("0$second") else second.toString() + ""
                    entity.second = String.format("%s:%s", showMinute, showSecond)
                }
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