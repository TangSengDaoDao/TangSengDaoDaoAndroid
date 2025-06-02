package com.chat.file.search.remote

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.ui.components.AvatarView
import com.chat.file.R
import com.xinbida.wukongim.entity.WKChannelType

class SearchGlobalAdapter :
    BaseQuickAdapter<SearchGlobalFileEntity, BaseViewHolder>(R.layout.item_chat_file_layout) {
    override fun convert(holder: BaseViewHolder, item: SearchGlobalFileEntity) {
        val avatarView: AvatarView = holder.getView(R.id.avatarView)
        avatarView.showAvatar(
            item.msg.from_uid,
            WKChannelType.PERSONAL,
           item.fromAvatarCache
        )

        holder.setText(R.id.userNameTv, item.fromName)
        holder.setText(R.id.typeTv, item.fileType)
        holder.setText(R.id.fileNameTv, item.fileModel.name)
        holder.setText(R.id.fileSizeTv, item.fileSize)
        holder.setText(R.id.timeTv, item.time)
        holder.setText(R.id.dateTv, item.date)
        if (holder.absoluteAdapterPosition - 1 >= 0) {
            val entity = data[holder.absoluteAdapterPosition - 1]
            holder.setGone(R.id.dateTv, item.date == entity.date)
        } else {
            holder.setGone(R.id.dateTv, false)
        }
    }
}