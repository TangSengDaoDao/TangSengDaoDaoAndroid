package com.chat.moments.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chat.base.WKBaseApplication;
import com.chat.base.base.WKBaseActivity;
import com.chat.base.config.WKApiConfig;
import com.chat.base.config.WKConfig;
import com.chat.base.config.WKConstants;
import com.chat.base.config.WKSharedPreferencesUtil;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.entity.BottomSheetItem;
import com.chat.base.glide.ChooseMimeType;
import com.chat.base.glide.ChooseResult;
import com.chat.base.glide.GlideUtils;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.net.ud.WKDownloader;
import com.chat.base.net.ud.WKProgressManager;
import com.chat.base.net.ud.WKUploader;
import com.chat.base.ui.components.AvatarView;
import com.chat.base.ui.components.FilterImageView;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.LayoutHelper;
import com.chat.base.utils.WKDialogUtils;
import com.chat.base.utils.WKFileUtils;
import com.chat.base.utils.WKPermissions;
import com.chat.base.utils.WKReader;
import com.chat.base.utils.WKTimeUtils;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.moments.R;
import com.chat.moments.WKMomentsApplication;
import com.chat.moments.adapter.MomentsAdapter;
import com.chat.moments.databinding.ActMomentsLayoutBinding;
import com.chat.moments.entity.MomentSetting;
import com.chat.moments.entity.Moments;
import com.chat.moments.entity.MomentsPraise;
import com.chat.moments.entity.MomentsReply;
import com.chat.moments.entity.MomentsType;
import com.chat.moments.service.MomentFileUpload;
import com.chat.moments.service.MomentsContact;
import com.chat.moments.service.MomentsModel;
import com.chat.moments.service.MomentsPresenter;
import com.chat.moments.utils.MomentSpanUtils;
import com.chat.moments.views.FeedActionPopup;
import com.chat.moments.views.FeedCommentDialog;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKChannel;
import com.xinbida.wukongim.entity.WKChannelType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 2020-11-05 10:43
 * 朋友圈列表
 */
public class MomentsActivity extends WKBaseActivity<ActMomentsLayoutBinding> implements MomentsContact.MomentsView {
    RecyclerView recyclerView;
    private View newMomentsLayout;
    private AvatarView newMomentsAvatarIv;//新消息头像
    private TextView newMomentsCountTv; //新消息数量
    private MomentsAdapter adapter;
    private MomentsPresenter presenter;
    private int page = 1;
    //被回复的动态ID
    private String replyMomentNo;
    private MomentsReply replyMomentsReply;
    private String uid;//查看某人的朋友圈


