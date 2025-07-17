package com.chat.sticker.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chat.base.base.WKBaseActivity
import com.chat.base.endpoint.EndpointCategory
import com.chat.base.endpoint.EndpointManager
import com.chat.base.endpoint.entity.StickerCategoryRefreshMenu
import com.chat.base.net.HttpResponseCode
import com.chat.base.ui.Theme
import com.chat.base.utils.singleclick.SingleClickUtil
import com.chat.sticker.R
import com.chat.sticker.adapter.StickerManagerAdapter
import com.chat.sticker.databinding.ActMyStickerLayoutBinding
import com.chat.sticker.entity.StickerCategory
import com.chat.sticker.service.StickerModel

/**
 * 1/4/21 5:02 PM
 * 管理表情
 */
class StickerManagerActivity : WKBaseActivity<ActMyStickerLayoutBinding>() {
    private lateinit var adapter: StickerManagerAdapter
    override fun setTitle(titleTv: TextView?) {
        titleTv!!.setText(R.string.str_my_sticker)
    }

    override fun initPresenter() {
    }

    override fun initView() {
        adapter = StickerManagerAdapter()
        initAdapter(wkVBinding.recyclerView, adapter)
        adapter.addHeaderView(getHeader())
    }

    override fun getRightTvText(textView: TextView?): String {
        return getString(R.string.str_sticker_reorder)
    }

    override fun rightLayoutClick() {
        super.rightLayoutClick()
        val intent = Intent(this, StickerReorderActivity::class.java)
        startActivity(intent)
    }

    override fun initListener() {
        adapter.addChildClickViewIds(R.id.removeBtn)
        adapter.setOnItemChildClickListener { _, _, position ->
            run {
                val stickerCategory: StickerCategory = adapter.data[position]
                StickerModel().removeStickerWithCategory(stickerCategory.category) { code, msg ->
                    if (code == HttpResponseCode.success.toInt()) {
                        StickerModel().fetchCategoryList(object :
                            StickerModel.IStickerCategoryListener {
                            override fun onResult(
                                code: Int,
                                msg: String,
                                list: List<StickerCategory>
                            ) {
                                if (code == HttpResponseCode.success.toInt()) {
                                    val menus: List<StickerCategoryRefreshMenu> =
                                        EndpointManager.getInstance().invokes(
                                            EndpointCategory.wkRefreshStickerCategory,
                                            null
                                        )
                                    for (menu: StickerCategoryRefreshMenu in menus) {
                                        menu.iRefreshCategory.onRefresh(
                                            stickerCategory.category,
                                            false
                                        )
                                        menu.iRefreshCategory.onReset()
                                    }
                                }
                                adapter.removeAt(position + adapter.headerLayoutCount)
                            }

                        })


                    } else {
                        showToast(msg)
                    }
                }
            }
        }

        EndpointManager.getInstance()
            .setMethod("", EndpointCategory.wkRefreshStickerCategory) {
                StickerCategoryRefreshMenu(object :
                    StickerCategoryRefreshMenu.IRefreshCategory {
                    override fun onRefresh(category: String?, isAdd: Boolean) {
                    }

                    override fun onReset() {
                        initData()
                    }

                })

            }
    }

    override fun initData() {
        super.initData()

        StickerModel().fetchCategoryList(object : StickerModel.IStickerCategoryListener {
            override fun onResult(code: Int, msg: String, list: List<StickerCategory>) {
                if (code == HttpResponseCode.success.toInt()) {
                    adapter.setList(list)
                    if (list.isEmpty()) {
                        hideTitleRightView()
                    }
                } else {
                    showToast(msg)
                }
            }

        })
    }

    private fun getHeader(): View {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.sticker_manager_header, wkVBinding.recyclerView, false)
        val customView = view.findViewById<View>(R.id.customLayout)
        SingleClickUtil.onSingleClick(customView) {
            val intent =
                Intent(this@StickerManagerActivity, CustomStickerActivity::class.java)
            startActivity(intent)
        }
        val favoriteIV = view.findViewById<ImageView>(R.id.favoriteIV)
        Theme.setColorFilter(this@StickerManagerActivity, favoriteIV, R.color.popupTextColor)

        return view
    }

    override fun getViewBinding(): ActMyStickerLayoutBinding {
        return ActMyStickerLayoutBinding.inflate(layoutInflater)
    }

}