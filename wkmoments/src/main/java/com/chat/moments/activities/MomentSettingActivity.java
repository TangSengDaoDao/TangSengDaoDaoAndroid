package com.chat.moments.activities;

import android.widget.TextView;

import com.chat.base.base.WKBaseActivity;
import com.chat.base.net.HttpResponseCode;
import com.chat.moments.databinding.ActMomentSettingLayoutBinding;
import com.chat.moments.entity.MomentSetting;
import com.chat.moments.entity.Moments;
import com.chat.moments.service.MomentsContact;
import com.chat.moments.service.MomentsModel;
import com.chat.moments.service.MomentsPresenter;

import java.util.List;

/**
 * 3/15/21 1:54 PM
 * 朋友圈权限
 */
public class MomentSettingActivity extends WKBaseActivity<ActMomentSettingLayoutBinding> implements MomentsContact.MomentsView {
    private MomentsPresenter presenter;
    private String uid;

    @Override
    protected ActMomentSettingLayoutBinding getViewBinding() {
        return ActMomentSettingLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText("");
    }

    @Override
    protected void initPresenter() {
        presenter = new MomentsPresenter(this);
    }

    @Override
    protected void initView() {
        uid = getIntent().getStringExtra("uid");
        presenter.momentSetting(uid);
    }

    @Override
    protected void initListener() {
        wkVBinding.hideHisSwitchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                MomentsModel.getInstance().hideHis(uid, isChecked ? 1 : 0, (code, msg) -> {
                    if (code != HttpResponseCode.success) {
                        showToast(msg);
                        wkVBinding.hideHisSwitchView.setChecked(!wkVBinding.hideHisSwitchView.isChecked());
                    }
                });
            }
        });
        wkVBinding.hideMySwitchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                MomentsModel.getInstance().hideMy(uid, isChecked ? 1 : 0, (code, msg) -> {
                    if (code != HttpResponseCode.success) {
                        showToast(msg);
                        wkVBinding.hideMySwitchView.setChecked(!wkVBinding.hideMySwitchView.isChecked());
                    }
                });
            }
        });
    }

    @Override
    public void setList(List<Moments> list) {

    }

    @Override
    public void setMomentSetting(MomentSetting momentSetting) {
        wkVBinding.hideHisSwitchView.setChecked(momentSetting.is_hide_his == 1);
        wkVBinding.hideMySwitchView.setChecked(momentSetting.is_hide_my == 1);
    }

    @Override
    public void showError(String msg) {

    }

    @Override
    public void hideLoading() {

    }
}
