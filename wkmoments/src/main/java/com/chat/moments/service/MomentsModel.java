package com.chat.moments.service;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.chat.base.base.WKBaseModel;
import com.chat.base.config.WKConfig;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.net.ICommonListener;
import com.chat.base.net.IRequestResultListener;
import com.chat.base.net.entity.CommonResponse;
import com.chat.base.utils.WKReader;
import com.chat.base.utils.WKTimeUtils;
import com.chat.moments.WKMomentsApplication;
import com.chat.moments.R;
import com.chat.moments.entity.MomentSetting;
import com.chat.moments.entity.Moments;
import com.chat.moments.entity.MomentsType;
import com.chat.moments.entity.Comment;
import com.chat.moments.entity.MomentUploadUrl;
import com.chat.moments.utils.MomentSpanUtils;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKChannel;
import com.xinbida.wukongim.entity.WKChannelType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 2020-11-25 14:51
 */
public class MomentsModel extends WKBaseModel {
    private MomentsModel() {
    }

    private static class MomentsModelBinder {
        private static final MomentsModel model = new MomentsModel();
    }

    public static MomentsModel getInstance() {
        return MomentsModelBinder.model;
    }

    public void list(int page, final IList iList) {
        request(createService(MomentsService.class).list(page, 12), new IRequestResultListener<List<Moments>>() {
            @Override
            public void onSuccess(List<Moments> result) {
                iList.onResult(HttpResponseCode.success, "", resetData(result));
            }

            @Override
            public void onFail(int code, String msg) {
                iList.onResult(code, msg, null);
            }
        });
    }

    public void listByUIDWithAttachment(String uid, final IList iList) {

        request(createService(MomentsService.class).listWithAttachment(uid), new IRequestResultListener<List<Moments>>() {
            @Override
            public void onSuccess(List<Moments> result) {
                iList.onResult(HttpResponseCode.success, "", resetData(result));
            }

            @Override
            public void onFail(int code, String msg) {
                iList.onResult(code, msg, null);
            }
        });
    }

    public void listByUid(int page, String uid, final IList iList) {

        request(createService(MomentsService.class).listByUid(page, 12, uid), new IRequestResultListener<List<Moments>>() {
            @Override
            public void onSuccess(List<Moments> result) {
                iList.onResult(HttpResponseCode.success, "", resetData(result));
            }

            @Override
            public void onFail(int code, String msg) {
                iList.onResult(code, msg, null);
            }
        });
    }

    public interface IList {
        void onResult(int code, String msg, List<Moments> list);
    }


