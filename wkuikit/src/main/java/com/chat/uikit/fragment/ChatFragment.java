package com.chat.uikit.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chat.base.base.WKBaseFragment;
import com.chat.base.common.WKCommonModel;
import com.chat.base.config.WKConfig;
import com.chat.base.config.WKSharedPreferencesUtil;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.ChatViewMenu;
import com.chat.base.entity.PopupMenuItem;
import com.chat.base.msgitem.WKContentType;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.ui.Theme;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.WKDialogUtils;
import com.chat.base.utils.WKReader;
import com.chat.base.utils.WKTimeUtils;
import com.chat.base.utils.WKToastUtils;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.uikit.R;
import com.chat.uikit.TabActivity;
import com.chat.uikit.WKUIKitApplication;
import com.chat.uikit.chat.adapter.ChatConversationAdapter;
import com.chat.uikit.chat.manager.WKIMUtils;
import com.chat.uikit.contacts.service.FriendModel;
import com.chat.uikit.databinding.FragChatConversationLayoutBinding;
import com.chat.uikit.enity.ChatConversationMsg;
import com.chat.uikit.group.service.GroupModel;
import com.chat.uikit.message.MsgModel;
import com.chat.uikit.search.remote.GlobalActivity;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKCMDKeys;
import com.xinbida.wukongim.entity.WKChannel;
import com.xinbida.wukongim.entity.WKChannelState;
import com.xinbida.wukongim.entity.WKChannelType;
import com.xinbida.wukongim.entity.WKReminder;
import com.xinbida.wukongim.entity.WKUIConversationMsg;
import com.xinbida.wukongim.message.type.WKConnectReason;
import com.xinbida.wukongim.message.type.WKConnectStatus;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 2019-11-12 14:55
 * 会话
 */
public class ChatFragment extends WKBaseFragment<FragChatConversationLayoutBinding> {

    private ChatConversationAdapter chatConversationAdapter;
    private Disposable disposable;
    private Disposable sortDisposable; // 排序任务的 Disposable
    private final List<Integer> refreshIds = new ArrayList<>();
    private Timer connectTimer;
    private TabActivity tabActivity;
    // 会话位置缓存，用于快速查找
    private final HashMap<String, Integer> conversationIndexMap = new HashMap<>();
    // 缓存未读数，避免频繁计算
    private int cachedUnreadCount = 0;
    private boolean isUnreadCountDirty = true;

    @Override
    protected boolean isShowBackLayout() {
        return false;
    }

