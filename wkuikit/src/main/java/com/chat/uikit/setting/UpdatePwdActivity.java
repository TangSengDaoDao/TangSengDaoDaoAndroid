package com.chat.uikit.setting;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.chat.base.base.WKBaseActivity;
import com.chat.base.entity.UserInfoEntity;
import com.chat.base.entity.WKAPPConfig;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.net.ICommonListener;
import com.chat.base.net.IRequestResultListener;
import com.chat.base.net.entity.CommonResponse;
import com.chat.base.utils.WKToastUtils;
import com.chat.uikit.R;
import com.chat.uikit.databinding.ActUpdatePasswordLayoutBinding;
import com.chat.uikit.user.service.UserModel;

import java.util.List;
import java.util.Objects;

public class UpdatePwdActivity extends WKBaseActivity<ActUpdatePasswordLayoutBinding> {

    private WKAPPConfig wkappConfig;
    private String code = "0086";

    @Override
    protected ActUpdatePasswordLayoutBinding getViewBinding() {
        return ActUpdatePasswordLayoutBinding.inflate(getLayoutInflater());
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    protected void initListener() {
        super.initListener();

        wkVBinding.updateBtn.setOnClickListener( v -> {
            if (checkEditInputIsEmpty(wkVBinding.updatePwdEt, R.string.placeholder_pwd)) return;
            if (checkEditInputIsEmpty(wkVBinding.newPwdEt, R.string.update_pwd)) return;
            if (checkEditInputIsEmpty(wkVBinding.pwdConfirmEt, R.string.update_confirm_password)) return;

            if (Objects.requireNonNull(wkVBinding.updatePwdEt.getText()).toString().length() < 6 || wkVBinding.newPwdEt.getText().toString().length() > 16) {
                showSingleBtnDialog(getString(R.string.update_pwd_error));
                return;
            }

            if (!wkVBinding.newPwdEt.getText().toString().equals(Objects.requireNonNull(wkVBinding.pwdConfirmEt.getText()).toString())) {
                showSingleBtnDialog(getString(R.string.update_pwd_error));
                return;
            }

            loadingPopup.show();
            loadingPopup.setTitle(getString(R.string.loading));
            String oldPassword = Objects.requireNonNull(wkVBinding.updatePwdEt.getText()).toString();
            String newPassword = Objects.requireNonNull(wkVBinding.newPwdEt.getText()).toString();
            UserModel.getInstance().updatePassword(oldPassword, newPassword, new ICommonListener() {
                @Override
                public void onResult(int code, String msg) {
                    loadingPopup.dismiss();
                    if (code == HttpResponseCode.success) {
                        WKToastUtils.getInstance().showToastSuccess("OK");
                        finish();
                    } else {
                        WKToastUtils.getInstance().showToastFail(msg);
                    }
                }
            });
        });
    }
}
