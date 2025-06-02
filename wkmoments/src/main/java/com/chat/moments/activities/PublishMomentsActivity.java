package com.chat.moments.activities;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.chad.library.adapter.base.listener.OnItemDragListener;
import com.chat.base.base.WKBaseActivity;
import com.chat.base.config.WKConstants;
import com.chat.base.config.WKSharedPreferencesUtil;
import com.chat.base.emoji.EmojiAdapter;
import com.chat.base.emoji.EmojiEntry;
import com.chat.base.emoji.EmojiManager;
import com.chat.base.emoji.MoonUtil;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.ChooseContactsMenu;
import com.chat.base.endpoint.entity.ChooseLabelEntity;
import com.chat.base.endpoint.entity.ChooseLocationMenu;
import com.chat.base.endpoint.entity.VideoReadingMenu;
import com.chat.base.glide.ChooseMimeType;
import com.chat.base.glide.ChooseResult;
import com.chat.base.glide.ChooseResultModel;
import com.chat.base.glide.GlideUtils;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.net.ud.WKProgressManager;
import com.chat.base.net.ud.WKUploader;
import com.chat.base.ui.Theme;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.SoftKeyboardUtils;
import com.chat.base.utils.StringUtils;
import com.chat.base.utils.WKFileUtils;
import com.chat.base.utils.WKMediaFileUtils;
import com.chat.base.utils.WKPermissions;
import com.chat.base.utils.WKReader;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.base.views.FullyGridLayoutManager;
import com.chat.moments.R;
import com.chat.moments.adapter.GridImgAdapter;
import com.chat.moments.adapter.SelectUserAdapter;
import com.chat.moments.databinding.ActPublishMomentsLayoutBinding;
import com.chat.moments.entity.ImgEntity;
import com.chat.moments.entity.MomentsFileUploadStatus;
import com.chat.moments.entity.MomentsRange;
import com.chat.moments.entity.MomentsType;
import com.chat.moments.service.MomentFileUpload;
import com.chat.moments.service.MomentsModel;
import com.chat.moments.views.PreviewImgView;
import com.effective.android.panel.PanelSwitchHelper;
import com.effective.android.panel.interfaces.ContentScrollMeasurer;
import com.effective.android.panel.interfaces.listener.OnKeyboardStateListener;
import com.effective.android.panel.interfaces.listener.OnPanelChangeListener;
import com.effective.android.panel.view.panel.IPanelView;
import com.effective.android.panel.view.panel.PanelView;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.util.SmartGlideImageLoader;
import com.xinbida.wukongim.entity.WKChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 2020-11-06 13:36
 * 发布动态
 */
public class PublishMomentsActivity extends WKBaseActivity<ActPublishMomentsLayoutBinding> {

    //发布类型 1：纯文本 2：图文 3：视频
    public int publishType;
    private Button titleRightBtn;
    private GridImgAdapter adapter;
    private List<WKChannel> remindUsers;
    LocationEntity entity;
    private SelectUserAdapter selectUserAdapter;
    private int visibleRangeType = 0;
    private List<WKChannel> rangeUser;
    private List<ChooseLabelEntity> labels;
    public boolean isVideoUploaded = false;//判断视频是否上传成功
    private PanelSwitchHelper mHelper;
    private EmojiAdapter emojiAdapter;

    @Override
    protected ActPublishMomentsLayoutBinding getViewBinding() {
        return ActPublishMomentsLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        if (publishType == MomentsType.single_text)
            titleTv.setText(R.string.publish_signle_text);
        else titleTv.setText("");
    }

    @Override
    protected void initPresenter() {
        publishType = getIntent().getIntExtra("publishType", MomentsType.single_text);
        if (publishType == MomentsType.video_text) {
            EndpointManager.getInstance().invoke("video_recording", (VideoReadingMenu.IRedingResult) (second, path, videoPath, size) -> {
                ImgEntity imgEntity;
                List<ImgEntity> list = new ArrayList<>();
                if (!TextUtils.isEmpty(videoPath)) {
                    //拍摄的图片
                    imgEntity = new ImgEntity(videoPath, 2);
                    imgEntity.coverPath = path;
                    list.add(imgEntity);
                } else {
                    imgEntity = new ImgEntity(path, 1);
                    list.add(imgEntity);
                    list.add(new ImgEntity());
                }
                adapter.setList(list);
                uploadMomentsFile();
            });
        } else if (publishType == MomentsType.image_text) {
            chooseIMG(9, ChooseMimeType.all);
//            GlideUtils.getInstance().chooseImgs(this, false, 9, MimeType.ofAll());
        }
    }

    @Override
    protected String getRightBtnText(Button titleRightBtn) {
        this.titleRightBtn = titleRightBtn;
        titleRightBtn.setEnabled(false);
        titleRightBtn.setAlpha(0.2f);
        return getString(R.string.str_send);
    }

