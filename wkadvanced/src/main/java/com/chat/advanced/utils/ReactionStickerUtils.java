package com.chat.advanced.utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.chat.advanced.R;
import com.chat.advanced.ui.AllMsgReactionsPopupView;
import com.chat.base.msg.ChatAdapter;
import com.chat.base.msgitem.WKChatIteMsgFromType;
import com.chat.base.ui.components.AvatarView;
import com.chat.base.ui.components.RoundLayout;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.LayoutHelper;
import com.chat.base.utils.WKReader;
import com.chat.base.views.CommonAnim;
import com.chat.base.views.ShadowLayout;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.interfaces.SimpleCallback;
import com.xinbida.wukongim.entity.WKChannelType;
import com.xinbida.wukongim.entity.WKMsgReaction;

import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReactionStickerUtils {
    public static String showAnimation = "";
    static RLottieImageView bigIv = null;

    public static int getReactionStickerBig(String text) {
        int id = R.raw.like_big;
        if (TextUtils.isEmpty(text)) {
            return id;
        }
        switch (text) {
            case "like" -> id = R.raw.like_big;
            case "bad" -> id = R.raw.bad_big;
            case "love" -> id = R.raw.love_big;
            case "fire" -> id = R.raw.fire_big;
            case "celebrate" -> id = R.raw.celebrate_big;
            case "happy" -> id = R.raw.happy_big;
            case "terrified" -> id = R.raw.terrified_big;
            case "haha" -> id = R.raw.haha_big;
            case "depressed" -> id = R.raw.depressed_big;
            case "shit" -> id = R.raw.shit_big;
            case "vomit" -> id = R.raw.vomit_big;
        }
        return id;
    }

    public static String getReactionStickerLittle(String text) {
        String id = "reactions/like_little.lim";
        if (TextUtils.isEmpty(text)) {
            return id;
        }
        switch (text) {
            case "like" -> id = "reactions/like_little.lim";
            case "bad" -> id = "reactions/bad_little.lim";
            case "love" -> id = "reactions/love_little.lim";
            case "fire" -> id = "reactions/fire_little.lim";
            case "celebrate" -> id = "reactions/celebrate_little.lim";
            case "happy" -> id = "reactions/happy_little.lim";
            case "terrified" -> id = "reactions/terrified_little.lim";
            case "haha" -> id = "reactions/haha_little.lim";
            case "depressed" -> id = "reactions/depressed_little.lim";
            case "shit" -> id = "reactions/shit_little.lim";
            case "vomit" -> id = "reactions/vomit_little.lim";
        }
        return id;
    }


    public static int getEmojiID(String text) {
        int id = R.raw.like;
        if (TextUtils.isEmpty(text)) {
            return id;
        }
        switch (text) {
            case "like" -> id = R.raw.like;
            case "bad" -> id = R.raw.bad;
            case "love" -> id = R.raw.love;
            case "fire" -> id = R.raw.fire;
            case "celebrate" -> id = R.raw.celebrate;
            case "happy" -> id = R.raw.happy;
            case "terrified" -> id = R.raw.terrified;
            case "haha" -> id = R.raw.haha;
            case "depressed" -> id = R.raw.depressed;
            case "shit" -> id = R.raw.shit;
            case "vomit" -> id = R.raw.vomit;
        }
        return id;
    }

    private static Map<String, Integer> getMaps(List<WKMsgReaction> list) {
        HashMap<String, Integer> map = new HashMap<>();
        if (WKReader.isNotEmpty(list)) {
            for (WKMsgReaction reaction : list) {
                if (reaction.isDeleted == 1) continue;
                if (map.containsKey(reaction.emoji)) {
                    Integer integer = map.get(reaction.emoji);
                    if (integer != null)
                        map.put(reaction.emoji, (integer + 1));
                } else map.put(reaction.emoji, 1);

            }
        }
        return map;
    }

    private static List<String> getEmojis(List<WKMsgReaction> list) {
        List<String> emojis = new ArrayList<>();
        Map<String, Integer> map = getMaps(list);
        if (map.isEmpty()) {
            return emojis;
        }
        Map<String, Integer> integerHashMap = sortMapByValue(map);
        for (String key : integerHashMap.keySet()) {
            boolean isAdd = true;
            for (String text : emojis) {
                if (key.equals(text)) {
                    isAdd = false;
                    break;
                }
            }
            if (isAdd) {
                emojis.add(key);
            }
            if (emojis.size() >= 3) break;
        }
        return emojis;
    }

    public static void refreshMsgReactionsData(FrameLayout parentView, ChatAdapter chatAdapter, WKChatIteMsgFromType from, List<WKMsgReaction> list) {
        List<String> emojis = getEmojis(list);
        if (parentView.getChildCount() == 0 && emojis.size() != 0) {
            parentView.setTag(1);
            CommonAnim.getInstance().showOrHide(parentView, true, true, true);
        }
        if (parentView.getChildCount() != 0 && WKReader.isEmpty(list)) {
            CommonAnim.getInstance().showOrHide(parentView, false, true, true);
            return;
        }
        setMsgReactionsData(parentView, chatAdapter, from, list);
    }

    public static void setMsgReactionsData(FrameLayout parentView, ChatAdapter chatAdapter, WKChatIteMsgFromType from, List<WKMsgReaction> list) {
        if (parentView == null || chatAdapter == null) {
            return;
        }
        if (WKReader.isEmpty(list)) {
            parentView.setVisibility(View.GONE);
            return;
        } else {
            parentView.setTag(1);
            CommonAnim.getInstance().showOrHide(parentView, true, true);
        }
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.gravity = from == WKChatIteMsgFromType.SEND ? Gravity.START : Gravity.END;
        param.topMargin = -AndroidUtilities.dp(10);
        param.leftMargin = AndroidUtilities.dp(from == WKChatIteMsgFromType.SEND ? 5 : 0);
        param.rightMargin = AndroidUtilities.dp(from == WKChatIteMsgFromType.SEND ? 0 : 5);
        parentView.setLayoutParams(param);

        parentView.removeAllViews();

        ShadowLayout shadowLayout = new ShadowLayout(chatAdapter.getContext());
        shadowLayout.setCornerRadius(AndroidUtilities.dp(30));
        parentView.addView(shadowLayout);
        FrameLayout frameLayout = new FrameLayout(chatAdapter.getContext());
        shadowLayout.addView(frameLayout,LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 5, 3, 5, 3));

        List<String> emojis = getEmojis(list);
        if (emojis.isEmpty()) {
            CommonAnim.getInstance().showOrHide(parentView, false, true);
            return;
        }
        for (int i = emojis.size() - 1; i >= 0; i--) {
            frameLayout.addView(getLottieView(emojis.get(i), i, chatAdapter));
        }
        TextView textView = new TextView(chatAdapter.getContext());
        textView.setTextColor(ContextCompat.getColor(chatAdapter.getContext(), R.color.color999));
        textView.setText(String.valueOf(list.size()));
        textView.setTextSize(14f);
        textView.getPaint().setFakeBoldText(true);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (emojis.size() == 1)
            layoutParams.leftMargin = emojis.size() * AndroidUtilities.dp(28);
        else if (emojis.size() == 2)
            layoutParams.leftMargin = emojis.size() * AndroidUtilities.dp(28) - AndroidUtilities.dp(10);
        else if (emojis.size() == 3)
            layoutParams.leftMargin = emojis.size() * AndroidUtilities.dp(28) - AndroidUtilities.dp(15);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        layoutParams.rightMargin = AndroidUtilities.dp(3);
        textView.setLayoutParams(layoutParams);
        frameLayout.addView(textView);


        frameLayout.setOnClickListener(v -> {
            chatAdapter.getConversationContext().hideSoftKeyboard();
            new XPopup.Builder(chatAdapter.getConversationContext().getChatActivity())
                    .isThreeDrag(true).hasShadowBg(true).setPopupCallback(new SimpleCallback() {
                        @Override
                        public void onShow(BasePopupView popupView) {
                            chatAdapter.getConversationContext().onViewPicture(true);
                        }

                        @Override
                        public void onDismiss(BasePopupView popupView) {
                            chatAdapter.getConversationContext().onViewPicture(false);
                        }
                    }).asCustom(new AllMsgReactionsPopupView(chatAdapter.getConversationContext().getChatActivity(), list, sortMapByValue(getMaps(list))))
                    .show();
        });
        if (!TextUtils.isEmpty(showAnimation)) {
            ViewGroup mRootView = ((Activity) chatAdapter.getContext()).findViewById(android.R.id.content);
            if (bigIv != null && bigIv.isPlaying()) {
                bigIv.stopAnimation();
                mRootView.removeView(bigIv);
            } else {
                bigIv = new RLottieImageView(chatAdapter.getContext());
                mRootView.addView(bigIv);
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {

                RLottieDrawable drawable = new RLottieDrawable(chatAdapter.getContext(), getReactionStickerBig(showAnimation), showAnimation, AndroidUtilities.dp(80), AndroidUtilities.dp(80), false, null);
                bigIv.setAutoRepeat(false);
                bigIv.setAnimation(drawable);


                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bigIv.getLayoutParams();
                params.width = AndroidUtilities.dp(80);
                params.height = AndroidUtilities.dp(80);
                bigIv.setLayoutParams(params);
                int[] location = new int[2];
                frameLayout.getLocationOnScreen(location);

                bigIv.setX(location[0]);
                bigIv.setY(location[1] - AndroidUtilities.dp(30));
                bigIv.playAnimation();
                showAnimation = "";
            }, 200);
        }


    }

    private static View getPersonalView(String emoji, ChatAdapter chatAdapter, String uid) {
        LinearLayout layout = new LinearLayout(chatAdapter.getContext());
        int padding = AndroidUtilities.dp(1);
        FrameLayout.LayoutParams lp = LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER);
        lp.setMargins(padding, padding, padding, padding);
        layout.setLayoutParams(lp);
        ImageView imageView1 = new ImageView(chatAdapter.getContext());
        AvatarView avatar = new AvatarView(chatAdapter.getContext());
        avatar.setSize(25, 25 * 0.5f);
        layout.addView(imageView1, AndroidUtilities.dp(25), AndroidUtilities.dp(25));
        layout.addView(avatar, AndroidUtilities.dp(25), AndroidUtilities.dp(25));
        avatar.showAvatar(uid, WKChannelType.PERSONAL);
        imageView1.setImageResource(getEmojiID(emoji));

        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams1.width = AndroidUtilities.dp(25);
        layoutParams1.height = AndroidUtilities.dp(25);
        layoutParams1.leftMargin = AndroidUtilities.dp(5);
        avatar.setLayoutParams(layoutParams1);

        RoundLayout roundLayout1 = new RoundLayout(chatAdapter.getContext());
        int[] colors = chatAdapter.getContext().getResources().getIntArray(R.array.name_colors);
        int i = Math.abs(emoji.hashCode()) % colors.length;
        roundLayout1.setCorner(AndroidUtilities.dp(20));
        roundLayout1.setBgColor(colors[i]);
        roundLayout1.addView(layout);
        return roundLayout1;
    }

    private static View getLottieView(String emoji, int index, ChatAdapter chatAdapter) {
        AppCompatImageView imageView = new AppCompatImageView(chatAdapter.getContext());
        imageView.setImageResource(getEmojiID(emoji));
        RoundLayout roundLayout = new RoundLayout(chatAdapter.getContext());
        roundLayout.setCorner(AndroidUtilities.dp(20));
        int[] colors = chatAdapter.getContext().getResources().getIntArray(R.array.name_colors);
        int i = Math.abs(emoji.hashCode()) % colors.length;
        roundLayout.setBgColor(colors[i]);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.width = AndroidUtilities.dp(25);
        layoutParams.height = AndroidUtilities.dp(25);
        if (index == 2)
            layoutParams.leftMargin = AndroidUtilities.dp(30);
        else if (index == 1)
            layoutParams.leftMargin = AndroidUtilities.dp(15);
        else layoutParams.leftMargin = 0;
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        roundLayout.setLayoutParams(layoutParams);
        roundLayout.addView(imageView);


        FrameLayout.LayoutParams layoutParams1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParams1.leftMargin = AndroidUtilities.dp(3);
        layoutParams1.rightMargin = AndroidUtilities.dp(3);
        layoutParams1.topMargin = AndroidUtilities.dp(3);
        layoutParams1.bottomMargin = AndroidUtilities.dp(3);
        imageView.setLayoutParams(layoutParams1);
        return roundLayout;
    }

    private static Map<String, Integer> sortMapByValue(Map<String, Integer> oriMap) {
        if (oriMap == null || oriMap.isEmpty()) {
            return null;
        }
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(
                oriMap.entrySet());
        Collections.sort(entryList, new MapValueComparator());

        Iterator<Map.Entry<String, Integer>> iterator = entryList.iterator();
        Map.Entry<String, Integer> tmpEntry;
        while (iterator.hasNext()) {
            tmpEntry = iterator.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }

    static class MapValueComparator implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> me1, Map.Entry<String, Integer> me2) {

            return me1.getValue().compareTo(me2.getValue());
        }
    }
}
