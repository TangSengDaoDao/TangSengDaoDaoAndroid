package com.chat.sticker.ui

import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.chat.base.base.WKBaseActivity
import com.chat.base.endpoint.EndpointManager
import com.chat.base.net.HttpResponseCode
import com.chat.base.utils.AndroidUtilities
import com.chat.base.utils.WKDialogUtils
import com.chat.base.utils.WKReader
import com.chat.sticker.R
import com.chat.sticker.adapter.AddCustomStickerAdapter
import com.chat.sticker.databinding.ActCustomStickerLayoutBinding
import com.chat.sticker.entity.Sticker
import com.chat.sticker.msg.StickerFormat
import com.chat.sticker.service.StickerModel
import java.io.File

/**
 * 1/3/21 7:44 PM
 * 自定义表情
 */
class CustomStickerActivity : WKBaseActivity<ActCustomStickerLayoutBinding>() {
    private lateinit var adapter: AddCustomStickerAdapter
    private lateinit var titleTv: TextView
    private lateinit var rightTv: TextView
    private var isManage = false
    private var isEnable = false

    override fun setTitle(titleTv: TextView?) {
        this.titleTv = titleTv!!
    }

    override fun initPresenter() {
    }

    override fun getRightTvText(textView: TextView?): String {
        rightTv = textView!!
        return getString(R.string.str_sticker_arrangement)
    }

    override fun rightLayoutClick() {
        super.rightLayoutClick()
        if (adapter.data.size == 1) return
        for (item in adapter.data) {
            item.showManager = !isManage
        }
        isManage = !isManage
        adapter.notifyItemRangeChanged(0, adapter.data.size)
        updateBottomViewStatus()
    }

    private fun updateBottomViewStatus() {
        if (isManage) {
            adapter.removeAt(0)
            rightTv.setText(R.string.str_sticker_reorder_done)
            wkVBinding.bottomView.visibility = View.VISIBLE
        } else {
            val sticker = Sticker()
            sticker.path = "addCustom"
            adapter.addData(0, sticker)
            rightTv.setText(R.string.str_sticker_arrangement)
            wkVBinding.bottomView.visibility = View.GONE
        }
        checkBottomViewStatus()
        val count: Int = if (adapter.data[0].path == "addCustom") {
            adapter.data.size - 1
        } else {
            adapter.data.size
        }
        titleTv.text = String.format("%s(%d)", getString(R.string.str_add_custom_sticker), count)
    }

