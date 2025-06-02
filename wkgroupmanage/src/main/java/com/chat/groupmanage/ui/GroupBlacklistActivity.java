package com.chat.groupmanage.ui;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.TextView;

import com.chat.base.base.WKBaseActivity;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.groupmanage.R;
import com.chat.groupmanage.adapter.GroupBlackListAdapter;
import com.chat.groupmanage.databinding.ActGroupBlackListLayoutBinding;
import com.chat.groupmanage.entity.GroupMemberEntity;
import com.chat.groupmanage.service.GroupManageModel;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKChannelMember;
import com.xinbida.wukongim.entity.WKChannelType;

import java.util.ArrayList;
import java.util.List;

/**
 * 2020-10-19 16:19
 * 群成员黑名单
 */
public class GroupBlacklistActivity extends WKBaseActivity<ActGroupBlackListLayoutBinding> {
    private String groupId;
    private GroupBlackListAdapter adapter;

    @Override
    protected ActGroupBlackListLayoutBinding getViewBinding() {
        return ActGroupBlackListLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.black_list);
    }

    @Override
    protected void initPresenter() {

    }

    @Override
    protected void initView() {
        adapter = new GroupBlackListAdapter(new ArrayList<>());
        initAdapter(wkVBinding.recyclerView, adapter);
    }

    @Override
    protected void initListener() {
        adapter.addChildClickViewIds(R.id.removeIv);
        adapter.setOnItemChildClickListener((adapter1, view1, position) -> {
            GroupMemberEntity entity = (GroupMemberEntity) adapter1.getItem(position);
            if (entity != null) {
                showDialog(String.format(getString(R.string.remove_user_in_black_list), entity.channelMember.memberName), index -> {
                    if (index == 0) {
                        return;
                    }
                    List<String> uids = new ArrayList<>();
                    uids.add(entity.channelMember.memberUID);
                    GroupManageModel.getInstance().addOrRemoveBlackList(groupId, "remove", uids, (code, msg) -> {
                        if (code != HttpResponseCode.success && !TextUtils.isEmpty(msg)) {
                            showToast(msg);
                        } else {
                            WKIM.getInstance().getChannelMembersManager().updateMemberStatus(groupId, WKChannelType.GROUP, entity.channelMember.memberUID, 0);
                        }
                    });
                });
            }
        });
        adapter.setOnItemClickListener((adapter1, view1, position) -> SingleClickUtil.determineTriggerSingleClick(view1, view -> {
            GroupMemberEntity entity = (GroupMemberEntity) adapter1.getItem(position);
            if (entity != null) {
                if (entity.itemType == 2) {
                    Intent intent = new Intent(GroupBlacklistActivity.this, ChooseNormalMembersActivity.class);
                    intent.putExtra("groupId", groupId);
                    intent.putExtra("type", 1);
                    startActivity(intent);
                }
            }
        }));
        WKIM.getInstance().getChannelMembersManager().addOnRefreshChannelMemberInfo("group_blacklist_refresh_channel_member", (channelMember,isEnd) -> resetData());
    }

    @Override
    protected void initData() {
        super.initData();
        groupId = getIntent().getStringExtra("groupId");
        resetData();
    }

    private void resetData() {
        List<WKChannelMember> list = WKIM.getInstance().getChannelMembersManager().getWithStatus(groupId, WKChannelType.GROUP, 2);
        List<GroupMemberEntity> tempList = new ArrayList<>();
        for (int i = 0, size = list.size(); i < size; i++) {
            tempList.add(new GroupMemberEntity(list.get(i)));
        }
        GroupMemberEntity entity = new GroupMemberEntity(null);
        entity.itemType = 2;
        tempList.add(entity);
        adapter.setList(tempList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WKIM.getInstance().getChannelMembersManager().removeRefreshChannelMemberInfo("group_blacklist_refresh_channel_member");
    }
}
