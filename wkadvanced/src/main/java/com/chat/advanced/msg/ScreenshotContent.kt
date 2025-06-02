package com.chat.advanced.msg

import android.text.TextUtils
import com.chat.advanced.R
import com.chat.base.WKBaseApplication
import com.chat.base.config.WKConfig
import com.chat.base.msgitem.WKContentType
import com.chat.base.utils.WKLogUtils
import com.xinbida.wukongim.WKIM
import com.xinbida.wukongim.entity.WKChannelType
import com.xinbida.wukongim.msgmodel.WKMessageContent
import org.json.JSONException
import org.json.JSONObject

class ScreenshotContent : WKMessageContent() {
    var fromname: String = ""
    var fromuid: String = ""

    init {
        type = WKContentType.screenshot
    }

    override fun encodeMsg(): JSONObject {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("from_uid", fromuid)
            jsonObject.put("from_name", fromname)
        } catch (e: JSONException) {
            WKLogUtils.e("构建截屏消息错误")
        }
        return jsonObject
    }

    override fun decodeMsg(jsonObject: JSONObject): WKMessageContent {
        fromname = jsonObject.optString("from_name")
        fromuid = jsonObject.optString("from_uid")
        return this
    }

    override fun getDisplayContent(): String {

        return if (!TextUtils.isEmpty(this.fromuid)) {
            if (fromuid == WKConfig.getInstance().uid) {
                WKBaseApplication.getInstance().context.getString(R.string.screenshort_inchat_my)
            } else {
                var showName = fromname
                val channel = WKIM.getInstance().channelManager.getChannel(
                    fromuid,
                    WKChannelType.PERSONAL
                )
                if (channel != null) {
                    showName =
                        if (TextUtils.isEmpty(channel.channelRemark)) channel.channelName else channel.channelRemark
                }
                String.format(
                    WKBaseApplication.getInstance().context.getString(R.string.screenshot_inchat1),
                    showName
                )
            }
        } else ""
    }
}