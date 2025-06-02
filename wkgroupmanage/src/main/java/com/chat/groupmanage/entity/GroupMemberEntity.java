package com.chat.groupmanage.entity;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xinbida.wukongim.entity.WKChannelMember;

/**
 * 2020-04-11 23:06
 * 群成员
 */
public class GroupMemberEntity implements MultiItemEntity {
    public boolean isChecked;
    public WKChannelMember channelMember;
    public String pying;
    public int itemType = 1;
    public boolean isSetDelete;
    public GroupMemberEntity(WKChannelMember channelMember) {
        this.channelMember = channelMember;
    }

    @Override
    public int getItemType() {
        return itemType;
    }
}
