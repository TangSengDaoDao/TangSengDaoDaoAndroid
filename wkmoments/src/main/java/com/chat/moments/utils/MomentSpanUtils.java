package com.chat.moments.utils;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.chat.base.config.WKConfig;
import com.chat.base.emoji.EmojiManager;
import com.chat.base.emoji.MoonUtil;
import com.chat.base.entity.PopupMenuItem;
import com.chat.base.ui.components.AlignImageSpan;
import com.chat.base.ui.components.NormalClickableContent;
import com.chat.base.ui.components.NormalClickableSpan;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.WKDialogUtils;
import com.chat.base.utils.WKReader;
import com.chat.base.utils.WKToastUtils;
import com.chat.moments.R;
import com.chat.moments.WKMomentsApplication;
import com.chat.moments.entity.MomentsPraise;
import com.chat.moments.entity.MomentsReply;
import com.chat.moments.span.TextClickSpan;
import com.chat.moments.span.VerticalImageSpan;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKChannel;
import com.xinbida.wukongim.entity.WKChannelType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * 2020-11-05 16:37
 * 回复点赞效果
 */
public class MomentSpanUtils {
    private MomentSpanUtils() {
    }

    private static class MomentSpanUtilsBinder {
        final static MomentSpanUtils utils = new MomentSpanUtils();
    }

    public static MomentSpanUtils getInstance() {
        return MomentSpanUtilsBinder.utils;
    }

    private boolean isCanClick = true;

