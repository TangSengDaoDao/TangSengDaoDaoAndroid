package com.chat.advanced.ui

import android.Manifest
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chat.advanced.R
import com.chat.advanced.databinding.ActChatBgListLayoutBinding
import com.chat.advanced.entity.ChatBgEntity
import com.chat.advanced.entity.ChatBgKeys
import com.chat.advanced.service.AdvancedModel
import com.chat.base.base.WKBaseActivity
import com.chat.base.config.WKSharedPreferencesUtil
import com.chat.base.glide.ChooseMimeType
import com.chat.base.glide.ChooseResult
import com.chat.base.glide.GlideUtils
import com.chat.base.glide.GlideUtils.ISelectBack
import com.chat.base.net.HttpResponseCode
import com.chat.base.ui.Theme
import com.chat.base.utils.AndroidUtilities
import com.chat.base.utils.WKPermissions
import com.chat.base.utils.WKPermissions.IPermissionResult
import com.chat.base.utils.singleclick.SingleClickUtil
import com.chat.base.views.FullyGridLayoutManager
import com.xinbida.wukongim.WKIM

class ChatBgListActivity : WKBaseActivity<ActChatBgListLayoutBinding>() {
    private lateinit var channelID: String
    private var channelType: Byte = 0

    private lateinit var adapter: ChatBgAdapter
    override fun getViewBinding(): ActChatBgListLayoutBinding {
        return ActChatBgListLayoutBinding.inflate(layoutInflater)
    }

    override fun setTitle(titleTv: TextView?) {
        titleTv!!.setText(R.string.chat_bg)
    }

    override fun initPresenter() {
        channelID = intent.getStringExtra("channelID").toString()
        channelType = intent.getByteExtra("channelType", 0)
    }

    override fun getRightTvText(textView: TextView?): String {
        return getString(R.string.gallery)
    }

    override fun rightLayoutClick() {
        chooseIMG()
    }

