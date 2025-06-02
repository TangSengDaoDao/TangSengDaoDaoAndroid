package com.chat.moments.adapter;

import android.text.TextUtils;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.endpoint.entity.ChooseLabelEntity;
import com.chat.base.utils.WKReader;
import com.chat.moments.R;
import com.xinbida.wukongim.entity.WKChannel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 2020-11-19 13:41
 * 选择标签
 */
public class ChooseLabelAdapter extends BaseQuickAdapter<ChooseLabelEntity, BaseViewHolder> {
    private final int type;

    public ChooseLabelAdapter(int type, @Nullable List<ChooseLabelEntity> data) {
        super(R.layout.item_choose_label_layout, data);
        this.type = type;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, ChooseLabelEntity ChooseLabelEntity) {
        baseViewHolder.setText(R.id.nameTv, ChooseLabelEntity.labelName);
        ImageView imageView = baseViewHolder.getView(R.id.checkIv);
        if (type == 1) {
            imageView.setImageResource(R.drawable.img_check);
        } else
            imageView.setImageResource(R.drawable.img_check_red);
        imageView.setSelected(ChooseLabelEntity.isSelected);
        StringBuilder stringBuilder = new StringBuilder();
        if (WKReader.isNotEmpty(ChooseLabelEntity.members)) {
            for (WKChannel channel : ChooseLabelEntity.members) {

                if (TextUtils.isEmpty(stringBuilder)) {
                    stringBuilder.append(TextUtils.isEmpty(channel.channelRemark) ? channel.channelName : channel.channelRemark);
                } else {
                    stringBuilder.append(",").append(TextUtils.isEmpty(channel.channelRemark) ? channel.channelName : channel.channelRemark);
                }
            }
        }
        baseViewHolder.setText(R.id.contentTv, stringBuilder);
    }
}
