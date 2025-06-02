package com.chat.moments.service;

import com.alibaba.fastjson.JSONObject;
import com.chat.base.net.entity.CommonResponse;
import com.chat.moments.entity.MomentSetting;
import com.chat.moments.entity.Moments;
import com.chat.moments.entity.Comment;
import com.chat.moments.entity.MomentUploadUrl;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 2020-11-25 14:47
 * 动态
 */
public interface MomentsService {

    @POST("moments")
    Observable<CommonResponse> publish(@Body JSONObject jsonObject);

    @GET("moments")
    Observable<List<Moments>> list(@Query("page_index") int page_index, @Query("page_size") int page_size);

    @GET("moments")
    Observable<List<Moments>> listByUid(@Query("page_index") int page_index, @Query("page_size") int page_size, @Query("uid") String uid);

    @PUT("moments/{moment_no}/like")
    Observable<CommonResponse> like(@Path("moment_no") String moment_no);

    @PUT("moments/{moment_no}/unlike")
    Observable<CommonResponse> unlike(@Path("moment_no") String moment_no);

    @POST("moments/{moment_no}/comments")
    Observable<Comment> comments(@Path("moment_no") String moment_no, @Body JSONObject jsonObject);

    @DELETE("moments/{moment_no}/comments/{id}")
    Observable<CommonResponse> deleteReply(@Path("moment_no") String moment_no, @Path("id") String id);

    @DELETE("moments/{moment_no}")
    Observable<CommonResponse> delete(@Path("moment_no") String moment_no);

    @GET("moments/{moment_no}")
    Observable<Moments> detail(@Path("moment_no") String moment_no);

    @GET("moment/cover/upload")
    Observable<MomentUploadUrl> getMomentUploadUrl();

    @PUT("moment/setting/hidemy/{uid}/{on}")
    Observable<CommonResponse> hideMy(@Path("uid") String uid, @Path("on") int on);

    @PUT("moment/setting/hidehis/{uid}/{on}")
    Observable<CommonResponse> hideHis(@Path("uid") String uid, @Path("on") int on);

    @GET("moment/setting/{uid}")
    Observable<MomentSetting> momentSetting(@Path("uid") String uid);

    @GET("moment/attachment")
    Observable<List<Moments>>  listWithAttachment(@Query("uid") String uid);
}