    public SpannableStringBuilder makePraiseSpan(Context context, List<MomentsPraise> praiseBeans) {
        if (WKReader.isNotEmpty(praiseBeans)) {
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(" ");
            int praiseSize = praiseBeans.size();
            String loginUID = WKConfig.getInstance().getUid();
            for (int i = 0; i < praiseSize; i++) {
                MomentsPraise praiseBean = praiseBeans.get(i);
                String praiseUserName = praiseBean.name;
                if (praiseBean.uid.equals(loginUID)) {
                    praiseUserName = WKConfig.getInstance().getUserName();
                } else {
                    WKChannel channel = WKIM.getInstance().getChannelManager().getChannel(praiseBeans.get(i).uid, WKChannelType.PERSONAL);
                    if (channel != null) {
                        praiseUserName = TextUtils.isEmpty(channel.channelRemark) ? channel.channelName : channel.channelRemark;
                    }
                }
                int start = builder.length();
                int end = start + praiseUserName.length();
                builder.append(praiseUserName);
                if (i != praiseSize - 1) {
                    builder.append(", ");
                }
                builder.setSpan(new TextClickSpan(context, praiseBean.uid, praiseUserName), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            builder.setSpan(new VerticalImageSpan(ContextCompat.getDrawable(context, R.drawable.heart_drawable_blue)),
                    0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return builder;
        }
        return null;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void addReplyView(Context context, String momentNo, List<MomentsReply> list, LinearLayout linearLayout, final IReplyItemClick iReplyItemClick) {
        linearLayout.removeAllViews();
        for (MomentsReply reply : list) {
            TextView richTextView = new TextView(context);
            richTextView.setTextSize(16f);
            richTextView.setTextColor(ContextCompat.getColor(context, R.color.colorDark));
            richTextView.setBackgroundResource(R.drawable.reply_click_bg);
            richTextView.setPadding(0, 5, 0, 5);

            String content;
            SpannableString spannableString;
            if (TextUtils.isEmpty(reply.reply_uid)) {
                content = reply.name + "：" + reply.content;
            } else {
                content = String.format("%s " + context.getString(R.string.str_moments_reply) + " %s：%s", reply.name, reply.reply_name, reply.content);
            }
            spannableString = new SpannableString(content);
            int nameIndex = content.indexOf(reply.name);
            spannableString.setSpan(new NormalClickableSpan(false, ContextCompat.getColor(context, R.color.color697A9F), new NormalClickableContent(NormalClickableContent.NormalClickableTypes.Other, ""), view -> {
                isCanClick = false;
                WKMomentsApplication.getInstance().gotoUserDetail(context, reply.uid);
            }), nameIndex, (nameIndex + reply.name.length()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (!TextUtils.isEmpty(reply.reply_uid)) {
                int replyIndex = content.indexOf(reply.reply_name);
                spannableString.setSpan(new NormalClickableSpan(false, ContextCompat.getColor(context, R.color.color697A9F), new NormalClickableContent(NormalClickableContent.NormalClickableTypes.Other, ""), view -> {
                    isCanClick = false;
                    WKMomentsApplication.getInstance().gotoUserDetail(context, reply.reply_uid);
                }), replyIndex, (replyIndex + reply.reply_name.length()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            Matcher matcher = EmojiManager.getInstance().getPattern().matcher(content);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                String emoji = content.substring(start, end);
                Drawable d = MoonUtil.getEmotDrawable(context, emoji, MoonUtil.SMALL_SCALE);
                if (d != null) {
                    AlignImageSpan span = new AlignImageSpan(d, AlignImageSpan.ALIGN_CENTER) {
                        @Override
                        public void onClick(View view) {

                        }
                    };
                    spannableString.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            richTextView.setText(spannableString);
            richTextView.setMovementMethod(LinkMovementMethod.getInstance());
            linearLayout.addView(richTextView);

            List<PopupMenuItem> longClickList = new ArrayList<>();
            longClickList.add(new PopupMenuItem(context.getString(R.string.str_dynmaic_copy), R.mipmap.msg_copy, () -> {
                ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", reply.content);
                assert cm != null;
                cm.setPrimaryClip(mClipData);
                WKToastUtils.getInstance().showToastNormal(context.getString(R.string.copyed));
            }));
            if (reply.uid.equals(WKConfig.getInstance().getUid())) {
                longClickList.add(new PopupMenuItem(context.getString(R.string.str_delete), R.mipmap.msg_delete, () -> {
                    WKDialogUtils.getInstance().showDialog(context, context.getString(R.string.delete_comment_title), context.getString(R.string.str_moment_delete_reply), true, "", context.getString(R.string.base_delete), 0, ContextCompat.getColor(context, R.color.red), index -> {
                        if (index == 1) {
                            iReplyItemClick.onDelete(momentNo, reply);
                        }
                    });
                }));
            } else {
                longClickList.add(new PopupMenuItem(context.getString(R.string.str_reply), R.mipmap.msg_reply, () -> {
                    int[] location = new int[2];
                    richTextView.getLocationOnScreen(location);
                    iReplyItemClick.onClick(momentNo, reply, location[1] + richTextView.getMeasuredHeight());
                }));
            }
            WKDialogUtils.getInstance().setViewLongClickPopup(richTextView, longClickList);
            //长按事件

            richTextView.setOnClickListener(view -> {
                if (!isCanClick) {
                    isCanClick = true;
                    return;
                }

                if (reply.uid.equals(WKConfig.getInstance().getUid())) {
                    WKDialogUtils.getInstance().showDialog(context, context.getString(R.string.delete_comment_title), context.getString(R.string.str_moment_delete_reply), true, "", context.getString(R.string.base_delete), 0, ContextCompat.getColor(context, R.color.red), index -> {
                        if (index == 1) {
                            iReplyItemClick.onDelete(momentNo, reply);
                        }
                    });

                } else {
                    //二级回复
                    int[] location = new int[2];
                    richTextView.getLocationOnScreen(location);
                    iReplyItemClick.onClick(momentNo, reply, location[1] + richTextView.getMeasuredHeight());
                }
            });
        }
    }

    public boolean calculateShowCheckAllText(String content) {
        Paint textPaint = new Paint();
        textPaint.setTextSize(AndroidUtilities.dp(16f));
        float textWidth = textPaint.measureText(content);
        float maxContentViewWidth = AndroidUtilities.getScreenWidth() - AndroidUtilities.dp(80f);
        float maxLines = textWidth / maxContentViewWidth;
        return maxLines > 6;
    }

    public interface IReplyItemClick {
        void onClick(String momentNo, MomentsReply reply, int locationY);

        void onDelete(String momentNo, MomentsReply reply);
    }

}