    @Override
    protected FragChatConversationLayoutBinding getViewBinding() {
        return FragChatConversationLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        wkVBinding.textSwitcher.setTag(-1);
        wkVBinding.textSwitcher.setFactory(() -> {
            TextView textView = new TextView(getActivity());
            textView.setTextSize(22);
            Typeface face = Typeface.createFromAsset(getResources().getAssets(),
                    "fonts/mw_bold.ttf");
            textView.setTypeface(face);
            textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorDark));
            return textView;
        });
        wkVBinding.textSwitcher.setText(getString(R.string.app_name));
        //去除刷新条目闪动动画
        ((DefaultItemAnimator) Objects.requireNonNull(wkVBinding.recyclerView.getItemAnimator())).setSupportsChangeAnimations(false);
        chatConversationAdapter = new ChatConversationAdapter(new ArrayList<>());
        initAdapter(wkVBinding.recyclerView, chatConversationAdapter);
        chatConversationAdapter.setAnimationEnable(false);
        wkVBinding.refreshLayout.setEnableOverScrollDrag(true);
        wkVBinding.refreshLayout.setEnableLoadMore(false);
        wkVBinding.refreshLayout.setEnableRefresh(false);

        Theme.setPressedBackground(wkVBinding.deviceIv);
        Theme.setPressedBackground(wkVBinding.searchIv);
        Theme.setPressedBackground(wkVBinding.rightIv);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initListener() {
        wkVBinding.rightIv.setOnClickListener(view -> {
            List<PopupMenuItem> list = EndpointManager.getInstance().invokes(EndpointCategory.tabMenus, null);
            WKDialogUtils.getInstance().showScreenPopup(view, list);
        });

        wkVBinding.deviceIv.setOnClickListener(v -> EndpointManager.getInstance().invoke("show_pc_login_view", getActivity()));
        wkVBinding.searchIv.setOnClickListener(view1 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                @SuppressWarnings("unchecked") ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), new Pair<>(wkVBinding.searchIv, "searchView"));
                startActivity(new Intent(getActivity(), GlobalActivity.class), activityOptions.toBundle());
            } else {
                startActivity(new Intent(getActivity(), GlobalActivity.class));
            }
        });
        chatConversationAdapter.addChildClickViewIds(R.id.contentLayout);
        chatConversationAdapter.setOnItemChildClickListener((adapter, view, position) -> SingleClickUtil.determineTriggerSingleClick(view, v -> {
            ChatConversationMsg uiConversationMsg = (ChatConversationMsg) adapter.getItem(position);
            if (uiConversationMsg != null && uiConversationMsg.uiConversationMsg != null) {
                if (view.getId() == R.id.contentLayout) {
                    if (uiConversationMsg.uiConversationMsg.channelType == WKChannelType.COMMUNITY) {
                        EndpointManager.getInstance().invoke("show_community", uiConversationMsg.uiConversationMsg.channelID);
                    } else
                        WKIMUtils.getInstance().startChatActivity(new ChatViewMenu(getActivity(), uiConversationMsg.uiConversationMsg.channelID, uiConversationMsg.uiConversationMsg.channelType, 0, false));
                }
            }
        }));
        chatConversationAdapter.addListener((menu, item) -> {
            if (menu == ChatConversationAdapter.ItemMenu.delete) {
                WKDialogUtils.getInstance().showDialog(getActivity(), getString(R.string.delete_chat), getString(R.string.delete_conver_msg_tips), true, "", getString(R.string.base_delete), 0, ContextCompat.getColor(requireActivity(), R.color.red), index -> {
                    if (index == 1) {
                        List<WKReminder> list = WKIM.getInstance().getReminderManager().getReminders(item.channelID, item.channelType);
                        if (WKReader.isNotEmpty(list)) {
                            List<Long> reminderIds = new ArrayList<>();
                            for (WKReminder reminder : list) {
                                if (reminder.done == 0) {
                                    reminder.done = 1;
                                    reminderIds.add(reminder.reminderID);
                                }
                            }
                            if (WKReader.isNotEmpty(reminderIds))
                                MsgModel.getInstance().doneReminder(reminderIds);
                        }
                        MsgModel.getInstance().offsetMsg(item.channelID, item.channelType, null);
                        WKIM.getInstance().getReminderManager().saveOrUpdateReminders(list);
                        MsgModel.getInstance().clearUnread(item.channelID, item.channelType, 0, null);
                        boolean result = WKIM.getInstance().getConversationManager().deleteWitchChannel(item.channelID, item.channelType);
                        if (result) {
                            if (item.getWkChannel() != null && item.getWkChannel().top == 1) {
                                updateTop(item.channelID, item.channelType, 0);
                            }
                            WKIM.getInstance().getMsgManager().clearWithChannel(item.channelID, item.channelType);
                        }
                    }
                });
            } else if (menu == ChatConversationAdapter.ItemMenu.top) {
                boolean top = false;
                if (item.getWkChannel() != null) {
                    top = item.getWkChannel().top == 1;
                }
                updateTop(item.channelID, item.channelType, top ? 0 : 1);
            } else if (menu == ChatConversationAdapter.ItemMenu.mute) {
                boolean mute = false;
                if (item.getWkChannel() != null) {
                    mute = item.getWkChannel().mute == 1;
                }
                //免打扰
                if (item.channelType == WKChannelType.GROUP) {
                    GroupModel.getInstance().updateGroupSetting(item.channelID, "mute", mute ? 0 : 1, (code, msg) -> {
                        if (code != HttpResponseCode.success) {
                            WKToastUtils.getInstance().showToastNormal(msg);
                        }
                    });
                } else {
                    FriendModel.getInstance().updateUserSetting(item.channelID, "mute", mute ? 0 : 1, (code, msg) -> {
                        if (code != HttpResponseCode.success) {
                            WKToastUtils.getInstance().showToastNormal(msg);
                        }
                    });
                }
            }
        });
        //频道刷新监听 - 使用缓存快速查找
        WKIM.getInstance().getChannelManager().addOnRefreshChannelInfo("chat_fragment_refresh_channel", (channel, isEnd) -> {
            if (channel != null && !TextUtils.isEmpty(channel.channelID)) {
                int i = findConversationIndex(channel.channelID, channel.channelType);
                if (i >= 0) {
                    ChatConversationMsg msg = chatConversationAdapter.getData().get(i);
                    msg.uiConversationMsg.setWkChannel(channel);
                    // fixme 不能强制刷新整个列表，导致重新获取channel 频繁刷新UI卡顿
                    if (msg.isTop != channel.top) {
                        msg.isTop = channel.top;
                        sortMsg(chatConversationAdapter.getData());
                    } else {
                        msg.isRefreshChannelInfo = true;
                        msg.isResetCounter = true;
                        notifyRecycler(i, msg);
                    }
                    markUnreadCountDirty();
                    setAllCount();
                }
            }
        });
        //监听移除最近会话 - 使用缓存快速查找
        WKIM.getInstance().getConversationManager().addOnDeleteMsgListener("chat_fragment", (s, b) -> {
            if (!TextUtils.isEmpty(s)) {
                int i = findConversationIndex(s, b);
                if (i >= 0) {
                    boolean isResetCount = chatConversationAdapter.getData().get(i).uiConversationMsg.unreadCount > 0;
                    chatConversationAdapter.removeAt(i);
                    // 删除后重建索引
                    rebuildIndexCache();
                    if (isResetCount) {
                        markUnreadCountDirty();
                        setAllCount();
                    }
                }
            }
        });

        WKIM.getInstance().getCMDManager().addCmdListener("chat_fragment_cmd", wkCmd -> {
            if (wkCmd == null || TextUtils.isEmpty(wkCmd.cmdKey)) return;
            //监听正在输入
            switch (wkCmd.cmdKey) {
                case WKCMDKeys.wk_typing -> {
                    String channelID = wkCmd.paramJsonObject.optString("channel_id");
                    byte channelType = (byte) wkCmd.paramJsonObject.optInt("channel_type");
                    String from_uid = wkCmd.paramJsonObject.optString("from_uid");
                    String from_name = wkCmd.paramJsonObject.optString("from_name");
                    WKChannel channel = new WKChannel(from_uid, WKChannelType.PERSONAL);
                    channel.channelName = from_name;
                    if (TextUtils.isEmpty(from_name)) {
                        WKChannel tempChannel = WKIM.getInstance().getChannelManager().getChannel(from_uid, WKChannelType.PERSONAL);
                        if (tempChannel != null) {
                            channel.channelName = tempChannel.channelName;
                            channel.channelRemark = tempChannel.channelRemark;
                        }
                    }
                    if (from_uid.equals(WKConfig.getInstance().getUid())) return;
                    // 使用缓存快速查找
                    int i = findConversationIndex(channelID, channelType);
                    if (i >= 0) {
                        ChatConversationMsg msg = chatConversationAdapter.getData().get(i);
                        msg.isResetTyping = true;
                        msg.typingUserName = TextUtils.isEmpty(channel.channelRemark) ? channel.channelName : channel.channelRemark;
                        msg.typingStartTime = WKTimeUtils.getInstance().getCurrentSeconds();
                        notifyRecycler(i, msg);
                        if (disposable == null) {
                            startTimer();
                        }
                    }
                }
                case WKCMDKeys.wk_onlineStatus -> {
                    if (wkCmd.paramJsonObject != null) {
                        int device_flag = wkCmd.paramJsonObject.optInt("device_flag");
                        int online = wkCmd.paramJsonObject.optInt("online");
                        String uid = wkCmd.paramJsonObject.optString("uid");
                        if (uid.equals(WKConfig.getInstance().getUid()) && device_flag == 1) {
                            wkVBinding.deviceIv.setVisibility(online == 1 ? View.VISIBLE : View.GONE);
                            WKSharedPreferencesUtil.getInstance().putInt(WKConfig.getInstance().getUid() + "_pc_online", online);
                        }
                    }
                }
                case "sync_channel_state" -> {
                    String fromUID = wkCmd.paramJsonObject.optString("from_uid");
                    String channelId = wkCmd.paramJsonObject.optString("channel_id");
                    int channelType = wkCmd.paramJsonObject.optInt("channel_type");
                    if (channelId.equals(WKConfig.getInstance().getUid())) {
                        channelId = fromUID;
                    }
                    String finalChannelId = channelId;
                    byte finalChannelType = (byte) channelType;
                    WKCommonModel.getInstance().getChannelState(channelId, (byte) channelType, channelState -> {
                        if (channelState != null) {
                            int isCalling = 0;
                            if (WKReader.isNotEmpty(channelState.call_info.getCalling_participants())) {
                                isCalling = 1;
                            }
                            // 使用缓存快速查找
                            int i = findConversationIndex(finalChannelId, finalChannelType);
                            if (i >= 0) {
                                chatConversationAdapter.getData().get(i).isCalling = isCalling;
                                chatConversationAdapter.notifyItemChanged(i);
                            }
                        }
                    });
                }
            }
        });
        // 监听刷新消息 - 使用缓存快速查找
        WKIM.getInstance().getMsgManager().addOnRefreshMsgListener("chat_fragment", (msg, left) -> {
            if (msg == null) return;
            int i = findConversationIndex(msg.channelID, msg.channelType);
            if (i >= 0) {
                ChatConversationMsg convMsg = chatConversationAdapter.getData().get(i);
                if (convMsg.uiConversationMsg.getWkMsg() != null
                        && (convMsg.uiConversationMsg.getWkMsg().clientSeq == msg.clientSeq
                        || convMsg.uiConversationMsg.getWkMsg().clientMsgNO.equals(msg.clientMsgNO))) {
                    if (convMsg.uiConversationMsg.getWkMsg().status != msg.status || convMsg.uiConversationMsg.getWkMsg().remoteExtra.readedCount != msg.remoteExtra.readedCount) {
                        convMsg.isRefreshStatus = true;
                    }
                    if (convMsg.uiConversationMsg.getWkMsg().remoteExtra.revoke != msg.remoteExtra.revoke) {
                        convMsg.isResetContent = true;
                    }
                    convMsg.uiConversationMsg.getWkMsg().status = msg.status;
                    if (convMsg.uiConversationMsg.getWkMsg().remoteExtra.editedAt != msg.remoteExtra.editedAt) {
                        convMsg.uiConversationMsg.getWkMsg().remoteExtra.editedAt = msg.remoteExtra.editedAt;
                        convMsg.uiConversationMsg.getWkMsg().remoteExtra.contentEdit = msg.remoteExtra.contentEdit;
                        WKIMUtils.getInstance().resetMsgProhibitWord(convMsg.uiConversationMsg.getWkMsg());
                    }
                    convMsg.uiConversationMsg.getWkMsg().remoteExtra.revoker = msg.remoteExtra.revoker;
                    convMsg.uiConversationMsg.getWkMsg().remoteExtra.revoke = msg.remoteExtra.revoke;
                    convMsg.uiConversationMsg.getWkMsg().remoteExtra.unreadCount = msg.remoteExtra.unreadCount;
                    convMsg.uiConversationMsg.getWkMsg().remoteExtra.readedCount = msg.remoteExtra.readedCount;
                    convMsg.uiConversationMsg.getWkMsg().messageID = msg.messageID;
                    refreshIds.add(i);
                }
            }
            if (left && WKReader.isNotEmpty(refreshIds)) {
                for (int j = 0, size = refreshIds.size(); j < size; j++) {
                    int idx = refreshIds.get(j);
                    notifyRecycler(idx, chatConversationAdapter.getData().get(idx));
                }
                refreshIds.clear();
            }
        });
        WKIM.getInstance().getMsgManager().addOnClearMsgListener("chat_fragment", (channelID, channelType, fromUID) -> {
            if (TextUtils.isEmpty(fromUID)) {
                // 使用缓存快速查找
                int i = findConversationIndex(channelID, channelType);
                if (i >= 0) {
                    ChatConversationMsg msg = chatConversationAdapter.getData().get(i);
                    msg.uiConversationMsg.setWkMsg(null);
                    msg.isResetContent = true;
                    notifyRecycler(i, msg);
                }
            }
        });
        WKIM.getInstance().getReminderManager().addOnNewReminderListener("chat_fragment", list -> {
            if (WKReader.isEmpty(list) || WKReader.isEmpty(chatConversationAdapter.getData()))
                return;
            for (WKReminder reader : list) {
                for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                    if (reader.done == 0
                            && !TextUtils.isEmpty(reader.messageID)
                            && chatConversationAdapter.getData().get(i).uiConversationMsg.getWkMsg() != null
                            && !TextUtils.isEmpty(chatConversationAdapter.getData().get(i).uiConversationMsg.getWkMsg().messageID)
                            && chatConversationAdapter.getData().get(i).uiConversationMsg.getWkMsg() != null
                            && reader.messageID.equals(chatConversationAdapter.getData().get(i).uiConversationMsg.getWkMsg().messageID)) {
                        chatConversationAdapter.getData().get(i).isResetReminders = true;
                        notifyRecycler(i, chatConversationAdapter.getData().get(i));
                        break;
                    }
                }
            }
        });
        // 监听刷新最近列表
        WKIM.getInstance().getConversationManager().addOnRefreshMsgListListener("chat_fragment", list -> {
            if (WKReader.isEmpty(list)) {
                return;
            }
            if (list.size() == 1) {
                resetData(list.get(0), true);
                return;
            }

            if (chatConversationAdapter.getData().isEmpty()) {
                List<ChatConversationMsg> uiList = new ArrayList<>();
                for (WKUIConversationMsg uiConversationMsg : list) {
                    ChatConversationMsg msg = new ChatConversationMsg(uiConversationMsg);
                    uiList.add(msg);
                }
                sortMsg(uiList);
                setAllCount();
                return;
            }
            List<ChatConversationMsg> uiList = new ArrayList<>();
            // 多条
            for (WKUIConversationMsg uiConversationMsg : list) {
                boolean isAdd = true;
                for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                    if (!TextUtils.isEmpty(chatConversationAdapter.getData().get(i).uiConversationMsg.channelID) && !TextUtils.isEmpty(uiConversationMsg.channelID) && chatConversationAdapter.getData().get(i).uiConversationMsg.channelID.equals(uiConversationMsg.channelID) && chatConversationAdapter.getData().get(i).uiConversationMsg.channelType == uiConversationMsg.channelType) {
//                            if (!isEnd) {
//                                isAdd = false;
//                                chatConversationAdapter.getData().get(i).uiConversationMsg = uiConversationMsg;
//                                break;
//                            }
                        isAdd = false;
                        if (chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgSeq != uiConversationMsg.lastMsgSeq || chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp != uiConversationMsg.lastMsgTimestamp || (chatConversationAdapter.getData().get(i).uiConversationMsg.getWkMsg() != null && uiConversationMsg.getWkMsg() != null && !chatConversationAdapter.getData().get(i).uiConversationMsg.getWkMsg().clientMsgNO.equals(uiConversationMsg.getWkMsg().clientMsgNO))) {
                            chatConversationAdapter.getData().get(i).isResetTyping = true;
                            chatConversationAdapter.getData().get(i).typingUserName = "";
                            chatConversationAdapter.getData().get(i).typingStartTime = 0;
                            chatConversationAdapter.getData().get(i).isRefreshStatus = true;
                        }
                        if (chatConversationAdapter.getData().get(i).uiConversationMsg.unreadCount != uiConversationMsg.unreadCount) {
                            chatConversationAdapter.getData().get(i).isResetCounter = true;
                        }
                        if (chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp != uiConversationMsg.lastMsgTimestamp) {
                            chatConversationAdapter.getData().get(i).isResetTime = true;
                        }
                        chatConversationAdapter.getData().get(i).uiConversationMsg.setWkMsg(uiConversationMsg.getWkMsg());
                        if (!chatConversationAdapter.getData().get(i).uiConversationMsg.clientMsgNo.equals(uiConversationMsg.clientMsgNo)) {
                            chatConversationAdapter.getData().get(i).isResetContent = true;
                        }
                        WKIMUtils.getInstance().resetMsgProhibitWord(chatConversationAdapter.getData().get(i).uiConversationMsg.getWkMsg());
                        // todo 比较是否真的改过提醒内容
                        chatConversationAdapter.getData().get(i).isResetReminders = true;
                        chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgSeq = uiConversationMsg.lastMsgSeq;
                        chatConversationAdapter.getData().get(i).uiConversationMsg.clientMsgNo = uiConversationMsg.clientMsgNo;
                        chatConversationAdapter.getData().get(i).uiConversationMsg.unreadCount = uiConversationMsg.unreadCount;
                        chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp = uiConversationMsg.lastMsgTimestamp;
                        chatConversationAdapter.getData().get(i).uiConversationMsg.setRemoteMsgExtra(uiConversationMsg.getRemoteMsgExtra());

                        chatConversationAdapter.getData().get(i).uiConversationMsg.setReminderList(uiConversationMsg.getReminderList());
                        chatConversationAdapter.getData().get(i).uiConversationMsg.localExtraMap = null;
                        notifyRecycler(i, chatConversationAdapter.getData().get(i));
                        setAllCount();
                        break;
                    }
                }
                if (isAdd) {
                    uiList.add(new ChatConversationMsg(uiConversationMsg));
                }
            }
//            if (!uiList.isEmpty()) {
//                uiList.addAll(chatConversationAdapter.getData());
//                sortMsg(uiList);
//                setAllCount();
//            } else {
//                resetData(list.get(0), true);
//            }
            uiList.addAll(chatConversationAdapter.getData());
            sortMsg(uiList);
            setAllCount();
        });
