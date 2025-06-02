package com.chat.groupmanage.service;

import com.chat.base.base.WKBasePresenter;
import com.chat.base.base.WKBaseView;
import com.chat.groupmanage.entity.ForbiddenTime;

import java.util.List;

/**
 * 2020-04-12 16:20
 * 群管理
 */
public class GroupManageContract {
    interface GroupManagePresenter extends WKBasePresenter {
        void updateGroupSetting(String groupID, String key, int on);

        void removeGroupManager(String groupID, List<String> uids);

        void addGroupManager(String groupID, List<String> uids);

        void transferGroup(String groupID, String uid);

        void addOrRemoveGroupBlackList(String groupID, String action, List<String> uids);

        void forbiddenTimeList();

        void setForbiddenTime(String groupNo, String uid, int key, int action);
    }

    public interface GroupManageView extends WKBaseView {
        void refreshData();

        void forbiddenTimeList(List<ForbiddenTime> list);
    }
}
