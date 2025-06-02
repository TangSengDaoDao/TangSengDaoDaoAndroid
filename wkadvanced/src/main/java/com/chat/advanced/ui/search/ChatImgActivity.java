package com.chat.advanced.ui.search;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.chat.advanced.R;
import com.chat.advanced.databinding.ActSearchChatImgLayoutBinding;
import com.chat.base.base.WKBaseActivity;
import com.chat.base.config.WKApiConfig;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.EndpointSID;
import com.chat.base.endpoint.entity.ChatChooseContacts;
import com.chat.base.endpoint.entity.ChatViewMenu;
import com.chat.base.endpoint.entity.ChooseChatMenu;
import com.chat.base.entity.ImagePopupBottomSheetItem;
import com.chat.base.msgitem.WKContentType;
import com.chat.base.ui.Theme;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.WKDialogUtils;
import com.chat.base.utils.WKReader;
import com.chat.base.utils.WKTimeUtils;
import com.chat.base.views.CustomImageViewerPopup;
import com.chat.base.views.FullyGridLayoutManager;
import com.chat.base.views.pinnedsectionitemdecoration.PinnedHeaderItemDecoration;
import com.google.android.material.snackbar.Snackbar;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKChannel;
import com.xinbida.wukongim.entity.WKChannelType;
import com.xinbida.wukongim.entity.WKMsg;
import com.xinbida.wukongim.msgmodel.WKImageContent;
import com.xinbida.wukongim.msgmodel.WKMessageContent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 3/23/21 10:07 AM
 * 搜索聊天图片
 */
public class ChatImgActivity extends WKBaseActivity<ActSearchChatImgLayoutBinding> {
    private String channelID;
    private byte channelType;
    private long oldestOrderSeq = 0;
    private final int[] types = new int[]{WKContentType.WK_IMAGE};
    private ChatImgAdapter adapter;

