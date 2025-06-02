package com.chat.advanced.ui;

import android.widget.TextView;

import com.chat.advanced.R;
import com.chat.advanced.databinding.ActMsgRemindLayoutBinding;
import com.chat.advanced.service.AdvancedModel;
import com.chat.base.base.WKBaseActivity;
import com.chat.base.net.HttpResponseCode;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKChannel;
import com.xinbida.wukongim.entity.WKChannelExtras;
import com.xinbida.wukongim.entity.WKChannelType;

/**
 * 2020-10-20 11:15
 * 消息提醒设置
 */
public class MsgRemindActivity extends WKBaseActivity<ActMsgRemindLayoutBinding> {

    private String channelID;
    private byte channelType;
    WKChannel channel;

    @Override
    protected ActMsgRemindLayoutBinding getViewBinding() {
        return ActMsgRemindLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.msg_remind_setting);
    }

    @Override
    protected void initPresenter() {
        channelType = getIntent().getByteExtra("channelType", channelType);
        channelID = getIntent().getStringExtra("channelID");
    }

    @Override
    protected void initView() {
        resetData();
    }

    @Override
    protected void initListener() {

        wkVBinding.screenshotSwitchView.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed()) {
                if (channelType == WKChannelType.GROUP)
                    AdvancedModel.Companion.getInstance().updateGroupSetting(channelID, "screenshot", b ? 1 : 0, (code, msg) -> {
                        if (code != HttpResponseCode.success) {
                            wkVBinding.screenshotSwitchView.setChecked(!b);
                            showToast(msg);
                        }
                    });
                else {
                    AdvancedModel.Companion.getInstance().updateUserSetting(channelID, "screenshot", b ? 1 : 0, (code, msg) -> {
                        if (code != HttpResponseCode.success) {
                            wkVBinding.screenshotSwitchView.setChecked(!b);
                            showToast(msg);
                        }
                    });
                }
            }
        });

        wkVBinding.joinGroupSwitchView.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed()) {
                if (channelType == WKChannelType.GROUP) {
                    AdvancedModel.Companion.getInstance().updateGroupSetting(channelID, "join_group_remind", b ? 1 : 0, (code, msg) -> {
                        if (code != HttpResponseCode.success) {
                            wkVBinding.joinGroupSwitchView.setChecked(!b);
                            showToast(msg);
                        }
                    });
                }
            }
        });
        wkVBinding.revokeSwitchView.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed()) {
                if (channelType == WKChannelType.GROUP) {
                    AdvancedModel.Companion.getInstance().updateGroupSetting(channelID, "revoke_remind", b ? 1 : 0, (code, msg) -> {
                        if (code != HttpResponseCode.success) {
                            wkVBinding.revokeSwitchView.setChecked(!b);
                            showToast(msg);
                        }
                    });
                } else {
                    AdvancedModel.Companion.getInstance().updateUserSetting(channelID, "revoke_remind", b ? 1 : 0, (code, msg) -> {
                        if (code != HttpResponseCode.success) {
                            wkVBinding.revokeSwitchView.setChecked(!b);
                            showToast(msg);
                        }
                    });
                }
            }
        });
    }


    private void resetData() {
        channel = WKIM.getInstance().getChannelManager().getChannel(channelID, channelType);
        if (channel == null) return;

        if (channel.remoteExtraMap != null) {
            if (channel.remoteExtraMap.containsKey(WKChannelExtras.screenshot)) {
                Object object = channel.remoteExtraMap.get(WKChannelExtras.screenshot);
                if (object != null) {
                    int screenshot = (int) object;
                    wkVBinding.screenshotSwitchView.setChecked(screenshot == 1);
                }
            }
            if (channel.remoteExtraMap.containsKey(WKChannelExtras.revokeRemind)) {
                Object object = channel.remoteExtraMap.get(WKChannelExtras.revokeRemind);
                if (object != null) {
                    int revokeRemind = (int) object;
                    wkVBinding.revokeSwitchView.setChecked(revokeRemind == 1);
                }
            }
            if (channelType == WKChannelType.GROUP && channel.remoteExtraMap.containsKey(WKChannelExtras.joinGroupRemind)) {
                Object object = channel.remoteExtraMap.get(WKChannelExtras.joinGroupRemind);
                if (object != null) {
                    int joinGroupRemind = (int) object;
                    wkVBinding.joinGroupSwitchView.setChecked(joinGroupRemind == 1);
                }
            }
        }
    }
}
