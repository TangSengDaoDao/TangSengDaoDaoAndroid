package com.chat.groupmanage.adapter

import android.app.Activity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.WKBaseApplication
import com.chat.base.ui.components.AvatarView
import com.chat.base.ui.components.BackDrawable
import com.chat.base.ui.components.RoundLayout
import com.chat.base.utils.SoftKeyboardUtils
import com.chat.base.utils.WKTimeUtils
import com.chat.groupmanage.R
import com.chat.groupmanage.entity.GroupMemberEntity
import com.xinbida.wukongim.entity.WKChannelType
import kotlin.math.abs

class SelectedAdapter(iListener: IListener) :
    BaseMultiItemQuickAdapter<GroupMemberEntity, BaseViewHolder>() {
    private val iListener: IListener
    private var colors: IntArray

    init {
        this.iListener = iListener
        colors =
            WKBaseApplication.getInstance().context.resources.getIntArray(R.array.name_colors)

        addItemType(1, R.layout.item_user_selected)
        addItemType(0, R.layout.item_search_layout)
    }

    override fun convert(holder: BaseViewHolder, item: GroupMemberEntity, payloads: List<Any>) {
        super.convert(holder, item, payloads)
        val roundLayout = holder.getView<RoundLayout>(R.id.roundLayout)
        roundLayout.setBgColor(ContextCompat.getColor(context, R.color.red))
        val avatarView = holder.getView<AvatarView>(R.id.avatarView)
        if (item.isChecked) {
            val drawable = BackDrawable(true)
            drawable.setColor(ContextCompat.getColor(context, R.color.white))
            val rotate = RotateAnimation(
                0f,
                90f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
            avatarView.setStrokeWidth(0f)
            rotate.interpolator = LinearInterpolator()
            rotate.duration = 150
            rotate.repeatCount = 0
            rotate.fillAfter = true
            rotate.startOffset = 10
            avatarView.imageView.animation = rotate
            rotate.start()
            avatarView.imageView.setImageDrawable(drawable)
        } else {
            val index: Int = abs(item.channelMember.memberUID.hashCode()) % colors.size
            roundLayout.setBgColor(colors[index])
        }
    }

    override fun convert(holder: BaseViewHolder, item: GroupMemberEntity) {
        if (item.itemType == 1) {
            val name: String = if (TextUtils.isEmpty(item.channelMember.memberRemark)) {
                item.channelMember.memberName
            } else {
                item.channelMember.memberRemark
            }
            holder.setText(R.id.nameTv, name)
            val avatarView = holder.getView<AvatarView>(R.id.avatarView)
            avatarView.setSize(25f)
            avatarView.setStrokeWidth(0f)
            avatarView.showAvatar(
                item.channelMember.memberUID,
                WKChannelType.PERSONAL
            )
            val roundLayout = holder.getView<RoundLayout>(R.id.roundLayout)
            val index: Int = abs(item.channelMember.memberUID.hashCode()) % colors.size
            roundLayout.setBgColor(colors[index])
        } else {
            val editText = holder.getView<EditText>(R.id.searchEt)
            addListener(editText)
        }
    }

    private var lastTime = 0L
    private fun addListener(editText: EditText) {
        editText.imeOptions = EditorInfo.IME_ACTION_SEARCH
        editText.setOnKeyListener { _: View?, _: Int, keyEvent: KeyEvent ->
            val nowTime = WKTimeUtils.getInstance().currentMills
            if (nowTime - lastTime < 300) return@setOnKeyListener false
            lastTime = WKTimeUtils.getInstance().currentMills
            if (keyEvent.keyCode == KeyEvent.KEYCODE_DEL && keyEvent.action == KeyEvent.ACTION_DOWN) {
                val content = editText.text.toString()
                if (TextUtils.isEmpty(content) && data.size > 1) {
                    val uiEntity: GroupMemberEntity = data[data.size - 2]
                    iListener.onDelete(uiEntity.channelMember.memberUID)
                    removeAt(data.size - 2)
                    return@setOnKeyListener true
                } else return@setOnKeyListener false
            }
            false
        }
        editText.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                SoftKeyboardUtils.getInstance().hideSoftKeyboard(context as Activity)
                return@setOnEditorActionListener true
            }
            false
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                iListener.onSearch(editable.toString())
            }
        })
    }

    interface IListener {
        fun onSearch(key: String)
        fun onDelete(uid: String)
    }
}