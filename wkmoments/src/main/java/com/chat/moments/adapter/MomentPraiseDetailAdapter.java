package com.chat.moments.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.ui.components.AvatarView;
import com.chat.moments.WKMomentsApplication;
import com.chat.moments.R;
import com.chat.moments.entity.MomentsPraise;
import com.xinbida.wukongim.entity.WKChannelType;

import org.jetbrains.annotations.NotNull;

/**
 * 2020-11-30 11:53
 * 点赞
 */
public class MomentPraiseDetailAdapter extends BaseQuickAdapter<MomentsPraise, BaseViewHolder> {
    MomentPraiseDetailAdapter() {
        super(R.layout.item_moment_detail_praise_layout);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, MomentsPraise momentsPraise) {

        AvatarView avatarView = baseViewHolder.getView(R.id.avatarView);
        avatarView.setSize(30);
        avatarView.showAvatar(momentsPraise.uid, WKChannelType.PERSONAL, momentsPraise.avatarCacheKey);
        baseViewHolder.getView(R.id.avatarView).setOnClickListener(view -> WKMomentsApplication.getInstance().gotoUserDetail(getContext(), momentsPraise.uid));
    }
}
