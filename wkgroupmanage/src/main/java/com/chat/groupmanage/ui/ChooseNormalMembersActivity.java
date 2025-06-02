package com.chat.groupmanage.ui;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chat.base.base.WKBaseActivity;
import com.chat.base.config.WKConfig;
import com.chat.base.msgitem.WKChannelMemberRole;
import com.chat.base.utils.SoftKeyboardUtils;
import com.chat.base.utils.WKReader;
import com.chat.groupmanage.R;
import com.chat.groupmanage.adapter.ChooseNormalMemberAdapter;
import com.chat.groupmanage.adapter.SelectedAdapter;
import com.chat.groupmanage.databinding.ActChooseMemberLayoutBinding;
import com.chat.groupmanage.entity.ForbiddenTime;
import com.chat.groupmanage.entity.GroupMemberEntity;
import com.chat.groupmanage.service.GroupManageContract;
import com.chat.groupmanage.service.GroupManagePresenter;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKChannel;
import com.xinbida.wukongim.entity.WKChannelExtras;
import com.xinbida.wukongim.entity.WKChannelMember;
import com.xinbida.wukongim.entity.WKChannelType;

import java.util.ArrayList;
import java.util.List;

/**
 * 2020-04-11 21:06
 * 选择普通成员
 */
public class ChooseNormalMembersActivity extends WKBaseActivity<ActChooseMemberLayoutBinding> implements GroupManageContract.GroupManageView {

    String groupId;
    private ChooseNormalMemberAdapter adapter;
    private SelectedAdapter selectedAdapter;
    private TextView titleRightTv;
    private GroupManagePresenter presenter;
    private int type = 0;
    //    private List<GroupMemberEntity> allList;
    private String searchKey;
    private int page = 1;
    private int groupType = 0;

    @Override
    protected ActChooseMemberLayoutBinding getViewBinding() {
        return ActChooseMemberLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        if (type == 0)
            titleTv.setText(R.string.add_group_managers);
        else if (type == 1) {
            titleTv.setText(R.string.group_manager_members);
        } else if (type == 2) {
            titleTv.setText(R.string.choose_new_group_admin);
        }
    }

    @Override
    protected void initPresenter() {
        type = getIntent().getIntExtra("type", 0);
        groupId = getIntent().getStringExtra("groupId");
        presenter = new GroupManagePresenter(this);
    }

    @Override
    protected void initView() {
        WKChannel channel = WKIM.getInstance().getChannelManager().getChannel(groupId, WKChannelType.GROUP);
        if (channel != null && channel.remoteExtraMap != null && channel.remoteExtraMap.containsKey(WKChannelExtras.groupType)) {
            Object groupTypeObject = channel.remoteExtraMap.get(WKChannelExtras.groupType);
            if (groupTypeObject instanceof Integer) {
                groupType = (int) groupTypeObject;
            }
        }

        adapter = new ChooseNormalMemberAdapter(new ArrayList<>(), type, this::resetData);
        initAdapter(wkVBinding.recyclerView, adapter);

        selectedAdapter = new SelectedAdapter(new SelectedAdapter.IListener() {
            @Override
            public void onSearch(@NonNull String key) {
                page = 1;
                searchKey = key;
                adapter.setSearch(searchKey);
                wkVBinding.refreshLayout.setEnableLoadMore(true);
                getData();
            }

            @Override
            public void onDelete(@NonNull String uid) {
                for (int i = 0, size = adapter.getData().size(); i < size; i++) {
                    if (adapter.getData().get(i).channelMember.memberUID.equals(uid)) {
                        adapter.getData().get(i).isChecked = false;
                        adapter.notifyItemChanged(i, adapter.getData().get(i));
                        break;
                    }
                }
                new Handler().postDelayed(() -> resetData(), 300);
            }
        });

        GroupMemberEntity entity = new GroupMemberEntity(new WKChannelMember());
        entity.itemType = 0;
        selectedAdapter.addData(entity);
        wkVBinding.selectUserRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        wkVBinding.selectUserRecyclerView.setAdapter(selectedAdapter);
    }

    @Override
    protected String getRightTvText(TextView textView) {
        titleRightTv = textView;
        return getString(R.string.sure);
    }

