package com.chat.moments.activities;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.chat.base.base.WKBaseActivity;
import com.chat.base.config.WKConfig;
import com.chat.base.config.WKSharedPreferencesUtil;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.utils.WKDialogUtils;
import com.chat.base.utils.WKReader;
import com.chat.base.utils.WKTimeUtils;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.moments.R;
import com.chat.moments.adapter.MomentsMsgAdapter;
import com.chat.moments.databinding.ActMomentsMsgListLayoutBinding;
import com.chat.moments.db.MomentsDBManager;
import com.chat.moments.db.MomentsDBMsg;
import com.chat.moments.entity.MomentsMsg;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKChannel;
import com.xinbida.wukongim.entity.WKChannelType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 2020-11-24 15:56
 * 动态消息列表
 */
public class MomentsMsgListActivity extends WKBaseActivity<ActMomentsMsgListLayoutBinding> {
    private MomentsMsgAdapter adapter;

    @Override
    protected ActMomentsMsgListLayoutBinding getViewBinding() {
        return ActMomentsMsgListLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.moment_msg);
    }

    @Override
    protected void initPresenter() {

    }

    @Override
    protected String getRightTvText(TextView textView) {
        textView.setTextColor(ContextCompat.getColor(this, R.color.colorDark));
        return getString(R.string.moments_clear);
    }

    @Override
    protected void initView() {
        adapter = new MomentsMsgAdapter();
        initAdapter(wkVBinding.recyclerView, adapter);
    }

    @Override
    protected void rightLayoutClick() {
        super.rightLayoutClick();
        WKDialogUtils.getInstance().showDialog(this, getString(R.string.clear_moment_messages), getString(R.string.comments_clear_tips), true, "", getString(R.string.base_delete), 0, ContextCompat.getColor(this, R.color.red), index -> {
            if (index == 1) {
                boolean result = MomentsDBManager.getInstance().clear();
                if (result) {
                    adapter.setList(new ArrayList<>());
                    hideTitleRightView();
                }
            }
        });

    }

    @Override
    protected void initListener() {
        adapter.addChildClickViewIds(R.id.contentLayout);
        adapter.setOnItemChildClickListener((adapter1, view1, position) -> SingleClickUtil.determineTriggerSingleClick(view1, view2 -> {
            MomentsMsg momentsMsg = (MomentsMsg) adapter1.getItem(position);
            if (momentsMsg != null) {
                Intent intent = new Intent(this, MomentsDetailActivity.class);
                intent.putExtra("momentNo", momentsMsg.moment_no);
                startActivity(intent);
            }
        }));
    }

    @Override
    protected void initData() {
        super.initData();
        hideTitleRightView();
        List<MomentsMsg> momentsMsgList = new ArrayList<>();
        List<MomentsDBMsg> list = MomentsDBManager.getInstance().query();
        if (WKReader.isNotEmpty(list)) {
            for (int i = 0, size = list.size(); i < size; i++) {
                MomentsDBMsg dbMsg = list.get(i);
                MomentsMsg momentsMsg = new MomentsMsg();
                momentsMsg.moment_no = dbMsg.moment_no;
                WKChannel channel = WKIM.getInstance().getChannelManager().getChannel(dbMsg.uid, WKChannelType.PERSONAL);
                if (channel != null) {
                    momentsMsg.avatarCacheKey = channel.avatarCacheKey;
                    momentsMsg.name = TextUtils.isEmpty(channel.channelRemark) ? channel.channelName : channel.channelRemark;
                } else {
                    momentsMsg.name = dbMsg.name;
                    momentsMsg.avatarCacheKey = "";
                }
                momentsMsg.id = dbMsg.id;
                momentsMsg.uid = dbMsg.uid;
                momentsMsg.comment = dbMsg.comment;
                momentsMsg.is_deleted = dbMsg.is_deleted;
                momentsMsg.action = dbMsg.action;

                momentsMsg.time = WKTimeUtils.getInstance().getTimeFormatText(dbMsg.action_at);
                if (!TextUtils.isEmpty(dbMsg.content)) {
                    try {
                        JSONObject jsonObject = new JSONObject(dbMsg.content);
                        momentsMsg.content = jsonObject.optString("moment_content");
                        String video_cover_path = jsonObject.optString("video_conver_path");
                        JSONArray jsonArray = jsonObject.optJSONArray("imgs");
                        if (jsonArray != null && jsonArray.length() > 0) {
                            momentsMsg.url = jsonArray.optString(0);
                            momentsMsg.contentType = 1;
                        } else {
                            if (!TextUtils.isEmpty(video_cover_path)) {
                                momentsMsg.url = video_cover_path;
                                momentsMsg.contentType = 2;
                            } else {
                                momentsMsg.contentType = 0;
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                momentsMsgList.add(momentsMsg);
            }
        }
        if (WKReader.isNotEmpty(momentsMsgList)) {
            adapter.setList(momentsMsgList);
            showTitleRightView();
        } else {
            wkVBinding.noDataTv.setVisibility(View.VISIBLE);
        }

        //刷新消息
        WKSharedPreferencesUtil.getInstance().putInt(WKConfig.getInstance().getUid() + "_moments_msg_count", 0);
        //操作者的用户ID
        WKSharedPreferencesUtil.getInstance().putSP(WKConfig.getInstance().getUid() + "_moments_msg_action_uid", "");
        EndpointManager.getInstance().invokes(EndpointCategory.wkRefreshMailList, null);
    }
}
