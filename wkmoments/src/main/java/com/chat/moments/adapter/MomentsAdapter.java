package com.chat.moments.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.config.WKApiConfig;
import com.chat.base.config.WKConfig;
import com.chat.base.emoji.EmojiManager;
import com.chat.base.emoji.MoonUtil;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.EndpointSID;
import com.chat.base.endpoint.entity.ChatChooseContacts;
import com.chat.base.endpoint.entity.ChooseChatMenu;
import com.chat.base.endpoint.entity.LocationMenu;
import com.chat.base.endpoint.entity.PlayVideoMenu;
import com.chat.base.entity.ImagePopupBottomSheetItem;
import com.chat.base.entity.PopupMenuItem;
import com.chat.base.glide.GlideUtils;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.ui.components.AlignImageSpan;
import com.chat.base.ui.components.AvatarView;
import com.chat.base.ui.components.FilterImageView;
import com.chat.base.ui.components.NormalClickableContent;
import com.chat.base.ui.components.NormalClickableSpan;
import com.chat.base.utils.ImageUtils;
import com.chat.base.utils.WKDialogUtils;
import com.chat.base.utils.WKReader;
import com.chat.base.utils.WKToastUtils;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.base.views.FullyGridLayoutManager;
import com.chat.moments.R;
import com.chat.moments.WKMomentsApplication;
import com.chat.moments.activities.PrivacyUserActivity;
import com.chat.moments.entity.Moments;
import com.chat.moments.entity.MomentsReply;
import com.chat.moments.entity.MomentsType;
import com.chat.moments.service.MomentsModel;
import com.chat.moments.span.TextMovementMethod;
import com.chat.moments.utils.MomentSpanUtils;
import com.google.android.material.snackbar.Snackbar;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKChannel;
import com.xinbida.wukongim.entity.WKChannelType;
import com.xinbida.wukongim.msgmodel.WKImageContent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

/**
 * 2020-11-05 11:02
 * 朋友圈
 */
public class MomentsAdapter extends BaseMultiItemQuickAdapter<Moments, BaseViewHolder> {
    private IReplyClick iReplyClick;
    private final boolean isDetail;

