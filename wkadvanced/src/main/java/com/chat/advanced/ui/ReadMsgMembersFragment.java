package com.chat.advanced.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.chat.advanced.databinding.FragReadMsgMembersLayoutBinding;
import com.chat.advanced.entity.MsgReadDetailEntity;
import com.chat.advanced.service.AdvancedModel;
import com.chat.base.base.WKBaseFragment;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.EndpointSID;
import com.chat.base.endpoint.entity.UserDetailMenu;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.utils.WKReader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKMsg;

/**
 * 4/9/21 10:23 AM
 */
public class ReadMsgMembersFragment extends WKBaseFragment<FragReadMsgMembersLayoutBinding> {
    MsgReadDetailAdapter adapter;
    private String group_no;
    private String messageID;
    private int read;
    private int page = 1;
    private String channelID;
    private byte channelType;

    @Override
    protected FragReadMsgMembersLayoutBinding getViewBinding() {
        return FragReadMsgMembersLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        adapter = new MsgReadDetailAdapter();
        initAdapter(wkVBinding.recyclerView, adapter);
    }

    @Override
    protected void initListener() {
        wkVBinding.refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                page++;
                getData();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                page = 1;
                getData();
            }
        });
        getData();
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            MsgReadDetailEntity entity = (MsgReadDetailEntity) adapter1.getData().get(position);
            if (entity != null) {
                EndpointManager.getInstance().invoke(EndpointSID.userDetailView, new UserDetailMenu(requireContext(),entity.uid, group_no));
            }
        });
    }


    @Override
    protected void getDataBundle(Bundle bundle) {
        super.getDataBundle(bundle);
        messageID = bundle.getString("message_id");
        group_no = bundle.getString("group_no");
        read = bundle.getInt("type");
        WKMsg msg = WKIM.getInstance().getMsgManager().getWithMessageID(messageID);
        if (msg != null) {
            channelID = msg.channelID;
            channelType = msg.channelType;
        }
    }


    private void getData() {
        AdvancedModel.Companion.getInstance().receipt(messageID, page, channelID, channelType, read, (code, msg, list) -> {
            wkVBinding.refreshLayout.finishRefresh();
            if (code == HttpResponseCode.success) {
                if (page == 1) {
                    adapter.setList(list);
                } else {
                    if (WKReader.isEmpty(list)) {
                        wkVBinding.refreshLayout.finishLoadMoreWithNoMoreData();
                        return;
                    }
                    adapter.addData(list);
                    wkVBinding.refreshLayout.finishLoadMore();
                }
            }
        });
    }
}
