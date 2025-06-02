package com.chat.groupmanage.adapter

import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.msgitem.WKChannelMemberRole
import com.chat.base.ui.Theme
import com.chat.base.ui.components.AvatarView
import com.chat.base.ui.components.CheckBox
import com.chat.base.ui.components.RoundTextView
import com.chat.base.utils.AndroidUtilities
import com.chat.base.utils.StringUtils
import com.chat.groupmanage.R
import com.chat.groupmanage.entity.GroupMemberEntity
import com.xinbida.wukongim.entity.WKChannelType

class ChooseNormalMemberAdapter(
    val list: List<GroupMemberEntity>,
    var type: Int,
    private val iItemCheck: IItemCheck
) :
    BaseQuickAdapter<GroupMemberEntity, BaseViewHolder>(R.layout.item_choose_normal_member_layout) {
    init {
        setList(list)
    }

    var searchKey: String? = null
    override fun convert(holder: BaseViewHolder, item: GroupMemberEntity, payloads: List<Any>) {
        super.convert(holder, item, payloads)
        val groupMemberEntity = payloads[0] as GroupMemberEntity
        val checkBox = holder.getView<CheckBox>(R.id.checkBox)
        checkBox.setDrawBackground(groupMemberEntity.isChecked)
        checkBox.setChecked(groupMemberEntity.isChecked, true)
    }

    override fun convert(holder: BaseViewHolder, item: GroupMemberEntity) {
        val checkBox: CheckBox = holder.getView(R.id.checkBox)
        checkBox.setChecked(item.isChecked, true)
        checkBox.setResId(context, R.mipmap.round_check2)
        checkBox.setDrawBackground(item.isChecked)
        checkBox.setHasBorder(true)
        checkBox.setBorderColor(ContextCompat.getColor(context, R.color.layoutColor))
        checkBox.setSize(24)
        checkBox.setStrokeWidth(AndroidUtilities.dp(2f))
        checkBox.setColor(Theme.colorAccount, ContextCompat.getColor(context, R.color.white))
        checkBox.visibility = View.VISIBLE
        checkBox.isEnabled = true
        checkBox.setChecked(item.isChecked, true)


        var showName = ""
        if (item.channelMember != null && !TextUtils.isEmpty(item.channelMember.remark)) {
            showName = item.channelMember.remark
        }
        if (TextUtils.isEmpty(showName)) {
            showName =
                if (TextUtils.isEmpty(item.channelMember.memberRemark)) item.channelMember.memberName else item.channelMember.memberRemark
        }
        if (!TextUtils.isEmpty(searchKey)) {
            holder.setText(
                R.id.nameTv, StringUtils.findSearch(
                    Theme.colorAccount,
                    showName,
                    searchKey
                )
            )
        } else {
            holder.setText(R.id.nameTv, showName)
        }
        val avatarView: AvatarView = holder.getView(R.id.avatarView)
        avatarView.showAvatar(
            item.channelMember.memberUID,
            WKChannelType.PERSONAL,
            item.channelMember.memberAvatar
        )
        checkBox.setOnClickListener {
            item.isChecked = !item.isChecked
            notifyItemChanged(holder.bindingAdapterPosition)
            iItemCheck.onCheck()
        }

        checkBox.visibility = if (type == 2) View.GONE else View.VISIBLE
        val roleTv: RoundTextView = holder.getView(R.id.roleTv)
        when (item.channelMember.role) {
            WKChannelMemberRole.admin -> {
                holder.setText(R.id.roleTv, R.string.group_admin)
                roleTv.setBackGroundColor(ContextCompat.getColor(context, R.color.colorFFC107))
                holder.setGone(R.id.roleTv, false)
            }
            WKChannelMemberRole.manager -> {
                holder.setText(R.id.roleTv, R.string.group_manager)
                holder.setGone(R.id.roleTv, false)
                roleTv.setBackGroundColor(Theme.colorAccount)
            }
            else -> {
                holder.setGone(R.id.roleTv, true)
            }
        }
    }


    interface IItemCheck {
        fun onCheck()
    }

    fun setSearch(searchKey: String) {
        this.searchKey = searchKey
        notifyItemRangeChanged(0, data.size)
    }
}