    @Override
    protected ActMomentsLayoutBinding getViewBinding() {
        return ActMomentsLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initPresenter() {
        if (getIntent().hasExtra("uid"))
            uid = getIntent().getStringExtra("uid");
        presenter = new MomentsPresenter(this);

    }

    @Override
    protected void backListener(int type) {
        super.backListener(type);
        finish();
    }

    @Override
    protected void initView() {
        WKSharedPreferencesUtil.getInstance().putSP(WKConfig.getInstance().getUid() + "_moments_msg_uid", "");
        EndpointManager.getInstance().invokes(EndpointCategory.wkRefreshMailList, null);
//        wkVBinding.emojiPanelView.initEmojiPanel(this);
        wkVBinding.refreshLayout.setEnableRefresh(false);
        wkVBinding.wrapview.setTitleViews(wkVBinding.titleCenterTv, wkVBinding.backIv, wkVBinding.cameraIv, wkVBinding.titleView);
        wkVBinding.wrapview.setUid(uid);
        adapter = new MomentsAdapter(false, new ArrayList<>());
        recyclerView = wkVBinding.wrapview.getmContentView();
        ((DefaultItemAnimator) Objects.requireNonNull(recyclerView.getItemAnimator())).setSupportsChangeAnimations(false);
        initAdapter(recyclerView, adapter);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        View headerView = wkVBinding.wrapview.getmHeadViw();
        adapter.addHeaderView(headerView);
        newMomentsLayout = headerView.findViewById(R.id.newMomentsLayout);
        //用户头像
        FilterImageView avatarIv = headerView.findViewById(R.id.avatarIv);
        avatarIv.setAllCorners(8);
        //用户名称
        TextView nameTv = headerView.findViewById(R.id.nameTv);
        newMomentsAvatarIv = headerView.findViewById(R.id.avatarView);
        newMomentsAvatarIv.setSize(25);
        newMomentsAvatarIv.setStrokeWidth(1);
        newMomentsCountTv = headerView.findViewById(R.id.newMomentsCountTv);
        wkVBinding.wrapview.startRefresh();
        //隐藏发布按钮

        if (!TextUtils.isEmpty(uid) && !uid.equals(WKConfig.getInstance().getUid())) {
            wkVBinding.cameraIv.setVisibility(View.GONE);
            WKChannel channel = WKIM.getInstance().getChannelManager().getChannel(uid, WKChannelType.PERSONAL);
            if (channel != null) {
                nameTv.setText(TextUtils.isEmpty(channel.channelRemark) ? channel.channelName : channel.channelRemark);
                GlideUtils.getInstance().showAvatarImg(this, uid, WKChannelType.PERSONAL, channel.avatarCacheKey, avatarIv);
            } else {
                GlideUtils.getInstance().showImg(this, WKApiConfig.getShowAvatar(uid, WKChannelType.PERSONAL), avatarIv);
            }
            wkVBinding.cameraIv.setVisibility(View.GONE);
        } else {
            WKChannel channel = WKIM.getInstance().getChannelManager().getChannel(WKConfig.getInstance().getUid(), WKChannelType.PERSONAL);
            if (channel != null) {
                GlideUtils.getInstance().showAvatarImg(this, WKConfig.getInstance().getUid(), WKChannelType.PERSONAL, channel.avatarCacheKey, avatarIv);
            } else {
                GlideUtils.getInstance().showImg(this, WKApiConfig.getShowAvatar(WKConfig.getInstance().getUid(), WKChannelType.PERSONAL), avatarIv);
            }
            wkVBinding.cameraIv.setVisibility(View.VISIBLE);
            nameTv.setText(WKConfig.getInstance().getUserName());
            if (TextUtils.isEmpty(uid))
                SingleClickUtil.onSingleClick(avatarIv, view1 -> {
                    Intent intent = new Intent(this, MomentsActivity.class);
                    intent.putExtra("uid", WKConfig.getInstance().getUid());
                    startActivity(intent);
                });
        }
        if (TextUtils.isEmpty(uid))
            presenter.list(page);
        else presenter.listByUid(page, uid);
        getUserMomentBg();
        getMomentMsg();
    }

    LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this) {
        @Override
        public boolean canScrollVertically() {
            return !wkVBinding.wrapview.isRefreshing();
        }
    };