    @Override
    protected void initView() {
//        wkVBinding.emojiPanelView.initEmojiPanel(this);
//        wkVBinding.emojiPanelView.setOutsideEditText(wkVBinding.contentEt);
        FullyGridLayoutManager manager = new FullyGridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        wkVBinding.imageRecycleView.setLayoutManager(manager);
        adapter = new GridImgAdapter(new ArrayList<>());
        wkVBinding.imageRecycleView.setAdapter(adapter);

        selectUserAdapter = new SelectUserAdapter();
        wkVBinding.userRecyclerView.setLayoutManager(new FullyGridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false, true));
        wkVBinding.userRecyclerView.setAdapter(selectUserAdapter);
    }

    @Override
    protected void rightButtonClick() {
        super.rightButtonClick();
        boolean uploadFail = false;
        boolean uploadWaiting = false;
        List<ImgEntity> publishFiles = new ArrayList<>();
        for (int i = 0, size = adapter.getData().size(); i < size; i++) {
            if (adapter.getData().get(i).fileType != 0 && adapter.getData().get(i).uploadStatus == MomentsFileUploadStatus.success) {
                publishFiles.add(adapter.getData().get(i));
            }
            if (adapter.getData().get(i).fileType != 0 && adapter.getData().get(i).uploadStatus == MomentsFileUploadStatus.fail) {
                uploadFail = true;
            }
            if (adapter.getData().get(i).fileType != 0 && (adapter.getData().get(i).uploadStatus == MomentsFileUploadStatus.waiting || adapter.getData().get(i).uploadStatus == MomentsFileUploadStatus.uploading)) {
                uploadWaiting = true;
            }
        }
        if (uploadWaiting) {
            showToast(R.string.moment_file_uploading);
            return;
        }

        if (uploadFail) {
            showToast(R.string.moment_file_upload_fail);
            return;
        }
        String content = Objects.requireNonNull(wkVBinding.contentEt.getText()).toString();
        if (TextUtils.isEmpty(content.replaceAll(" ", "")) && WKReader.isEmpty(publishFiles)) {
            showToast(R.string.moments_no_content_tips);
            return;
        }
        String vidoeCoverUrl = "", videoUrl = "";
        List<String> imgs = new ArrayList<>();
        if (publishFiles.size() == 1 && publishFiles.get(0).fileType == 2) {
            vidoeCoverUrl = publishFiles.get(0).coverUrl;
            videoUrl = publishFiles.get(0).url;
        } else {
            for (int i = 0; i < publishFiles.size(); i++) {
                imgs.add(publishFiles.get(i).url);
            }
        }
        loadingPopup.show();
        loadingPopup.setTitle(getString(R.string.moments_publishing));
        List<String> uidList = new ArrayList<>();
        if (WKReader.isNotEmpty(rangeUser)) {
            for (int i = 0, size = rangeUser.size(); i < size; i++) {
                uidList.add(rangeUser.get(i).channelID);
            }
        }
        if (WKReader.isNotEmpty(labels)) {
            for (int i = 0, size = labels.size(); i < size; i++) {
                for (WKChannel channel : labels.get(i).members) {
                    uidList.add(channel.channelID);
                }
            }
        }
        List<String> remindUids = new ArrayList<>();
        if (WKReader.isNotEmpty(remindUsers)) {
            for (WKChannel channel : remindUsers) {
                remindUids.add(channel.channelID);
            }
        }
        MomentsModel.getInstance().publish(visibleRangeType, entity == null ? "" : entity.title, entity == null ? "" : String.valueOf(entity.longitude), entity == null ? "" : String.valueOf(entity.latitude), remindUids, uidList, videoUrl, vidoeCoverUrl, imgs, content, (code, msg) -> {
            if (code == HttpResponseCode.success) {
                setResult(RESULT_OK);
                loadingPopup.dismiss();
                SoftKeyboardUtils.getInstance().hideInput(this, wkVBinding.contentEt);
                finish();
            } else showToast(msg);
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initListener() {
        wkVBinding.contentEt.setOnTouchListener((view1, motionEvent) -> {
//            if (!wkVBinding.emojiPanelView.isShowing()) {
//                wkVBinding.emojiPanelView.showEmojiPanel();
            wkVBinding.bottomView.setVisibility(View.VISIBLE);
//            }
//            wkVBinding.emojiPanelView.showNormalImgSwitch();
            return super.onTouchEvent(motionEvent);
        });
        OnItemDragListener listener = new OnItemDragListener() {
            @Override
            public void onItemDragStart(RecyclerView.ViewHolder viewHolder, int pos) {
                if (adapter.getData().size() < 9 && adapter.getItem(adapter.getItemCount() - 1).fileType == 0 && adapter.getItemCount() != 1) {
                    adapter.removeAt(adapter.getData().size() - 1);
                }
//                final BaseViewHolder holder = ((BaseViewHolder) viewHolder);
                // 开始时，item背景色变化，demo这里使用了一个动画渐变，使得自然
                int startColor = ContextCompat.getColor(PublishMomentsActivity.this, R.color.white);
                int endColor = ContextCompat.getColor(PublishMomentsActivity.this, R.color.colorF5F5F5);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ValueAnimator v = ValueAnimator.ofArgb(startColor, endColor);
                    v.addUpdateListener(animation -> viewHolder.itemView.setBackgroundColor((int) animation.getAnimatedValue()));
                    v.setDuration(300);
                    v.start();
                }
            }

            @Override
            public void onItemDragMoving(RecyclerView.ViewHolder source, int from, RecyclerView.ViewHolder target, int to) {
            }

            @Override
            public void onItemDragEnd(RecyclerView.ViewHolder viewHolder, int pos) {
//                final BaseViewHolder holder = ((BaseViewHolder) viewHolder);
                // 结束时，item背景色变化，demo这里使用了一个动画渐变，使得自然
                int startColor = ContextCompat.getColor(PublishMomentsActivity.this, R.color.colorF5F5F5);
                int endColor = ContextCompat.getColor(PublishMomentsActivity.this, R.color.white);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ValueAnimator v = ValueAnimator.ofArgb(startColor, endColor);
                    v.addUpdateListener(animation -> viewHolder.itemView.setBackgroundColor((int) animation.getAnimatedValue()));
                    v.setDuration(300);
                    v.start();
                }
                if (WKReader.isNotEmpty(adapter.getData()) && adapter.getData().get(0).fileType == 2) {
                    return;
                }
                if (wkVBinding.imageRecycleView.isComputingLayout()) {
                    wkVBinding.imageRecycleView.post(() -> {
                        if ((adapter.getData().size() < 9 && publishType == MomentsType.image_text) || (WKReader.isEmpty(adapter.getData()) && publishType == MomentsType.video_text)) {
                            adapter.addData(new ImgEntity());
                        }
                    });
                } else {
                    if ((adapter.getData().size() < 9 && publishType == MomentsType.image_text) || (WKReader.isEmpty(adapter.getData()) && publishType == MomentsType.video_text)) {
                        adapter.addData(new ImgEntity());
                    }
                }

            }
        };
        adapter.getDraggableModule().setDragEnabled(true);
        adapter.getDraggableModule().setOnItemDragListener(listener);
        adapter.addChildClickViewIds(R.id.imageView);
        adapter.setOnItemClickListener((adapter1, view1, position) -> {
            ImgEntity entity = (ImgEntity) adapter1.getItem(position);
            if (entity != null) {
                if (entity.fileType == 1) {
                    List<ImageView> imgList = new ArrayList<>();
                    ArrayList<Object> list = new ArrayList<>();

                    for (int i = 0, size = adapter.getData().size(); i < size; i++) {
                        if (adapter.getData().get(i).fileType == 1) {
                            list.add(adapter.getData().get(i).path);
                            ImageView imageView1 = (ImageView) adapter1.getViewByPosition(i, R.id.imageView);
                            imgList.add(imageView1);
                        }
                    }
                    PreviewImgView previewImgView = new PreviewImgView(PublishMomentsActivity.this, position, list, path -> {
                        for (int i = 0, size = adapter.getData().size(); i < size; i++) {
                            if (!TextUtils.isEmpty(adapter.getData().get(i).path) && adapter.getData().get(i).path.equals(path)) {
                                adapter.removeAt(i);
                                checkBtnStatus();
                                break;
                            }
                        }
                    });
                    previewImgView.setXPopupImageLoader(new SmartGlideImageLoader());
                    previewImgView.setSrcView(wkVBinding.imageRecycleView.getChildAt(position).findViewById(R.id.imageView), position);
                    previewImgView.setImageUrls(list);
                    previewImgView.isShowIndicator(false);
                    previewImgView.isShowPlaceholder(false);
                    previewImgView.isShowSaveButton(false);
                    previewImgView.setSrcViewUpdateListener((popupView, position1) -> popupView.updateSrcView(imgList.get(position1)));

                    new XPopup.Builder(PublishMomentsActivity.this)
                            .asCustom(previewImgView)
                            .show();
                } else if (entity.fileType == 2) {
                    @SuppressWarnings("unchecked") ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(PublishMomentsActivity.this, new Pair<>(view1, "coverIv"));
                    Intent intent = new Intent(PublishMomentsActivity.this, PreviewVideoActivity.class);
                    intent.putExtra("path", adapter.getData().get(0).path);
                    previewVideoResult.launch(intent, activityOptions);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } else {
                    boolean isChooseAll = true;
                    if (adapter1.getItemCount() > 1) {
                        isChooseAll = false;
                    }
                    chooseIMG(9 - adapter.getItemCount() + 1, isChooseAll ? ChooseMimeType.all : ChooseMimeType.img);
                    //  GlideUtils.getInstance().chooseImgs(this, false, 9 - adapter.getItemCount() + 1, isChooseAll ? MimeType.ofAll() : MimeType.ofImage());
                }
            }

        });
        selectUserAdapter.setOnItemClickListener((adapter1, view1, position) -> chooseRemindUser());

        wkVBinding.contentEt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int editEnd = wkVBinding.contentEt.getSelectionEnd();
                wkVBinding.contentEt.removeTextChangedListener(this);
                while (StringUtils.counterChars(s.toString()) > 1000 && editEnd > 0) {
                    s.delete(editEnd - 1, editEnd);
                    editEnd--;
                }
                wkVBinding.contentEt.setSelection(editEnd);
                wkVBinding.contentEt.addTextChangedListener(this);
                checkBtnStatus();
                int count = wkVBinding.contentEt.length();
                if (count > 500) count = 500;
                wkVBinding.countTv.setText(String.format("%s/500", count));
            }
        });
        SingleClickUtil.onSingleClick(wkVBinding.chooseUserLayout, v -> {
            Intent intent = new Intent(this, MomentsVisibleRangeActivity.class);
            intent.putParcelableArrayListExtra("list", (ArrayList<? extends Parcelable>) rangeUser);
            intent.putParcelableArrayListExtra("labels", (ArrayList<? extends Parcelable>) labels);
            intent.putExtra("visibleRangeType", visibleRangeType);
            visibleRangeResult.launch(intent);
        });
        wkVBinding.chooseLocationLayout.setOnClickListener(v -> {
            String desc = String.format(getString(R.string.location_permissions_desc), getString(R.string.app_name));
            WKPermissions.getInstance().checkPermissions(new WKPermissions.IPermissionResult() {
                @Override
                public void onResult(boolean result) {
                    if (result) {
                        EndpointManager.getInstance().invoke("choose_location", new ChooseLocationMenu(PublishMomentsActivity.this, (address, title, latitude, longitude) -> {
                            entity = new LocationEntity(address, title, latitude, longitude);
                            if (entity.latitude == 0) {
                                wkVBinding.addressIv.setImageResource(R.mipmap.icon_moments_location);
                                wkVBinding.addressTv.setText(R.string.choose_location);
                                wkVBinding.addressTv.setTextColor(ContextCompat.getColor(PublishMomentsActivity.this, R.color.colorDark));
                            } else {
                                wkVBinding.addressIv.setImageResource(R.mipmap.icon_moments_location_selected);
                                wkVBinding.addressTv.setText(entity.title);
                                wkVBinding.addressTv.setTextColor(Theme.colorAccount);
                            }
                        }));
//                        Intent intent = new Intent(PublishMomentsActivity.this, ChooseLocationActivity.class);
//                        intent.putExtra("entity", entity);
//                        chooseLocationResult.launch(intent);
                    }
                }

                @Override
                public void clickResult(boolean isCancel) {

                }
            }, this, desc, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);


        });
        wkVBinding.chooseRemindLayout.setOnClickListener(v -> chooseRemindUser());
        wkVBinding.bottomView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mHelper.resetState();
                wkVBinding.bottomView.setVisibility(View.GONE);
                wkVBinding.bottomAction.setVisibility(View.GONE);
                return false;
            }
        });
