package com.chat.sticker.ui

import android.content.Intent
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
import com.chat.sticker.adapter.StickerStoreAdapter
import com.chat.sticker.databinding.ActStickerStoreLayoutBinding
import com.chat.sticker.entity.Sticker
import com.chat.sticker.entity.StickerCategory
import com.chat.sticker.entity.StickerDetail
import com.chat.sticker.entity.StoreEntity
import com.chat.sticker.service.StickerContact
import com.chat.sticker.service.StickerModel
import com.chat.sticker.service.StickerPresenter
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

/**
 * 12/30/20 3:47 PM
 * 表情商店
 */
class StickerStoreActivity : WKBaseActivity<ActStickerStoreLayoutBinding>(),
    StickerContact.StickerView {
    override fun setUserCustomSticker(list: List<Sticker>) {

    }

    override fun setCategorySticker(list: List<Sticker>) {

    }

    private var page: Int = 1
    private lateinit var adapter: StickerStoreAdapter
    lateinit var presenter: StickerPresenter

    override fun setTitle(titleTv: TextView?) {
        titleTv!!.setText(R.string.str_sticker_store)
    }

    override fun getRightIvResourceId(imageView: ImageView?): Int {
        return R.mipmap.smiles_tab_settings
    }

    override fun rightLayoutClick() {
        super.rightLayoutClick()
        val intent = Intent(this, StickerManagerActivity::class.java)
        startActivity(intent)
    }

    override fun initPresenter() {
        presenter = StickerPresenter(this)
    }

    override fun initView() {
        adapter = StickerStoreAdapter()
        initAdapter(wkVBinding.recyclerView, adapter)
    }

    override fun initListener() {
        wkVBinding.spinKit.setColor(Theme.colorAccount)
        adapter.addChildClickViewIds(R.id.addBtn)
        adapter.setOnItemChildClickListener { _, _, position ->
            run {
                val store: StoreEntity = adapter.getItem(position)
                if (store.status == 1) {
                    // remove
                    StickerModel().removeStickerWithCategory(store.category) { code, msg ->
                        if (code == HttpResponseCode.success.toInt()) {
                            StickerModel().fetchCategoryList(object :
                                StickerModel.IStickerCategoryListener {
                                override fun onResult(
                                    code: Int, msg: String, list: List<StickerCategory>
                                ) {
                                    if (code == HttpResponseCode.success.toInt()) {
                                        val menus: List<StickerCategoryRefreshMenu> =
                                            EndpointManager.getInstance().invokes(
                                                EndpointCategory.wkRefreshStickerCategory, null
                                            )
                                        for (menu: StickerCategoryRefreshMenu in menus) {
                                            menu.iRefreshCategory.onRefresh(
                                                store.category, false
                                            )
                                            menu.iRefreshCategory.onReset()
                                        }
                                    }
                                    adapter.data[position].status = 0
                                    adapter.notifyItemChanged(position)
                                }

                            })
                        } else {
                            showToast(msg)
                        }
                    }
                } else {
                    StickerModel().addStickerByCategory(store.category) { code, msg ->
                        if (code == HttpResponseCode.success.toInt()) {
                            StickerModel().fetchStickerWithCategory(
                                store.category,
                                object : StickerModel.IStickerDetailListener {
                                    override fun onResult(
                                        code: Int, msg: String, stickerDetail: StickerDetail?
                                    ) {
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
                                                            EndpointManager.getInstance()
                                                                .invokes(
                                                                    EndpointCategory.wkRefreshStickerCategory,
                                                                    null
                                                                )
                                                        if (menus.isNotEmpty()) {
                                                            for (menu: StickerCategoryRefreshMenu in menus) {
                                                                menu.iRefreshCategory.onRefresh(
                                                                    store.category, true
                                                                )
                                                                menu.iRefreshCategory.onReset()
                                                            }
                                                        }
                                                    }
                                                }

                                            })
                                        }
                                    }
                                })

                        } else showError(msg)
                    }
                }
            }
        }
        adapter.setOnItemClickListener { _, view1, position ->
            SingleClickUtil.determineTriggerSingleClick(view1) {

                val store: StoreEntity = adapter.getItem(position)
                val intent = Intent(this, StickerStoreDetailActivity::class.java)
                intent.putExtra("status", store.status)
                intent.putExtra("category", store.category)
                intent.putExtra("title", store.title)
                intent.putExtra("desc", store.desc)
                startActivity(intent)
            }
        }
        wkVBinding.refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                presenter.storeList(page)
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                page = 1
                presenter.storeList(page)
            }

        })
        presenter.storeList(page)

        EndpointManager.getInstance()
            .setMethod("", EndpointCategory.wkRefreshStickerCategory) {
                StickerCategoryRefreshMenu(object :
                    StickerCategoryRefreshMenu.IRefreshCategory {
                    override fun onRefresh(category: String?, isAdd: Boolean) {
                        for (i in adapter.data.indices) {
                            if (adapter.data[i].category == category) {
                                if (isAdd) {
                                    adapter.data[i].status = 1
                                } else adapter.data[i].status = 0
                                adapter.notifyItemChanged(i)
                                break
                            }
                        }
                    }

                    override fun onReset() {
                    }

                })
            }
    }

    override fun setStoreList(list: List<StoreEntity>) {
        wkVBinding.refreshLayout.finishLoadMore()
        wkVBinding.refreshLayout.finishRefresh()
        if (list.isNotEmpty()) {
            if (page == 1) {
                adapter.setList(list)
            } else {
                adapter.addData(list)
            }
            page++
        } else {
            if (page != 1) {
                wkVBinding.refreshLayout.finishLoadMoreWithNoMoreData()
            }
        }
    }

    override fun showError(msg: String?) {
        showToast(msg)
    }

    override fun hideLoading() {
        wkVBinding.refreshLayout.finishLoadMore()
        wkVBinding.refreshLayout.finishRefresh()
    }

    override fun getViewBinding(): ActStickerStoreLayoutBinding {
        return ActStickerStoreLayoutBinding.inflate(layoutInflater)
    }
}