    public MomentsAdapter(boolean isDetail, @Nullable List<Moments> data) {
        super(data);
        this.isDetail = isDetail;
        addItemType(MomentsType.no_data, R.layout.item_moments_nodata_layout);
        addItemType(MomentsType.single_text, R.layout.item_moments_text_layout);
        addItemType(MomentsType.one_image, R.layout.item_moments_signle_img_layout);
        addItemType(MomentsType.image_text, R.layout.item_moments_more_img_layout);
        addItemType(MomentsType.video_text, R.layout.item_moments_video_layout);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, Moments moments) {
        if (moments.itemType > 0) {
            LinearLayout replyLayout = baseViewHolder.getView(R.id.replyLayout);
            baseViewHolder.setGone(R.id.contentStatusTv, !moments.showAllText);
            baseViewHolder.setText(R.id.timeTv, isDetail ? moments.created_at : moments.showDate);
            baseViewHolder.setGone(R.id.deleteTv, !moments.publisher.equals(WKConfig.getInstance().getUid()));
            baseViewHolder.setText(R.id.publisherNameTv, moments.publisher_name);
            //动态地点
            baseViewHolder.setText(R.id.addressTv, moments.address);
            baseViewHolder.setGone(R.id.addressTv, TextUtils.isEmpty(moments.address));
            //是否提及
            if (!TextUtils.isEmpty(moments.remindUserNames)) {
                if (moments.publisher.equals(WKConfig.getInstance().getUid())) {
                    baseViewHolder.setText(R.id.remindUserNamesTv, String.format(getContext().getString(R.string.mentioned), moments.remindUserNames));
                } else {
                    baseViewHolder.setText(R.id.remindUserNamesTv, getContext().getString(R.string.moment_remind_u));
                }
                baseViewHolder.setGone(R.id.remindUserNamesTv, false);
            } else {
                baseViewHolder.setGone(R.id.remindUserNamesTv, true);
            }
            baseViewHolder.getView(R.id.addressTv).setOnClickListener(view -> EndpointManager.getInstance().invoke("show_location", new LocationMenu(getContext(), moments.address, Double.parseDouble(moments.longitude), Double.parseDouble(moments.latitude))));
            //是否隐私
            if (!TextUtils.isEmpty(moments.privacy_type)) {
                if ((moments.privacy_type.equals("internal") || moments.privacy_type.equals("prohibit"))) {
                    if (moments.publisher.equals(WKConfig.getInstance().getUid())) {
                        baseViewHolder.setGone(R.id.privacyIv, false);
                        baseViewHolder.setImageResource(R.id.privacyIv, R.mipmap.icon_partially_visible);
                    } else {
                        baseViewHolder.setGone(R.id.privacyIv, true);
                    }
                } else {
                    if (moments.privacy_type.equals("private")) {
                        baseViewHolder.setGone(R.id.privacyIv, false);
                        baseViewHolder.setImageResource(R.id.privacyIv, R.mipmap.icon_partially_private);
                    } else {
                        baseViewHolder.setGone(R.id.privacyIv, true);
                    }
                }
            } else {
                baseViewHolder.setGone(R.id.privacyIv, true);
            }

            SingleClickUtil.onSingleClick(baseViewHolder.getView(R.id.privacyIv), view -> {
                if (moments.privacy_type.equals("private")) return;
                Intent intent = new Intent(getContext(), PrivacyUserActivity.class);
                intent.putExtra("privacyType", moments.privacy_type);
                intent.putStringArrayListExtra("list", (ArrayList<String>) moments.privacy_uids);
                getContext().startActivity(intent);
            });
            AvatarView avatarView = baseViewHolder.getView(R.id.avatarView);
            avatarView.showAvatar(moments.publisher, WKChannelType.PERSONAL, moments.publisherAvatarCacheKey);
            avatarView.setOnClickListener(view1 -> WKMomentsApplication.getInstance().gotoUserDetail(getContext(), moments.publisher));
            baseViewHolder.getView(R.id.publisherNameTv).setOnClickListener(view1 -> WKMomentsApplication.getInstance().gotoUserDetail(getContext(), moments.publisher));


            baseViewHolder.setText(R.id.contentStatusTv, !moments.isExpand ? R.string.full_text : R.string.put_it_away);
            TextView contentTv = baseViewHolder.getView(R.id.contentTv);
            contentTv.setMaxLines(moments.isExpand ? Integer.MAX_VALUE : 6);

            contentTv.setVisibility(TextUtils.isEmpty(moments.text) ? View.GONE : View.VISIBLE);
            MoonUtil.identifyFaceExpression(getContext(), contentTv, moments.text, MoonUtil.DEF_SCALE);
            contentTv.setMovementMethod(LinkMovementMethod.getInstance());

            List<PopupMenuItem> list = new ArrayList<>();
            list.add(new PopupMenuItem(getContext().getString(R.string.str_dynmaic_copy), R.mipmap.msg_copy, () -> {
                ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", moments.text);
                assert cm != null;
                cm.setPrimaryClip(mClipData);
                WKToastUtils.getInstance().showToastNormal(getContext().getString(R.string.copyed));
            }));
            list.add(new PopupMenuItem(getContext().getString(R.string.str_dynmaic_collect), R.mipmap.msg_fave, () -> collect(1, String.format("%s_text", moments.moment_no), moments.publisher, moments.publisher_name, moments.text)));
            WKDialogUtils.getInstance().setViewLongClickPopup(baseViewHolder.getView(R.id.contentTv), list);

            //设置评论和点赞布局是否可见
            if (WKReader.isNotEmpty(moments.comments) || WKReader.isNotEmpty(moments.likes)) {
                baseViewHolder.setGone(R.id.replyAllView, false);
            } else baseViewHolder.setGone(R.id.replyAllView, true);

            //设置评论和点赞分割线是否可见
            if (WKReader.isNotEmpty(moments.comments) && WKReader.isNotEmpty(moments.likes)) {
                baseViewHolder.setGone(R.id.lineView, false);
            } else baseViewHolder.setGone(R.id.lineView, true);

            if (WKReader.isNotEmpty(moments.comments)) {
                replyLayout.setVisibility(View.VISIBLE);
                replyLayout.removeAllViews();
                if (isDetail) {
                    for (int i = 0, size = moments.comments.size(); i < size; i++) {
                        replyLayout.addView(getDetailReplyView(replyLayout, i, moments.moment_no, moments.comments.get(i)));
                    }
                } else {
                    MomentSpanUtils.getInstance().addReplyView(
                            getContext(), moments.moment_no, moments.comments, replyLayout, new MomentSpanUtils.IReplyItemClick() {
                                @Override
                                public void onClick(String momentNo, MomentsReply reply, int locationY) {
                                    if (iReplyClick != null)
                                        iReplyClick.onClick(momentNo, reply, locationY);
                                }

                                @Override
                                public void onDelete(String momentNo, MomentsReply reply) {
                                    deleteReply(momentNo, reply.sid);
                                }
                            });
                }

            } else {
                replyLayout.setVisibility(View.GONE);
            }
            if (isDetail) {
                baseViewHolder.setGone(R.id.praiseTv, true);
                RecyclerView recyclerView = baseViewHolder.getView(R.id.likeRecycleView);
                recyclerView.setLayoutManager(new FullyGridLayoutManager(getContext(), 8, GridLayoutManager.VERTICAL, false));
                MomentPraiseDetailAdapter adapter = new MomentPraiseDetailAdapter();
                adapter.setList(moments.likes);
                recyclerView.setAdapter(adapter);
                baseViewHolder.setGone(R.id.likeView, WKReader.isEmpty(moments.likes));
            } else {
                //设置是否详情动态
                baseViewHolder.setGone(R.id.likeView, true);
                if (!TextUtils.isEmpty(moments.praiseSpan)) {
                    baseViewHolder.setText(R.id.praiseTv, moments.praiseSpan);
                    baseViewHolder.setGone(R.id.praiseTv, false);
                    ((TextView) baseViewHolder.getView(R.id.praiseTv)).setMovementMethod(new TextMovementMethod());
                } else {
                    baseViewHolder.setGone(R.id.praiseTv, true);
                }
            }
            switch (moments.itemType) {
                case MomentsType.single_text:
                    break;
                case MomentsType.one_image: {
                    FilterImageView imageView = baseViewHolder.getView(R.id.imageView);
                    imageView.setAllCorners(0);
                    imageView.setStrokeWidth(0);
                    GlideUtils.getInstance().showImg(getContext(), WKApiConfig.getShowUrl(moments.imgs.get(0)), baseViewHolder.getView(R.id.imageView));
                    List<PopupMenuItem> imgLongClicklist = new ArrayList<>();
                    imgLongClicklist.add(new PopupMenuItem(getContext().getString(R.string.str_dynmaic_collect), R.mipmap.msg_fave, () -> collect(2, String.format("%s_0", moments.moment_no), moments.publisher, moments.publisher_name, moments.imgs.get(0))));
                    imgLongClicklist.add(new PopupMenuItem(getContext().getString(R.string.moment_forward), R.mipmap.msg_forward, () -> forwardImg(moments.imgs.get(0))));
                    WKDialogUtils.getInstance().setViewLongClickPopup(baseViewHolder.getView(R.id.imageView), imgLongClicklist);

                    baseViewHolder.getView(R.id.imageView).setOnClickListener(view -> {
                        List<ImageView> imgList = new ArrayList<>();
                        imgList.add(baseViewHolder.getView(R.id.imageView));
                        List<String> imgClickList = new ArrayList<>();
                        imgClickList.add(moments.imgs.get(0));
                        showImg(imgClickList, imgList, moments.imgs.get(0), baseViewHolder.getView(R.id.imageView), moments.moment_no, moments.publisher, moments.publisher_name);
                    });
                    if (moments.imgs.get(0).contains("@") && moments.imgs.get(0).contains("x")) {
                        String url = moments.imgs.get(0).replaceAll(".png", "");
                        String[] strings = url.split("@")[1].split("x");
                        int[] w_h = ImageUtils.getInstance().getImgWidthAndHeightToDynamic(getContext(), strings[0], strings[1]);
                        baseViewHolder.getView(R.id.imageView).getLayoutParams().width = w_h[0];
                        baseViewHolder.getView(R.id.imageView).getLayoutParams().height = w_h[1];
                    }

                }
                break;
                case MomentsType.image_text: {
                    MomentsImageAdapter imgAdapter = new MomentsImageAdapter((index, url) -> collect(2, String.format("%s_%s", moments.moment_no, index), moments.publisher, moments.publisher_name, url));
                    RecyclerView recyclerView = baseViewHolder.getView(R.id.imgRecyclerView);
                    int showCount = 3;
                    if (moments.imgs.size() == 4) {
                        showCount = 2;
                    }
                    FullyGridLayoutManager layoutManager = new FullyGridLayoutManager(getContext(), showCount);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setAdapter(imgAdapter);
                    imgAdapter.setList(moments.imgs);
                    imgAdapter.addChildClickViewIds(R.id.imageView);
                    imgAdapter.setOnItemChildClickListener((adapter1, view, position) -> {
                        String url = (String) adapter1.getItem(position);
                        if (!TextUtils.isEmpty(url)) {
                            List<ImageView> imgList = new ArrayList<>();
                            for (int i = 0; i < moments.imgs.size(); i++) {
                                FilterImageView imageView1 = (FilterImageView) imgAdapter.getViewByPosition(i, R.id.imageView);
                                imgList.add(imageView1);
                            }
                            showImg(moments.imgs, imgList, url, recyclerView.getChildAt(position).findViewById(R.id.imageView), moments.moment_no, moments.publisher, moments.publisher_name);
                        }
                    });
                }
                break;
                case MomentsType.video_text: {
                    FilterImageView coverIv = baseViewHolder.getView(R.id.imageView);
                    coverIv.setAllCorners(0);
                    coverIv.setStrokeWidth(0);
                    View videoLayout = baseViewHolder.getView(R.id.videoLayout);
                    if (!TextUtils.isEmpty(moments.video_cover_path) && moments.video_cover_path.contains("@") && moments.video_cover_path.contains("x")) {
                        String url = moments.video_cover_path.replaceAll(".png", "");
                        String[] strings = url.split("@")[1].split("x");
                        int[] w_h = ImageUtils.getInstance().getImgWidthAndHeightToDynamic(getContext(), strings[0], strings[1]);
                        ViewGroup.LayoutParams layoutParams = coverIv.getLayoutParams();
                        layoutParams.height = w_h[1];
                        layoutParams.width = w_h[0];
                        coverIv.setLayoutParams(layoutParams);
                        LinearLayout.LayoutParams layoutParams1 = (LinearLayout.LayoutParams) videoLayout.getLayoutParams();
                        layoutParams1.height = w_h[1];
                        layoutParams1.width = w_h[0];
                        videoLayout.setLayoutParams(layoutParams1);
                        GlideUtils.getInstance().showImg(getContext(), WKApiConfig.getShowUrl(moments.video_cover_path), coverIv);
                    }
                    coverIv.setOnClickListener(view -> EndpointManager.getInstance().invoke("play_video", new PlayVideoMenu((Activity) getContext(), coverIv, "", WKApiConfig.getShowUrl(moments.video_path), WKApiConfig.getShowUrl(moments.video_cover_path))));
                }
                break;
            }
        }
    }


