package com.chat.sticker.ui

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.chat.base.base.WKBaseActivity
import com.chat.base.config.WKApiConfig
import com.chat.base.emoji.MoonUtil
import com.chat.base.endpoint.EndpointCategory
import com.chat.base.endpoint.EndpointManager
import com.chat.base.endpoint.entity.StickerCategoryRefreshMenu
import com.chat.base.glide.GlideRequestOptions
import com.chat.base.net.HttpResponseCode
import com.chat.base.ui.Theme
import com.chat.base.utils.AndroidUtilities
import com.chat.base.utils.LayoutHelper
import com.chat.sticker.R
import com.chat.sticker.adapter.StickerAdapter
import com.chat.sticker.databinding.ActStickerStoreDetailLayoutBinding
import com.chat.sticker.entity.Sticker
import com.chat.sticker.entity.StickerCategory
import com.chat.sticker.entity.StickerDetail
import com.chat.sticker.entity.StickerUI
import com.chat.sticker.service.StickerModel
import com.chat.sticker.touch.OnMovePreviewListener
import com.chat.sticker.touch.SimpleMovePreviewListener
import com.chat.sticker.ui.components.StickerView
import java.io.File

/**
 * 1/4/21 3:00 PM
 * 商店表情详情
 */

class StickerStoreDetailActivity : WKBaseActivity<ActStickerStoreDetailLayoutBinding>() {
    private lateinit var category: String
    private lateinit var adapter: StickerAdapter
    private lateinit var titleTv: TextView

    override fun setTitle(titleTv: TextView?) {
        this.titleTv = titleTv!!
    }

    override fun initPresenter() {
        category = intent.getStringExtra("category")!!
    }

    override fun initView() {
        wkVBinding.addBtn.background.setTint(Theme.colorAccount)
        val width = (AndroidUtilities.getScreenWidth() - AndroidUtilities
            .dp(30f)) / 5
        adapter = StickerAdapter(width)
        wkVBinding.recyclerView.layoutManager =
            StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL)
        wkVBinding.recyclerView.adapter = adapter
    }

    override fun initListener() {
        wkVBinding.recyclerView.addOnItemTouchListener(
            SimpleMovePreviewListener(
                wkVBinding.recyclerView,
                object : OnMovePreviewListener {
                    override fun onPreview(childView: View, childPosition: Int) {
                        val sticker: Sticker = adapter.data[childPosition]
                        val stickerUI = StickerUI(1, "", category, sticker)
                        if (stickerUI.itemType != 0) showPreviewAlert(
                            this@StickerStoreDetailActivity,
                            stickerUI
                        )
                        childView.performHapticFeedback(
                            HapticFeedbackConstants.KEYBOARD_TAP,
                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                        )
                    }

                    override fun onCancelPreview() {
                        if (linearLayout != null) {
                            val mRootView = findViewById<ViewGroup>(android.R.id.content)
                            mRootView.removeView(linearLayout)
                            linearLayout = null
                            textView = null
                        }
                    }
                })
        )
        wkVBinding.addBtn.setOnClickListener {
            StickerModel().addStickerByCategory(category) { code, msg ->
                if (code == HttpResponseCode.success.toInt()) {
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
                            if (code == HttpResponseCode.success.toInt()) {
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

    override fun initData() {
        super.initData()
        StickerModel().fetchStickerWithCategory(
            category,
            object : StickerModel.IStickerDetailListener {
                override fun onResult(code: Int, msg: String, stickerDetail: StickerDetail?) {
                    if (code == HttpResponseCode.success.toInt()) {
                        adapter.setList(stickerDetail!!.list)
                        wkVBinding.titleTv.text = stickerDetail.title
                        wkVBinding.descTv.text = stickerDetail.desc
                        titleTv.text = stickerDetail.title
                        if (!stickerDetail.added) {
                            wkVBinding.addBtn.setText(R.string.str_sticker_add)
                            wkVBinding.addBtn.alpha = 1f
                            wkVBinding.addBtn.isEnabled = true
                        } else {
                            wkVBinding.addBtn.alpha = 0.2f
                            wkVBinding.addBtn.isEnabled = false
                            wkVBinding.addBtn.setText(R.string.str_sticker_added)
                        }
                    }
                }
            })
    }

    override fun getViewBinding(): ActStickerStoreDetailLayoutBinding {
        return ActStickerStoreDetailLayoutBinding.inflate(layoutInflater)
    }

    var linearLayout: FrameLayout? = null
    var textView: TextView? = null

    private fun showPreviewAlert(context: Context, stickerUI: StickerUI) {
        val mRootView = (context as Activity).findViewById<ViewGroup>(android.R.id.content)
        var isAdd = false
        if (linearLayout == null) {
            isAdd = true
            linearLayout = FrameLayout(context)
        }
        var searchableWord = ""
        val stickerView =
            StickerView(context)
        if (stickerUI.itemType == 1) {
            if (stickerUI.sticker != null) {
                searchableWord = stickerUI.sticker!!.searchable_word
                stickerView.showSticker(
                    stickerUI.sticker!!.path,
                    stickerUI.sticker!!.placeholder,
                    500,
                    true
                )
            }
        } else if (stickerUI.itemType == 2) {
            if (stickerUI.sticker != null) {
                val localPath = StickerModel().getLocalPath(stickerUI.sticker!!.path)
                val file = File(localPath)
                if (file.exists()) {
                    Glide.with(context).asGif().load(file)
                        .apply(GlideRequestOptions.getInstance().normalRequestOption())
                        .into(stickerView.imageView)
                } else {
                    Glide.with(context).asGif()
                        .load(WKApiConfig.getShowUrl(stickerUI.sticker!!.path))
                        .apply(GlideRequestOptions.getInstance().normalRequestOption())
                        .into(stickerView.imageView)
                }
                stickerView.imageView.layoutParams.width = AndroidUtilities.dp(150f)
                stickerView.imageView.layoutParams.height = AndroidUtilities.dp(150f)
            }
        }
        linearLayout!!.removeAllViews()
        if (textView == null) {
            textView = TextView(context)
            textView!!.setTextColor(ContextCompat.getColor(this, R.color.colorDark))
        }
        if (!TextUtils.isEmpty(searchableWord)) {
            MoonUtil.identifyFaceExpression(
                context,
                textView,
                searchableWord,
                MoonUtil.DEF_SCALE
            )
        } else {
            textView!!.text = searchableWord
        }
        linearLayout!!.setBackgroundColor(0x50eeeeee)
        linearLayout!!.addView(
            stickerView,
            LayoutHelper.createFrame(
                LayoutHelper.WRAP_CONTENT,
                LayoutHelper.WRAP_CONTENT.toFloat(),
                Gravity.CENTER_HORIZONTAL,
                0f,
                100f,
                0f,
                0f
            )
        )
        linearLayout!!.addView(
            textView,
            LayoutHelper.createFrame(
                LayoutHelper.WRAP_CONTENT,
                LayoutHelper.WRAP_CONTENT.toFloat(),
                Gravity.CENTER_HORIZONTAL,
                0f,
                280f,
                0f,
                0f
            )
        )
        linearLayout!!.setOnClickListener { view: View? ->
            mRootView.removeView(linearLayout)
            textView = null
            linearLayout = null
        }
        if (isAdd) mRootView.addView(
            linearLayout,
            LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT.toFloat())
        )
    }
}