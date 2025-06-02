package com.chat.advanced.service

import com.alibaba.fastjson.JSONObject
import com.chat.advanced.entity.ChatBgEntity
import com.chat.advanced.entity.MsgReadDetailEntity
import com.chat.base.entity.UserInfoEntity
import com.chat.base.net.entity.CommonResponse
import com.xinbida.wukongim.entity.WKSyncMsgReaction
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AdvancedService {

    @POST("message/readed")
    fun readedMsg(@Body jsonObject: JSONObject): Observable<CommonResponse>


    @PUT("groups/{groupNo}/setting")
    fun updateGroupSetting(
        @Path("groupNo") groupNo: String,
        @Body jsonObject: JSONObject
    ): Observable<CommonResponse>


    @PUT("users/{uid}/setting")
    fun updateUserSetting(
        @Path("uid") uid: String,
        @Body jsonObject: JSONObject
    ): Observable<CommonResponse>

    @GET("messages/{message_id}/receipt")
    fun receipt(
        @Path("message_id") message_id: String,
        @Query("readed") readed: Int,
        @Query("page_index") page_index: Int,
        @Query("page_size") page_size: Int,
        @Query("channel_id") channel_id: String,
        @Query("channel_type") channel_type: Byte
    ): Observable<List<MsgReadDetailEntity>>

    @POST("reactions")
    fun reactionsMsg(@Body jsonObject: JSONObject?): Observable<CommonResponse>

    @POST("reaction/sync")
    fun syncReaction(@Body jsonObject: JSONObject?): Observable<List<WKSyncMsgReaction>>

    @POST("user/wxlogin")
    fun wxLogin(@Body jsonObject: JSONObject?): Observable<UserInfoEntity>

    @GET("common/chatbg")
    fun chatBGList(): Observable<List<ChatBgEntity>>

//    @PUT("user/chatbg")
//    fun updateMyChatBg(@Body jsonObject: JSONObject): Observable<CommonResponse>
//
//    @PUT("users/{uid}/chatbg")
//    fun updatePersonalChatBg(
//        @Path("uid") uid: String,
//        @Body jsonObject: JSONObject
//    ): Observable<CommonResponse>
//
//    @PUT("groups/{groupNo}/chatbg")
//    fun updateGroupChatBg(
//        @Path("groupNo") groupNo: String,
//        @Body jsonObject: JSONObject
//    ): Observable<CommonResponse>
}