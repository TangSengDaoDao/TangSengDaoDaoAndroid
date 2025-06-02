package com.chat.video.search;

import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.entity.PopupMenuItem;
import com.chat.base.glide.GlideUtils;
import com.chat.base.utils.WKDialogUtils;
import com.chat.base.views.pinnedsectionitemdecoration.utils.FullSpanUtil;
import com.chat.video.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 3/23/21 1:45 PM
 * 搜索聊天视频
 */
class SearchChatVideoAdapter extends BaseMultiItemQuickAdapter<SearchChatVideoEntity, BaseViewHolder> {
    private final int wH;
    private final int pWH;
    private final ICLick icLick;

    public SearchChatVideoAdapter(int wH, ICLick icLick) {
        super();
        this.icLick = icLick;
        this.wH = wH;
        this.pWH = wH + 6;
        addItemType(0, R.layout.item_search_chat_video_layout);
        addItemType(1, R.layout.item_search_chat_video_date_layout);
    }

    @Override
    public void onAttachedToRecyclerView(@NotNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        FullSpanUtil.onAttachedToRecyclerView(recyclerView, this, 1);
    }

    @Override
    public void onViewAttachedToWindow(@NotNull BaseViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        FullSpanUtil.onViewAttachedToWindow(holder, this, 1);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, SearchChatVideoEntity searchChatVideoEntity) {
        switch (searchChatVideoEntity.getItemType()) {
            case 1 -> baseViewHolder.setText(R.id.dateTv, searchChatVideoEntity.date);
            case 0 -> {
                FrameLayout layout = baseViewHolder.getView(R.id.contentLayout);
                layout.getLayoutParams().width = pWH;
                layout.getLayoutParams().height = pWH;
                ImageView imageView = baseViewHolder.getView(R.id.imageView);
                imageView.getLayoutParams().width = wH;
                imageView.getLayoutParams().height = wH;
                baseViewHolder.setText(R.id.durationTv, searchChatVideoEntity.second);
                GlideUtils.getInstance().showImg(getContext(), searchChatVideoEntity.coverUrl, imageView);
                List<PopupMenuItem> list = new ArrayList<>();
                list.add(new PopupMenuItem(getContext().getString(R.string.wk_video_go_to_chat_item), R.mipmap.msg_message, () -> icLick.onClick(searchChatVideoEntity)));

                WKDialogUtils.getInstance().setViewLongClickPopup(baseViewHolder.getView(R.id.imageView), list);
            }
        }
    }

    public interface ICLick {
        void onClick(SearchChatVideoEntity searchChatVideoEntity);
    }
}
