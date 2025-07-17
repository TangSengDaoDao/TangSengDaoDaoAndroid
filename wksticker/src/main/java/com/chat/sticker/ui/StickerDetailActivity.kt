package com.chat.sticker.ui

import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.chat.base.base.WKBaseActivity
import com.chat.base.config.WKApiConfig
import com.chat.base.endpoint.EndpointCategory
import com.chat.base.endpoint.EndpointManager
import com.chat.base.endpoint.entity.StickerCategoryRefreshMenu
import com.chat.base.glide.GlideUtils
import com.chat.base.net.HttpResponseCode.success
import com.chat.base.ui.Theme
import com.chat.base.utils.AndroidUtilities
import com.chat.base.utils.singleclick.SingleClickUtil
import com.chat.sticker.R
import com.chat.sticker.databinding.ActStickerDetailLayoutBinding
import com.chat.sticker.entity.StickerCategory
import com.chat.sticker.entity.StickerDetail
import com.chat.sticker.msg.StickerFormat
import com.chat.sticker.service.StickerModel

/**
 * 2021/7/28 17:35
 * 表情详情
 */
class StickerDetailActivity : WKBaseActivity<ActStickerDetailLayoutBinding>() {
    private lateinit var category: String
    private lateinit var path: String
    override fun getViewBinding(): ActStickerDetailLayoutBinding {
        return ActStickerDetailLayoutBinding.inflate(layoutInflater)
    }

    override fun setTitle(titleTv: TextView?) {
        titleTv!!.text = ""
    }

    override fun initPresenter() {
        category = intent.getStringExtra("category")!!
        path = intent.getStringExtra("path")!!
        var width = intent.getIntExtra("width", 0)
        var height = intent.getIntExtra("height", 0)
        if (width == 0) width = 180
        if (height == 0) height = 180
        wkVBinding.stickerView.imageView.layoutParams.width = width
        wkVBinding.stickerView.imageView.layoutParams.height = height
    }

    override fun initView() {
        wkVBinding.addBtn.background.setTint(Theme.colorAccount)
        if (TextUtils.isEmpty(category) || TextUtils.isEmpty(title)) {
            wkVBinding.bottomView.visibility = View.GONE
            GlideUtils.getInstance().showGif(
                this@StickerDetailActivity,
                WKApiConfig.getShowUrl(path),
                wkVBinding.stickerView.imageView,
                null
            )
        } else {
            StickerModel().fetchStickerWithCategory(
                category,
                object : StickerModel.IStickerDetailListener {
                    override fun onResult(
                        code: Int,
                        msg: String,
                        stickerDetail: StickerDetail?
                    ) {
                        if (code.toShort() == success) {
                            wkVBinding.bottomView.visibility = View.VISIBLE
//                            GlideUtils.getInstance().showImg(
//                                this@StickerDetailActivity,
//                                WKApiConfig.getShowUrl(stickerDetail!!.cover),
//                                wkVBinding.categoryIv
//                            )
                            wkVBinding.categoryIv.showSticker(
                                stickerDetail!!.cover_lim,
                                "",
                                AndroidUtilities.dp(35f),
                                true
                            )

                            wkVBinding.nameTv.text = stickerDetail.title
                            if (stickerDetail.added) {
                                wkVBinding.addBtn.text = getString(R.string.str_sticker_added)
                                wkVBinding.addBtn.isEnabled = false
                                wkVBinding.addBtn.alpha = 0.2f
                            } else {
                                wkVBinding.addBtn.text = getString(R.string.str_sticker_add)
                                wkVBinding.addBtn.isEnabled = true
                                wkVBinding.addBtn.alpha = 1f
                            }
                            if (stickerDetail.list.isNotEmpty()) {
                                for (e in stickerDetail.list) {
                                    if (e.path == path) {
                                        if (!TextUtils.isEmpty(e.format) && e.format == StickerFormat.lim) {
                                            wkVBinding.stickerView.showSticker(
                                                e.path,
                                                e.placeholder,
                                                AndroidUtilities.dp(180f),
                                                true
                                            )
                                        } else {
                                            GlideUtils.getInstance().showGif(
                                                this@StickerDetailActivity,
                                                WKApiConfig.getShowUrl(e.path),
                                                wkVBinding.stickerView.imageView,
                                                null
                                            )
                                        }
                                        break
                                    }
                                }
                            }
                        } else {
                            wkVBinding.bottomView.visibility = View.GONE
                        }
                    }

                })
        }
    }

    override fun initListener() {
        SingleClickUtil.onSingleClick(wkVBinding.bottomView) {
            val intent =
                Intent(this@StickerDetailActivity, StickerStoreDetailActivity::class.java)
            intent.putExtra("category", category)
            startActivity(intent)
        }

        wkVBinding.addBtn.setOnClickListener {
            StickerModel().addStickerByCategory(category) { code, msg ->
                if (code == success.toInt()) {
                    wkVBinding.addBtn.alpha = 0.2f
                    wkVBinding.addBtn.isEnabled = false
                    wkVBinding.addBtn.setText(R.string.str_sticker_added)
                    StickerModel().fetchCategoryList(object :
                        StickerModel.IStickerCategoryListener {
                        override fun onResult(
                            code: Int,
                            msg: String,
                            list: List<StickerCategory>
                        ) {
                            if (code == success.toInt()) {
                                val menus: List<StickerCategoryRefreshMenu> =
                                    EndpointManager.getInstance().invokes(
                                        EndpointCategory.wkRefreshStickerCategory,
                                        null
                                    )
                                if (menus.isNotEmpty()) {
                                    for (menu: StickerCategoryRefreshMenu in menus) {
                                        menu.iRefreshCategory.onRefresh(category, true)
                                        menu.iRefreshCategory.onReset()
                                    }
                                }
                            }
                        }

                    })

                } else {
                    showToast(msg)
                }
            }

        }
    }
}