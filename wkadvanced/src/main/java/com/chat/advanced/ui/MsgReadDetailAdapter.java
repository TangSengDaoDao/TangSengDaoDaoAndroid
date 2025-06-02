package com.chat.advanced.ui;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.advanced.R;
import com.chat.advanced.entity.MsgReadDetailEntity;
import com.chat.base.ui.components.AvatarView;
import com.xinbida.wukongim.entity.WKChannelType;

import org.jetbrains.annotations.NotNull;

/**
 * 4/9/21 10:57 AM
 * 消息查看详情
 */
public class MsgReadDetailAdapter extends BaseQuickAdapter<MsgReadDetailEntity, BaseViewHolder> {
    public MsgReadDetailAdapter() {
        super(R.layout.item_msg_read_detail_layout);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, MsgReadDetailEntity detailEntity) {
        AvatarView avatarView = baseViewHolder.getView(R.id.avatarView);
        avatarView.showAvatar(detailEntity.uid, WKChannelType.PERSONAL);
        baseViewHolder.setText(R.id.nameTv, detailEntity.name);
    }
}