    @Override
    protected void initListener() {

        SingleClickUtil.onSingleClick(newMomentsLayout, view1 -> {
            Intent intent = new Intent(this, MomentsMsgListActivity.class);
            startActivity(intent);
        });
        wkVBinding.wrapview.setOnRefreshListener(() -> {
            page = 1;
            wkVBinding.refreshLayout.setEnableLoadMore(true);
            if (TextUtils.isEmpty(uid))
                presenter.list(page);
            else presenter.listByUid(page, uid);
        });
        wkVBinding.refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                page++;
                if (TextUtils.isEmpty(uid))
                    presenter.list(page);
                else presenter.listByUid(page, uid);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

            }
        });
        //更改封面
        wkVBinding.wrapview.setOnConverClick(this::chooseIMG);

        wkVBinding.cameraIv.setOnLongClickListener(view1 -> {
            if (!TextUtils.isEmpty(uid) && uid.equals(WKConfig.getInstance().getUid()))
                return true;
            Intent intent = new Intent(MomentsActivity.this, PublishMomentsActivity.class);
            intent.putExtra("publishType", MomentsType.single_text);
            chooseResultLac.launch(intent);
            return true;
        });
        adapter.setReplyClick(((momentNo, reply, locationY) -> {
            recyclerView.smoothScrollBy(0, locationY + WKConstants.getKeyboardHeight() + AndroidUtilities.dp(48) - AndroidUtilities.getScreenHeight());
            replyMomentNo = momentNo;
            replyMomentsReply = reply;
            showCommentDialog(String.format(getString(R.string.str_moments_reply_user), reply.name));
//            wkVBinding.emojiPanelView.showEmojiPanel(String.format(getString(R.string.str_moments_reply_user), reply.name));
        }));
        adapter.addChildClickViewIds(R.id.contentStatusTv, R.id.moreIv, R.id.deleteTv, R.id.avatarIv);
        adapter.setOnItemChildClickListener((adapter1, view1, position) -> {
            Moments moments = (Moments) adapter1.getItem(position);
            if (moments != null) {
                if (view1.getId() == R.id.contentStatusTv) {
                    moments.isExpand = !moments.isExpand;
                    adapter1.notifyItemChanged(position + adapter1.getHeaderLayoutCount());
                } else if (view1.getId() == R.id.avatarIv) {
                    WKMomentsApplication.getInstance().gotoUserDetail(MomentsActivity.this, moments.publisher);
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
                            recyclerView.smoothScrollBy(0, location[1] + WKConstants.getKeyboardHeight() + AndroidUtilities.dp(88) - AndroidUtilities.getScreenHeight());
                            replyMomentNo = moments.moment_no;
                            replyMomentsReply = null;
                            showCommentDialog(getString(R.string.moments_reply));
                            //  wkVBinding.emojiPanelView.showEmojiPanel(getString(R.string.moments_reply));
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
                        int xOffset = location[0] - AndroidUtilities.dp(180f + 10);
                        int yOffset = location[1] + (view1.getHeight() - AndroidUtilities.dp(34)) / 2;
                        feedActionPopup.showAtLocation(view1, Gravity.NO_GRAVITY, xOffset, yOffset);
                    }

                } else if (view1.getId() == R.id.deleteTv) {
                    WKDialogUtils.getInstance().showDialog(this, getString(R.string.base_delete), getString(R.string.delete_moments_tips), true, "", getString(R.string.base_delete), 0, ContextCompat.getColor(this, R.color.red), index -> {
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
        wkVBinding.cameraIv.setOnClickListener(v -> {

            if (TextUtils.isEmpty(uid)) {
                String desc = String.format(getString(R.string.file_permissions_des), getString(R.string.app_name));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    WKPermissions.getInstance().checkPermissions(new WKPermissions.IPermissionResult() {
                        @Override
                        public void onResult(boolean result) {
                            if (result) {
                                choose();
                            }
                        }

                        @Override
                        public void clickResult(boolean isCancel) {
                        }
                    }, this, desc, Manifest.permission.CAMERA);

                } else {
                    WKPermissions.getInstance().checkPermissions(new WKPermissions.IPermissionResult() {
                        @Override
                        public void onResult(boolean result) {
                            if (result) {
                                choose();
                            }
                        }

                        @Override
                        public void clickResult(boolean isCancel) {
                        }
                    }, this, desc, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            } else {
                if (TextUtils.equals(uid, WKConfig.getInstance().getUid())) {
                    startActivity(new Intent(this, MomentsMsgListActivity.class));
                }
            }

        });
        wkVBinding.backIv.setOnClickListener(v -> finish());
        //监听刷新通讯录
        EndpointManager.getInstance().setMethod("moment_activity", EndpointCategory.wkRefreshMailList, object -> {
            getMomentMsg();
            return null;
        });
    }

    private void choose() {
        Object isRegisterVideo = EndpointManager.getInstance().invoke("is_register_video", null);
        if (isRegisterVideo instanceof Boolean) {
            boolean isRegister = (boolean) isRegisterVideo;
            if (isRegister) {
                List<BottomSheetItem> list = new ArrayList<>();
                String content = String.format("%s/%s", getString(R.string.photograph), getString(R.string.video_img));
                list.add(new BottomSheetItem(content, R.mipmap.msg_camera, () -> publish(0)));
                list.add(new BottomSheetItem(getString(R.string.choose_by_album), R.mipmap.ic_gallery, () -> publish(1)));
                WKDialogUtils.getInstance().showBottomSheet(this, "", false, list);
            }
            return;
        }
        publish(1);
    }

    private void publish(int position) {
        int publishType;
        if (position == 0) {
            publishType = MomentsType.video_text;
        } else {
            publishType = MomentsType.image_text;
        }
        Intent intent = new Intent(MomentsActivity.this, PublishMomentsActivity.class);
        intent.putExtra("publishType", publishType);
        chooseResultLac.launch(intent);
    }

    @Override
    public void setList(List<Moments> list) {
        wkVBinding.wrapview.stopRefresh();
        wkVBinding.refreshLayout.finishLoadMore();
        if (page == 1) {
            if (WKReader.isEmpty(list)) {
                list = new ArrayList<>();
                list.add(new Moments());
            }
            adapter.setList(list);
        } else {
            if (WKReader.isEmpty(list)) {
                wkVBinding.refreshLayout.setEnableLoadMore(false);
//                refreshLayout.finishLoadMoreWithNoMoreData();
            } else {
                adapter.addData(list);
            }
        }
    }

    @Override
    public void setMomentSetting(MomentSetting momentSetting) {

    }

    @Override
    public void showError(String msg) {

    }

    @Override
    public void hideLoading() {

    }

    private void getMomentMsg() {
        //不是查看自己的朋友圈不显示动态消息
        if (!TextUtils.isEmpty(uid) && !TextUtils.equals(uid, WKConfig.getInstance().getUid()))
            return;
        int num = WKSharedPreferencesUtil.getInstance().getInt(WKConfig.getInstance().getUid() + "_moments_msg_count");
        String uid = WKSharedPreferencesUtil.getInstance().getSP(WKConfig.getInstance().getUid() + "_moments_msg_action_uid");
        if (num > 0 && !TextUtils.isEmpty(uid)) {
            WKChannel channel = WKIM.getInstance().getChannelManager().getChannel(uid, WKChannelType.PERSONAL);
            String key = "";
            if (channel != null) {
                key = channel.avatarCacheKey;
            }
            GlideUtils.getInstance().showAvatarImg(this, uid, WKChannelType.PERSONAL, key, newMomentsAvatarIv.imageView);
            newMomentsCountTv.setText(String.format(getString(R.string.str_moments_new_msg_count), num));
            newMomentsLayout.setVisibility(View.VISIBLE);
        } else newMomentsLayout.setVisibility(View.GONE);
    }

    private void getUserMomentBg() {
        String tempUid;
        if (!TextUtils.isEmpty(uid)) tempUid = uid;
        else tempUid = WKConfig.getInstance().getUid();
        String showUrl = WKSharedPreferencesUtil.getInstance().getSP(String.format("moment_bg_url_%s", tempUid));
        if (!TextUtils.isEmpty(showUrl)) {
            Bitmap bitmap = BitmapFactory.decodeFile(showUrl);
            wkVBinding.wrapview.getMomentBgIv().setImageBitmap(bitmap);
            //避免重复下载
            if (tempUid.equals(WKConfig.getInstance().getUid())) return;
        }
        String finalTempUid = tempUid;
        String url = String.format("%s%s%s", WKApiConfig.baseUrl, "moment/cover?uid=", tempUid);
        String filePath = WKFileUtils.getInstance().getNormalFileSavePath("moment") + "/" + WKConfig.getInstance().getUid() + "_" + WKTimeUtils.getInstance().getCurrentMills() + ".wk_png";
        WKDownloader.Companion.getInstance().download(url, filePath, new WKProgressManager.IProgress() {
            @Override
            public void onProgress(@Nullable Object tag, int progress) {

            }

            @Override
            public void onSuccess(@Nullable Object tag, @Nullable String path) {
                wkVBinding.wrapview.hideOrShowUpdateBgTv(false);
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                wkVBinding.wrapview.getMomentBgIv().setImageBitmap(bitmap);
                WKSharedPreferencesUtil.getInstance().putSP(String.format("moment_bg_url_%s", finalTempUid), filePath);

            }

            @Override
            public void onFail(@Nullable Object tag, @Nullable String msg) {
                if (finalTempUid.equals(WKConfig.getInstance().getUid()))
                    wkVBinding.wrapview.hideOrShowUpdateBgTv(true);
                WKSharedPreferencesUtil.getInstance().putSP(String.format("moment_bg_url_%s", finalTempUid), "");
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        WKBaseApplication.getInstance().disconnect = true;
    }

    private void chooseIMG() {
        WKBaseApplication.getInstance().disconnect = false;
        String desc = String.format(getString(R.string.album_permissions_desc), getString(R.string.app_name));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            WKPermissions.getInstance().checkPermissions(new WKPermissions.IPermissionResult() {
                                                             @Override
                                                             public void onResult(boolean result) {
                                                                 if (result) {
                                                                     success();
                                                                 }
                                                             }

                                                             @Override
                                                             public void clickResult(boolean isCancel) {
                                                             }
                                                         }, this, desc, Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO);
        } else {
            WKPermissions.getInstance().checkPermissions(new WKPermissions.IPermissionResult() {
                @Override
                public void onResult(boolean result) {
                    if (result) {
                        success();
                    }
                }

                @Override
                public void clickResult(boolean isCancel) {
                }
            }, this, desc, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        }


    }

    private void success() {

        GlideUtils.getInstance().chooseIMG(MomentsActivity.this, 1, true, ChooseMimeType.img, false, new GlideUtils.ISelectBack() {
            @Override
            public void onBack(List<ChooseResult> paths) {
                if (WKReader.isNotEmpty(paths)) {
                    MomentFileUpload.getInstance().getMomentCoverUploadUrl((url, path) -> {
                        if (!TextUtils.isEmpty(url)) {
                            WKUploader.getInstance().upload(url, paths.get(0).path, new WKUploader.IUploadBack() {
                                @Override
                                public void onSuccess(String url) {
                                    String filePath = WKFileUtils.getInstance().getNormalFileSavePath("moment") + "/" + WKConfig.getInstance().getUid() + "_" + WKTimeUtils.getInstance().getCurrentMills() + ".wk_png";
                                    WKFileUtils.getInstance().fileCopy(paths.get(0).path, filePath);
                                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                                    wkVBinding.wrapview.getMomentBgIv().setImageBitmap(bitmap);

                                    WKSharedPreferencesUtil.getInstance().putSP(String.format("moment_bg_url_%s", WKConfig.getInstance().getUid()), filePath);
                                    getUserMomentBg();
                                    wkVBinding.wrapview.hideOrShowUpdateBgTv(false);
                                }

                                @Override
                                public void onError() {
                                    showToast(R.string.moment_upload_bg_err);
                                }
                            });

                        }
                    });

                }
            }

            @Override
            public void onCancel() {

            }
        });

    }

    ActivityResultLauncher<Intent> chooseResultLac = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            page = 1;
            presenter.list(page);
            recyclerView.scrollToPosition(0);
        }
    });

    @Override
    public void finish() {
        super.finish();
        EndpointManager.getInstance().remove("moment_activity");
    }

    private void showCommentDialog(String hintText) {
        new FeedCommentDialog(MomentsActivity.this, hintText, (visible, currentTop) -> {

        }, new FeedCommentDialog.IEmojiClick() {
            @Override
            public void onEmojiClick(String emojiName) {

            }

            @Override
            public void onSendClick(String content) {
                MomentsModel.getInstance().comments(replyMomentNo, content, replyMomentsReply == null ? "" : replyMomentsReply.sid, replyMomentsReply == null ? "" : replyMomentsReply.uid, replyMomentsReply == null ? "" : replyMomentsReply.name, (code, msg, commentID) -> {
                    if (code == HttpResponseCode.success) {

                        for (int i = 0, size = adapter.getData().size(); i < size; i++) {
                            if (adapter.getData().get(i).moment_no.equals(replyMomentNo)) {
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
