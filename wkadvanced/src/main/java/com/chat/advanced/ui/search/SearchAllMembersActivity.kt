package com.chat.advanced.ui.search

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chat.advanced.R
import com.chat.advanced.databinding.ActSearchAllMembersLayoutBinding
import com.chat.base.base.WKBaseActivity
import com.chat.base.utils.HanziToPinyin
import com.chat.base.utils.SoftKeyboardUtils
import com.chat.base.utils.singleclick.SingleClickUtil
import com.chat.base.views.FullyGridLayoutManager
import com.xinbida.wukongim.WKIM
import com.xinbida.wukongim.entity.WKChannelType
import java.util.*

class SearchAllMembersActivity : WKBaseActivity<ActSearchAllMembersLayoutBinding>() {
    var channelID: String = ""
    var channelType: Byte = 0
    lateinit var adapter: SearchAllMembersAdapter
    lateinit var allList: ArrayList<GroupMember>
    override fun getViewBinding(): ActSearchAllMembersLayoutBinding {
        return ActSearchAllMembersLayoutBinding.inflate(layoutInflater)
    }

    override fun setTitle(titleTv: TextView?) {
        titleTv!!.setText(R.string.search_with_member)
    }

    override fun initPresenter() {
        channelID = intent.getStringExtra("channelID").toString()
        channelType = intent.getByteExtra("channelType", WKChannelType.GROUP)
    }

    override fun initView() {
        wkVBinding.searchIv.colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(
                this, R.color.color999
            ), PorterDuff.Mode.MULTIPLY
        )
        adapter = SearchAllMembersAdapter()
    }

    override fun initListener() {
        wkVBinding.searchEt.imeOptions = EditorInfo.IME_ACTION_SEARCH
        wkVBinding.searchEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                SoftKeyboardUtils.getInstance().hideSoftKeyboard(this@SearchAllMembersActivity)
                return@setOnEditorActionListener true
            }
            false
        }
        wkVBinding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                val key = editable.toString()
                if (TextUtils.isEmpty(key)) {
                    adapter.setList(allList)
                } else {
                    searchMembers(key)
                }
            }
        })
        adapter.setOnItemClickListener { adapter1: BaseQuickAdapter<*, *>, view: View, position: Int ->
            SingleClickUtil.determineTriggerSingleClick(view) {
                val member: GroupMember? =
                    adapter1.getItem(position) as GroupMember?
                if (member != null) {
                    val intent =
                        Intent(
                            this@SearchAllMembersActivity,
                            ChatWithFromUIDActivity::class.java
                        )
                    intent.putExtra("channelID", channelID)
                    intent.putExtra("fromUID", member.member.memberUID)
                    startActivity(intent)
                }
            }
        }
    }

    override fun initData() {
        super.initData()
        val layoutManager = FullyGridLayoutManager(this, 5)
        wkVBinding.recyclerView.layoutManager = layoutManager
        wkVBinding.recyclerView.adapter = adapter

        allList = ArrayList()
        val list = WKIM.getInstance().channelMembersManager.getMembers(channelID, channelType)
        for (member in list) {
            val py: String = if (!TextUtils.isEmpty(member.memberName)) {
                if (isStartNum(member.memberName)) {
                    "#"
                } else HanziToPinyin.getInstance().getPY(member.memberName)
            } else "#"
            val entity = GroupMember()
            entity.pying = py
            entity.member = member
            allList.add(entity)
        }
        adapter.setList(allList)
    }


    private fun searchMembers(key: String) {
        val tempList: MutableList<GroupMember> =
            ArrayList()
        var i = 0
        val size = allList.size
        while (i < size) {
            if (!TextUtils.isEmpty(allList[i].member.memberRemark) && allList[i].member.memberRemark.lowercase(
                    Locale.getDefault()
                )
                    .contains(key.lowercase(Locale.getDefault()))
                || !TextUtils.isEmpty(allList[i].member.memberName) && allList[i].member.memberName.lowercase(
                    Locale.getDefault()
                )
                    .contains(key.lowercase(Locale.getDefault()))
                || !TextUtils.isEmpty(allList[i].pying) && allList[i].pying.lowercase(Locale.getDefault())
                    .contains(key.lowercase(Locale.getDefault()))
            ) {
                tempList.add(allList[i])
            }
            i++
        }
        adapter.setList(tempList)
    }

    private fun isStartNum(str: String): Boolean {
        val temp = str.substring(0, 1)
        return Character.isDigit(temp[0])
    }
}