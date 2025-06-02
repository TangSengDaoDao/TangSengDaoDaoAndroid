package com.chat.moments.activities;

import android.view.Gravity;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.chat.base.base.WKBaseActivity;
import com.chat.base.config.WKConfig;
import com.chat.base.config.WKConstants;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.WKDialogUtils;
import com.chat.base.utils.WKReader;
import com.chat.moments.R;
import com.chat.moments.WKMomentsApplication;
import com.chat.moments.adapter.MomentsAdapter;
import com.chat.moments.databinding.ActMomentsDetailLayoutBinding;
import com.chat.moments.entity.Moments;
import com.chat.moments.entity.MomentsPraise;
import com.chat.moments.entity.MomentsReply;
import com.chat.moments.service.MomentsModel;
import com.chat.moments.utils.MomentSpanUtils;
import com.chat.moments.views.FeedActionPopup;
import com.chat.moments.views.FeedCommentDialog;

import java.util.ArrayList;

/**
 * 2020-11-12 10:43
 * 动态详情
 */
public class MomentsDetailActivity extends WKBaseActivity<ActMomentsDetailLayoutBinding> {
    private MomentsAdapter adapter;
    private String momentNo;
    private MomentsReply replyMomentsReply;

    @Override
    protected ActMomentsDetailLayoutBinding getViewBinding() {
        return ActMomentsDetailLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.detail_moments);
    }

    @Override
    protected void initPresenter() {
        momentNo = getIntent().getStringExtra("momentNo");
    }

    @Override
    protected void initView() {
//        wkVBinding.emojiPanelView.initEmojiPanel(this);
        adapter = new MomentsAdapter(true, new ArrayList<>());
        initAdapter(wkVBinding.recyclerView, adapter);
    }

    @Override
    protected void initListener() {
        adapter.addChildClickViewIds(R.id.contentStatusTv, R.id.moreIv, R.id.deleteTv);
        adapter.setOnItemChildClickListener((adapter1, view1, position) -> {
            Moments moments = (Moments) adapter1.getItem(position);
            if (moments != null) {
                if (view1.getId() == R.id.contentStatusTv) {
                    moments.isExpand = !moments.isExpand;
                    adapter1.notifyItemChanged(position + adapter1.getHeaderLayoutCount());
                } else if (view1.getId() == R.id.moreIv) {
                    boolean isLiked = false;
                    if (WKReader.isNotEmpty(moments.likes)) {
                        for (int i = 0, size = moments.likes.size(); i < size; i++) {
                            if (moments.likes.get(i).uid.equals(WKConfig.getInstance().getUid())) {
                                isLiked = true;
                                break;
                            }
                        }
                    }
                    FeedActionPopup feedActionPopup = new FeedActionPopup(this, isLiked, type -> {
                        if (type == 1) {
                            int[] location = new int[2];
                            view1.getLocationOnScreen(location);
                            wkVBinding.recyclerView.smoothScrollBy(0, location[1] + WKConstants.getKeyboardHeight() + AndroidUtilities.dp(88) - AndroidUtilities.getScreenHeight());
                            replyMomentsReply = null;
                            showCommentDialog(getString(R.string.moments_reply));
//                            wkVBinding.emojiPanelView.showEmojiPanel(getString(R.string.moments_reply));
                        } else {
                            boolean isLike = true;
                            if (WKReader.isNotEmpty(moments.likes)) {
                                for (MomentsPraise praise : moments.likes) {
                                    if (praise.uid.equals(WKConfig.getInstance().getUid())) {
                                        isLike = false;
                                        break;
                                    }
                                }
                            }
                            if (isLike) {
                                MomentsModel.getInstance().like(moments.moment_no, (code, msg) -> {
                                    if (code == HttpResponseCode.success) {
                                        if (moments.likes == null)
                                            moments.likes = new ArrayList<>();
                                        moments.likes.add(new MomentsPraise(WKConfig.getInstance().getUid(), WKConfig.getInstance().getUserName()));
                                        moments.praiseSpan = MomentSpanUtils.getInstance().makePraiseSpan(WKMomentsApplication.getInstance().getContext(), moments.likes);
                                        adapter1.notifyItemChanged(position + adapter1.getHeaderLayoutCount());
                                    } else showToast(msg);
                                });
                            } else {
                                MomentsModel.getInstance().unlike(moments.moment_no, (code, msg) -> {
                                    if (code == HttpResponseCode.success) {
                                        for (int i = 0, size = moments.likes.size(); i < size; i++) {
                                            if (moments.likes.get(i).uid.equals(WKConfig.getInstance().getUid())) {
                                                moments.likes.remove(i);
                                                break;
                                            }
                                        }
                                        moments.praiseSpan = MomentSpanUtils.getInstance().makePraiseSpan(WKMomentsApplication.getInstance().getContext(), moments.likes);
                                        adapter1.notifyItemChanged(position + adapter1.getHeaderLayoutCount());
                                    } else showToast(msg);
                                });
                            }
                        }
                    });
                    if (feedActionPopup.isShowing()) {
                        feedActionPopup.dismiss();
                    } else {
                        int[] location = new int[2];
                        view1.getLocationOnScreen(location);
                        int xOffset = location[0] - AndroidUtilities.dp(150f + 10);
                        int yOffset = location[1] + (view1.getHeight() - AndroidUtilities.dp(34)) / 2;
                        feedActionPopup.showAtLocation(view1, Gravity.NO_GRAVITY, xOffset, yOffset);
                    }

                } else if (view1.getId() == R.id.deleteTv) {
                    WKDialogUtils.getInstance().showDialog(this, getString(R.string.delete_moments_tips), getString(R.string.delete_moments_tips), true, "", getString(R.string.base_delete), 0, ContextCompat.getColor(this, R.color.red), index -> {
                        if (index == 1) {
                            MomentsModel.getInstance().delete(moments.moment_no, (code, msg) -> {
                                if (code == HttpResponseCode.success) {
                                    adapter1.removeAt(position);
                                } else showToast(msg);
                            });
                        }
                    });
                }
            }
        });
        adapter.setReplyClick(((momentNo, reply, locationY) -> {
            wkVBinding.recyclerView.smoothScrollBy(0, locationY + WKConstants.getKeyboardHeight() + AndroidUtilities.dp(48) - AndroidUtilities.getScreenHeight());
            replyMomentsReply = reply;
            showCommentDialog(String.format(getString(R.string.str_moments_reply_user), reply.name));
//            wkVBinding.emojiPanelView.showEmojiPanel(String.format(getString(R.string.str_moments_reply_user), reply.name));
        }));
//        wkVBinding.emojiPanelView.addOnInputResult(content -> MomentsModel.getInstance().comments(momentNo, content, replyMomentsReply == null ? "" : replyMomentsReply.sid, replyMomentsReply == null ? "" : replyMomentsReply.uid, replyMomentsReply == null ? "" : replyMomentsReply.name, (code, msg, commentID) -> {
//            if (code == HttpResponseCode.success) {
//
//                for (int i = 0, size = adapter.getData().size(); i < size; i++) {
//                    if (adapter.getData().get(i).moment_no.equals(momentNo)) {
//                        if (adapter.getData().get(i).comments == null) {
//                            adapter.getData().get(i).comments = new ArrayList<>();
//                        }
//                        //添加一条评论
//                        MomentsReply reply = new MomentsReply();
//                        reply.sid = commentID;
//                        reply.uid = WKConfig.getInstance().getUid();
//                        reply.name = WKConfig.getInstance().getUserName();
//                        reply.content = content;
//                        if (replyMomentsReply != null) {
//                            reply.reply_uid = replyMomentsReply.uid;
//                            reply.reply_name = replyMomentsReply.name;
//                        }
//                        adapter.getData().get(i).comments.add(reply);
//                        adapter.notifyItemChanged(i + adapter.getHeaderLayoutCount());
//                        break;
//                    }
//                }
//            } else showToast(msg);
//        }));

    }

    @Override
    protected void initData() {
        super.initData();
        MomentsModel.getInstance().detail(momentNo, (code, msg, moments) -> {
            if (code == HttpResponseCode.success) {
                adapter.addData(moments);
            } else showDialog(msg, null);
        });
    }

    @Override
    protected void backListener(int type) {
        super.backListener(type);
        onBack();
    }

    private void onBack() {
//        if (wkVBinding.emojiPanelView.isShowing()) {
//            wkVBinding.emojiPanelView.dismiss();
//        } else {
//            finish();
//        }
        finish();
    }


    private void showCommentDialog(String hintText) {
        new FeedCommentDialog(MomentsDetailActivity.this, hintText, (visible, currentTop) -> {

        }, new FeedCommentDialog.IEmojiClick() {
            @Override
            public void onEmojiClick(String emojiName) {

            }

            @Override
            public void onSendClick(String content) {
                MomentsModel.getInstance().comments(momentNo, content, replyMomentsReply == null ? "" : replyMomentsReply.sid, replyMomentsReply == null ? "" : replyMomentsReply.uid, replyMomentsReply == null ? "" : replyMomentsReply.name, (code, msg, commentID) -> {
                    if (code == HttpResponseCode.success) {

                        for (int i = 0, size = adapter.getData().size(); i < size; i++) {
                            if (adapter.getData().get(i).moment_no.equals(momentNo)) {
                                if (adapter.getData().get(i).comments == null) {
                                    adapter.getData().get(i).comments = new ArrayList<>();
                                }
                                //添加一条评论
                                MomentsReply reply = new MomentsReply();
                                reply.sid = commentID;
                                reply.uid = WKConfig.getInstance().getUid();
                                reply.name = WKConfig.getInstance().getUserName();
                                reply.content = content;
                                if (replyMomentsReply != null) {
                                    reply.reply_uid = replyMomentsReply.uid;
                                    reply.reply_name = replyMomentsReply.name;
                                }
                                adapter.getData().get(i).comments.add(reply);
                                adapter.notifyItemChanged(i + adapter.getHeaderLayoutCount());
                                break;
                            }
                        }
                    } else showToast(msg);
                });
            }
        }).show();
    }
}
