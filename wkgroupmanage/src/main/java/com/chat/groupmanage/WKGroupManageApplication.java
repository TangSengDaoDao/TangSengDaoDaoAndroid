package com.chat.groupmanage;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chat.base.WKBaseApplication;
import com.chat.base.config.WKConfig;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.ChatSettingCellMenu;
import com.chat.base.endpoint.entity.UserDetailViewMenu;
import com.chat.base.entity.AppModule;
import com.chat.base.msgitem.WKChannelMemberRole;
import com.chat.base.msgitem.WKContentType;
import com.chat.base.msgitem.WKMsgItemViewManager;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.ui.Theme;
import com.chat.base.utils.WKDialogUtils;
import com.chat.base.utils.WKReader;
import com.chat.base.utils.WKTimeUtils;
import com.chat.base.utils.WKToastUtils;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.groupmanage.msgitem.GroupApproveItemProvider;
import com.chat.groupmanage.service.GroupManageModel;
import com.chat.groupmanage.ui.ForbiddenWitchGroupMemberActivity;
import com.chat.groupmanage.ui.GroupHeaderActivity;
import com.chat.groupmanage.ui.GroupManageActivity;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKChannel;
import com.xinbida.wukongim.entity.WKChannelMember;
import com.xinbida.wukongim.entity.WKChannelType;

import java.util.ArrayList;
import java.util.List;

/**
 * 2020-04-06 21:02
 * 群管理
 */
public class WKGroupManageApplication {


    private WKGroupManageApplication() {

    }

    private static class GroupManageApplicationBinder {
        final static WKGroupManageApplication MANAGER = new WKGroupManageApplication();
    }

    public static WKGroupManageApplication getInstance() {
        return GroupManageApplicationBinder.MANAGER;
    }

    public void init() {
        AppModule appModule = WKBaseApplication.getInstance().getAppModuleWithSid("groupManager");
        if (!WKBaseApplication.getInstance().appModuleIsInjection(appModule)) {
            return;
        }
        initListener();
        WKMsgItemViewManager.getInstance().addChatItemViewProvider(WKContentType.approveGroupMember, new GroupApproveItemProvider());
    }

    private void initListener() {

        // 群头像
        EndpointManager.getInstance().setMethod("group_avatar_view", object -> {
            if (object instanceof ChatSettingCellMenu menu) {
                return getGroupAvatarView(menu.getParentLayout().getContext(), menu.getChannelID(), menu.getParentLayout());
            }
            return null;
        });
        EndpointManager.getInstance().setMethod("group_manager_view", object -> {
            if (object instanceof ChatSettingCellMenu menu) {
                return getGroupManagerView(menu.getParentLayout().getContext(), menu.getChannelID(), menu.getParentLayout());
            }
            return null;
        });
        // 用户资料详情设置群内禁言
        EndpointManager.getInstance().setMethod("group_manager", EndpointCategory.wkUserDetailView, 3, object -> {
            UserDetailViewMenu menu = (UserDetailViewMenu) object;
            if (!TextUtils.isEmpty(menu.groupNo) && !TextUtils.isEmpty(menu.uid) && menu.context != null && menu.context.get() != null) {
                WKChannelMember member = WKIM.getInstance().getChannelMembersManager().getMember(menu.groupNo, WKChannelType.GROUP, WKConfig.getInstance().getUid());
                if (member != null && member.isDeleted == 0) {
                    if (member.role == WKChannelMemberRole.normal) {
                        return null;
                    }
                    WKChannelMember uidMember = WKIM.getInstance().getChannelMembersManager().getMember(menu.groupNo, WKChannelType.GROUP, menu.uid);
                    if (uidMember != null) {
                        if (uidMember.role == WKChannelMemberRole.admin || (member.role == uidMember.role) || uidMember.isDeleted == 1) {
                            return null;
                        }
                    }
                    return getGroupInUserDetailView(menu.context.get(), menu.uid, menu.groupNo, menu.parentView);
                }

            }

            return null;
        });
    }

