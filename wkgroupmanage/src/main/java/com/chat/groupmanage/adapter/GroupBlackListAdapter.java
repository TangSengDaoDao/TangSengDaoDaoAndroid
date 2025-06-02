package com.chat.groupmanage.adapter;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.ui.components.AvatarView;
import com.chat.groupmanage.R;
import com.chat.groupmanage.entity.GroupMemberEntity;
import com.xinbida.wukongim.entity.WKChannelType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 2020-10-19 17:26
 * 黑名单
 */
public class GroupBlackListAdapter extends BaseMultiItemQuickAdapter<GroupMemberEntity, BaseViewHolder> {
    public GroupBlackListAdapter(@Nullable List<GroupMemberEntity> data) {
        super(data);
        addItemType(1, R.layout.item_group_manager_layout);
        addItemType(2, R.layout.item_group_manager_add_layout);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, GroupMemberEntity groupMemberEntity) {
        if (groupMemberEntity.getItemType() == 1) {
            baseViewHolder.setText(R.id.nameTv, groupMemberEntity.channelMember.memberName);
            AvatarView avatarView = baseViewHolder.getView(R.id.avatarView);
            avatarView.showAvatar(groupMemberEntity.channelMember.memberUID, WKChannelType.PERSONAL,groupMemberEntity.channelMember.memberAvatarCacheKey);

//            if (groupMemberEntity.channelMember.role == GroupMemberRoleType.admin) {
//                baseViewHolder.setGone(R.id.removeIv, true);
//            } else {
//                baseViewHolder.setGone(R.id.removeIv, myRoleInGroup != GroupMemberRoleType.admin);
//            }
            baseViewHolder.setGone(R.id.roleTv, true);
        } else {
            baseViewHolder.setText(R.id.nameTv, R.string.black_list);
        }
    }
}