    @Override
    protected ActSearchChatImgLayoutBinding getViewBinding() {
        return ActSearchChatImgLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.image);
    }

    @Override
    protected void initPresenter() {
        channelID = getIntent().getStringExtra("channel_id");
        channelType = getIntent().getByteExtra("channel_type", WKChannelType.PERSONAL);
    }

    @Override
    protected void initView() {
        PinnedHeaderItemDecoration mHeaderItemDecoration = new PinnedHeaderItemDecoration.Builder(1).enableDivider(false).create();
        int wH = (AndroidUtilities.getScreenWidth() - AndroidUtilities.dp(6)) / 4;
        FullyGridLayoutManager layoutManager = new FullyGridLayoutManager(this, 4);
        wkVBinding.recyclerView.setLayoutManager(layoutManager);
        adapter = new ChatImgAdapter(wH, new ChatImgAdapter.ICLick() {
            @Override
            public void onClick(ChatImgEntity searchChatImgEntity) {
                EndpointManager.getInstance().invoke(EndpointSID.chatView, new ChatViewMenu(ChatImgActivity.this, channelID, channelType, searchChatImgEntity.oldestOrderSeq, false));
            }

            @Override
            public void onForward(ChatImgEntity searchChatImgEntity) {
                forward(searchChatImgEntity);
            }
        });
        wkVBinding.recyclerView.setAdapter(adapter);
        wkVBinding.recyclerView.addItemDecoration(mHeaderItemDecoration);
    }

    @Override
    protected void initListener() {
        getData();
        wkVBinding.spinKit.setColor(Theme.colorAccount);
        wkVBinding.refreshLayout.setEnableRefresh(false);
        wkVBinding.refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                oldestOrderSeq = adapter.getData().get(adapter.getData().size() - 1).oldestOrderSeq;
                getData();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

            }
        });
        adapter.addChildClickViewIds(R.id.imageView);
        adapter.setOnItemChildClickListener((adapter1, view1, position) -> {
            ChatImgEntity entity = (ChatImgEntity) adapter1.getData().get(position);
            if (entity != null && entity.getItemType() == 0) {
                showImg(entity.url, (ImageView) view1);
            }
        });
    }

    private void getData() {
        List<WKMsg> list = WKIM.getInstance().getMsgManager().searchMsgWithChannelAndContentTypes(channelID, channelType, oldestOrderSeq, 120, types);
        if (WKReader.isNotEmpty(list)) {
            List<ChatImgEntity> fileEntityList = new ArrayList<>();

            wkVBinding.refreshLayout.finishLoadMore();
            // 构造数据
            for (WKMsg msg : list) {
                String date = WKTimeUtils.getInstance().time2YearMonth(msg.timestamp * 1000);
                if (WKReader.isNotEmpty(fileEntityList)) {
                    if (!fileEntityList.get(fileEntityList.size() - 1).date.equals(date)) {
                        ChatImgEntity entity = new ChatImgEntity();
                        entity.date = date;
                        entity.itemType = 1;
                        fileEntityList.add(entity);
                    }
                } else {
                    ChatImgEntity entity = new ChatImgEntity();
                    entity.date = date;
                    entity.itemType = 1;
                    fileEntityList.add(entity);
                }
                ChatImgEntity entity = new ChatImgEntity();
                entity.date = date;
                entity.clientMsgNo = msg.clientMsgNO;
                entity.messageContent = msg.baseContentMsgModel;
                entity.oldestOrderSeq = msg.orderSeq;
                WKImageContent msgModel = (WKImageContent) msg.baseContentMsgModel;
                String showUrl = "";
                if (!TextUtils.isEmpty(msgModel.localPath)) {
                    File file = new File(msgModel.localPath);
                    if (file.exists()) {
                        showUrl = msgModel.localPath;
                    }
                }
                if (TextUtils.isEmpty(showUrl)) {
                    showUrl = WKApiConfig.getShowUrl(msgModel.url);
                }
                entity.url = showUrl;
                fileEntityList.add(entity);
            }
            if (WKReader.isNotEmpty(adapter.getData())) {
                ChatImgEntity entity = adapter.getData().get(adapter.getData().size() - 1);
                if (entity.date.equals(fileEntityList.get(0).date)) {
                    fileEntityList.remove(0);
                }
            }
            adapter.addData(fileEntityList);
        } else {
            wkVBinding.refreshLayout.finishLoadMoreWithNoMoreData();
            if (oldestOrderSeq == 0) {
                wkVBinding.refreshLayout.setEnableLoadMore(false);
                wkVBinding.nodataTv.setVisibility(View.VISIBLE);
            }
        }

    }


    private void showImg(String uri, ImageView imageView) {
        //查看大图
        List<Object> tempImgList = new ArrayList<>();
        List<Object> urlList = new ArrayList<>();
        List<ImageView> imgList = new ArrayList<>();
        for (int i = 0, size = adapter.getData().size(); i < size; i++) {
            if (adapter.getData().get(i).getItemType() == 0) {
                tempImgList.add(adapter.getData().get(i));
                urlList.add(adapter.getData().get(i).url);
                ImageView imageView1 = (ImageView) adapter.getViewByPosition(i, R.id.imageView);
                imgList.add(imageView1);
            }
        }
        int index = 0;
        for (int i = 0; i < tempImgList.size(); i++) {
            ChatImgEntity entity = (ChatImgEntity) tempImgList.get(i);
            if (entity.url.equals(uri)) {
                index = i;
                break;
            }
        }

        List<ImagePopupBottomSheetItem> bottomEntityArrayList = new ArrayList<>();
        bottomEntityArrayList.add(new ImagePopupBottomSheetItem(getString(R.string.forward), R.mipmap.msg_forward, position -> {
            ChatImgEntity entity = (ChatImgEntity) tempImgList.get(position);
            if (entity == null || entity.messageContent == null) return;
            forward(entity);
        }));
        bottomEntityArrayList.add(new ImagePopupBottomSheetItem(getString(R.string.go_to_chat_item), R.mipmap.msg_message, position -> {
            ChatImgEntity entity = (ChatImgEntity) tempImgList.get(position);
            EndpointManager.getInstance().invoke(EndpointSID.chatView, new ChatViewMenu(ChatImgActivity.this, channelID, channelType, entity.oldestOrderSeq, false));
        }));
        WKDialogUtils.getInstance().showImagePopup(this, urlList, imgList, imageView, index, bottomEntityArrayList, new CustomImageViewerPopup.IImgPopupMenu() {
            @Override
            public void onForward(int position) {
            }

            @Override
            public void onFavorite(int position) {
                ChatImgEntity entity = (ChatImgEntity) tempImgList.get(position);
                WKMsg msg = WKIM.getInstance().getMsgManager().getWithClientMsgNO(entity.clientMsgNo);
                if (msg != null) {
                    collect(msg);
                }
            }

            @Override
            public void onShowInChat(int position) {
                ChatImgEntity entity = (ChatImgEntity) tempImgList.get(position);
                EndpointManager.getInstance().invoke(EndpointSID.chatView, new ChatViewMenu(ChatImgActivity.this, channelID, channelType, entity.oldestOrderSeq, false));
            }
        }, null);

    }


    private void collect(WKMsg msg) {
        JSONObject jsonObject = new JSONObject();
        WKImageContent msgModel = (WKImageContent) msg.baseContentMsgModel;
        jsonObject.put("content", WKApiConfig.getShowUrl(msgModel.url));
        jsonObject.put("width", msgModel.width);
        jsonObject.put("height", msgModel.height);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("type", msg.type);
        String unique_key = msg.messageID;
        if (TextUtils.isEmpty(unique_key))
            unique_key = msg.clientMsgNO;
        hashMap.put("unique_key", unique_key);
        if (msg.getFrom() != null) {
            hashMap.put("author_uid", msg.getFrom().channelID);
            hashMap.put("author_name", msg.getFrom().channelName);
        }
        hashMap.put("payload", jsonObject);
        hashMap.put("activity", this);
        EndpointManager.getInstance().invoke("favorite_add", hashMap);
    }

    private void forward(ChatImgEntity entity) {
        WKMessageContent finalWKMessageContent = entity.messageContent;
        EndpointManager.getInstance().invoke(EndpointSID.showChooseChatView, new ChooseChatMenu(new ChatChooseContacts(list1 -> {
            if (WKReader.isNotEmpty(list1)) {
                for (WKChannel channel : list1) {
                    WKIM.getInstance().getMsgManager().send(finalWKMessageContent, channel);
                }
                ViewGroup viewGroup = (ViewGroup) findViewById(android.R.id.content).getRootView();
                Snackbar.make(viewGroup, getString(R.string.is_forward), 1000)
                        .setAction("", v1 -> {
                        })
                        .show();
            }
        }), entity.messageContent));

    }
}
