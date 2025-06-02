package com.chat.groupmanage.service;

import com.chat.base.net.HttpResponseCode;
import com.chat.base.net.ICommonListener;
import com.chat.base.utils.WKToastUtils;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 2020-04-12 16:22
 * 群管理
 */
public class GroupManagePresenter implements GroupManageContract.GroupManagePresenter {
    private final WeakReference<GroupManageContract.GroupManageView> groupView;

    public GroupManagePresenter(GroupManageContract.GroupManageView groupManageView) {
        groupView = new WeakReference<>(groupManageView);
    }

    @Override
    public void updateGroupSetting(String groupID, String key, int on) {
        GroupManageModel.getInstance().updateGroupSetting(groupID, key, on, this::setCommonResult);
    }

    @Override
    public void removeGroupManager(String groupID, List<String> uids) {
        GroupManageModel.getInstance().removeGroupManager(groupID, uids, this::setCommonResult);
    }

    @Override
    public void addGroupManager(String groupID, List<String> uids) {
        GroupManageModel.getInstance().addGroupManager(groupID, uids, this::setCommonResult);
    }

    @Override
    public void transferGroup(String groupID, String uid) {
        GroupManageModel.getInstance().transferGroup(groupID, uid, this::setCommonResult);
    }

    @Override
    public void addOrRemoveGroupBlackList(String groupID, String action, List<String> uids) {
        GroupManageModel.getInstance().addOrRemoveBlackList(groupID, action, uids, this::setCommonResult);
    }

    @Override
    public void forbiddenTimeList() {
        GroupManageModel.getInstance().forbiddenTimeList((code, msg, list) -> {
            if (code == HttpResponseCode.success) {
                groupView.get().forbiddenTimeList(list);
            } else {
                WKToastUtils.getInstance().showToastNormal(msg);
            }
        });
    }

    @Override
    public void setForbiddenTime(String groupNo, String uid, int key, int action) {
        GroupManageModel.getInstance().forbiddenWithMember(groupNo, uid, action, key, new ICommonListener() {
            @Override
            public void onResult(int code, String msg) {
                if (code == HttpResponseCode.success)
                    groupView.get().refreshData();
                else WKToastUtils.getInstance().showToastNormal(msg);
            }
        });
    }

    @Override
    public void showLoading() {

    }

    private void setCommonResult(int code, String msg) {
        if (groupView.get() != null) {
            if (code == HttpResponseCode.success) {
                groupView.get().refreshData();
            } else {
                groupView.get().hideLoading();
                WKToastUtils.getInstance().showToastNormal(msg);
            }
        }
    }
}