//        wkVBinding.bottomView.setOnClickListener(v -> {
////            wkVBinding.emojiPanelView.dismiss();
//            mHelper.resetState();
//            wkVBinding.bottomView.setVisibility(View.GONE);
//            wkVBinding.bottomAction.setVisibility(View.GONE);
//        });
    }

    @Override
    protected void backListener(int type) {
        SoftKeyboardUtils.getInstance().hideInput(this, wkVBinding.contentEt);
        new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(this::finish, 200);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == GlideUtils.getInstance().REQUEST_CODE_CHOOSE && resultCode == 0) {
//            if (adapter.getData().size() == 0) {
//                adapter.addData(new ImgEntity());
//            }
//            return;
//        }
//        if (requestCode == GlideUtils.getInstance().REQUEST_CODE_CHOOSE && resultCode == RESULT_OK && data != null) {
//            //选择图片
//            List<String> pathList = Matisse.obtainPathResult(data);
//            List<ImgEntity> list = new ArrayList<>();
//            isVideoUploaded = true;
//            if (pathList.size() == 1) {
//                //视频文件
//                if (WKMediaFileUtils.getInstance().isVideoFileType(pathList.get(0))) {
//                    if (WKFileUtils.getInstance().isFileOverSize(this, pathList.get(0))) {
//                        if (adapter.getData().size() == 0) {
//                            adapter.addData(new ImgEntity());
//                        }
//                        return;
//                    }
//                    isVideoUploaded = false;
//                    ImgEntity ImgEntity = new ImgEntity(pathList.get(0), 2);
//                    ImgEntity.coverPath = WKMediaFileUtils.getInstance().getVideoCover(pathList.get(0));
//                    list.add(ImgEntity);
//                    adapter.setList(list);
//                    checkBtnStatus();
//                    uploadMomentsFile();
//                    return;
//                }
//            }
//            for (int i = 0, size = pathList.size(); i < size; i++) {
//                list.add(new ImgEntity(pathList.get(i), 1));
//            }
//            List<ImgEntity> oldList = new ArrayList<>();
//            for (int i = 0, size = adapter.getData().size(); i < size; i++) {
//                if (adapter.getData().get(i).fileType == 1) {
//                    oldList.add(adapter.getData().get(i));
//                }
//            }
//            list.addAll(0, oldList);
//            if (list.size() < 9) {
//                list.add(new ImgEntity());
//            }
//            adapter.setList(list);
//            checkBtnStatus();
//            uploadMomentsFile();
//        } else if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
//            entity = data.getParcelableExtra("entity");
//            if (entity != null) {
//                if (entity.latitude == 0) {
//                    wkVBinding.addressIv.setImageResource(R.mipmap.icon_moments_location);
//                    wkVBinding.addressTv.setText(R.string.choose_location);
//                    wkVBinding.addressTv.setTextColor(ContextCompat.getColor(this, R.color.colorDark));
//                } else {
//                    wkVBinding.addressIv.setImageResource(R.mipmap.icon_moments_location_selected);
//                    wkVBinding.addressTv.setText(entity.title);
//                    wkVBinding.addressTv.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
//                }
//            }
//        } else if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
//            List<String> imgList = data.getStringArrayListExtra("imgList");
//            List<ImgEntity> list = new ArrayList<>();
//            if (imgList == null) {
//                imgList = new ArrayList<>();
//            }
//
//            for (int i = 0, size = imgList.size(); i < size; i++) {
//                list.add(new ImgEntity(imgList.get(i), 1));
//            }
//            if (list.size() < 9) {
//                list.add(new ImgEntity());
//            }
//            adapter.setList(list);
//            checkBtnStatus();
//            uploadMomentsFile();
//        }
//    }

    private void checkBtnStatus() {
        boolean isEnabled = false;
        String content = Objects.requireNonNull(wkVBinding.contentEt.getText()).toString();
        if (adapter.getItemCount() > 0) {
//            if (adapter.getItem(adapter.getItemCount()-1).fileType == 2){
//                isEnabled = true;
//            }else {
//                if (adapter.getItem(adapter.getItemCount() - 1).fileType == 0 && adapter.getItemCount() > 1){
//                    isEnabled = true;
//                    isVideoUploaded = true;
//                }
//            }
            if (adapter.getItem(adapter.getItemCount() - 1).fileType == 0 && adapter.getItemCount() > 1) {
                isEnabled = true;
                isVideoUploaded = true;
            } else if (adapter.getItem(adapter.getItemCount() - 1).fileType == 2) {
                isEnabled = true;
            }
        }
        if (!TextUtils.isEmpty(content)) {
            isEnabled = true;
        }
        if (publishType == 1) {
            titleRightBtn.setEnabled(isEnabled);
        } else
            isEnabled = (isEnabled && isVideoUploaded);
        titleRightBtn.setEnabled(isEnabled);
        if (isEnabled) {
            titleRightBtn.setEnabled(true);
            titleRightBtn.setAlpha(1);
        } else {
            titleRightBtn.setEnabled(false);
            titleRightBtn.setAlpha(0.2f);
        }
    }

    private void chooseRemindUser() {
        if (visibleRangeType == MomentsRange.PRIVATE) {
            showSingleBtnDialog(getString(R.string.can_not_choose_reminder));
            return;
        }
        EndpointManager.getInstance().invoke("choose_contacts", new ChooseContactsMenu(10, true, false, remindUsers, selectedList -> {
            remindUsers = selectedList;
            selectUserAdapter.setList(remindUsers);
            if (WKReader.isNotEmpty(remindUsers)) {
                wkVBinding.remindTv.setTextColor(Theme.colorAccount);
                wkVBinding.remindIv.setImageResource(R.mipmap.icon_moments_aite_selected);
            } else {
                wkVBinding.remindTv.setTextColor(ContextCompat.getColor(PublishMomentsActivity.this, R.color.colorDark));
                wkVBinding.remindIv.setImageResource(R.mipmap.icon_moments_aite);
            }
        }));
    }

    private void uploadMomentsFile() {
        for (int i = 0, size = adapter.getData().size(); i < size; i++) {
            if (adapter.getData().get(i).fileType != 0 && adapter.getData().get(i).uploadStatus == MomentsFileUploadStatus.waiting) {
                adapter.getData().get(i).uploadStatus = MomentsFileUploadStatus.uploading;

                adapter.notifyItemChanged(i);
                if (adapter.getData().get(i).fileType == 1) {
                    //图片
                    uploadFile(adapter.getData().get(i));
                } else {
                    //视频
                    ImgEntity entity = adapter.getData().get(i);
                    MomentFileUpload.getInstance().getMomentFileUploadUrl(adapter.getData().get(i).coverPath, (url, path) -> {
                        if (!TextUtils.isEmpty(url)) {
                            WKUploader.getInstance().upload(url, entity.coverPath, entity.key, new WKUploader.IUploadBack() {
                                @Override
                                public void onSuccess(String url) {
                                    entity.coverUrl = url;
                                    uploadFile(entity);
                                }

                                @Override
                                public void onError() {
                                    uploadMomentsFile();
                                    entity.uploadStatus = MomentsFileUploadStatus.fail;
                                    adapter.notifyItemChanged(0, adapter.getData().size());

                                }
                            });
                        }
                    });
                }
                break;
            }
        }
    }

    private void uploadFile(ImgEntity entity) {
        checkBtnStatus();
        MomentFileUpload.getInstance().getMomentFileUploadUrl(entity.path, (url, path) -> {
            if (!TextUtils.isEmpty(url)) {
                WKUploader.getInstance().upload(url, entity.path, entity.key, new WKUploader.IUploadBack() {
                    @Override
                    public void onSuccess(String url) {
                        isVideoUploaded = true;
                        if (adapter.getData().size() == 1) {
                            checkBtnStatus();
                        }
                        entity.url = url;
                        for (int i = 0, size = adapter.getData().size(); i < size; i++) {
                            if (!TextUtils.isEmpty(entity.key) && !TextUtils.isEmpty(adapter.getData().get(i).key) && adapter.getData().get(i).key.equals(entity.key)) {
                                adapter.getData().get(i).uploadStatus = MomentsFileUploadStatus.success;
                                adapter.getData().get(i).url = entity.url;
                                adapter.getData().get(i).progress = 100;
                                adapter.getData().get(i).coverUrl = entity.coverUrl;
                                adapter.notifyItemChanged(i);
                                break;
                            }
                        }
                        uploadMomentsFile();
                    }

                    @Override
                    public void onError() {
                        entity.uploadStatus = MomentsFileUploadStatus.fail;//标记上传失败
                    }
                });

            }
        });
    }


    ActivityResultLauncher<Intent> previewVideoResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            boolean isDelete = result.getData().getBooleanExtra("isDelete", false);
            if (isDelete) {
                WKProgressManager.Companion.getInstance().unregisterProgress(adapter.getData().get(0).key);
                isVideoUploaded = true;
                adapter.removeAt(0);
                adapter.addData(new ImgEntity());
            }
            checkBtnStatus();
        }
    });
    //    ActivityResultLauncher<Intent> chooseLocationResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//            entity = result.getData().getParcelableExtra("entity");