    private void showImg(List<String> list, List<ImageView> imgList, String uri, ImageView imageView, String unique_key, String author_uid, String author_name) {
        //查看大图
        List<Object> tempImgList = new ArrayList<>();
        for (int i = 0, size = list.size(); i < size; i++) {
            tempImgList.add(WKApiConfig.getShowUrl(list.get(i)));
        }
        int index = 0;
        for (int i = 0; i < tempImgList.size(); i++) {
            if (tempImgList.get(i).equals(WKApiConfig.getShowUrl(uri))) {
                index = i;
                break;
            }
        }
        List<ImagePopupBottomSheetItem> list1 = new ArrayList<>();
        list1.add(new ImagePopupBottomSheetItem(getContext().getString(R.string.forward), R.mipmap.msg_forward, position -> {
            String path = list.get(position);
            forwardImg(path);
        }));
        list1.add(new ImagePopupBottomSheetItem(getContext().getString(R.string.favorite), R.mipmap.msg_fave, position -> collect(2, unique_key, author_uid, author_name, list.get(position))));

        WKDialogUtils.getInstance().showImagePopup(getContext(), tempImgList, imgList, imageView, index, list1, null, null);

    }

    public void setReplyClick(IReplyClick iReplyClick) {
        this.iReplyClick = iReplyClick;
    }