    private View getGroupAvatarView(Context context, String groupNo, ViewGroup parentView) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_in_user_detail_layout, parentView, false);
        TextView timeTv = view.findViewById(R.id.timeTv);
        timeTv.setText(R.string.group_avatar);
        View bottomView = view.findViewById(R.id.bottomView);
        bottomView.setVisibility(View.GONE);
        view.findViewById(R.id.topView).setVisibility(View.GONE);
        SingleClickUtil.onSingleClick(view.findViewById(R.id.forbiddenLayout), view1 -> {
            Intent intent = new Intent(context, GroupHeaderActivity.class);
            intent.putExtra("groupNo", groupNo);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
        return view;
    }

    private View getGroupManagerView(Context context, String groupNo, ViewGroup parentView) {
        WKChannel channel = WKIM.getInstance().getChannelManager().getChannel(groupNo, WKChannelType.GROUP);
        WKChannelMember member = WKIM.getInstance().getChannelMembersManager().getMember(groupNo, WKChannelType.GROUP, WKConfig.getInstance().getUid());
        if (channel == null || member == null || member.role == WKChannelMemberRole.normal || channel.status == 2) {
            return null;
        }
        View view = LayoutInflater.from(context).inflate(R.layout.group_in_user_detail_layout, parentView, false);
        TextView timeTv = view.findViewById(R.id.timeTv);
        timeTv.setText(R.string.group_manage);
        View bottomView = view.findViewById(R.id.bottomView);
        view.findViewById(R.id.topView).setVisibility(View.GONE);
        bottomView.setVisibility(View.VISIBLE);
        SingleClickUtil.onSingleClick(view.findViewById(R.id.forbiddenLayout), view1 -> {
            Intent intent = new Intent(context, GroupManageActivity.class);
            intent.putExtra("groupId", groupNo);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
        return view;
    }

    private View getGroupInUserDetailView(Context context, String uid, String groupNo, ViewGroup parentView) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_in_user_detail_layout, parentView, false);
        TextView timeTv = view.findViewById(R.id.timeTv);
        TextView contentTv = view.findViewById(R.id.contentTv);
        View deleteMemberLayout = view.findViewById(R.id.deleteMemberLayout);
        WKChannelMember member = WKIM.getInstance().getChannelMembersManager().getMember(groupNo, WKChannelType.GROUP, uid);
        setDeleteMemberLayout(deleteMemberLayout, groupNo, uid);
        if (member != null) {
            if (member.forbiddenExpirationTime > 0) {
                timeTv.setText(R.string.forbiddening);
                setTime(member.forbiddenExpirationTime, context, contentTv);
            } else {
                timeTv.setText(R.string.forbidden_with_group_member);
            }
        } else {
            timeTv.setText(R.string.forbidden_with_group_member);
        }

        SingleClickUtil.onSingleClick(view.findViewById(R.id.forbiddenLayout), view1 -> {
            WKChannelMember tempMember = WKIM.getInstance().getChannelMembersManager().getMember(groupNo, WKChannelType.GROUP, uid);

            if (tempMember != null && tempMember.forbiddenExpirationTime == 0) {
                Intent intent = new Intent(context, ForbiddenWitchGroupMemberActivity.class);
                intent.putExtra("memberUID", uid);
                intent.putExtra("groupNo", groupNo);
                context.startActivity(intent);
            } else {
                WKDialogUtils.getInstance().showDialog(context, context.getString(R.string.lift_ban), context.getString(R.string.lift_forbiddening), true, "", "", 0, Theme.colorAccount, index -> {
                    if (index == 1) {
                        GroupManageModel.getInstance().forbiddenWithMember(groupNo, uid, 0, 0, (code, msg) -> {
                            if (code != HttpResponseCode.success) {
                                WKToastUtils.getInstance().showToastNormal(msg);
                            }
                        });
                    }
                });
            }
        });
        WKIM.getInstance().getChannelMembersManager().addOnRefreshChannelMemberInfo("forbidden", (member1, isEnd) -> {
            if (member1 != null && member1.channelID.equals(groupNo) && member1.memberUID.equals(uid)) {
                setDeleteMemberLayout(deleteMemberLayout, groupNo, uid);
                if (member != null) {
                    member.forbiddenExpirationTime = member1.forbiddenExpirationTime;
                }
                if (member1.forbiddenExpirationTime > 0) {
                    timeTv.setText(R.string.forbiddening);
                    setTime(member1.forbiddenExpirationTime, context, contentTv);
                } else {
                    contentTv.setVisibility(View.GONE);
                    timeTv.setText(R.string.forbidden_with_group_member);
                }
            }
        });
        WKIM.getInstance().getChannelMembersManager().addOnAddChannelMemberListener("forbidden", list -> {
            if (WKReader.isEmpty(list)) {
                return;
            }
            for (WKChannelMember m : list) {
                if (m.channelID.equals(groupNo) && uid.equals(m.memberUID)) {
                    setDeleteMemberLayout(deleteMemberLayout, groupNo, uid);
                    if (m.forbiddenExpirationTime > 0) {
                        timeTv.setText(R.string.forbiddening);
                        setTime(m.forbiddenExpirationTime, context, contentTv);
                    } else {
                        contentTv.setVisibility(View.GONE);
                        timeTv.setText(R.string.forbidden_with_group_member);
                    }
                    break;
                }
            }
        });
        return view;
    }

    private void setDeleteMemberLayout(View deleteMemberLayout, String groupNo, String uid) {
        WKChannelMember member = WKIM.getInstance().getChannelMembersManager().getMember(groupNo, WKChannelType.GROUP, uid);
        WKChannelMember loginMember = WKIM.getInstance().getChannelMembersManager().getMember(groupNo, WKChannelType.GROUP, WKConfig.getInstance().getUid());
        if (member == null || loginMember == null || loginMember.role == WKChannelMemberRole.normal) {
            deleteMemberLayout.setVisibility(View.GONE);
            return;
        }
        if (loginMember.role == WKChannelMemberRole.admin || (loginMember.role == WKChannelMemberRole.manager && member.role == WKChannelMemberRole.normal)) {
            deleteMemberLayout.setVisibility(View.VISIBLE);
        } else {
            deleteMemberLayout.setVisibility(View.GONE);
        }
        SingleClickUtil.onSingleClick(deleteMemberLayout, v -> WKDialogUtils.getInstance().showDialog(
                deleteMemberLayout.getContext(), deleteMemberLayout.getContext().getString(R.string.delete_member), deleteMemberLayout.getContext().getString(R.string.delete_member_dialog_content), true, "", "", 0, Theme.colorAccount, index -> {
                    if (index == 1) {
                        List<String> uids = new ArrayList<>();
                        List<String> names = new ArrayList<>();
                        uids.add(member.memberUID);
                        names.add(member.memberName);
                        GroupManageModel.getInstance().deleteMember(groupNo, uids, names, (code, msg) -> {
                            if (code != HttpResponseCode.success) {
                                WKToastUtils.getInstance().showToastNormal(msg);
                            } else {
                                WKToastUtils.getInstance().showToastNormal(deleteMemberLayout.getContext().getString(R.string.str_success));
                                deleteMemberLayout.setVisibility(View.GONE);
                            }
                        });
                    }
                }));
    }

    private void setTime(long time, Context context, TextView contentTv) {
        long nowTime = WKTimeUtils.getInstance().getCurrentSeconds();
        long day = (time - nowTime) / (3600 * 24);
        long hour = (time - nowTime) / 3600;
        long min = (time - nowTime) / 60;
        String showText = String.format(context.getString(R.string.gm_forbidden_to_minute), 1);
        if (day > 0) {
            showText = String.format(context.getString(R.string.gm_forbidden_to_day), day);
        } else {
            if (hour > 0) {
                showText = String.format(context.getString(R.string.gm_forbidden_to_hour), hour);
            } else {
                if (min > 0) {
                    showText = String.format(context.getString(R.string.gm_forbidden_to_minute), min);
                }
            }
        }
        contentTv.setText(showText);
        contentTv.setVisibility(View.VISIBLE);
    }
}
