package com.chat.file

import android.text.TextUtils
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.utils.WKTimeUtils
import com.chat.base.utils.StringUtils
import com.chat.file.msgitem.FileContent
import java.util.*

class FileAdapter :BaseQuickAdapter<ChooseFileEntity,BaseViewHolder>(R.layout.item_choose_file) {

    override fun convert(holder: BaseViewHolder, item: ChooseFileEntity) {
        val checkBox: CheckBox = holder.getView(R.id.checkBox)
        val mFileContent = item.msg.baseContentMsgModel as FileContent
        holder.setText(R.id.nameTv, mFileContent.name)
        val typeTv: TextView = holder.getView(R.id.typeTv)
        val sizeTv: TextView = holder.getView(R.id.sizeTv)
        val timeTv: TextView = holder.getView(R.id.timeTv)
        if (mFileContent.name.contains(".")) {
            val type = mFileContent.name.substring(mFileContent.name.lastIndexOf(".") + 1)
            if (!TextUtils.isEmpty(type)) typeTv.text =
                type.uppercase(Locale.getDefault()) else typeTv.setText(R.string.unknown_file)
        } else typeTv.setText(R.string.unknown_file)

        sizeTv.text = StringUtils.sizeFormatNum2String(mFileContent.size)
        checkBox.isChecked = item.checked
        timeTv.text =
            WKTimeUtils.getInstance().getTimeString(item.msg.timestamp * 1000)
        checkBox.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (compoundButton.isPressed) {
                var index = -1
                var i = 0
                val size = data.size
                while (i < size) {
                    if (data[i].checked) {
                        data[i].checked = false
                        index = i
                        break
                    }
                    i++
                }
                item.checked = b
                notifyItemChanged(holder.bindingAdapterPosition)
                if (index != -1) notifyItemChanged(index)
            }
        }
    }
}