    public interface IReplyClick {
        void onClick(String momentNo, MomentsReply reply, int locationY);
    }


    private void collect(int type, String unique_key, String author_uid, String author_name, String content) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("content", content);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("type", type);

        hashMap.put("unique_key", unique_key);
        hashMap.put("author_uid", author_uid);
        hashMap.put("author_name", author_name);
        hashMap.put("payload", jsonObject);
        hashMap.put("activity", getContext());
        EndpointManager.getInstance().invoke("favorite_add", hashMap);
    }


    private View getDetailReplyView(ViewGroup viewGroup, int index, String momentNo, MomentsReply momentsReply) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_moment_detail_reply_layout, viewGroup, false);
        ImageView commentIv = view.findViewById(R.id.commentIv);
        AvatarView avatarView = view.findViewById(R.id.avatarView);
        TextView timeTv = view.findViewById(R.id.timeTv);
        TextView contentTv = view.findViewById(R.id.contentTv);

        TextView nameTv = view.findViewById(R.id.nameTv);
        avatarView.setSize(25);
        avatarView.showAvatar(momentsReply.uid, WKChannelType.PERSONAL, momentsReply.avatarCacheKey);
        nameTv.setText(momentsReply.name);
        timeTv.setText(momentsReply.comment_at);

        if (index == 0) commentIv.setVisibility(View.VISIBLE);
        else commentIv.setVisibility(View.INVISIBLE);
        if (TextUtils.isEmpty(momentsReply.reply_uid)) {
            contentTv.setText(momentsReply.content);
            MoonUtil.identifyFaceExpression(getContext(), contentTv, momentsReply.content, MoonUtil.DEF_SCALE);
        } else {
            String user = String.format(getContext().getString(R.string.str_moments_reply_user), momentsReply.reply_name);
            String content = String.format("%s%s", user, momentsReply.content);
            MoonUtil.identifyFaceExpression(getContext(), contentTv, momentsReply.content, MoonUtil.DEF_SCALE);

            int startIndex = content.indexOf(momentsReply.reply_name);
            SpannableString spannableString = new SpannableString(content);
            spannableString.setSpan(new NormalClickableSpan(false, ContextCompat.getColor(getContext(), R.color.color697A9F), new NormalClickableContent(NormalClickableContent.NormalClickableTypes.Other, ""), view12 -> WKMomentsApplication.getInstance().gotoUserDetail(getContext(), momentsReply.reply_uid)), startIndex, (startIndex + momentsReply.reply_name.length()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            Matcher matcher = EmojiManager.getInstance().getPattern().matcher(content);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                String emoji = content.substring(start, end);
                Drawable d = MoonUtil.getEmotDrawable(getContext(), emoji, MoonUtil.SMALL_SCALE);
                if (d != null) {
                    AlignImageSpan span = new AlignImageSpan(d, AlignImageSpan.ALIGN_CENTER) {
                        @Override
                        public void onClick(View view) {

                        }
                    };
                    spannableString.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            contentTv.setText(spannableString);
        }
        contentTv.setMovementMethod(LinkMovementMethod.getInstance());
        view.setOnClickListener(view1 -> {
            if (momentsReply.uid.equals(WKConfig.getInstance().getUid())) {
                WKDialogUtils.getInstance().showDialog(getContext(), getContext().getString(R.string.delete_comment_title), getContext().getString(R.string.str_moment_delete_reply), true, "", getContext().getString(R.string.base_delete), 0, ContextCompat.getColor(getContext(), R.color.red), index1 -> {
                    if (index1 == 1) {
                        deleteReply(momentNo, momentsReply.sid);
                    }
                });
            } else {
                //二级回复
                int[] location = new int[2];
                contentTv.getLocationOnScreen(location);
                if (iReplyClick != null)
                    iReplyClick.onClick(momentNo, momentsReply, location[1]);
            }
        });
        avatarView.setOnClickListener(view1 -> WKMomentsApplication.getInstance().gotoUserDetail(getContext(), momentsReply.uid));
        nameTv.setOnClickListener(view1 -> WKMomentsApplication.getInstance().gotoUserDetail(getContext(), momentsReply.uid));
        return view;
    }


    private void deleteReply(String momentNo, String sid) {
        MomentsModel.getInstance().deleteReply(momentNo, sid, (code, msg) -> {
            if (code == HttpResponseCode.success) {
                for (int i = 0, size = getData().size(); i < size; i++) {
                    if (getData().get(i).moment_no.equals(momentNo)) {
                        for (int j = 0, len = getData().get(i).comments.size(); j < len; j++) {
                            if (getData().get(i).comments.get(j).sid.equals(sid)) {
                                getData().get(i).comments.remove(j);
                                break;
                            }
                        }
                        notifyItemChanged(i + getHeaderLayoutCount());
                        break;
                    }
                }

            } else WKToastUtils.getInstance().showToastNormal(msg);
        });

    }

    private void forwardImg(String path) {
        String url = path.replaceAll(".png", "");
        String[] strings = url.split("@")[1].split("x");
        int[] w_h = ImageUtils.getInstance().getImgWidthAndHeightToDynamic(getContext(), strings[0], strings[1]);
        WKImageContent imageContent = new WKImageContent();
        imageContent.url = path;
        imageContent.width = w_h[0];
        imageContent.height = w_h[1];
        EndpointManager.getInstance().invoke(EndpointSID.showChooseChatView, new ChooseChatMenu(new ChatChooseContacts(list1 -> {
            if (WKReader.isNotEmpty(list1)) {
                for (WKChannel channel : list1) {
                    WKIM.getInstance().getMsgManager().send(imageContent, channel);
                }
                ViewGroup viewGroup = (ViewGroup) ((Activity) getContext()).findViewById(android.R.id.content).getRootView();
                Snackbar.make(viewGroup, getContext().getString(R.string.str_forward), 1000)
                        .setAction("", v1 -> {
                        })
                        .show();
            }
        }), imageContent));
    }
}