    @Override
    protected void rightLayoutClick() {
        super.rightLayoutClick();
        List<String> uids = new ArrayList<>();
        for (int i = 0, size = selectedAdapter.getData().size(); i < size; i++) {
            if (selectedAdapter.getData().get(i).getItemType() == 1 && selectedAdapter.getData().get(i).channelMember != null && !TextUtils.isEmpty(selectedAdapter.getData().get(i).channelMember.memberUID))
                uids.add(selectedAdapter.getData().get(i).channelMember.memberUID);
        }
        if (WKReader.isNotEmpty(uids)) {
            showTitleRightLoading();
            if (type == 0)
                presenter.addGroupManager(groupId, uids);
            else if (type == 1) {
                presenter.addOrRemoveGroupBlackList(groupId, "add", uids);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initListener() {
        wkVBinding.refreshLayout.setEnableRefresh(false);
        wkVBinding.refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                page++;
                getData();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

            }
        });
        wkVBinding.selectUserRecyclerView.setOnTouchListener((view, motionEvent) -> {
            View childView = wkVBinding.selectUserRecyclerView.getChildAt(selectedAdapter.getData().size() - 1);
            if (childView != null) {
                EditText editText = childView.findViewById(R.id.searchEt);
                SoftKeyboardUtils.getInstance().showSoftKeyBoard(ChooseNormalMembersActivity.this, editText);
            }
            return false;
        });
        selectedAdapter.setOnItemClickListener((_adapter, view, position) -> {
            GroupMemberEntity entity = (GroupMemberEntity) _adapter.getData().get(position);
            if (!entity.isSetDelete) {
                entity.isSetDelete = true;
                _adapter.notifyItemChanged(position, entity);
                return;
            }
            for (int i = 0, size = adapter.getData().size(); i < size; i++) {
                if (adapter.getData().get(i).channelMember.memberUID.equalsIgnoreCase(entity.channelMember.memberUID)) {
                    adapter.getData().get(i).isChecked = false;
                    adapter.notifyItemChanged(i, adapter.getData().get(i));
                    break;
                }
            }
            for (int i = 0, size = selectedAdapter.getData().size(); i < size; i++) {
                selectedAdapter.getData().get(i).isSetDelete = false;
            }
            selectedAdapter.removeAt(position);
            resetData();
        });
        adapter.setOnItemClickListener((adapter1, view1, position) -> {
            GroupMemberEntity entity = (GroupMemberEntity) adapter1.getItem(position);
            if (entity != null) {
                if (type == 2) {
                    showDialog(String.format(getString(R.string.choose_new_group_admin_desc), entity.channelMember.memberName), index -> {
                        if (index == 1) {
                            presenter.transferGroup(groupId, entity.channelMember.memberUID);
                        }
                    });

                } else {

                    entity.isChecked = !entity.isChecked;
                    adapter1.notifyItemChanged(position, entity);

                    if (entity.isChecked) {
                        entity.isSetDelete = false;
                        selectedAdapter.addData(selectedAdapter.getData().size() - 1, entity);
                        wkVBinding.selectUserRecyclerView.scrollToPosition(selectedAdapter.getData().size() - 1);
                    } else {
                        for (int i = 0, size = selectedAdapter.getData().size(); i < size; i++) {
                            if (!TextUtils.isEmpty(selectedAdapter.getData().get(i).channelMember.memberUID)
                                    && selectedAdapter.getData().get(i).channelMember.memberUID.equalsIgnoreCase(entity.channelMember.memberUID)) {
                                selectedAdapter.removeAt(i);
                                break;
                            }
                        }
                    }
                }
            }
            if (type != 2)
                resetData();
        });
    }

    private void resetData() {
        int count = 0;
        for (int i = 0, size = adapter.getData().size(); i < size; i++) {
            if (adapter.getData().get(i).isChecked)
                count++;
        }
        if (count > 0) {
            titleRightTv.setText(String.format("%s(%s)", getString(R.string.sure), count));
            showTitleRightView();
        } else hideTitleRightView();
    }

    @Override
    protected void initData() {
        super.initData();
        hideTitleRightView();
        getData();
    }

    @Override
    public void refreshData() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void forbiddenTimeList(List<ForbiddenTime> list) {

    }

    @Override
    public void showError(String msg) {

    }

    @Override
    public void hideLoading() {
        hideTitleRightView();
        showTitleRightView();
    }

    private void getData() {
        WKIM.getInstance().getChannelMembersManager().getWithPageOrSearch(groupId, WKChannelType.GROUP, searchKey, page, 20, (list, b) -> {
            if (groupType == 0)
                resortData(list);
            else {
                if (b) {
                    resortData(list);
                }
            }
        });
    }


    private void resortData(List<WKChannelMember> list) {
        List<GroupMemberEntity> tempList = new ArrayList<>();
        int loginMemberRole = 0;
        String loginUID = WKConfig.getInstance().getUid();
        WKChannelMember loginUserMember = WKIM.getInstance().getChannelMembersManager().getMember(groupId, WKChannelType.GROUP, loginUID);
        if (loginUserMember != null) {
            loginMemberRole = loginUserMember.role;
        }
        for (int i = 0, size = list.size(); i < size; i++) {
            if (loginUID.equals(list.get(i).memberUID)) continue;
            boolean isAdd = isAdd(list, i, loginMemberRole);
            if (isAdd) {
                GroupMemberEntity entity = new GroupMemberEntity(list.get(i));
                for (int j = 0, len = selectedAdapter.getData().size(); j < len; j++) {
                    if (list.get(i).memberUID.equals(selectedAdapter.getData().get(j).channelMember.memberUID)) {
                        entity.isChecked = true;
                        break;
                    }
                }
                tempList.add(entity);
            }

        }
        wkVBinding.refreshLayout.finishLoadMore();
        if (page == 1) {
            adapter.setList(tempList);
        } else {
            adapter.addData(tempList);
        }
        if (WKReader.isEmpty(tempList)) {
            wkVBinding.refreshLayout.finishLoadMoreWithNoMoreData();
        }
    }

    private boolean isAdd(List<WKChannelMember> list, int i, int loginMemberRole) {
        boolean isAdd = false;
        if (type == 0) {
            if (list.get(i).role == WKChannelMemberRole.normal) {
                isAdd = true;
            }
        } else if (type == 1) {
            if (loginMemberRole == WKChannelMemberRole.manager) {
                if (list.get(i).role == WKChannelMemberRole.normal) {
                    isAdd = true;
                }
            } else {
                isAdd = true;
            }
        } else if (type == 2) {
            if (!list.get(i).memberUID.equals(WKConfig.getInstance().getUid())) {
                isAdd = true;
            }
        }
        return isAdd;
    }
}
