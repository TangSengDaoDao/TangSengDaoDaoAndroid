package com.chat.groupmanage.adapter

import android.text.TextUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.ui.components.AvatarView
import com.chat.groupmanage.R
import com.chat.groupmanage.entity.GroupMemberEntity
import com.xinbida.wukongim.entity.WKChannelType

class OutUserAdapter :
    BaseQuickAdapter<GroupMemberEntity, BaseViewHolder>(R.layout.item_out_member_layout) {
    override fun convert(holder: BaseViewHolder, item: GroupMemberEntity) {
        var showName = item.channelMember.memberRemark
        if (TextUtils.isEmpty(showName))
            showName = item.channelMember.memberName
        holder.setText(R.id.nameTv, showName)
        holder.setText(R.id.timeTv,item.channelMember.updatedAt)
        val avatarView = holder.getView<AvatarView>(R.id.avatarView)
        avatarView.showAvatar(
            item.channelMember.memberUID,
            WKChannelType.PERSONAL,
            item.channelMember.memberAvatarCacheKey
        )
        val index: Int = holder.bindingAdapterPosition
        val index1: Int = getPositionForSection(item.pying.substring(0, 1))
        if (index == index1) {
            holder.setVisible(R.id.pyTv, true)
            holder.setText(R.id.pyTv, item.pying.substring(0, 1))
        } else {
            holder.setVisible(R.id.pyTv, false)
        }
    }


    private fun getPositionForSection(catalog: String): Int {
        var i = 0
        val size = data.size
        while (i < size) {
            val sortStr = data[i].pying.substring(0, 1)
            if (catalog.equals(sortStr, ignoreCase = true)) {
                return i
            }
            i++
        }
        return -1
    }
}