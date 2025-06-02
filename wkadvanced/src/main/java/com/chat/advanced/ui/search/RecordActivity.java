package com.chat.advanced.ui.search;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.chat.advanced.R;
import com.chat.advanced.databinding.ActSearchRecordLayoutBinding;
import com.chat.base.base.WKBaseActivity;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.EndpointSID;
import com.chat.base.endpoint.entity.ChatViewMenu;
import com.chat.base.endpoint.entity.SearchChatContentMenu;
import com.chat.base.ui.Theme;
import com.chat.base.utils.SoftKeyboardUtils;
import com.chat.base.utils.WKReader;
import com.chat.base.views.FullyGridLayoutManager;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKChannel;
import com.xinbida.wukongim.entity.WKMsg;
import com.xinbida.wukongim.entity.WKChannelType;

import java.util.ArrayList;
import java.util.List;

/**
 * 3/22/21 10:56 AM
 * 聊天搜索记录
 */
public class RecordActivity extends WKBaseActivity<ActSearchRecordLayoutBinding> {
    private String channelID;
    private byte channelType;
    ResultAdapter adapter;
    private RecordTypeAdapter searchRecordTypeAdapter;

    @Override
    protected ActSearchRecordLayoutBinding getViewBinding() {
        return ActSearchRecordLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {

    }

    @Override
    protected void initPresenter() {
        channelID = getIntent().getStringExtra("channel_id");
        channelType = getIntent().getByteExtra("channel_type", WKChannelType.PERSONAL);
    }

    @Override
    protected void initView() {
        wkVBinding.searchIv.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.color999), PorterDuff.Mode.MULTIPLY));
        adapter = new ResultAdapter("", new ArrayList<>());
        initAdapter(wkVBinding.recyclerView, adapter);
        WKChannel channel = new WKChannel();
        channel.channelID = channelID;
        channel.channelType = channelType;
        List<SearchChatContentMenu> list = EndpointManager.getInstance().invokes(EndpointCategory.wkSearchChatContent, channel);
        for (int i = 0, size = list.size(); i < size; i++) {
            if (list.get(i) == null || TextUtils.isEmpty(list.get(i).text)) {
                list.remove(i);
                i--;
            }
        }
        searchRecordTypeAdapter = new RecordTypeAdapter(list);
        FullyGridLayoutManager layoutManager = new FullyGridLayoutManager(this, 3);
        wkVBinding.typeRecyclerView.setLayoutManager(layoutManager);
        wkVBinding.typeRecyclerView.setAdapter(searchRecordTypeAdapter);
        SoftKeyboardUtils.getInstance().showSoftKeyBoard(RecordActivity.this, wkVBinding.searchEt);
        Theme.setPressedBackground(wkVBinding.cancelTv);
    }

    @Override
    protected void initListener() {
        wkVBinding.searchEt.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        wkVBinding.searchEt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                SoftKeyboardUtils.getInstance().hideSoftKeyboard(RecordActivity.this);
                return true;
            }
            return false;
        });

        wkVBinding.cancelTv.setOnClickListener(v -> finish());
        wkVBinding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String key = s.toString();
                if (TextUtils.isEmpty(key)) {
                    wkVBinding.resultView.setVisibility(View.GONE);
                    wkVBinding.searchTypeLayout.setVisibility(View.VISIBLE);
                } else {
                    searchChannel(key);
                    wkVBinding.resultView.setVisibility(View.VISIBLE);
                    wkVBinding.searchTypeLayout.setVisibility(View.GONE);
                }
            }
        });
        searchRecordTypeAdapter.setOnItemClickListener((adapter1, view1, position) -> {
            SearchChatContentMenu menu = (SearchChatContentMenu) adapter1.getData().get(position);
            if (menu != null && menu.iClick != null) {
                SoftKeyboardUtils.getInstance().hideSoftKeyboard(this);
                menu.iClick.onClick(channelID, channelType);
            }
        });
        adapter.setOnItemClickListener((adapter1, view1, position) -> {
            WKMsg item = (WKMsg) adapter1.getItem(position);
            if (item != null) {
                SoftKeyboardUtils.getInstance().hideSoftKeyboard(this);
                EndpointManager.getInstance().invoke(EndpointSID.chatView, new ChatViewMenu(this, item.channelID, item.channelType, item.orderSeq, false));
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        SoftKeyboardUtils.getInstance().hideSoftKeyboard(this);
    }

    private void searchChannel(String content) {
        List<WKMsg> msgList = WKIM.getInstance().getMsgManager().searchWithChannel(content,channelID, channelType);
        if (WKReader.isEmpty(msgList)) {
            wkVBinding.nodataTv.setVisibility(View.VISIBLE);
            wkVBinding.recyclerView.setVisibility(View.GONE);
        } else {
            adapter.setSearchKey(content);
            wkVBinding.nodataTv.setVisibility(View.GONE);
            wkVBinding.recyclerView.setVisibility(View.VISIBLE);
            adapter.setList(msgList);
        }
    }
}
