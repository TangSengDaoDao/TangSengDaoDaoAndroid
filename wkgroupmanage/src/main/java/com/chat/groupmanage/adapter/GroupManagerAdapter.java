package com.chat.groupmanage.adapter;

import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.msgitem.WKChannelMemberRole;
import com.chat.base.ui.Theme;
import com.chat.base.ui.components.AvatarView;
import com.chat.base.ui.components.RoundTextView;
import com.chat.groupmanage.R;
import com.chat.groupmanage.entity.GroupMemberEntity;
import com.xinbida.wukongim.entity.WKChannelType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 2020-04-11 20:34
 * 管理员列表
 */
public class GroupManagerAdapter extends BaseMultiItemQuickAdapter<GroupMemberEntity, BaseViewHolder> {
    private int myRoleInGroup;

    public GroupManagerAdapter(@Nullable List<GroupMemberEntity> data) {
        super(data);
        addItemType(1, R.layout.item_group_manager_layout);
        addItemType(2, R.layout.item_group_manager_add_layout);
    }


    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, GroupMemberEntity groupMemberEntity) {
        if (groupMemberEntity.getItemType() == 1) {
            baseViewHolder.setText(R.id.nameTv, groupMemberEntity.channelMember.memberName);
            AvatarView avatarView = baseViewHolder.getView(R.id.avatarView);
            avatarView.showAvatar(groupMemberEntity.channelMember.memberUID, WKChannelType.PERSONAL, groupMemberEntity.channelMember.memberAvatarCacheKey);
            if (groupMemberEntity.channelMember.role == WKChannelMemberRole.admin) {
                baseViewHolder.setGone(R.id.removeIv, true);
            } else {
                baseViewHolder.setGone(R.id.removeIv, myRoleInGroup != WKChannelMemberRole.admin);
            }
            baseViewHolder.setGone(R.id.roleTv, groupMemberEntity.channelMember.role == WKChannelMemberRole.normal);
            baseViewHolder.setText(R.id.roleTv, groupMemberEntity.channelMember.role == WKChannelMemberRole.admin ? R.string.group_admin : R.string.group_manager);
            RoundTextView roleTv = baseViewHolder.getView(R.id.roleTv);
            roleTv.setBackGroundColor(groupMemberEntity.channelMember.role == WKChannelMemberRole.admin ? ContextCompat.getColor(getContext(), R.color.colorFFC107) : Theme.colorAccount);
        }
    }

    public void setMyRoleInGroup(int myRoleInGroup) {
        this.myRoleInGroup = myRoleInGroup;
        notifyItemRangeChanged(0, getData().size());
    }
}
