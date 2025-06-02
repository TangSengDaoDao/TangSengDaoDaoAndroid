package com.chat.advanced.ui.search;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.advanced.R;
import com.chat.base.endpoint.entity.SearchChatContentMenu;
import com.chat.base.ui.Theme;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 3/22/21 5:11 PM
 * 搜索类型
 */
class RecordTypeAdapter extends BaseQuickAdapter<SearchChatContentMenu, BaseViewHolder> {
    public RecordTypeAdapter(List<SearchChatContentMenu> data) {
        super(R.layout.item_search_record_type_layout, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, SearchChatContentMenu menu) {
        baseViewHolder.setText(R.id.nameTv, menu.text);
        baseViewHolder.setTextColor(R.id.nameTv,Theme.colorAccount);
        Theme.setPressedBackground(baseViewHolder.getView(R.id.nameTv));
    }
}
