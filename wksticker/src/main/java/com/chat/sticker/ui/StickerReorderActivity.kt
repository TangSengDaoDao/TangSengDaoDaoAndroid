package com.chat.sticker.ui

import android.animation.ValueAnimator
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.base.WKBaseActivity
import com.chat.base.endpoint.EndpointCategory
import com.chat.base.endpoint.EndpointManager
import com.chat.base.endpoint.entity.StickerCategoryRefreshMenu
import com.chat.base.net.HttpResponseCode
import com.chat.sticker.R
import com.chat.sticker.adapter.StickerReorderAdapter
import com.chat.sticker.databinding.ActStickerReorderLayoutBinding
import com.chat.sticker.entity.StickerCategory
import com.chat.sticker.service.StickerModel

/**
 * 1/4/21 6:00 PM
 * 重新排序
 */
class StickerReorderActivity : WKBaseActivity<ActStickerReorderLayoutBinding>() {
    private lateinit var adapter: StickerReorderAdapter
    override fun setTitle(titleTv: TextView?) {
        titleTv!!.setText(R.string.str_sticker_manager)
    }

    override fun getRightTvText(textView: TextView?): String {
        return getString(R.string.str_sticker_reorder_done)
    }

    override fun rightLayoutClick() {
        super.rightLayoutClick()
        val list = ArrayList<String>()
        for (item in adapter.data) {
            list.add(item.category)
        }
        loadingPopup.show()
        StickerModel().reorderCategory(list) { code, msg ->
            if (code == HttpResponseCode.success.toInt()) {
                StickerModel().fetchCategoryList(object : StickerModel.IStickerCategoryListener {
                    override fun onResult(code: Int, msg: String, list: List<StickerCategory>) {
                        loadingPopup.dismiss()
                        if (code == HttpResponseCode.success.toInt()) {
                            val menus: List<StickerCategoryRefreshMenu> = EndpointManager.getInstance().invokes(EndpointCategory.wkRefreshStickerCategory, null)
                            if (menus.isNotEmpty()) {
                                for (menu: StickerCategoryRefreshMenu in menus) {
                                    menu.iRefreshCategory.onReset()
                                }
                            }
                        } else showToast(msg)
                        finish()
                    }
                })

            } else {
                loadingPopup.dismiss()
                showToast(msg)
            }
        }
    }

    override fun initPresenter() {
    }

    override fun initView() {
        adapter = StickerReorderAdapter()
        initAdapter(wkVBinding.recyclerView, adapter)
    }

    override fun initListener() {
        val listener = object : OnItemDragListener {
            override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder, pos: Int) {

                val holder = viewHolder as BaseViewHolder
                // 开始时，item背景色变化，demo这里使用了一个动画渐变，使得自然
                val startColor = ContextCompat.getColor(this@StickerReorderActivity, R.color.white)
                val endColor = ContextCompat.getColor(this@StickerReorderActivity, R.color.colorF5F5F5)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    val v = ValueAnimator.ofArgb(startColor, endColor)
                    v.addUpdateListener { animation -> holder.itemView.setBackgroundColor(animation.animatedValue as Int) }
                    v.duration = 300
                    v.start()
                }
            }

            override fun onItemDragMoving(source: RecyclerView.ViewHolder, from: Int, target: RecyclerView.ViewHolder, to: Int) {}

            override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder, pos: Int) {
                val holder = viewHolder as BaseViewHolder
                // 结束时，item背景色变化，demo这里使用了一个动画渐变，使得自然
                val startColor = ContextCompat.getColor(this@StickerReorderActivity, R.color.colorF5F5F5)
                val endColor = ContextCompat.getColor(this@StickerReorderActivity, R.color.white)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    val v = ValueAnimator.ofArgb(startColor, endColor)
                    v.addUpdateListener { animation -> holder.itemView.setBackgroundColor(animation.animatedValue as Int) }
                    v.duration = 300
                    v.start()
                }

            }
        }
        adapter.draggableModule.isDragEnabled = true
        adapter.draggableModule.setOnItemDragListener(listener)
    }

    override fun initData() {
        super.initData()

        StickerModel().fetchCategoryList(object : StickerModel.IStickerCategoryListener {
            override fun onResult(code: Int, msg: String, list: List<StickerCategory>) {
                if (code == HttpResponseCode.success.toInt()) {
                    adapter.setList(list)

                } else {
                    showToast(msg)
                }
            }

        })
    }

    override fun getViewBinding(): ActStickerReorderLayoutBinding {
        return ActStickerReorderLayoutBinding.inflate(layoutInflater)
    }

}