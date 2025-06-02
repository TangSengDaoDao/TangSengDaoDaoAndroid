package com.chat.file.search;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chat.base.base.WKBaseActivity;
import com.chat.base.msgitem.WKContentType;
import com.chat.base.ui.Theme;
import com.chat.base.utils.WKReader;
import com.chat.base.utils.WKTimeUtils;
import com.chat.base.utils.StringUtils;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.file.ChatFileActivity;
import com.chat.file.R;
import com.chat.file.databinding.ActSearchChatFileLayoutBinding;
import com.chat.file.msgitem.FileContent;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKMsg;
import com.xinbida.wukongim.entity.WKChannelType;

import java.util.ArrayList;
import java.util.List;

/**
 * 3/22/21 2:12 PM
 * 搜索聊天文件
 */
public class SearchChatFileActivity extends WKBaseActivity<ActSearchChatFileLayoutBinding> {
    private String channelID;
    private byte channelType;
    private long oldestOrderSeq = 0;
    private final int[] types = new int[]{WKContentType.WK_FILE};
    private SearchFileAdapter adapter;

    @Override
    protected ActSearchChatFileLayoutBinding getViewBinding() {
        return ActSearchChatFileLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.str_file_file);
    }

    @Override
    protected void initPresenter() {
        channelID = getIntent().getStringExtra("channel_id");
        channelType = getIntent().getByteExtra("channel_type", WKChannelType.PERSONAL);
    }

    @Override
    protected void initView() {
        adapter = new SearchFileAdapter();
        initAdapter(wkVBinding.recyclerView, adapter);
    }

    @Override
    protected void initListener() {
        getData();
        wkVBinding.spinKit.setColor(Theme.colorAccount);
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
        adapter.setOnItemClickListener((adapter1, view1, position) -> SingleClickUtil.determineTriggerSingleClick(view1, view -> {
            SearchFileEntity entity = (SearchFileEntity) adapter1.getData().get(position);
            if (entity != null) {
                Intent intent = new Intent(SearchChatFileActivity.this, ChatFileActivity.class);
                intent.putExtra("clientMsgNo", entity.msg.clientMsgNO);
                startActivity(intent);
            }
        }));
    }

    private void getData() {
        List<WKMsg> list = WKIM.getInstance().getMsgManager().searchMsgWithChannelAndContentTypes(channelID, channelType, oldestOrderSeq, 20, types);
        if (WKReader.isNotEmpty(list)) {
            wkVBinding.noDataTv.setVisibility(View.GONE);
            List<SearchFileEntity> fileEntityList = new ArrayList<>();
            for (WKMsg msg : list) {
                SearchFileEntity entity = new SearchFileEntity();
                entity.UID = msg.fromUID;
                entity.oldestOrderSeq = msg.orderSeq;
                if (channelType == WKChannelType.PERSONAL) {
                    if (msg.getFrom() != null) {
                        entity.avatarCacheKey = msg.getFrom().avatarCacheKey;
                        entity.userName = msg.getFrom().channelName;
                    }
                } else {
                    if (msg.getMemberOfFrom() != null) {
                        entity.avatarCacheKey = msg.getMemberOfFrom().memberAvatarCacheKey;
                        entity.userName = msg.getMemberOfFrom().memberName;
                    }
                }
                entity.date = WKTimeUtils.getInstance().time2YearMonth(msg.timestamp * 1000);
                entity.time = WKTimeUtils.getInstance().time2DataDay(msg.timestamp * 1000);
                entity.msg = msg;
                FileContent fileContent = (FileContent) msg.baseContentMsgModel;
                if (fileContent != null) {
                    entity.fileName = fileContent.name;
                    entity.fileSize = StringUtils.sizeFormatNum2String(fileContent.size);
                    if (fileContent.name.contains(".")) {
                        String type = fileContent.name.substring(fileContent.name.lastIndexOf(".") + 1);
                        if (!TextUtils.isEmpty(type))
                            entity.fileType = type.toUpperCase();
                        else entity.fileType = getString(R.string.unknown_file);
                    } else entity.fileType = getString(R.string.unknown_file);
                }
                fileEntityList.add(entity);
            }
            adapter.addData(fileEntityList);
            wkVBinding.refreshLayout.finishLoadMore();
        } else {
            wkVBinding.refreshLayout.finishLoadMoreWithNoMoreData();
            if (oldestOrderSeq == 0) {
                wkVBinding.noDataTv.setVisibility(View.VISIBLE);
                wkVBinding.refreshLayout.setEnableLoadMore(false);
            }
        }

    }
}
