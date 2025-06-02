package com.chat.advanced.ui.search;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xinbida.wukongim.msgmodel.WKMessageContent;

/**
 * 3/23/21 10:31 AM
 * 搜索聊天图片
 */
class ChatImgEntity implements MultiItemEntity {
    public int itemType;
    public String url;
    public String date;
    public String clientMsgNo;
    public long oldestOrderSeq;
    public WKMessageContent messageContent;

    @Override
    public int getItemType() {
        return itemType;
    }
}
