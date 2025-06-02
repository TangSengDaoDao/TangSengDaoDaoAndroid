package com.chat.groupmanage.adapter;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.groupmanage.R;
import com.chat.groupmanage.entity.ForbiddenTime;

public class ForbiddenWitchGroupMemberAdapter extends BaseQuickAdapter<ForbiddenTime, BaseViewHolder> {
    public ForbiddenWitchGroupMemberAdapter() {
        super(R.layout.item_forbidden_with_group_member);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, ForbiddenTime forbiddenWitchGroupMemberEntity) {
        baseViewHolder.setText(R.id.timeTv, forbiddenWitchGroupMemberEntity.text);
        baseViewHolder.setVisible(R.id.checkIv, forbiddenWitchGroupMemberEntity.isChecked);
    }
}
