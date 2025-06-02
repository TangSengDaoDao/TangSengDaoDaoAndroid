package com.chat.advanced.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.advanced.R;
import com.chat.advanced.databinding.FragAllMsgReactionsLayoutBinding;
import com.chat.advanced.utils.ReactionStickerUtils;
import com.chat.base.base.WKBaseFragment;
import com.chat.base.ui.components.AvatarView;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKChannelType;
import com.xinbida.wukongim.entity.WKMsgReaction;

import java.util.ArrayList;
import java.util.List;

public class AllMsgReactionsFragment extends WKBaseFragment<FragAllMsgReactionsLayoutBinding> {
    String text = "";
    String msgID = "";

    @Override
    protected FragAllMsgReactionsLayoutBinding getViewBinding() {
        return FragAllMsgReactionsLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initData() {
        List<WKMsgReaction> list = WKIM.getInstance().getMsgManager().getMsgReactions(msgID);
        List<WKMsgReaction> tempList = new ArrayList<>();
        if (text.equals(getString(R.string.str_all))) {
            tempList = list;
        } else {
            for (WKMsgReaction reaction : list) {
                if (reaction.emoji.equals(text)) {
                    tempList.add(reaction);
                }
            }
        }
        MsgReactionsAdapter adapter = new MsgReactionsAdapter(text, new ArrayList<>());
        initAdapter(wkVBinding.recyclerView, adapter);
        adapter.setNewInstance(tempList);
    }

    @Override
    protected void getDataBundle(Bundle bundle) {
        super.getDataBundle(bundle);
        text = bundle.getString("text");
        msgID = bundle.getString("msgID");
    }

    private static class MsgReactionsAdapter extends BaseQuickAdapter<WKMsgReaction, BaseViewHolder> {
        private final String text;

        public MsgReactionsAdapter(String text, List<WKMsgReaction> list) {
            super(R.layout.item_msg_reactions_user_layout, list);
            this.text = text;
        }

        @Override
        protected void convert(@NonNull BaseViewHolder baseViewHolder, WKMsgReaction msgReaction) {
            baseViewHolder.setText(R.id.nameTv, msgReaction.name);
            AvatarView avatarView = baseViewHolder.getView(R.id.avatarView);
            avatarView.showAvatar(msgReaction.uid, WKChannelType.PERSONAL, false);
            if (text.equals(getContext().getString(R.string.str_all)))
                baseViewHolder.setImageResource(R.id.imageView, ReactionStickerUtils.getEmojiID(msgReaction.emoji));
        }
    }
}
