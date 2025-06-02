package com.chat.advanced.ui.search;

import android.text.TextUtils;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.advanced.R;
import com.chat.base.msgitem.WKChannelMemberRole;
import com.chat.base.ui.Theme;
import com.chat.base.ui.components.AvatarView;
import com.xinbida.wukongim.entity.WKChannelType;

import org.jetbrains.annotations.NotNull;

/**
 * 2020-12-11 15:23
 * 所有成员
 */
public class SearchAllMembersAdapter extends BaseQuickAdapter<GroupMember, BaseViewHolder> {

    public SearchAllMembersAdapter() {
        super(R.layout.item_search_all_members_layout);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, GroupMember member) {
        String showName = member.member.remark;
        if (TextUtils.isEmpty(showName)) {
            showName = TextUtils.isEmpty(member.member.memberRemark) ? member.member.memberName : member.member.memberRemark;
        }
        AvatarView avatarView = baseViewHolder.getView(R.id.avatarView);
        baseViewHolder.setText(R.id.nameTv, showName);
        avatarView.showAvatar(member.member.memberUID, WKChannelType.PERSONAL, member.member.memberAvatar);
        if (member.member.role == WKChannelMemberRole.admin) {
            avatarView.onlineTv.setVisibility(View.VISIBLE);
            avatarView.spotView.setVisibility(View.GONE);
            avatarView.onlineTv.setText(R.string.group_owner);
            avatarView.onlineTv.setBackgroundResource(R.drawable.radian_normal_layout);
            avatarView.onlineTv.setTextColor(ContextCompat.getColor(getContext(), R.color.colorFFC107));
        } else if (member.member.role == WKChannelMemberRole.manager) {
            avatarView.onlineTv.setText(R.string.group_manager);
            avatarView.onlineTv.setTextColor(Theme.colorAccount);
            avatarView.onlineTv.setVisibility(View.VISIBLE);
            avatarView.onlineTv.setBackgroundResource(R.drawable.radian_normal_layout);
        } else {
            avatarView.spotView.setVisibility(View.GONE);
            avatarView.onlineTv.setVisibility(View.GONE);
        }
    }
}