    override fun initView() {
        val showWidth = AndroidUtilities.getScreenWidth() / 3
        val showHeight = (AndroidUtilities.getScreenHeight() - AndroidUtilities.dp(160f)) / 3
        var url = ""
        if (TextUtils.isEmpty(channelID)) {
            url = WKSharedPreferencesUtil.getInstance().getSPWithUID(ChatBgKeys.chatBgUrl)
        } else {
            val channel =
                WKIM.getInstance().channelManager.getChannel(channelID, channelType)
            if (channel?.localExtra != null) {
                val urlObject = channel.localExtra[ChatBgKeys.chatBgUrl]
                if (urlObject != null && urlObject is String) {
                    url = urlObject
                }
            }
        }
        adapter = ChatBgAdapter(showWidth, showHeight, url, Theme.isDark())
        wkVBinding.recyclerView.adapter = adapter
        wkVBinding.recyclerView.layoutManager =
            FullyGridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        wkVBinding.recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                // super.getItemOffsets(outRect, view, parent, state)
                outRect.top = AndroidUtilities.dp(5f)
                outRect.bottom = AndroidUtilities.dp(5f)
                if (parent.getChildAdapterPosition(view) % 3 == 0) {
                    outRect.left = AndroidUtilities.dp(10f)
                    outRect.right = AndroidUtilities.dp(5f)
                } else if (parent.getChildAdapterPosition(view) % 3 == 1) {
                    outRect.left = AndroidUtilities.dp(5f)
                    outRect.right = AndroidUtilities.dp(5f)
                } else if (parent.getChildAdapterPosition(view) % 3 == 2) {
                    outRect.left = AndroidUtilities.dp(5f)
                    outRect.right = AndroidUtilities.dp(10f)
                }
            }
        })
    }

    override fun initListener() {
        WKIM.getInstance().channelManager.addOnRefreshChannelInfo(
            "chat_bg"
        ) { _, _ ->
            var url = ""
            if (TextUtils.isEmpty(channelID)) {
                url = WKSharedPreferencesUtil.getInstance()
                    .getSPWithUID(ChatBgKeys.chatBgUrl)
            } else {
                val channel =
                    WKIM.getInstance().channelManager.getChannel(
                        channelID,
                        channelType
                    )
                if (channel?.localExtra != null) {
                    val urlObject = channel.localExtra[ChatBgKeys.chatBgUrl]
                    if (urlObject != null && urlObject is String) {
                        url = urlObject
                    }
                }
            }
            adapter.setURL(url)
        }
        adapter.setOnItemClickListener { adapter, view, position ->
            SingleClickUtil.determineTriggerSingleClick(view) {
                val item = adapter.data[position] as ChatBgEntity
                val intent = Intent(this@ChatBgListActivity, PreviewChatBgActivity::class.java)
                intent.putExtra("channelID", channelID)
                intent.putExtra("channelType", channelType)
                intent.putExtra("url", item.url)
                intent.putExtra("isSvg", item.is_svg)
                if (item.is_svg == 1) {
                    val sp = StringBuffer()
                    for (s in item.light_colors) {
                        if (!TextUtils.isEmpty(sp)) sp.append(",")
                        sp.append(s)
                    }
                    intent.putExtra("lightColors", sp.toString())

                    val darkSP = StringBuilder()
                    for (s in item.dark_colors) {
                        if (!TextUtils.isEmpty(darkSP)) darkSP.append(",")
                        darkSP.append(s)
                    }
                    intent.putExtra("darkColors", darkSP.toString())
                }
                chooseResultLac.launch(intent)
            }
        }
    }

    override fun initData() {
        super.initData()
        AdvancedModel.instance.chatBGList(object : AdvancedModel.IChatBG {
            override fun onResult(list: List<ChatBgEntity>, code: Int, msg: String) {
                if (list.isNotEmpty()) {
                    val allList = ArrayList<ChatBgEntity>(list)
                    allList.add(0, ChatBgEntity())
                    adapter.setList(allList)
                }
                if (code != HttpResponseCode.success.toInt() && !TextUtils.isEmpty(msg)) {
                    showToast(msg)
                }
            }
        })
    }

    private var chooseResultLac = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            finish()
        }
    }


    private fun chooseIMG() {
        val desc =
            String.format(getString(R.string.file_permissions_des), getString(R.string.app_name))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            WKPermissions.getInstance().checkPermissions(
                object : IPermissionResult {
                    override fun onResult(result: Boolean) {
                        if (result) {
                            select()
                        }
                    }

                    override fun clickResult(isCancel: Boolean) {}
                },
                this,
                desc,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            WKPermissions.getInstance().checkPermissions(
                object : IPermissionResult {
                    override fun onResult(result: Boolean) {
                        if (result) {
                            select()
                        }
                    }

                    override fun clickResult(isCancel: Boolean) {}
                },
                this,
                desc,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun select() {

        GlideUtils.getInstance().chooseIMG(
            this@ChatBgListActivity,
            1,
            true,
            ChooseMimeType.img,
            false,
            object : ISelectBack {
                override fun onBack(paths: List<ChooseResult>) {
                    if (paths.isNotEmpty()) {
                        val path = paths[0].path
                        if (!TextUtils.isEmpty(path)) {
                            val intent = Intent(
                                this@ChatBgListActivity,
                                PreviewChatBgActivity::class.java
                            )
                            intent.putExtra("channelID", channelID)
                            intent.putExtra("channelType", channelType)
                            intent.putExtra("url", path)
                            intent.putExtra("isSvg", 0)
                            intent.putExtra("isLocal", 1)
                            chooseResultLac.launch(intent)
                        }
                    }
                }

                override fun onCancel() {}
            })

    }

    override fun finish() {
        super.finish()
        WKIM.getInstance().channelManager.removeRefreshChannelInfo("chat_bg")
    }
}