//            if (entity != null) {
//                if (entity.latitude == 0) {
//                    wkVBinding.addressIv.setImageResource(R.mipmap.icon_moments_location);
//                    wkVBinding.addressTv.setText(R.string.choose_location);
//                    wkVBinding.addressTv.setTextColor(ContextCompat.getColor(this, R.color.colorDark));
//                } else {
//                    wkVBinding.addressIv.setImageResource(R.mipmap.icon_moments_location_selected);
//                    wkVBinding.addressTv.setText(entity.title);
//                    wkVBinding.addressTv.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
//                }
//            }
//        }
//    });
    ActivityResultLauncher<Intent> visibleRangeResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getData() != null && result.getResultCode() == RESULT_OK) {
            rangeUser = result.getData().getParcelableArrayListExtra("list");
            labels = result.getData().getParcelableArrayListExtra("labels");
            visibleRangeType = result.getData().getIntExtra("visibleRangeType", 0);

            StringBuilder stringBuilder = new StringBuilder();
            if (visibleRangeType > MomentsRange.PRIVATE) {
                if (WKReader.isNotEmpty(labels)) {
                    for (ChooseLabelEntity entity : labels) {
                        if (TextUtils.isEmpty(stringBuilder)) {
                            stringBuilder.append(entity.labelName);
                        } else stringBuilder.append("、").append(entity.labelName);
                    }
                }
                if (WKReader.isNotEmpty(rangeUser)) {
                    for (int i = 0, size = rangeUser.size(); i < size; i++) {
                        if (TextUtils.isEmpty(stringBuilder)) {
                            stringBuilder.append(rangeUser.get(i).channelName);
                        } else stringBuilder.append("、").append(rangeUser.get(i).channelName);
                    }
                }
            }
            if (visibleRangeType == MomentsRange.PRIVATE) {
                remindUsers = new ArrayList<>();
                wkVBinding.remindTv.setTextColor(ContextCompat.getColor(PublishMomentsActivity.this, R.color.colorDark));
                wkVBinding.remindIv.setImageResource(R.mipmap.icon_moments_aite);
                selectUserAdapter.setList(remindUsers);
            }
            if (TextUtils.isEmpty(stringBuilder)) {
                if (visibleRangeType > MomentsRange.PRIVATE) {
                    visibleRangeType = MomentsRange.PUBLIC;
                }
            }
            if (visibleRangeType == MomentsRange.PUBLIC || visibleRangeType == MomentsRange.PRIVATE || visibleRangeType == MomentsRange.PARTIALLY) {
                wkVBinding.visibleRangeIv.setImageResource(R.mipmap.icon_moments_user_selected);
                wkVBinding.visibleRangeTv.setTextColor(Theme.colorAccount);
                wkVBinding.visibleRangeTitleTv.setTextColor(Theme.colorAccount);
                wkVBinding.visibleRangeTitleTv.setText(R.string.str_moments_shield);
            } else {
                wkVBinding.visibleRangeIv.setImageResource(R.mipmap.icon_moments_user_red);
                wkVBinding.visibleRangeTv.setTextColor(ContextCompat.getColor(this, R.color.red));
                wkVBinding.visibleRangeTitleTv.setTextColor(ContextCompat.getColor(this, R.color.red));
                wkVBinding.visibleRangeTitleTv.setText(R.string.moments_invisible_user);
            }
            if (visibleRangeType == MomentsRange.PUBLIC) {
                wkVBinding.visibleRangeTv.setText(R.string.moments_public);
            } else if (visibleRangeType == MomentsRange.PRIVATE) {
                wkVBinding.visibleRangeTv.setText(R.string.moments_private);
            } else {
                if (!TextUtils.isEmpty(stringBuilder)) {
                    wkVBinding.visibleRangeTv.setText(stringBuilder);
                } else {
                    wkVBinding.visibleRangeTv.setText("");
                }
            }

        }
    });

    private void chooseIMG(int maxCount, ChooseMimeType mimeType) {
        GlideUtils.getInstance().chooseIMG(this, maxCount, true, mimeType, false, new GlideUtils.ISelectBack() {
            @Override
            public void onBack(List<ChooseResult> paths) {
                if (WKReader.isEmpty(paths)) {
                    if (WKReader.isEmpty(adapter.getData())) {
                        adapter.addData(new ImgEntity());
                    }
                    return;
                }

                //选择图片
                List<ImgEntity> list = new ArrayList<>();
                isVideoUploaded = true;
                if (paths.size() == 1 && paths.get(0).model == ChooseResultModel.video) {
                    //视频文件
                    if (WKFileUtils.getInstance().isFileOverSize(PublishMomentsActivity.this, paths.get(0).path)) {
                        if (WKReader.isEmpty(adapter.getData())) {
                            adapter.addData(new ImgEntity());
                        }
                        return;
                    }
                    isVideoUploaded = false;
                    ImgEntity imgEntity = new ImgEntity(paths.get(0).path, 2);
                    imgEntity.coverPath = WKMediaFileUtils.getInstance().getVideoCover(paths.get(0).path);
                    list.add(imgEntity);
                    adapter.setList(list);
                    checkBtnStatus();
                    uploadMomentsFile();
                    return;
                }
                for (int i = 0, size = paths.size(); i < size; i++) {
                    list.add(new ImgEntity(paths.get(i).path, 1));
                }
                List<ImgEntity> oldList = new ArrayList<>();
                for (int i = 0, size = adapter.getData().size(); i < size; i++) {
                    if (adapter.getData().get(i).fileType == 1) {
                        oldList.add(adapter.getData().get(i));
                    }
                }
                list.addAll(0, oldList);
                if (list.size() < 9) {
                    list.add(new ImgEntity());
                }
                adapter.setList(list);
                checkBtnStatus();
                uploadMomentsFile();
            }

            @Override
            public void onCancel() {
                if (WKReader.isEmpty(adapter.getData())) {
                    adapter.addData(new ImgEntity());
                }
            }
        });
    }

    static class LocationEntity {
        LocationEntity(String address, String title, double latitude, double longitude) {
            this.address = address;
            this.title = title;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String address;
        public String title;
        public double latitude;
        public double longitude;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mHelper == null) {
            mHelper = new PanelSwitchHelper.Builder(this).addKeyboardStateListener(new OnKeyboardStateListener() {
                        @Override
                        public void onKeyboardChange(boolean b, int i) {
                            if (i == 0 && !mHelper.isPanelState()) {
                                wkVBinding.bottomAction.setVisibility(View.GONE);
                            }
                        }
                    }).addEditTextFocusChangeListener((view, b) -> {
                        if (b) {
                            wkVBinding.bottomAction.setVisibility(View.VISIBLE);
                        } else {
                            wkVBinding.bottomAction.setVisibility(View.GONE);
                        }
                    }).addContentScrollMeasurer(new ContentScrollMeasurer() {
                        @Override
                        public int getScrollDistance(int i) {
                            return 0;
                        }

                        @Override
                        public int getScrollViewId() {
                            return R.id.scrollView;
                        }
                    }).addPanelChangeListener(new OnPanelChangeListener() {


                        @Override
                        public void onPanelSizeChange(@Nullable IPanelView iPanelView, boolean portrait, int oldWidth, int oldHeight, int width, int height) {
                            if (iPanelView instanceof PanelView) {
                                if (((PanelView) iPanelView).getId() == R.id.panel_emotion) {
                                    WKConstants.setKeyboardHeight(height);
//                                    EmotionPagerView pagerView = rootView.findViewById(R.id.view_pager);
//                                    int viewPagerSize = height - AndroidUtilities.dp(30f);
//                                    pagerView.buildEmotionViews(
//                                            rootView.findViewById(R.id.pageIndicatorView),
//                                            rootView.findViewById(R.id.edit_text),
//                                            Emotions.getEmotions(), width, viewPagerSize);
                                    RelativeLayout frameLayout = findViewById(R.id.emojiLayout);
                                    frameLayout.getLayoutParams().height = height - AndroidUtilities.dp(30f);
                                    AppCompatImageView imageView = findViewById(R.id.deleteIv);
                                    View deleteLayout = findViewById(R.id.deleteLayout);
                                    RecyclerView recyclerView = findViewById(R.id.recyclerView);
                                    initEmoji(imageView, deleteLayout, recyclerView);
//                                    activity.getSupportFragmentManager()
//                                            .beginTransaction()
//                                            .add(R.id.emojiLayout1, emojiFragment)
//                                            .commit();

                                }
                            }
                        }

                        @Override
                        public void onPanel(@Nullable IPanelView iPanelView) {
//                            wkVBinding.scrollView.setPadding(0, 0, 0, PanelUtil.getKeyBoardHeight(PublishMomentsActivity.this));
//                            wkVBinding.scrollView.invalidate();
                            assert iPanelView != null;
                            wkVBinding.emotionBtn.setSelected(((PanelView) iPanelView).getId() == R.id.panel_emotion);
                        }

                        @Override
                        public void onNone() {
                            wkVBinding.emotionBtn.setSelected(false);
//                            wkVBinding.scrollView.setPadding(0, 0, 0, 0);
//                            wkVBinding.scrollView.invalidate();
                        }

                        @Override
                        public void onKeyboard() {
                            wkVBinding.emotionBtn.setSelected(false);
//                            wkVBinding.scrollView.setPadding(0, 0, 0, PanelUtil.getKeyBoardHeight(PublishMomentsActivity.this));
//                            wkVBinding.scrollView.invalidate();
                        }
                    })
                    .logTrack(true) //output log
                    .build();
        }
    }

    private void initEmoji(AppCompatImageView deleteIv, View deleteLayout, RecyclerView recyclerView) {
        int width = AndroidUtilities.getScreenWidth() - (AndroidUtilities.dp(30) * 8);
        Theme.setColorFilter(this, deleteIv, R.color.popupTextColor);
        List<EmojiEntry> emojiIndexs = new ArrayList<>();
        List<EmojiEntry> normalList = EmojiManager.getInstance().getEmojiWithType("0_");
        List<EmojiEntry> naturelList = EmojiManager.getInstance().getEmojiWithType("1_");
        List<EmojiEntry> symbolsList = EmojiManager.getInstance().getEmojiWithType("2_");
        emojiIndexs.addAll(normalList);
        emojiIndexs.addAll(naturelList);
        emojiIndexs.addAll(symbolsList);
        emojiAdapter = new EmojiAdapter(new ArrayList<>(), width);
        emojiAdapter.setList(emojiIndexs);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(8, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(emojiAdapter);
        emojiAdapter.addFooterView(getFooterView());
        getCommonEmoji(width);
        emojiAdapter.setOnItemClickListener((adapter, view, position) -> {
            EmojiEntry entry = emojiAdapter.getItem(position);
            emojiClick(entry.getText());
        });
        deleteLayout.setOnClickListener(v -> emojiClick(""));
    }

    private void getCommonEmoji(int width) {
        //查看最近使用到表情
        String ids = WKSharedPreferencesUtil.getInstance().getSPWithUID("common_used_emojis");
        List<EmojiEntry> list = new ArrayList<>();
        String tempIds = "";
        if (!TextUtils.isEmpty(ids)) {
            if (ids.contains(",")) {
                String[] emojiIds = ids.split(",");
                for (String emojiId : emojiIds) {
                    if (list.size() == 32) break;
                    if (!TextUtils.isEmpty(emojiId)) {
                        EmojiEntry entry = EmojiManager.getInstance().getEmojiEntry(emojiId);
                        if (entry != null) {
                            list.add(entry);
                        }
                        if (TextUtils.isEmpty(tempIds)) {
                            tempIds = emojiId;
                        } else tempIds = tempIds + "," + emojiId;
                    }

                }
            } else {
                EmojiEntry entry = EmojiManager.getInstance().getEmojiEntry(ids);
                if (entry != null) {
                    list.add(entry);
                }
                tempIds = ids;
            }
        }
        if (list.isEmpty()) return;
        emojiAdapter.removeAllHeaderView();
        View headerView = LayoutInflater.from(this).inflate(com.chat.base.R.layout.common_used_emoji_header_layout, null);
        RecyclerView recyclerView = headerView.findViewById(com.chat.base.R.id.recyclerView);
        EmojiAdapter headerAdapter = new EmojiAdapter(new ArrayList<>(), width);
        headerAdapter.addData(list);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(8, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(headerAdapter);
        emojiAdapter.addHeaderView(headerView);
        WKSharedPreferencesUtil.getInstance().putSPWithUID("common_used_emojis", tempIds);
        headerAdapter.setOnItemClickListener((adapter, view, position) -> {
            EmojiEntry entry = headerAdapter.getItem(position);
            emojiClick(entry.getText());
        });
    }

    private void emojiClick(String name) {
        if (!TextUtils.isEmpty(name)) {
            int curPosition = wkVBinding.contentEt.getSelectionStart();
            StringBuilder sb = new StringBuilder(Objects.requireNonNull(wkVBinding.contentEt.getText()).toString());
            sb.insert(curPosition, name);
//                    mEditText.setText(sb.toString());
            MoonUtil.addEmojiSpan(wkVBinding.contentEt, name, this);
            // 将光标设置到新增完表情的右侧
            wkVBinding.contentEt.setSelection(curPosition + name.length());

            String usedIndexs = WKSharedPreferencesUtil.getInstance().getSPWithUID("common_used_emojis");
            String tempIndexs = "";
            if (!TextUtils.isEmpty(usedIndexs)) {
                if (usedIndexs.contains(",")) {
                    String[] strings = usedIndexs.split(",");
                    for (String string : strings) {
                        if (!string.equals(name)) {
                            if (TextUtils.isEmpty(tempIndexs)) {
                                tempIndexs = string;
                            } else {
                                tempIndexs = tempIndexs + "," + string;
                            }
                        }
                    }
                }
            }
            tempIndexs = name + "," + tempIndexs;
            WKSharedPreferencesUtil.getInstance().putSPWithUID("common_used_emojis", tempIndexs);
        } else {
            wkVBinding.contentEt.dispatchKeyEvent(new KeyEvent(
                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        }

    }

    private View getFooterView() {
        return LayoutInflater.from(this).inflate(R.layout.common_used_emoji_footer_layout, null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mHelper.isKeyboardState()) {
                mHelper.resetState();
                wkVBinding.emotionBtn.setSelected(false);
                wkVBinding.bottomAction.setVisibility(View.GONE);
                return super.onKeyDown(keyCode, event);
            } else if (mHelper.isPanelState()) {
                mHelper.resetState();
                wkVBinding.emotionBtn.setSelected(false);
                wkVBinding.bottomAction.setVisibility(View.GONE);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
