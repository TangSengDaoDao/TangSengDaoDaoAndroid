package com.chat.groupmanage.msgitem;

import android.content.Intent;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.act.WKWebViewActivity;
import com.chat.base.msgitem.WKChatBaseProvider;
import com.chat.base.msgitem.WKChatIteMsgFromType;
import com.chat.base.msgitem.WKContentType;
import com.chat.base.msgitem.WKUIChatMsgItemEntity;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.ui.components.NormalClickableContent;
import com.chat.base.ui.components.NormalClickableSpan;
import com.chat.base.ui.components.SystemMsgBackgroundColorSpan;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.WKLogUtils;
import com.chat.base.utils.WKToastUtils;
import com.chat.groupmanage.R;
import com.chat.groupmanage.service.GroupManageModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 2020-08-07 09:52
 * 进群审批消息
 */
public class GroupApproveItemProvider extends WKChatBaseProvider {

    @Override
    public int getLayoutId() {
        return R.layout.chat_system_layout;
    }

    @Override
    protected View getChatViewItem(@NonNull ViewGroup parentView, @NonNull WKChatIteMsgFromType from) {
        return null;
    }

    @Override
    public void convert(@NotNull BaseViewHolder baseViewHolder, @NonNull WKUIChatMsgItemEntity chatMsgItemEntity) {
        super.convert(baseViewHolder, chatMsgItemEntity);
        TextView textView = baseViewHolder.getView(R.id.contentTv);
        String content = getShowContent(chatMsgItemEntity.wkMsg.content);
        SpannableStringBuilder displaySpans = new SpannableStringBuilder();
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setShadowLayer(AndroidUtilities.dp(5f), 0f, 0f, 0);
        String str = context.getString(R.string.group_approve);
        displaySpans.append(content).append(str);
        NormalClickableSpan span = new NormalClickableSpan(false, ContextCompat.getColor(context, R.color.blue), new NormalClickableContent(NormalClickableContent.NormalClickableTypes.Other, ""), view -> {
            try {
                JSONObject jsonObject = new JSONObject(chatMsgItemEntity.wkMsg.content);
                String invite_no = jsonObject.optString("invite_no");
                GroupManageModel.getInstance().getH5confirmUrl(chatMsgItemEntity.wkMsg.channelID, invite_no, (code, msg) -> {
                    if (code == HttpResponseCode.success && !TextUtils.isEmpty(msg)) {
                        Intent intent = new Intent(getContext(), WKWebViewActivity.class);
                        intent.putExtra("url", msg);
                        getContext().startActivity(intent);
                    } else WKToastUtils.getInstance().showToastNormal(msg);
                });
            } catch (JSONException e) {
                WKLogUtils.e("解析群邀请数据错误");
            }

        });
        assert content != null;
        displaySpans.setSpan(
                new SystemMsgBackgroundColorSpan(
                        ContextCompat.getColor(
                                context,
                                R.color.colorSystemBg
                        ), AndroidUtilities.dp(5f), AndroidUtilities.dp((2 * 5))
                ), 0, content.length() + str.length(), 0
        );
        displaySpans.setSpan(new StyleSpan(Typeface.BOLD), content.length(), (content.length() + str.length()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        displaySpans.setSpan(span, content.length(), content.length() + str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        displaySpans.setSpan(new AbsoluteSizeSpan(17, true),
                content.length(), (content.length() + str.length()),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(displaySpans);
    }

    @Override
    protected void setData(int adapterPosition, @NonNull View parentView, @NonNull WKUIChatMsgItemEntity uiChatMsgItemEntity, @NonNull WKChatIteMsgFromType from) {

    }

    @Override
    public int getItemViewType() {
        return WKContentType.approveGroupMember;
    }

}
