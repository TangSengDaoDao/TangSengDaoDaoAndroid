package com.chat.moments.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.ui.components.AvatarView;
import com.chat.moments.R;
import com.xinbida.wukongim.entity.WKChannel;

import org.jetbrains.annotations.NotNull;

/**
 * 2020-11-12 16:47
 */
public class SelectUserAdapter extends BaseQuickAdapter<WKChannel, BaseViewHolder> {
    public SelectUserAdapter() {
        super(R.layout.item_select_user_layout);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, WKChannel channel) {
        AvatarView avatarView = baseViewHolder.getView(R.id.avatarView);
        avatarView.setSize(30);
        avatarView.showAvatar(channel);
    }
}
