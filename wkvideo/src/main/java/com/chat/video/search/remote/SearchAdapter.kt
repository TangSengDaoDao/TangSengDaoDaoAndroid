package com.chat.video.search.remote

import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.config.WKApiConfig
import com.chat.base.entity.PopupMenuItem
import com.chat.base.glide.GlideUtils
import com.chat.base.utils.WKDialogUtils
import com.chat.base.views.pinnedsectionitemdecoration.utils.FullSpanUtil
import com.chat.video.R

class SearchAdapter(private val wH: Int, val iClick: (item: SearchEntity) -> Unit) :
    BaseMultiItemQuickAdapter<SearchEntity, BaseViewHolder>() {
    private var pWH = 0

    init {
        this.pWH = wH + 6
        addItemType(0, R.layout.item_search_chat_video_layout)
        addItemType(1, R.layout.item_search_chat_video_date_layout)
    }

    override fun convert(holder: BaseViewHolder, item: SearchEntity) {
        when (item.itemType) {
            1 -> holder.setText(R.id.dateTv, item.date)
            0 -> {
                val layout: FrameLayout = holder.getView(R.id.contentLayout)
                layout.layoutParams.width = pWH
                layout.layoutParams.height = pWH
                val imageView: ImageView = holder.getView(R.id.imageView)
                imageView.layoutParams.width = wH
                imageView.layoutParams.height = wH
                holder.setText(R.id.durationTv, item.second)
                GlideUtils.getInstance().showImg(context, WKApiConfig.getShowUrl(item.videoModel.cover), imageView)
                val list: MutableList<PopupMenuItem> = ArrayList()
                list.add(
                    PopupMenuItem(context.getString(R.string.wk_video_go_to_chat_item),
                        R.mipmap.msg_message
                    ) { iClick(item) }
                )

                WKDialogUtils.getInstance()
                    .setViewLongClickPopup(holder.getView(R.id.imageView), list)
            }
        }
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        FullSpanUtil.onAttachedToRecyclerView(recyclerView, this, 1)
    }

    override fun onViewAttachedToWindow(holder: BaseViewHolder) {
        super.onViewAttachedToWindow(holder)
        FullSpanUtil.onViewAttachedToWindow(holder, this, 1)
    }

}