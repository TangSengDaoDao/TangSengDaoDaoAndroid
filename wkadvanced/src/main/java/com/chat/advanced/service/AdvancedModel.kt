package com.chat.advanced.service

import android.text.TextUtils
import com.alibaba.fastjson.JSONObject
import com.chat.advanced.entity.ChatBgEntity
import com.chat.advanced.entity.MsgReadDetailEntity
import com.chat.base.base.WKBaseModel
import com.chat.base.config.WKConfig
import com.chat.base.config.WKConstants
import com.chat.base.endpoint.EndpointManager
import com.chat.base.endpoint.entity.OtherLoginResultMenu
import com.chat.base.entity.UserInfoEntity
import com.chat.base.net.HttpResponseCode
import com.chat.base.net.ICommonListener
import com.chat.base.net.IRequestResultErrorInfoListener
import com.chat.base.net.IRequestResultListener
import com.chat.base.net.entity.CommonResponse
import com.chat.base.utils.WKDeviceUtils
import com.chat.base.utils.WKLogUtils
import com.chat.base.utils.WKToastUtils
import com.xinbida.wukongim.WKIM
import com.xinbida.wukongim.entity.WKChannelType
import com.xinbida.wukongim.entity.WKSyncMsgReaction
import org.json.JSONException

class AdvancedModel private constructor() : WKBaseModel() {

    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = AdvancedModel()
    }

    fun readMsg(channelID: String, channelType: Byte, msgIds: List<String>) {
        val jsonObject = JSONObject()
        jsonObject["channel_id"] = channelID
        jsonObject["channel_type"] = channelType
        jsonObject["message_ids"] = msgIds
        request(
                createService(AdvancedService::class.java).readedMsg(jsonObject),
                object : IRequestResultListener<CommonResponse> {
                    override fun onSuccess(result: CommonResponse) {}
                    override fun onFail(code: Int, msg: String) {}
                })
    }

    fun updateGroupSetting(
            groupNo: String,
            key: String,
            value: Int,
            iCommonListener: ICommonListener
    ) {
        val jsonObject = JSONObject()
        jsonObject[key] = value
        request(
                createService(AdvancedService::class.java).updateGroupSetting(
                        groupNo,
                        jsonObject
                ), object : IRequestResultListener<CommonResponse> {
            override fun onSuccess(result: CommonResponse) {
                iCommonListener.onResult(result.status, result.msg)
            }

            override fun onFail(code: Int, msg: String) {
                iCommonListener.onResult(code, msg)
            }
        })
    }

    fun updateUserSetting(
            uid: String,
            key: String,
            value: Int,
            iCommonListener: ICommonListener
    ) {
        val jsonObject = JSONObject()
        jsonObject[key] = value
        request(
                createService(AdvancedService::class.java).updateUserSetting(
                        uid,
                        jsonObject
                ), object : IRequestResultListener<CommonResponse> {
            override fun onSuccess(result: CommonResponse) {
                iCommonListener.onResult(result.status, result.msg)
            }

            override fun onFail(code: Int, msg: String) {
                iCommonListener.onResult(code, msg)
            }
        })
    }


    fun receipt(
            messageID: String,
            page: Int,
            channelID: String,
            channelType: Byte,
            read: Int,
            iMsgReadReceipt: IMsgReadReceipt
    ) {
        request(
                createService(
                        AdvancedService::class.java
                ).receipt(messageID, read, page, 20, channelID, channelType),
                object : IRequestResultListener<List<MsgReadDetailEntity>> {
                    override fun onSuccess(result: List<MsgReadDetailEntity>) {
                        if (result.isNotEmpty()) {
                            var i = 0
                            val size = result.size
                            while (i < size) {
                                val mChannel = WKIM.getInstance().channelManager.getChannel(
                                        result[i].uid,
                                        WKChannelType.PERSONAL
                                )
                                if (mChannel != null) {
                                    result[i].name =
                                            if (TextUtils.isEmpty(mChannel.channelRemark)) mChannel.channelName else mChannel.channelRemark
                                }
                                i++
                            }
                        }
                        iMsgReadReceipt.onResult(HttpResponseCode.success.toInt(), "", result)
                    }

                    override fun onFail(code: Int, msg: String) {
                        iMsgReadReceipt.onResult(code, msg, null)
                    }
                })
    }

    interface IMsgReadReceipt {
        fun onResult(code: Int, msg: String, list: List<MsgReadDetailEntity?>?)
    }


    fun reactionsMsg(channelID: String, channelType: Byte, messageID: String, emoji: String) {
        val jsonObject = JSONObject()
        jsonObject["channel_id"] = channelID
        jsonObject["channel_type"] = channelType
        jsonObject["message_id"] = messageID
        jsonObject["emoji"] = emoji
        request(
                createService(AdvancedService::class.java).reactionsMsg(jsonObject),
                object : IRequestResultListener<CommonResponse?> {
                    override fun onSuccess(result: CommonResponse?) {}
                    override fun onFail(code: Int, msg: String) {
                        if (TextUtils.isEmpty(msg)) WKToastUtils.getInstance().showToastNormal(msg)
                    }
                })
    }

    fun chatBGList(iChatBG: IChatBG) {
        request(createService(AdvancedService::class.java).chatBGList(),
                object : IRequestResultListener<List<ChatBgEntity>> {
                    override fun onSuccess(result: List<ChatBgEntity>) {
                        iChatBG.onResult(result, HttpResponseCode.success.toInt(), "")
                    }

                    override fun onFail(code: Int, msg: String) {
                        iChatBG.onResult(emptyList(), code, msg)
                    }

                })
    }

    interface IChatBG {
        fun onResult(list: List<ChatBgEntity>, code: Int, msg: String)
    }

    fun wxLogin(code: String) {
        val jsonObject = JSONObject()
        jsonObject["code"] = code
        val deviceJson = JSONObject()
        deviceJson["device_id"] = WKConstants.getDeviceID()
        deviceJson["device_name"] = WKDeviceUtils.getInstance().deviceName
        deviceJson["device_model"] = WKDeviceUtils.getInstance().systemModel
        jsonObject["device"] = deviceJson
        requestAndErrorBack(createService(AdvancedService::class.java).wxLogin(jsonObject),
                object : IRequestResultErrorInfoListener<UserInfoEntity?> {
                    override fun onSuccess(userInfo: UserInfoEntity?) {
                        if (userInfo != null) {
                            WKConfig.getInstance().saveUserInfo(userInfo)
                            WKConfig.getInstance().token = userInfo.token
                            if (!TextUtils.isEmpty(userInfo.im_token)) {
                                WKConfig.getInstance().imToken = userInfo.im_token
                            } else WKConfig.getInstance().imToken = userInfo.token
                            WKConfig.getInstance().uid = userInfo.uid
                            WKConfig.getInstance().userName = userInfo.name
                            EndpointManager.getInstance()
                                    .invoke("other_login_result", OtherLoginResultMenu(0, userInfo))
                        }
                    }

                    override fun onFail(code: Int, msg: String?, errJson: String) {
                        val userInfoEntity = UserInfoEntity()
                        if (code == 110 && !TextUtils.isEmpty(errJson)) {
                            try {
                                val jsonObject1: org.json.JSONObject = org.json.JSONObject(errJson)
                                userInfoEntity.phone = jsonObject1.optString("phone")
                                userInfoEntity.uid = jsonObject1.optString("uid")
                            } catch (e: JSONException) {
                                WKLogUtils.e("微信登录错误")
                            }
                        }
                        EndpointManager.getInstance()
                                .invoke("other_login_result", OtherLoginResultMenu(code, userInfoEntity))
                    }

                })
    }

    fun syncReaction(channelID: String?, channelType: Byte) {
        val maxSeq = WKIM.getInstance().msgManager.getMaxReactionSeqWithChannel(channelID, channelType)
        val jsonObject = JSONObject()
        jsonObject["channel_id"] = channelID
        jsonObject["channel_type"] = channelType
        jsonObject["seq"] = maxSeq
        request(createService(AdvancedService::class.java).syncReaction(jsonObject), object : IRequestResultListener<List<WKSyncMsgReaction>> {
            override fun onSuccess(result: List<WKSyncMsgReaction>?) {
                WKIM.getInstance().msgManager.saveMessageReactions(result)
            }

            override fun onFail(code: Int, msg: String) {
            }
        })
    }

}