    public void like(String moment_no, final ICommonListener iCommonListener) {
        request(createService(MomentsService.class).like(moment_no), new IRequestResultListener<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse result) {
                iCommonListener.onResult(HttpResponseCode.success, "");
            }

            @Override
            public void onFail(int code, String msg) {
                iCommonListener.onResult(code, msg);
            }
        });
    }

    public void unlike(String moment_no, final ICommonListener iCommonListener) {
        request(createService(MomentsService.class).unlike(moment_no), new IRequestResultListener<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse result) {
                iCommonListener.onResult(HttpResponseCode.success, "");
            }

            @Override
            public void onFail(int code, String msg) {
                iCommonListener.onResult(code, msg);
            }
        });
    }

    public void comments(String moment_no, String content, String reply_comment_id, String reply_uid, String reply_name, final ICommentResult iCommentResult) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("reply_comment_id", reply_comment_id);
        jsonObject.put("reply_uid", reply_uid);
        jsonObject.put("reply_name", reply_name);
        jsonObject.put("content", content);
        request(createService(MomentsService.class).comments(moment_no, jsonObject), new IRequestResultListener<Comment>() {
            @Override
            public void onSuccess(Comment result) {
                iCommentResult.onResult(HttpResponseCode.success, "", result.id);
            }

            @Override
            public void onFail(int code, String msg) {
                iCommentResult.onResult(code, msg, "");
            }
        });
    }

    public interface ICommentResult {
        void onResult(int code, String msg, String commentID);
    }

    public void deleteReply(String moment_no, String id, final ICommonListener iCommonListener) {
        request(createService(MomentsService.class).deleteReply(moment_no, id), new IRequestResultListener<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse result) {
                iCommonListener.onResult(HttpResponseCode.success, "");
            }

            @Override
            public void onFail(int code, String msg) {
                iCommonListener.onResult(code, msg);
            }
        });
    }

    public void publish(int type, String address, String longitude, String latitude, List<String> remindUids, List<String> uids, String video_path, String video_cover_path, List<String> imgs, String text, final ICommonListener iCommonListener) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("video_path", video_path);
        jsonObject.put("video_cover_path", video_cover_path);
        jsonObject.put("text", text);
        jsonObject.put("imgs", imgs);
        jsonObject.put("address", address);
        jsonObject.put("longitude", longitude);
        jsonObject.put("latitude", latitude);
        jsonObject.put("remind_uids", remindUids);
        String privacy_type;
        if (type == 0) {
            privacy_type = "public";
        } else if (type == 1) {
            privacy_type = "private";
        } else if (type == 2) {
            privacy_type = "internal";
        } else {
            privacy_type = "prohibit";
        }
        jsonObject.put("privacy_type", privacy_type);
        jsonObject.put("privacy_uids", uids);
        request(createService(MomentsService.class).publish(jsonObject), new IRequestResultListener<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse result) {
                iCommonListener.onResult(HttpResponseCode.success, "");
            }

            @Override
            public void onFail(int code, String msg) {
                iCommonListener.onResult(code, msg);
            }
        });
    }

    public void delete(String moment_no, final ICommonListener iCommonListener) {
        request(createService(MomentsService.class).delete(moment_no), new IRequestResultListener<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse result) {
                iCommonListener.onResult(HttpResponseCode.success, "");
            }

            @Override
            public void onFail(int code, String msg) {
                iCommonListener.onResult(code, msg);
            }
        });
    }

    public void detail(String moment_no, final IDetail iDetail) {
        request(createService(MomentsService.class).detail(moment_no), new IRequestResultListener<Moments>() {
            @Override
            public void onSuccess(Moments result) {
                List<Moments> list = new ArrayList<>();
                list.add(result);
                iDetail.onResult(HttpResponseCode.success, "", resetData(list).get(0));
            }

            @Override
            public void onFail(int code, String msg) {
                iDetail.onResult(code, msg, null);
            }
        });
    }

    public interface IDetail {
        void onResult(int code, String msg, Moments moments);
    }

    public void getMomentUploadUrl(final IMomentUploadUrl iMomentUploadUrl) {
        request(createService(MomentsService.class).getMomentUploadUrl(), new IRequestResultListener<MomentUploadUrl>() {
            @Override
            public void onSuccess(MomentUploadUrl result) {
                iMomentUploadUrl.onResult(HttpResponseCode.success, "", result.url);
            }

            @Override
            public void onFail(int code, String msg) {
                iMomentUploadUrl.onResult(code, msg, null);
            }
        });
    }

    public interface IMomentUploadUrl {
        void onResult(int code, String msg, String uploadUrl);
    }

    /**
     * 设置对某人不让他看我的朋友圈
     *
     * @param toUID           对方ID
     * @param on              1：不可见
     * @param iCommonListener 返回
     */
    public void hideMy(String toUID, int on, final ICommonListener iCommonListener) {
        request(createService(MomentsService.class).hideMy(toUID, on), new IRequestResultListener<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse result) {
                iCommonListener.onResult(result.status, result.msg);
            }

            @Override
            public void onFail(int code, String msg) {
                iCommonListener.onResult(code, msg);
            }
        });
    }

    /**
     * 不看对方的朋友圈
     *
     * @param toUID           对方用户ID
     * @param on              1：不可见
     * @param iCommonListener 返回
     */
    public void hideHis(String toUID, int on, final ICommonListener iCommonListener) {
        request(createService(MomentsService.class).hideHis(toUID, on), new IRequestResultListener<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse result) {
                iCommonListener.onResult(result.status, result.msg);
            }

            @Override
            public void onFail(int code, String msg) {
                iCommonListener.onResult(code, msg);
            }
        });
    }

    /**
     * 登录用户对某人的朋友圈设置
     *
     * @param toUID          对方用户uid
     * @param iMomentSetting 返回
     */
    public void momentSetting(String toUID, final IMomentSetting iMomentSetting) {
        request(createService(MomentsService.class).momentSetting(toUID), new IRequestResultListener<MomentSetting>() {
            @Override
            public void onSuccess(MomentSetting result) {
                iMomentSetting.onResult(HttpResponseCode.success, "", result);
            }

            @Override
            public void onFail(int code, String msg) {
                iMomentSetting.onResult(code, msg, null);
            }
        });
    }

    public interface IMomentSetting {
        void onResult(int code, String msg, MomentSetting momentSetting);
    }

    private List<Moments> resetData(List<Moments> result) {
        for (int i = 0, size = result.size(); i < size; i++) {
            if (TextUtils.isEmpty(result.get(i).video_path)) {
                if (WKReader.isEmpty(result.get(i).imgs)) {
                    result.get(i).itemType = MomentsType.single_text;
                } else {
                    if (result.get(i).imgs.size() == 1) {
                        result.get(i).itemType = MomentsType.one_image;
                    } else result.get(i).itemType = MomentsType.image_text;
                }
            } else {
                result.get(i).itemType = MomentsType.video_text;
            }
            long time = WKTimeUtils.getInstance().date2TimeStamp(result.get(i).created_at, "yyyy-MM-dd HH:mm:ss");
            result.get(i).showDate = WKTimeUtils.getInstance().getTimeFormatText(new Date(time * 1000));
            //获取发布者的信息
            WKChannel publisherWKChannel = WKIM.getInstance().getChannelManager().getChannel(result.get(i).publisher, WKChannelType.PERSONAL);
            if (publisherWKChannel != null) {
                if (result.get(i).publisher.equals(WKConfig.getInstance().getUid())) {
                    result.get(i).publisher_name = WKConfig.getInstance().getUserName();
                } else {
                    result.get(i).publisher_name = TextUtils.isEmpty(publisherWKChannel.channelRemark) ? publisherWKChannel.channelName : publisherWKChannel.channelRemark;
                }
                result.get(i).publisherAvatarCacheKey = publisherWKChannel.avatarCacheKey;
            }

            // 获取点赞用户信息
            if (WKReader.isNotEmpty(result.get(i).likes)) {
                for (int j = 0, len = result.get(i).likes.size(); j < len; j++) {
                    WKChannel channel = WKIM.getInstance().getChannelManager().getChannel(result.get(i).likes.get(j).uid, WKChannelType.PERSONAL);
                    if (channel != null) {
                        result.get(i).likes.get(j).avatarCacheKey = channel.avatarCacheKey;
                    }
                }
            }
            //设置点赞数据
            result.get(i).praiseSpan = MomentSpanUtils.getInstance().makePraiseSpan(WKMomentsApplication.getInstance().getContext(), result.get(i).likes);
            if (TextUtils.isEmpty(result.get(i).text)) {
                result.get(i).showAllText = false;
            } else {
                result.get(i).showAllText = MomentSpanUtils.getInstance().calculateShowCheckAllText(result.get(i).text);
            }

            if (WKReader.isNotEmpty(result.get(i).remind_uids)) {
                //查询提及到到用户
                StringBuilder stringBuilder = new StringBuilder();
                for (int j = 0, len = result.get(i).remind_uids.size(); j < len; j++) {
                    if (result.get(i).remind_uids.get(j).equals(WKConfig.getInstance().getUid())) {
                        result.get(i).isRemindMe = true;
                        continue;
                    }
                    WKChannel channel = WKIM.getInstance().getChannelManager().getChannel(result.get(i).remind_uids.get(j), WKChannelType.PERSONAL);
                    if (channel != null) {
                        if (!TextUtils.isEmpty(stringBuilder)) {
                            stringBuilder.append("，");
                        }
                        stringBuilder.append(channel.channelName);
                    }
                }
                String remindUserNames;
                if (result.get(i).isRemindMe) {
                    if (!TextUtils.isEmpty(stringBuilder)) {
                        remindUserNames = String.format("%s %s", WKMomentsApplication.getInstance().getContext().getString(R.string.me), stringBuilder);
                    } else {
                        remindUserNames = WKMomentsApplication.getInstance().getContext().getString(R.string.moment_remind_u);
                    }
                } else {
                    remindUserNames = stringBuilder.toString();
                }
                result.get(i).remindUserNames = remindUserNames;
            }
            //重置评论
            if (WKReader.isNotEmpty(result.get(i).comments)) {
                for (int j = 0, len = result.get(i).comments.size(); j < len; j++) {
                    WKChannel channel = WKIM.getInstance().getChannelManager().getChannel(result.get(i).comments.get(j).uid, WKChannelType.PERSONAL);
                    if (channel != null) {
                        result.get(i).comments.get(j).name = TextUtils.isEmpty(channel.channelRemark) ? channel.channelName : channel.channelRemark;
                        result.get(i).comments.get(j).avatarCacheKey = channel.avatarCacheKey;
                    }
                    if (!TextUtils.isEmpty(result.get(i).comments.get(j).reply_uid)) {
                        WKChannel replyWKChannel = WKIM.getInstance().getChannelManager().getChannel(result.get(i).comments.get(j).reply_uid, WKChannelType.PERSONAL);
                        if (replyWKChannel != null) {
                            result.get(i).comments.get(j).avatarCacheKey = replyWKChannel.avatarCacheKey;
                            result.get(i).comments.get(j).reply_name = TextUtils.isEmpty(replyWKChannel.channelRemark) ? replyWKChannel.channelName : replyWKChannel.channelRemark;
                        }
                    }
                }
            }

        }
        return result;
    }
}
