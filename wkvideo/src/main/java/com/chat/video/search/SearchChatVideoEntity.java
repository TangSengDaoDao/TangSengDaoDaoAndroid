package com.chat.video.search;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xinbida.wukongim.msgmodel.WKMessageContent;

/**
 * 3/23/21 12:52 PM
 * 搜索聊天视频
 */
public class SearchChatVideoEntity implements MultiItemEntity {
    public int itemType;
    public String coverUrl;
    public String videoUrl;
    public String second;
    public String date;
    public long oldestOrderSeq;
    public WKMessageContent messageContent;

    @Override
    public int getItemType() {
        return itemType;
    }
}
