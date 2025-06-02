package com.chat.groupmanage.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chat.base.net.entity.CommonResponse;
import com.chat.groupmanage.entity.ForbiddenTime;
import com.chat.groupmanage.entity.H5ConfirmUrl;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 2020-07-20 22:53
 * 群管理
 */
public interface GroupManageService {
    @PUT("groups/{groupNo}/setting")
    Observable<CommonResponse> updateGroupSetting(@Path("groupNo") String groupNo, @Body JSONObject jsonObject);

    @HTTP(method = "DELETE", path = "groups/{groupNo}/managers", hasBody = true)
    Observable<CommonResponse> removeGroupManager(@Path("groupNo") String groupNo, @Body JSONArray jsonArray);

    @POST("groups/{groupID}/managers")
    Observable<CommonResponse> addGroupManager(@Path("groupID") String groupID, @Body JSONArray jsonArray);

    @POST("groups/{groupID}/transfer/{uid}")
    Observable<CommonResponse> transferGroup(@Path("groupID") String groupID, @Path("uid") String uid);

    @GET("groups/{groupNo}/member/h5confirm")
    Observable<H5ConfirmUrl> getH5confirmUrl(@Path("groupNo") String groupNo, @Query("invite_no") String invite_no);

    @POST("groups/{groupNo}/blacklist/{action}")
    Observable<CommonResponse> addOrRemoveBlackList(@Path("groupNo") String groupNo, @Path("action") String action, @Body JSONObject jsonObject);

    @GET("group/forbidden_times")
    Observable<List<ForbiddenTime>> forbiddenTimeList();

    @POST("groups/{groupNo}/forbidden_with_member")
    Observable<CommonResponse> forbiddenWithMember(@Path("groupNo")String groupNo,@Body JSONObject jsonObject);
    @DELETE("groups/{groupNo}/disband")
    Observable<CommonResponse> disbandGroup(@Path("groupNo")String groupNo);

    @HTTP(method = "DELETE", path = "groups/{groupNo}/members", hasBody = true)
    Observable<CommonResponse> deleteGroupMembers(@Path("groupNo") String groupNo, @Body JSONObject jsonObject);

}
