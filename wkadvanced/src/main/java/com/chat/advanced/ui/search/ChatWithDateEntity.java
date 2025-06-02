package com.chat.advanced.ui.search;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.List;

/**
 * 3/23/21 6:02 PM
 * 通过日期搜索聊天记录
 */
public class ChatWithDateEntity implements MultiItemEntity {
    public int itemType;
    public String day;
    public boolean selected;
    public boolean isToDay;
    public String date;
    public long dayCount;
    public long orderSeq;
    public boolean isNull;
    public List<ChatWithDateEntity> list;

    @Override
    public int getItemType() {
        return itemType;
    }

    public ChatWithDateEntity(int itemType) {
        this.itemType = itemType;
    }

}
