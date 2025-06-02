package com.chat.video.search;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;

import com.chat.base.act.PlayVideoActivity;
import com.chat.base.base.WKBaseActivity;
import com.chat.base.config.WKApiConfig;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.EndpointSID;
import com.chat.base.endpoint.entity.ChatViewMenu;
import com.chat.base.msgitem.WKContentType;
import com.chat.base.ui.Theme;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.WKReader;
import com.chat.base.utils.WKTimeUtils;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.base.views.FullyGridLayoutManager;
import com.chat.base.views.pinnedsectionitemdecoration.PinnedHeaderItemDecoration;
import com.chat.video.R;
import com.chat.video.databinding.ActSearchChatVideoLayoutBinding;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKMsg;
import com.xinbida.wukongim.entity.WKChannelType;
import com.xinbida.wukongim.msgmodel.WKVideoContent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 3/23/21 12:32 PM
 * 查询聊天视频
 */
public class SearchChatVideoActivity extends WKBaseActivity<ActSearchChatVideoLayoutBinding> {
    private String channelID;
    private byte channelType;
    private long oldestOrderSeq = 0;
    private final int[] types = new int[]{WKContentType.WK_VIDEO};
    private SearchChatVideoAdapter adapter;

    @Override
    protected ActSearchChatVideoLayoutBinding getViewBinding() {
        return ActSearchChatVideoLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.wk_video);
    }

    @Override
    protected void initPresenter() {
        channelID = getIntent().getStringExtra("channel_id");
        channelType = getIntent().getByteExtra("channel_type", WKChannelType.PERSONAL);
    }

    @Override
    protected void initView() {
        wkVBinding.spinKit.setColor(Theme.colorAccount);
        PinnedHeaderItemDecoration mHeaderItemDecoration = new PinnedHeaderItemDecoration.Builder(1).enableDivider(false).create();
        int wH = (AndroidUtilities.getScreenWidth() - AndroidUtilities.dp( 6)) / 4;
        FullyGridLayoutManager layoutManager = new FullyGridLayoutManager(this, 4);
        wkVBinding.recyclerView.setLayoutManager(layoutManager);
        adapter = new SearchChatVideoAdapter(wH, searchChatVideoEntity -> EndpointManager.getInstance().invoke(EndpointSID.chatView, new ChatViewMenu(SearchChatVideoActivity.this, channelID, channelType, searchChatVideoEntity.oldestOrderSeq, false)));
        wkVBinding.recyclerView.setAdapter(adapter);
        wkVBinding.recyclerView.addItemDecoration(mHeaderItemDecoration);
    }

    @Override
    protected void initListener() {
        getData();
        wkVBinding.refreshLayout.setEnableRefresh(false);
        wkVBinding.refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                oldestOrderSeq = adapter.getData().get(adapter.getData().size() - 1).oldestOrderSeq;
                getData();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

            }
        });
        adapter.addChildClickViewIds(R.id.imageView);
        adapter.setOnItemChildClickListener((adapter1, view1, position) -> SingleClickUtil.determineTriggerSingleClick(view1, view2 -> {
            SearchChatVideoEntity entity = (SearchChatVideoEntity) adapter1.getData().get(position);
            if (entity != null && entity.getItemType() == 0) {
                @SuppressWarnings("unchecked") ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(SearchChatVideoActivity.this, new Pair<>(view1, "coverIv"));
                Intent intent = new Intent(this, PlayVideoActivity.class);
                intent.putExtra("coverImg", WKApiConfig.getShowUrl(entity.coverUrl));
                intent.putExtra("url", entity.videoUrl);
                startActivity(intent, activityOptions.toBundle());
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        }));
    }


    private void getData() {
        List<WKMsg> list = WKIM.getInstance().getMsgManager().searchMsgWithChannelAndContentTypes(channelID, channelType, oldestOrderSeq, 120, types);
        if (WKReader.isNotEmpty(list)) {
            List<SearchChatVideoEntity> fileEntityList = new ArrayList<>();

            wkVBinding.refreshLayout.finishLoadMore();
            // 构造数据
            for (WKMsg msg : list) {
                String date = WKTimeUtils.getInstance().time2YearMonth(msg.timestamp * 1000);
                if (WKReader.isNotEmpty(fileEntityList)) {
                    if (!fileEntityList.get(fileEntityList.size() - 1).date.equals(date)) {
                        SearchChatVideoEntity entity = new SearchChatVideoEntity();
                        entity.date = date;
                        entity.itemType = 1;
                        fileEntityList.add(entity);
                    }
                } else {
                    SearchChatVideoEntity entity = new SearchChatVideoEntity();
                    entity.date = date;
                    entity.itemType = 1;
                    fileEntityList.add(entity);
                }
                SearchChatVideoEntity entity = new SearchChatVideoEntity();
                entity.date = date;
                entity.messageContent = msg.baseContentMsgModel;
                entity.oldestOrderSeq = msg.orderSeq;
                WKVideoContent videoContent = (WKVideoContent) msg.baseContentMsgModel;
                String showUrl = "";
                if (!TextUtils.isEmpty(videoContent.localPath)) {
                    File file = new File(videoContent.localPath);
                    if (file.exists()) {
                        showUrl = videoContent.localPath;
                    }
                }
                if (TextUtils.isEmpty(showUrl)) {
                    showUrl = WKApiConfig.getShowUrl(videoContent.url);
                }
                entity.videoUrl = showUrl;
                if (videoContent.second > 0) {
                    //分
                    int minute = (int) (videoContent.second / (60));
                    //秒
                    int second = (int) (videoContent.second % 60);
                    String showMinute = minute < 10 ? ("0" + minute) : minute + "";
                    String showSecond = second < 10 ? ("0" + second) : second + "";
                    entity.second = String.format("%s:%s", showMinute, showSecond);
                }
                entity.coverUrl = WKApiConfig.getShowUrl(videoContent.cover);
                fileEntityList.add(entity);
            }
            if (WKReader.isNotEmpty(adapter.getData()) && WKReader.isNotEmpty(fileEntityList)) {
                SearchChatVideoEntity entity = adapter.getData().get(adapter.getData().size() - 1);
                if (entity.date.equals(fileEntityList.get(0).date)) {
                    fileEntityList.remove(0);
                }
            }
            adapter.addData(fileEntityList);
            wkVBinding.noDataTv.setVisibility(View.GONE);
        } else {
            wkVBinding.refreshLayout.finishLoadMoreWithNoMoreData();
            if (oldestOrderSeq == 0) {
                wkVBinding.refreshLayout.setEnableLoadMore(false);
                wkVBinding.noDataTv.setVisibility(View.VISIBLE);
            }
        }

    }
}
