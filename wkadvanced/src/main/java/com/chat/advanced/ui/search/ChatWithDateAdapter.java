package com.chat.advanced.ui.search;

import androidx.activity.ComponentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.advanced.R;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.EndpointSID;
import com.chat.base.endpoint.entity.ChatViewMenu;
import com.chat.base.views.FullyGridLayoutManager;

import org.jetbrains.annotations.NotNull;

/**
 * 3/23/21 6:04 PM
 * 通过日期搜索聊天记录
 */
class ChatWithDateAdapter extends BaseMultiItemQuickAdapter<ChatWithDateEntity, BaseViewHolder> {
    private final int wH;
    private final String channelID;
    private final byte channelType;

    public ChatWithDateAdapter(int wH, String channelID, byte channelType) {
        super();
        this.channelID = channelID;
        this.channelType = channelType;
        this.wH = wH;
        addItemType(1, R.layout.item_search_with_date_title_layout);
        addItemType(0, R.layout.item_search_with_date_layout);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, ChatWithDateEntity chatWithDateEntity) {
        switch (chatWithDateEntity.getItemType()) {
            case 0:
                RecyclerView recyclerView = baseViewHolder.getView(R.id.recyclerView);
                ChatWithDateChildAdapter adapter = new ChatWithDateChildAdapter(chatWithDateEntity.list, wH);
                FullyGridLayoutManager layoutManager = new FullyGridLayoutManager(getContext(), 7);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setNestedScrollingEnabled(false);
                recyclerView.setAdapter(adapter);

                adapter.addChildClickViewIds(R.id.dayTv);
                adapter.setOnItemChildClickListener((adapter1, view1, position) -> {
                    ChatWithDateEntity entity = (ChatWithDateEntity) adapter1.getData().get(position);
                    if (entity != null && entity.dayCount > 0 && entity.orderSeq > 0) {
                        for (int i = 0, size = getData().size(); i < size; i++) {
                            if (getData().get(i).list != null) {
                                for (int j = 0, len = getData().get(i).list.size(); j < len; j++) {
                                    if (getData().get(i).list.get(j).selected) {
                                        getData().get(i).list.get(j).selected = false;
                                        notifyItemChanged(i);
                                        break;
                                    }
                                }
                            }
                        }
                        adapter.getData().get(position).selected = true;
                        adapter.notifyItemChanged(position);
                        startChat(entity.orderSeq);
                    }
                });
                break;
            case 1:
                baseViewHolder.setText(R.id.dateTv, chatWithDateEntity.date);
                break;
        }
    }

    private void startChat(long orderSeq) {
        EndpointManager.getInstance().invoke(EndpointSID.chatView,new ChatViewMenu((ComponentActivity) getContext(), channelID, channelType, orderSeq, false));
    }
}