//        WKIM.getInstance().getConversationManager().addOnRefreshMsgListener("chat_fragment", this::resetData);
        // 监听连接状态
        WKIM.getInstance().getConnectionManager().addOnConnectionStatusListener("chat_fragment", (i, reason) -> {
            if (wkVBinding.textSwitcher.getTag() != null) {
                Object tag = wkVBinding.textSwitcher.getTag();
                if (tag instanceof Integer) {
                    int tag1 = (int) tag;
                    if (tag1 == i) {
                        return;
                    }
                }
            }
            if (i == WKConnectStatus.syncMsg) {
                wkVBinding.textSwitcher.setText(getString(R.string.sync_msg));
            } else if (i == WKConnectStatus.success) {
                wkVBinding.textSwitcher.setText(getString(R.string.app_name));
            } else if (i == WKConnectStatus.connecting) {
                wkVBinding.textSwitcher.setText(getString(R.string.connecting));
            } else if (i == WKConnectStatus.noNetwork) {
                wkVBinding.textSwitcher.setText(getString(R.string.network_error_tips));
            } else if (i == WKConnectStatus.kicked) {
                int from = 0;
                if (reason.equals(WKConnectReason.ReasonConnectKick)) {
                    from = 1;
                }
                WKUIKitApplication.getInstance().exitLogin(from);
            }
            wkVBinding.textSwitcher.setTag(i);
            if (i != WKConnectStatus.success && i != WKConnectStatus.syncMsg) {
                startConnectTimer();
            } else {
                EndpointManager.getInstance().invoke("wk_close_disconnect_screen", null);
                stopConnectTimer();
            }
        });
        EndpointManager.getInstance().setMethod("", EndpointCategory.wkExitChat, object -> {
            if (object != null) {
                WKChannel channel = (WKChannel) object;
                // 使用缓存快速查找
                int i = findConversationIndex(channel.channelID, channel.channelType);
                if (i >= 0) {
                    boolean isResetCount = chatConversationAdapter.getData().get(i).uiConversationMsg.unreadCount > 0;
                    chatConversationAdapter.removeAt(i);
                    rebuildIndexCache();
                    if (isResetCount) {
                        markUnreadCountDirty();
                        setAllCount();
                    }
                }
            }
            return null;
        });

        EndpointManager.getInstance().setMethod("chat_cover", EndpointCategory.refreshProhibitWord, object -> {
            if (WKReader.isEmpty(chatConversationAdapter.getData())) {
                return 1;
            }
            for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                if (chatConversationAdapter.getData().get(i).uiConversationMsg != null && chatConversationAdapter.getData().get(i).uiConversationMsg.getWkMsg() != null && chatConversationAdapter.getData().get(i).uiConversationMsg.getWkMsg().type == WKContentType.WK_TEXT) {
                    WKIMUtils.getInstance().resetMsgProhibitWord(chatConversationAdapter.getData().get(i).uiConversationMsg.getWkMsg());
                    chatConversationAdapter.notifyItemChanged(i);
                }
            }
            return 1;
        });

        EndpointManager.getInstance().setMethod("refresh_conversation_calling", object -> {
            if (WKReader.isNotEmpty(MsgModel.getInstance().channelStatus)) {
                for (WKChannelState state : MsgModel.getInstance().channelStatus) {
                    for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                        if (chatConversationAdapter.getData().get(i).uiConversationMsg != null
                                && !TextUtils.isEmpty(chatConversationAdapter.getData().get(i).uiConversationMsg.channelID)
                                && state.channel_id.equals(chatConversationAdapter.getData().get(i).uiConversationMsg.channelID)) {
                            chatConversationAdapter.getData().get(i).isCalling = state.calling;
                            chatConversationAdapter.notifyItemChanged(i);
                        }
                    }
                }
                return null;
            }
            for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                if (chatConversationAdapter.getData().get(i).isCalling == 1) {
                    chatConversationAdapter.getData().get(i).isCalling = 0;
                    chatConversationAdapter.notifyItemChanged(i);
                }
            }
            return null;
        });
    }


    @Override
    protected void initData() {
        getData();
    }

    private void getData() {
        getChatMsg();
    }


    private void getChatMsg() {
        WKIM.getInstance().getConversationManager().getAll(list -> {
            List<ChatConversationMsg> tempList = new ArrayList<>();
            if (WKReader.isNotEmpty(list)) {
                for (int i = 0, size = list.size(); i < size; i++) {
                    tempList.add(new ChatConversationMsg(list.get(i)));
                }
            }
            AndroidUtilities.runOnUIThread(() -> sortMsg(tempList));
        });

//        List<ChatConversationMsg> list = new ArrayList<>();
//        List<WKUIConversationMsg> tempList = WKIM.getInstance().getConversationManager().getAll();
//        if (WKReader.isNotEmpty(tempList)) {
//            for (int i = 0, size = tempList.size(); i < size; i++) {
//                list.add(new ChatConversationMsg(tempList.get(i)));
//            }
//        }
//        return list;
    }

    private void setAllCount() {
        if (!isUnreadCountDirty) {
            // 未读数未变化，直接使用缓存
            if (tabActivity != null) {
                tabActivity.setMsgCount(cachedUnreadCount);
            }
            return;
        }
        int allCount = 0;
        List<ChatConversationMsg> data = chatConversationAdapter.getData();
        for (int i = 0, size = data.size(); i < size; i++) {
            ChatConversationMsg msg = data.get(i);
            if (msg.uiConversationMsg.getWkChannel() != null && msg.uiConversationMsg.getWkChannel().mute == 0)
                allCount = allCount + msg.uiConversationMsg.unreadCount;
        }
        cachedUnreadCount = allCount;
        isUnreadCountDirty = false;
        if (tabActivity != null) {
            tabActivity.setMsgCount(allCount);
        }
        // EndpointManager.getInstance().invoke("refresh_chat_unread_count",allCount);
    }

    // 生成会话的唯一键
    private String getChannelKey(String channelID, byte channelType) {
        return channelID + "_" + channelType;
    }

    // 根据 channelID 和 channelType 快速查找位置
    private int findConversationIndex(String channelID, byte channelType) {
        if (TextUtils.isEmpty(channelID)) return -1;
        String key = getChannelKey(channelID, channelType);
        Integer index = conversationIndexMap.get(key);
        if (index != null && index >= 0 && index < chatConversationAdapter.getData().size()) {
            ChatConversationMsg msg = chatConversationAdapter.getData().get(index);
            if (msg.uiConversationMsg.channelID.equals(channelID) && msg.uiConversationMsg.channelType == channelType) {
                return index;
            }
        }
        // 缓存失效，遍历查找并更新缓存
        List<ChatConversationMsg> data = chatConversationAdapter.getData();
        for (int i = 0, size = data.size(); i < size; i++) {
            ChatConversationMsg msg = data.get(i);
            if (!TextUtils.isEmpty(msg.uiConversationMsg.channelID)
                    && msg.uiConversationMsg.channelID.equals(channelID)
                    && msg.uiConversationMsg.channelType == channelType) {
                conversationIndexMap.put(key, i);
                return i;
            }
        }
        return -1;
    }

    // 重建索引缓存
    private void rebuildIndexCache() {
        conversationIndexMap.clear();
        List<ChatConversationMsg> data = chatConversationAdapter.getData();
        for (int i = 0, size = data.size(); i < size; i++) {
            ChatConversationMsg msg = data.get(i);
            if (!TextUtils.isEmpty(msg.uiConversationMsg.channelID)) {
                String key = getChannelKey(msg.uiConversationMsg.channelID, msg.uiConversationMsg.channelType);
                conversationIndexMap.put(key, i);
            }
        }
    }

    // 标记未读数需要重新计算
    private void markUnreadCountDirty() {
        isUnreadCountDirty = true;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        tabActivity = (TabActivity) context;
    }

    private void resetChildData(WKUIConversationMsg uiConversationMsg, boolean isEnd) {
        if (WKReader.isNotEmpty(chatConversationAdapter.getData())) {
            boolean isAdd = true;
            for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                boolean isBreak = false;
                if (WKReader.isNotEmpty(chatConversationAdapter.getData().get(i).childList)) {
                    for (int j = 0, len = chatConversationAdapter.getData().get(i).childList.size(); j < len; j++) {
                        if (chatConversationAdapter.getData().get(i).childList.get(j).uiConversationMsg.channelID.equals(uiConversationMsg.channelID)) {
                            chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp = uiConversationMsg.lastMsgTimestamp;
                            chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgSeq = uiConversationMsg.lastMsgSeq;
                            chatConversationAdapter.getData().get(i).uiConversationMsg.clientMsgNo = uiConversationMsg.clientMsgNo;
                            chatConversationAdapter.getData().get(i).uiConversationMsg.unreadCount += uiConversationMsg.unreadCount;
                            notifyRecycler(i, chatConversationAdapter.getData().get(i));
                            isBreak = true;
                            isAdd = false;
                        }
                    }
                }
                if (isBreak) break;
            }
            if (isAdd) {
                WKUIConversationMsg msg = new WKUIConversationMsg();
                msg.channelID = uiConversationMsg.parentChannelID;
                msg.channelType = uiConversationMsg.parentChannelType;
                msg.clientMsgNo = uiConversationMsg.clientMsgNo;
                msg.lastMsgSeq = uiConversationMsg.lastMsgSeq;
                msg.lastMsgTimestamp = uiConversationMsg.lastMsgTimestamp;
                msg.unreadCount = uiConversationMsg.unreadCount;
                msg.setReminderList(uiConversationMsg.getReminderList());
                msg.setRemoteMsgExtra(uiConversationMsg.getRemoteMsgExtra());

                ChatConversationMsg chatConversationMsg = new ChatConversationMsg(msg);
                ChatConversationMsg child = new ChatConversationMsg(uiConversationMsg);
                chatConversationMsg.childList = new ArrayList<>();
                chatConversationMsg.childList.add(child);
                if (!isEnd) {
                    chatConversationAdapter.addData(chatConversationMsg);
                } else {
                    int insertIndex = getInsertIndex(msg);
                    chatConversationAdapter.addData(insertIndex, chatConversationMsg);
                }
            }
        }
    }

    private int msgCount = 0;

    private void resetData(WKUIConversationMsg uiConversationMsg, boolean isEnd) {
        if (uiConversationMsg == null) {
            return;
        }
        // || (uiConversationMsg.getWkChannel() != null && uiConversationMsg.getWkChannel().follow == 0 && uiConversationMsg.channelType == WKChannelType.PERSONAL)
        if (uiConversationMsg.isDeleted == 1 || TextUtils.equals(uiConversationMsg.channelID, "0")) {
            if (isEnd) {
                sortMsg(chatConversationAdapter.getData());
            }
            return;
        }
        if (!TextUtils.isEmpty(uiConversationMsg.parentChannelID)) {
            resetChildData(uiConversationMsg, isEnd);
            return;
        }
        boolean isAdd = true;
        int index = -1;
        boolean isSort = false;
        if (WKReader.isNotEmpty(chatConversationAdapter.getData())) {
            for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                if (!TextUtils.isEmpty(chatConversationAdapter.getData().get(i).uiConversationMsg.channelID) && !TextUtils.isEmpty(uiConversationMsg.channelID) && chatConversationAdapter.getData().get(i).uiConversationMsg.channelID.equals(uiConversationMsg.channelID) && chatConversationAdapter.getData().get(i).uiConversationMsg.channelType == uiConversationMsg.channelType) {
                    if (!isEnd) {
                        isAdd = false;
                        chatConversationAdapter.getData().get(i).uiConversationMsg = uiConversationMsg;
                        break;
                    }
                    if (chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgSeq != uiConversationMsg.lastMsgSeq || chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp != uiConversationMsg.lastMsgTimestamp || (chatConversationAdapter.getData().get(i).uiConversationMsg.getWkMsg() != null && uiConversationMsg.getWkMsg() != null && !chatConversationAdapter.getData().get(i).uiConversationMsg.getWkMsg().clientMsgNO.equals(uiConversationMsg.getWkMsg().clientMsgNO))) {
                        isSort = true;
                        chatConversationAdapter.getData().get(i).isResetTyping = true;
                        chatConversationAdapter.getData().get(i).typingUserName = "";
                        chatConversationAdapter.getData().get(i).typingStartTime = 0;
                        chatConversationAdapter.getData().get(i).isRefreshStatus = true;
                        index = i;
                    }
                    if (chatConversationAdapter.getData().get(i).uiConversationMsg.unreadCount != uiConversationMsg.unreadCount) {
                        chatConversationAdapter.getData().get(i).isResetCounter = true;
                    }
                    if (chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp != uiConversationMsg.lastMsgTimestamp) {
                        chatConversationAdapter.getData().get(i).isResetTime = true;
                    }
                    chatConversationAdapter.getData().get(i).uiConversationMsg.setWkMsg(uiConversationMsg.getWkMsg());
                    if (!chatConversationAdapter.getData().get(i).uiConversationMsg.clientMsgNo.equals(uiConversationMsg.clientMsgNo)) {
                        chatConversationAdapter.getData().get(i).isResetContent = true;
                    }
                    WKIMUtils.getInstance().resetMsgProhibitWord(chatConversationAdapter.getData().get(i).uiConversationMsg.getWkMsg());
                    // todo 比较是否真的改过提醒内容
                    chatConversationAdapter.getData().get(i).isResetReminders = true;
                    chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgSeq = uiConversationMsg.lastMsgSeq;
                    chatConversationAdapter.getData().get(i).uiConversationMsg.clientMsgNo = uiConversationMsg.clientMsgNo;
                    chatConversationAdapter.getData().get(i).uiConversationMsg.unreadCount = uiConversationMsg.unreadCount;
                    chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp = uiConversationMsg.lastMsgTimestamp;
                    chatConversationAdapter.getData().get(i).uiConversationMsg.setRemoteMsgExtra(uiConversationMsg.getRemoteMsgExtra());

                    chatConversationAdapter.getData().get(i).uiConversationMsg.setReminderList(uiConversationMsg.getReminderList());
                    chatConversationAdapter.getData().get(i).uiConversationMsg.localExtraMap = null;
                    isAdd = false;
                    notifyRecycler(i, chatConversationAdapter.getData().get(i));
                    setAllCount();
                    break;
                }
            }
        }
        if (!isEnd) msgCount++;

        if (isAdd) {
            if (!isEnd) {
                chatConversationAdapter.addData(new ChatConversationMsg(uiConversationMsg));
            } else {
                int insertIndex = getInsertIndex(uiConversationMsg);
                chatConversationAdapter.addData(insertIndex, new ChatConversationMsg(uiConversationMsg));
            }
            setAllCount();
        }
        if (isEnd) {
            if (isSort && msgCount == 0) {
                int insertIndex = getInsertIndex(uiConversationMsg);
                if (insertIndex != index) {
                    if (index != -1) chatConversationAdapter.removeAt(index);
                    chatConversationAdapter.addData(insertIndex, new ChatConversationMsg(uiConversationMsg));
                }
            } else {
                if (msgCount > 0) {
                    msgCount = 0;
                    sortMsg(chatConversationAdapter.getData());
                }
            }
        }
    }

    //排序消息 - 在后台线程执行耗时操作
    private void sortMsg(List<ChatConversationMsg> list) {
        // 取消之前的排序任务，避免并发问题
        if (sortDisposable != null && !sortDisposable.isDisposed()) {
            sortDisposable.dispose();
        }

        // 复制列表避免并发修改
        List<ChatConversationMsg> listCopy = new ArrayList<>(list);

        sortDisposable = Observable.fromCallable(() -> {
                    // 在后台线程执行分组和排序
                    groupMsg(listCopy);
                    // 使用 Long.compare 避免溢出
                    listCopy.sort((conversationMsg, t1) ->
                            Long.compare(t1.uiConversationMsg.lastMsgTimestamp, conversationMsg.uiConversationMsg.lastMsgTimestamp));

                    List<ChatConversationMsg> topList = new ArrayList<>();
                    List<ChatConversationMsg> normalList = new ArrayList<>();
                    for (int i = 0, size = listCopy.size(); i < size; i++) {
                        ChatConversationMsg msg = listCopy.get(i);
                        if (msg.uiConversationMsg.getWkChannel() != null && msg.uiConversationMsg.getWkChannel().top == 1) {
                            topList.add(msg);
                        } else {
                            normalList.add(msg);
                        }
                    }
                    List<ChatConversationMsg> tempList = new ArrayList<>(topList.size() + normalList.size());
                    tempList.addAll(topList);
                    tempList.addAll(normalList);
                    return tempList;
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tempList -> {
                    chatConversationAdapter.setList(tempList);
                    rebuildIndexCache();
                    markUnreadCountDirty();
                    setAllCount();
                }, throwable -> {
                    // 发生错误时的降级处理
                    AndroidUtilities.runOnUIThread(() -> {
                        chatConversationAdapter.setList(listCopy);
                        rebuildIndexCache();
                        markUnreadCountDirty();
                        setAllCount();
                    });
                });
    }

    //检测正在输入的定时器
    private void startTimer() {
        Observable.interval(0, 1, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<>() {
            @Override
            public void onComplete() {
            }

            @Override
            public void onError(@NonNull Throwable e) {
            }

            @Override
            public void onSubscribe(@NonNull Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(@NonNull Long value) {
                boolean isCancel = true;
                for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                    if (chatConversationAdapter.getData().get(i).typingStartTime > 0) {
                        long typingStartTime = chatConversationAdapter.getData().get(i).typingStartTime;
                        if (WKTimeUtils.getInstance().getCurrentSeconds() - typingStartTime >= 8) {
                            chatConversationAdapter.getData().get(i).isResetTyping = true;
                            chatConversationAdapter.getData().get(i).typingStartTime = 0;
                            chatConversationAdapter.getData().get(i).typingUserName = "";
                            chatConversationAdapter.getData().get(i).isResetContent = true;
                            notifyRecycler(i, chatConversationAdapter.getData().get(i));
//                                    chatConversationAdapter.notifyItemChanged(i, chatConversationAdapter.getData().get(i));
                        }
                        isCancel = false;
                    }
                }
                if (disposable != null && isCancel) {
                    disposable.dispose();
                    disposable = null;
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
        // 取消排序任务
        if (sortDisposable != null) {
            sortDisposable.dispose();
            sortDisposable = null;
        }
        // 清理缓存
        conversationIndexMap.clear();
        WKIM.getInstance().getConversationManager().removeOnRefreshMsgListListener("chat_fragment");
        WKIM.getInstance().getConversationManager().removeOnRefreshMsgListener("chat_fragment");
        WKIM.getInstance().getConversationManager().removeOnDeleteMsgListener("chat_fragment");
        WKIM.getInstance().getCMDManager().removeCmdListener("chat_fragment_cmd");
        WKIM.getInstance().getMsgManager().removeRefreshMsgListener("chat_fragment");
        WKIM.getInstance().getConnectionManager().removeOnConnectionStatusListener("chat_fragment");
        WKIM.getInstance().getMsgManager().removeSendMsgAckListener("chat_fragment");
        WKIM.getInstance().getReminderManager().removeNewReminderListener("chat_fragment");
    }

    @Override
    public void onResume() {
        super.onResume();
        int pcOnline = WKSharedPreferencesUtil.getInstance().getInt(WKConfig.getInstance().getUid() + "_pc_online");
        wkVBinding.deviceIv.setVisibility(pcOnline == 1 ? View.VISIBLE : View.GONE);
//        String appLoginType = String.format(getString(R.string.pc_login), getString(R.string.app_name));
//        int muteForApp = WKSharedPreferencesUtil.getInstance().getInt(WKConfig.getInstance().getUid() + "_mute_of_app");
//        if (muteForApp == 1) {
//            pcLoginTv.setText(String.format("%s %s", appLoginType, getString(R.string.wk_kit_phone_notice_close)));
//        } else pcLoginTv.setText(appLoginType);
        EndpointManager.getInstance().setMethod("scroll_to_unread_channel", object -> {
            scrollToUnreadChannel();
            return null;
        });
    }

    private void startConnectTimer() {
        if (connectTimer == null) {
            connectTimer = new Timer();
        }
        connectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                EndpointManager.getInstance().invoke("show_disconnect_screen", getContext());
            }
        }, 1000);
    }

    private void stopConnectTimer() {
        if (connectTimer != null) {
            connectTimer.cancel();
            connectTimer = null;
        }
    }

    private int getTopChatCount() {
        int count = 0;
        for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
            if (chatConversationAdapter.getData().get(i).uiConversationMsg.getWkChannel() != null && chatConversationAdapter.getData().get(i).uiConversationMsg.getWkChannel().top == 1)
                count++;
        }
        return count;
    }

    private int getInsertIndex(WKUIConversationMsg msg) {
        if (msg.getWkChannel() != null && msg.getWkChannel().top == 1) return 0;
        return getTopChatCount();
    }

    private void notifyRecycler(int index, ChatConversationMsg msg) {
        if (wkVBinding.recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE || (!wkVBinding.recyclerView.isComputingLayout())) {
            chatConversationAdapter.notifyItemChanged(index, msg);
        }
    }

    private void updateTop(String channelID, byte channelType, int top) {
        if (channelType == WKChannelType.PERSONAL) {
            FriendModel.getInstance().updateUserSetting(channelID, "top", top, (code, msg) -> {
                if (code != HttpResponseCode.success) {
                    WKToastUtils.getInstance().showToastNormal(msg);
                }
            });
        } else {
            GroupModel.getInstance().updateGroupSetting(channelID, "top", top, (code, msg) -> {
                if (code != HttpResponseCode.success) {
                    WKToastUtils.getInstance().showToastNormal(msg);
                }
            });
        }

    }

    private void groupMsg(List<ChatConversationMsg> list) {
        // 将消息分组
        HashMap<String, List<ChatConversationMsg>> msgMap = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            if (!TextUtils.isEmpty(list.get(i).uiConversationMsg.parentChannelID)) {
                String key = list.get(i).uiConversationMsg.parentChannelID + "@" + list.get(i).uiConversationMsg.parentChannelType;
                List<ChatConversationMsg> tempList = null;
                if (msgMap.containsKey(key)) {
                    tempList = msgMap.get(key);
                }
                if (tempList == null) tempList = new ArrayList<>();
                tempList.add(list.get(i));
                msgMap.put(key, tempList);
                list.remove(i);
                i--;
            }
        }

        if (!msgMap.isEmpty()) {
            for (String key : msgMap.keySet()) {
                List<ChatConversationMsg> msgList = msgMap.get(key);
                WKUIConversationMsg lastMsg = new WKUIConversationMsg();
//                if (msgList != null && msgList.size() > 0) {
//                    msg.channelID = msgList.get(0).uiConversationMsg.parentChannelID;
//                    msg.channelType = msgList.get(0).uiConversationMsg.parentChannelType;
//                }
                //   Log.e("消息信息",msg.clientMsgNo+"");
                //  ChatConversationMsg lastMsg = new ChatConversationMsg(msg);
                //lastMsg.childList = msgList;
                ChatConversationMsg lastConvMsg = null;
                if (WKReader.isNotEmpty(msgList)) {
                    lastMsg.channelID = msgList.get(0).uiConversationMsg.parentChannelID;
                    lastMsg.channelType = msgList.get(0).uiConversationMsg.parentChannelType;
                    int unreadCount = 0;
                    List<WKReminder> reminderList = new ArrayList<>();
                    for (int i = 0, size = msgList.size(); i < size; i++) {
                        WKUIConversationMsg msg = msgList.get(i).uiConversationMsg;
                        if (msg.lastMsgSeq > lastMsg.lastMsgSeq) {
                            lastMsg.lastMsgSeq = msg.lastMsgSeq;
                        }
                        if (msg.lastMsgTimestamp > lastMsg.lastMsgTimestamp) {
                            lastMsg.lastMsgTimestamp = msg.lastMsgTimestamp;
                            lastMsg.clientMsgNo = msg.clientMsgNo;
                        }
                        unreadCount += msg.unreadCount;
                        List<WKReminder> tempReminders = msg.getReminderList();
                        if (WKReader.isNotEmpty(tempReminders)) {
                            reminderList.addAll(tempReminders);
                        }
                    }
                    lastMsg.unreadCount = unreadCount;
                    lastMsg.setReminderList(reminderList);

                    lastConvMsg = new ChatConversationMsg(lastMsg);
                    lastConvMsg.childList = msgList;
                }
                if (lastConvMsg != null)
                    list.add(lastConvMsg);
            }
        }
    }

    long lastMessageTime = 0L;

    private void scrollToUnreadChannel() {
        long firstTime = 0L;
        int firstIndex = 0;
        boolean isScrollToFirstIndex = true;
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) wkVBinding.recyclerView.getLayoutManager();
        for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
            if (chatConversationAdapter.getData().get(i).getUnReadCount() > 0 && chatConversationAdapter.getData().get(i).uiConversationMsg.getWkChannel() != null && chatConversationAdapter.getData().get(i).uiConversationMsg.getWkChannel().mute == 0) {
                if (firstTime == 0) {
                    firstTime = chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp;
                    firstIndex = i;
                }
                if (lastMessageTime == 0 || lastMessageTime > chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp) {
                    lastMessageTime = chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp;
                    if (linearLayoutManager != null) {
                        linearLayoutManager.scrollToPositionWithOffset(i, 0);
                    }
                    isScrollToFirstIndex = false;
                    break;
                }
            }

        }
        if (isScrollToFirstIndex) {
            lastMessageTime = firstTime;
            if (linearLayoutManager != null) {
                linearLayoutManager.scrollToPositionWithOffset(firstIndex, 0);
            }
        }
    }

}
