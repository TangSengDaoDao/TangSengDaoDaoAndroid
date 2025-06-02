package com.chat.moments.adapter

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.config.WKApiConfig
import com.chat.base.emoji.MoonUtil
import com.chat.base.entity.PopupMenuItem
import com.chat.base.glide.GlideUtils
import com.chat.base.ui.components.AvatarView
import com.chat.base.utils.WKDialogUtils
import com.chat.moments.R
import com.chat.moments.db.MomentsDBManager
import com.chat.moments.entity.MomentsMsg
import com.xinbida.wukongim.entity.WKChannelType

class MomentsMsgAdapter :
    BaseQuickAdapter<MomentsMsg, BaseViewHolder>(R.layout.item_moments_msg_layout) {
    override fun convert(holder: BaseViewHolder, item: MomentsMsg) {
        val richTextView: TextView = holder.getView(R.id.contentTv)

        holder.setText(R.id.nameTv, item.name)
        holder.setText(R.id.timeTv, item.time)
        holder.setText(R.id.publishTv, item.content)
        holder.setGone(R.id.likeIv, item.action != "like")
        holder.setGone(R.id.contentTv, item.action == "like")
        if (item.action == "comment" && item.is_deleted == 1) {
            richTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.color999))
            richTextView.setText(R.string.delete_comment)
        } else {
            richTextView.setBackgroundColor(
                ContextCompat.getColor(context, R.color.transparent)
            )
            MoonUtil.identifyFaceExpression(
                context,
                richTextView,
                item.comment,
                MoonUtil.SMALL_SCALE
            )
            richTextView.movementMethod = LinkMovementMethod.getInstance()
        }
        when (item.contentType) {
            0 -> {
                //文字
                holder.setGone(R.id.playIv, true)
                holder.setGone(R.id.publishTv, false)
                holder.setGone(R.id.pubishIv, true)
            }
            1 -> {
                //图片
                holder.setGone(R.id.playIv, true)
                holder.setGone(R.id.publishTv, true)
                holder.setGone(R.id.pubishIv, false)
                GlideUtils.getInstance().showImg(
                    context,
                    WKApiConfig.getShowUrl(item.url),
                    holder.getView(R.id.pubishIv)
                )
            }
            else -> {
                //视频
                holder.setGone(R.id.playIv, false)
                holder.setGone(R.id.pubishIv, false)
                holder.setGone(R.id.publishTv, true)
                GlideUtils.getInstance().showImg(
                    context,
                    WKApiConfig.getShowUrl(item.url),
                    holder.getView(R.id.pubishIv)
                )
            }
        }
        val avatarView: AvatarView = holder.getView(R.id.avatarView)
        avatarView.showAvatar(
            item.uid,
            WKChannelType.PERSONAL,
            item.avatarCacheKey
        )

        val list: MutableList<PopupMenuItem> = ArrayList()
        list.add(
            PopupMenuItem(
                context.getString(R.string.str_delete),
                R.mipmap.msg_delete,
                object : PopupMenuItem.IClick {
                    override fun onClick() {
                        val result =
                            MomentsDBManager.getInstance().delete(item.id)
                        if (result) {
                            removeAt(holder.bindingAdapterPosition)
                        }
                    }
                })
        )
        WKDialogUtils.getInstance().setViewLongClickPopup(holder.getView(R.id.contentLayout),list)
    }
}