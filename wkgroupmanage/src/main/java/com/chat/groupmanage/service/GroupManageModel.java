package com.chat.groupmanage.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chat.base.base.WKBaseModel;
import com.chat.base.config.WKApiConfig;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.net.ICommonListener;
import com.chat.base.net.IRequestResultListener;
import com.chat.base.net.entity.CommonResponse;
import com.chat.base.net.ud.WKUploader;
import com.chat.base.utils.WKTimeUtils;
import com.chat.groupmanage.entity.ForbiddenTime;
import com.chat.groupmanage.entity.H5ConfirmUrl;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKChannelMember;
import com.xinbida.wukongim.entity.WKChannelType;

import java.util.ArrayList;
import java.util.List;

/**
 * 2020-04-11 20:52
 * 群管理model
 */
public class GroupManageModel extends WKBaseModel {
    private GroupManageModel() {
    }

    private static class GroupManageModelBinder {
        static final GroupManageModel model = new GroupManageModel();
    }

    public static GroupManageModel getInstance() {
        return GroupManageModelBinder.model;
    }

    /**
     * 修改群设置
     *
     * @param groupID         群编号
     * @param key             修改字段
     * @param value           修改值
     * @param iCommonListener 返回
     */
    void updateGroupSetting(String groupID, String key, int value, final ICommonListener iCommonListener) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(key, value);
        request(createService(GroupManageService.class).updateGroupSetting(groupID, jsonObject), new IRequestResultListener<CommonResponse>() {
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

    void removeGroupManager(String groupID, List<String> uids, final ICommonListener iCommonListener) {
        JSONArray jsonArray1 = new JSONArray();
        jsonArray1.addAll(uids);
        request(createService(GroupManageService.class).removeGroupManager(groupID, jsonArray1), new IRequestResultListener<CommonResponse>() {
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
     * 添加群管理员
     *
     * @param groupID 群编号
     * @param uids    用户数组
     */
    void addGroupManager(String groupID, List<String> uids, final ICommonListener iCommonLisenter) {
        JSONArray jsonArray1 = new JSONArray();
        jsonArray1.addAll(uids);
        request(createService(GroupManageService.class).addGroupManager(groupID, jsonArray1), new IRequestResultListener<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse result) {
                iCommonLisenter.onResult(result.status, result.msg);
            }

            @Override
            public void onFail(int code, String msg) {
                iCommonLisenter.onResult(code, msg);
            }
        });
    }

    void transferGroup(String groupID, String uid, final ICommonListener iCommonLisenter) {
        request(createService(GroupManageService.class).transferGroup(groupID, uid), new IRequestResultListener<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse result) {
                iCommonLisenter.onResult(result.status, result.msg);
            }

            @Override
            public void onFail(int code, String msg) {
                iCommonLisenter.onResult(code, msg);
            }
        });

    }

    /**
     * 获取h5跳转地址
     *
     * @param groupId         群编号
     * @param invite_no       邀请编号
     * @param iCommonLisenter 返回
     */
    public void getH5confirmUrl(String groupId, String invite_no, final ICommonListener iCommonLisenter) {

        request(createService(GroupManageService.class).getH5confirmUrl(groupId, invite_no), new IRequestResultListener<H5ConfirmUrl>() {
            @Override
            public void onSuccess(H5ConfirmUrl result) {
                iCommonLisenter.onResult(HttpResponseCode.success, result.url);
            }

            @Override
            public void onFail(int code, String msg) {
                iCommonLisenter.onResult(code, msg);
            }
        });
    }

    /**
     * 添加或移除群成员黑名单
     *
     * @param groupNo         群ID
     * @param action          action-> add:添加 remove:移除
     * @param list            成员IDs
     * @param iCommonLisenter 返回
     */
    public void addOrRemoveBlackList(String groupNo, String action, List<String> list, final ICommonListener iCommonLisenter) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uids", list);
        request(createService(GroupManageService.class).addOrRemoveBlackList(groupNo, action, jsonObject), new IRequestResultListener<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse result) {
                iCommonLisenter.onResult(result.status, result.msg);
            }

            @Override
            public void onFail(int code, String msg) {
                iCommonLisenter.onResult(code, msg);
            }
        });
    }


    public void forbiddenTimeList(IForbiddenTimeList iForbiddenTimeList) {
        request(createService(GroupManageService.class).forbiddenTimeList(), new IRequestResultListener<List<ForbiddenTime>>() {
            @Override
            public void onSuccess(List<ForbiddenTime> result) {
                iForbiddenTimeList.onResult(HttpResponseCode.success, "", result);
            }

            @Override
            public void onFail(int code, String msg) {
                iForbiddenTimeList.onResult(code, msg, null);
            }
        });
    }

    public void forbiddenWithMember(String groupNo, String memberUID, int action, int key, final ICommonListener iCommonListener) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("member_uid", memberUID);
        jsonObject.put("action", action);
        jsonObject.put("key", key);
        request(createService(GroupManageService.class).forbiddenWithMember(groupNo, jsonObject), new IRequestResultListener<CommonResponse>() {
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

    public interface IForbiddenTimeList {
        void onResult(int code, String msg, List<ForbiddenTime> list);
    }


    public void uploadAvatar(String groupNO, String filePath, IUploadBack iUploadBack) {
        String url = WKApiConfig.baseUrl + "groups/" + groupNO + "/avatar?uuid=" + WKTimeUtils.getInstance().getCurrentMills();
        WKUploader.getInstance().upload(url, filePath, new WKUploader.IUploadBack() {
            @Override
            public void onSuccess(String url) {
                iUploadBack.onResult(HttpResponseCode.success);
            }

            @Override
            public void onError() {
                iUploadBack.onResult(HttpResponseCode.error);
            }
        });
    }

    public interface IUploadBack {
        void onResult(int code);
    }

    public void disbandGroup(String groupNo, ICommonListener iCommonListener) {
        request(createService(GroupManageService.class).disbandGroup(groupNo), new IRequestResultListener<>() {
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

    public void deleteMember(String groupNo,List<String> uidList, List<String> names, ICommonListener iCommonListener) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(uidList);
        jsonObject.put("members", jsonArray);
        JSONArray nameArr = new JSONArray();
        nameArr.addAll(names);
        jsonObject.put("names", nameArr);
        request(createService(GroupManageService.class).deleteGroupMembers(groupNo, jsonObject), new IRequestResultListener<>() {
            @Override
            public void onSuccess(CommonResponse result) {
                List<WKChannelMember> list = new ArrayList<>();
                for (int i = 0, size = uidList.size(); i < size; i++) {
                    WKChannelMember member = new WKChannelMember();
                    member.isDeleted = 1;
                    member.channelID = groupNo;
                    member.channelType = WKChannelType.GROUP;
                    member.memberUID = uidList.get(i);
                    list.add(member);
                }
                WKIM.getInstance().getChannelMembersManager().delete(list);
                iCommonListener.onResult(result.status, result.msg);
            }

            @Override
            public void onFail(int code, String msg) {
                iCommonListener.onResult(code, msg);
            }
        });
    }
}
