package com.chat.advanced.ui.search;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chat.advanced.R;
import com.chat.advanced.databinding.ActChatWithFromuidLayoutBinding;
import com.chat.base.base.WKBaseActivity;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.EndpointSID;
import com.chat.base.endpoint.entity.ChatViewMenu;
import com.chat.base.utils.WKReader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKChannel;
import com.xinbida.wukongim.entity.WKChannelMember;
import com.xinbida.wukongim.entity.WKMsg;
import com.xinbida.wukongim.entity.WKChannelType;

import java.util.ArrayList;
import java.util.List;


public class ChatWithFromUIDActivity extends WKBaseActivity<ActChatWithFromuidLayoutBinding> {
    ChatWithFromUIDAdapter adapter;
    private String channelID;
    private String fromUID;
    private long orderSeq = 0;


    @Override
    protected ActChatWithFromuidLayoutBinding getViewBinding() {
        return ActChatWithFromuidLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.search_with_member);
    }

    @Override
    protected void initPresenter() {
        channelID = getIntent().getStringExtra("channelID");
        fromUID = getIntent().getStringExtra("fromUID");
    }

    @Override
    protected void initView() {
        String name = "";
        String avatarKey = "";
        WKChannelMember member = WKIM.getInstance().getChannelMembersManager().getMember(channelID, WKChannelType.GROUP, fromUID);
        if (member != null) {
            name = TextUtils.isEmpty(member.memberRemark) ? member.memberName : member.memberRemark;
        }
        WKChannel channel = WKIM.getInstance().getChannelManager().getChannel(fromUID, WKChannelType.PERSONAL);
        if (channel != null) {
            if (!TextUtils.isEmpty(channel.channelRemark))
                name = channel.channelRemark;
            avatarKey = channel.avatarCacheKey;
        }
        adapter = new ChatWithFromUIDAdapter(name, avatarKey);
        initAdapter(wkVBinding.recyclerView, adapter);
    }

    @Override
    protected void initListener() {
        wkVBinding.refreshLayout.setEnableRefresh(false);
        wkVBinding.refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                getData();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

            }
        });
        adapter.setOnItemClickListener((adapter, view, position) -> {
            WKMsg msg = (WKMsg) adapter.getData().get(position);
            if (msg != null) {
                EndpointManager.getInstance().invoke(EndpointSID.chatView, new ChatViewMenu(ChatWithFromUIDActivity.this, channelID, WKChannelType.GROUP, msg.orderSeq, false));
            }
        });
        getData();
    }

    private void getData() {
        if (WKReader.isNotEmpty(adapter.getData()))
            orderSeq = adapter.getData().get(adapter.getData().size() - 1).orderSeq;
        List<WKMsg> list = WKIM.getInstance().getMsgManager().getWithFromUID(channelID, WKChannelType.GROUP, fromUID, orderSeq, 20);
        List<WKMsg> resultList = new ArrayList<>();
        for (int i = 0, size = list.size(); i < size; i++) {
            if (list.get(i).baseContentMsgModel != null) {
                resultList.add(list.get(i));
            }
        }
        if (orderSeq != 0)
            adapter.addData(resultList);
        else {
            adapter.setList(resultList);
            if (WKReader.isEmpty(resultList)) {
                wkVBinding.noDataTv.setVisibility(View.VISIBLE);
                wkVBinding.refreshLayout.setVisibility(View.GONE);
            }
        }
        if (WKReader.isEmpty(resultList)) {
            wkVBinding.refreshLayout.setEnableLoadMore(false);
        } else
            wkVBinding.refreshLayout.finishLoadMore();
    }
}
