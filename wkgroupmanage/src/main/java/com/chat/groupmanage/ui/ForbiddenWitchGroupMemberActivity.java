package com.chat.groupmanage.ui;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chat.base.base.WKBaseActivity;
import com.chat.groupmanage.R;
import com.chat.groupmanage.adapter.ForbiddenWitchGroupMemberAdapter;
import com.chat.groupmanage.databinding.ActForbiddenWitchGroupMemberLayoutBinding;
import com.chat.groupmanage.entity.ForbiddenTime;
import com.chat.groupmanage.service.GroupManageContract;
import com.chat.groupmanage.service.GroupManagePresenter;

import java.util.List;

public class ForbiddenWitchGroupMemberActivity extends WKBaseActivity<ActForbiddenWitchGroupMemberLayoutBinding> implements GroupManageContract.GroupManageView {
    private ForbiddenWitchGroupMemberAdapter adapter;
    private Button sureBtn;
    GroupManagePresenter presenter;
    private String groupNo, memberUID;

    @Override
    protected ActForbiddenWitchGroupMemberLayoutBinding getViewBinding() {
        return ActForbiddenWitchGroupMemberLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.forbidden_time);
    }

    @Override
    protected void initPresenter() {
        presenter = new GroupManagePresenter(this);
        memberUID = getIntent().getStringExtra("memberUID");
        groupNo = getIntent().getStringExtra("groupNo");
    }

    @Override
    protected String getRightBtnText(Button titleRightBtn) {
        this.sureBtn = titleRightBtn;
        return getString(R.string.sure);
    }

    @Override
    protected void rightButtonClick() {
        super.rightButtonClick();
        int key = 0;
        for (int i = 0; i < adapter.getData().size(); i++) {
            if (adapter.getData().get(i).isChecked) {
                key = adapter.getData().get(i).key;
                break;
            }
        }
        if (key != 0) {
            presenter.setForbiddenTime(groupNo, memberUID, key, 1);
        }
    }

    @Override
    protected void initView() {
        adapter = new ForbiddenWitchGroupMemberAdapter();
        initAdapter(wkVBinding.recyclerView, adapter);
    }

    @Override
    protected void initListener() {
        adapter.setOnItemClickListener((adapter1, view1, position) -> {
            ForbiddenTime entity = (ForbiddenTime) adapter1.getData().get(position);
            if (entity != null) {
                if (entity.isChecked) return;
                for (int i = 0; i < adapter.getData().size(); i++) {
                    if (adapter.getData().get(i).isChecked) {
                        adapter.getData().get(i).isChecked = false;
                        adapter.notifyItemChanged(i);
                        break;
                    }
                }
                adapter.getData().get(position).isChecked = true;
                sureBtn.setVisibility(View.VISIBLE);
                adapter.notifyItemChanged(position);
            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        sureBtn.setVisibility(View.GONE);
        presenter.forbiddenTimeList();
    }

    @Override
    public void showError(String msg) {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void refreshData() {
        finish();
    }

    @Override
    public void forbiddenTimeList(List<ForbiddenTime> list) {
        adapter.setList(list);
    }
}
