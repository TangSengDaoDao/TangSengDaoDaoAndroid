package com.chat.groupmanage.ui

import android.text.TextUtils
import android.widget.TextView
import com.chat.base.base.WKBaseActivity
import com.chat.base.utils.HanziToPinyin
import com.chat.groupmanage.CommUtils
import com.chat.groupmanage.R
import com.chat.groupmanage.adapter.OutUserAdapter
import com.chat.groupmanage.databinding.ActOutGroupMemberLayoutBinding
import com.chat.groupmanage.entity.GroupMemberEntity
import com.xinbida.wukongim.WKIM
import com.xinbida.wukongim.entity.WKChannelType
import java.util.*

class OutGroupMembersActivity : WKBaseActivity<ActOutGroupMemberLayoutBinding>() {
    private var groupId: String = ""
    val adapter = OutUserAdapter()
    private var allList = ArrayList<GroupMemberEntity>()
    override fun getViewBinding(): ActOutGroupMemberLayoutBinding {
        return ActOutGroupMemberLayoutBinding.inflate(layoutInflater)
    }

    override fun setTitle(titleTv: TextView?) {
        titleTv!!.setText(R.string.exit_members)
    }

    override fun initPresenter() {
        groupId = intent.getStringExtra("groupId")!!
    }

    override fun initView() {
        initAdapter(wkVBinding.recyclerView, adapter)
    }

    override fun initListener() {
        val list = WKIM.getInstance().channelMembersManager.getDeletedMembers(
            groupId,
            WKChannelType.GROUP
        )
        val tempList = ArrayList<GroupMemberEntity>()
        for (item in list) {
            val entity = GroupMemberEntity(item)
            var showName = entity.channelMember.remark
            if (TextUtils.isEmpty(showName)) showName =
                if (TextUtils.isEmpty(entity.channelMember.memberRemark)) entity.channelMember.memberName else entity.channelMember.memberRemark
            if (!TextUtils.isEmpty(showName)) {
                if (CommUtils.getInstance().isStartNum(showName)) {
                    entity.pying = "#"
                } else entity.pying =
                    HanziToPinyin.getInstance().getPY(showName)
            } else entity.pying = "#"
            tempList.add(entity)
        }

        CommUtils.getInstance().sortListBasic1(tempList)
        val letterList: MutableList<GroupMemberEntity> = ArrayList()
        val numList: MutableList<GroupMemberEntity> = ArrayList()
        val otherList: MutableList<GroupMemberEntity> = ArrayList()
        for (groupItem in tempList) {
            val showName =
                if (TextUtils.isEmpty(groupItem.channelMember.memberRemark)) groupItem.channelMember.memberName else groupItem.channelMember.memberRemark
            if (CommUtils.getInstance().isStartLetter(groupItem.pying)) {
                //字母
                letterList.add(groupItem)
            } else if (CommUtils.getInstance().isStartNum(showName)) {
                //数字
                numList.add(groupItem)
            } else otherList.add(groupItem)
        }
        allList = ArrayList()
        allList.addAll(letterList)
        allList.addAll(numList)
        allList.addAll(otherList)
        adapter.setList(allList)
    }

}