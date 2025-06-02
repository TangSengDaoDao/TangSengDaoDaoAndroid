package com.chat.moments.adapter;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.ui.components.AvatarView;
import com.chat.moments.R;
import com.xinbida.wukongim.entity.WKChannel;

import org.jetbrains.annotations.NotNull;

/**
 * 2020-12-10 15:46
 */
public class PrivacyUserAdapter extends BaseQuickAdapter<WKChannel, BaseViewHolder> {
    public PrivacyUserAdapter() {
        super(R.layout.item_privacy_user_layout);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, WKChannel channel) {
        AvatarView avatarView = baseViewHolder.getView(R.id.avatarView);
        avatarView.showAvatar(channel);
        baseViewHolder.setText(R.id.nameTv, TextUtils.isEmpty(channel.channelRemark) ? channel.channelName : channel.channelRemark);
    }
}