    override fun initView() {
        val width = (AndroidUtilities.getScreenWidth() - AndroidUtilities
            .dp(30f)) / 5
        adapter = AddCustomStickerAdapter(width)
        wkVBinding.recyclerView.layoutManager =
            StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL)
        wkVBinding.recyclerView.adapter = adapter
    }

    override fun initListener() {
        adapter.addChildClickViewIds(R.id.contentLayout)
        adapter.setOnItemChildClickListener { _, _, position ->
            run {
                val sticker: Sticker = adapter.data[position]
                if (sticker.path == "addCustom") {
                    val intent = Intent(this, AddCustomStickerActivity::class.java)
                    myActivityLauncher.launch(intent)
                } else {
                    if (isManage) {
                        sticker.isSelected = !sticker.isSelected
                        adapter.notifyItemChanged(position, sticker)
                        checkBottomViewStatus()
                    }
                }
            }
        }

        wkVBinding.removeToTopTv.setOnClickListener {
            val paths = ArrayList<String>()
            for (item in adapter.data) {
                if (item.isSelected) {
                    paths.add(item.path)
                }
            }
            StickerModel().moveToFront(paths) { code, msg ->
                if (code == HttpResponseCode.success.toInt()) {
                    val tempList = ArrayList<Sticker>()
                    for (j in paths.indices) {
                        for (i in adapter.data.indices) {
                            if (adapter.data[i].path == paths[j]) {
                                adapter.data[i].isSelected = false
                                tempList.add(adapter.data[i])
                                adapter.removeAt(i)
                                break
                            }
                        }
                    }
                    adapter.addData(0, tempList)
                    //刷新顺序
                    EndpointManager.getInstance().invoke("refresh_custom_sticker", null)
                    checkBottomViewStatus()
                } else showToast(msg)
            }
        }
        wkVBinding.deleteTv.setOnClickListener {
            if (isEnable) {
                WKDialogUtils.getInstance().showDialog(
                    this@CustomStickerActivity,
                    getString(R.string.str_sticker_delete),
                    getString(R.string.str_delete_sticker_desc),
                    true,
                    "",
                    getString(R.string.base_delete),
                    0,
                    ContextCompat.getColor(this@CustomStickerActivity, R.color.red)
                ) { index ->
                    if (index == 1) {

                        val paths = ArrayList<String>()
                        for (item in adapter.data) {
                            if (item.isSelected) {
                                paths.add(item.path)
                            }
                        }
                        StickerModel().deleteSticker(paths) { code, msg ->
                            if (code == HttpResponseCode.success.toInt()) {
                                isManage = false

                                val tempList = ArrayList<Sticker>()
                                for (item in adapter.data) {
                                    if (!item.isSelected) {
                                        item.showManager = false
                                        tempList.add(item)
                                    }
                                }
                                adapter.setList(tempList)
                                updateBottomViewStatus()
                                //重置表情
                                EndpointManager.getInstance()
                                    .invoke("refresh_custom_sticker", null)
                                //                                StickerModel().fetchUserCustomSticker(object :
                                //                                    StickerModel.IStickersListener {
                                //                                    override fun onResult(
                                //                                        code: Int,
                                //                                        msg: String,
                                //                                        list: List<Sticker>
                                //                                    ) {
                                //                                        if (code == HttpResponseCode.success.toInt()) {
                                //                                            //刷新表情
                                //                                            EndpointManager.getInstance()
                                //                                                .invoke("refresh_custom_sticker", null)
                                //                                        } else {
                                //                                            showToast(msg)
                                //                                        }
                                //                                    }
                                //                                })
                            } else showToast(msg)
                        }

                    }
                }
            }
        }
    }

    override fun initData() {
        super.initData()
        getStickers()
    }

    private fun getStickers() {
        StickerModel().getUserCustomSticker(object : StickerModel.IStickersListener {
            override fun onResult(code: Int, msg: String, list: List<Sticker>) {
                if (code == HttpResponseCode.success.toInt()) {

                    for (item in list) {
                        if (TextUtils.isEmpty(item.format) || item.format != StickerFormat.lim) {
                            val localPath = StickerModel().getLocalPath(item.path)
                            val file = File(localPath)
                            if (file.exists()) {
                                item.localPath = localPath
                            }
                        }
                    }
                    adapter.setList(list)
                }
                if (WKReader.isEmpty(list) || list.size == 1) {
                    wkVBinding.removeToTopTv.isClickable = false
                    wkVBinding.removeToTopTv.alpha = 0.2f
                }
                val sticker = Sticker()
                sticker.path = "addCustom"
                adapter.addData(0, sticker)
                titleTv.text =
                    String.format("%s(%d)", getString(R.string.str_add_custom_sticker), list.size)

                if (adapter.data.size == 1) {
                    hideTitleRightView()
                }
            }

        })
    }

    private fun checkBottomViewStatus() {
        isEnable = false
        var count = 0
        for (item in adapter.data) {
            if (item.isSelected) {
                isEnable = true
                count += 1
                if (count > 1) break
            }
        }
        wkVBinding.removeToTopTv.isClickable = isEnable
        wkVBinding.deleteTv.isClickable = isEnable
        if (isEnable) {
            wkVBinding.removeToTopTv.alpha = if (count >= 1) 1f else 0.2f
            wkVBinding.deleteTv.alpha = 1f
        } else {
            wkVBinding.removeToTopTv.alpha = 0.2f
            wkVBinding.deleteTv.alpha = 0.2f
        }
    }

    override fun getViewBinding(): ActCustomStickerLayoutBinding {
        return ActCustomStickerLayoutBinding.inflate(layoutInflater)
    }

    private val myActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                getStickers()
            }
        }
//     ActivityResultLauncher<Intent> chooseCardResultLac = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
//
//        @Override
//        public void onActivityResult(ActivityResult result) {
//            if (result.getResultCode() == RESULT_OK) {
//                finish();
//            }
//        }
//    });

}