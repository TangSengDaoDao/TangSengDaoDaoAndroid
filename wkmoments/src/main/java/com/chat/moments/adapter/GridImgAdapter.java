package com.chat.moments.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.BaseDraggableModule;
import com.chad.library.adapter.base.module.DraggableModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.glide.GlideUtils;
import com.chat.base.net.ud.WKProgressManager;
import com.chat.base.ui.Theme;
import com.chat.base.ui.components.FilterImageView;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.views.CircularProgressView;
import com.chat.moments.R;
import com.chat.moments.entity.ImgEntity;
import com.chat.moments.entity.MomentsFileUploadStatus;

import java.util.List;

/**
 * 2019-06-07 11:45
 * 选择图片宫格适配器
 */
public class GridImgAdapter extends BaseQuickAdapter<ImgEntity, BaseViewHolder> implements DraggableModule {
    public GridImgAdapter(@Nullable List<ImgEntity> data) {
        super(R.layout.item_grid_img_layout, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ImgEntity item) {
        FilterImageView roundedImageView = helper.getView(R.id.imageView);
        roundedImageView.setStrokeWidth(0);
        roundedImageView.setAllCorners(0);
        FrameLayout frameLayout = helper.getView(R.id.contentLayout);
        CircularProgressView progressView = helper.getView(R.id.progressView);
        progressView.setProgColor(Theme.colorAccount);
        if (item.fileType == 0) {
            roundedImageView.setImageResource(R.mipmap.icon_add_photo);
            helper.setGone(R.id.statusIv, true);
        } else {
            GlideUtils.getInstance().showImg(getContext(), item.path, roundedImageView);
            if (item.fileType == 2) {
                //视频
                helper.setGone(R.id.statusIv, false);
            } else {
                //图片
                if (item.uploadStatus == MomentsFileUploadStatus.fail || item.uploadStatus == MomentsFileUploadStatus.success) {
                    helper.setGone(R.id.statusIv, false);
                } else {
                    helper.setGone(R.id.statusIv, true);
                }
            }
        }
        if (item.progress == 0 || item.progress >= 100) {
            progressView.setVisibility(View.GONE);
        } else {
            progressView.setProgress(item.progress);
            progressView.setVisibility(View.VISIBLE);
        }
        //设置上传结果
        if (item.uploadStatus == MomentsFileUploadStatus.fail) {
            helper.setImageResource(R.id.statusIv, R.mipmap.icon_send_fail);
        } else if (item.uploadStatus == MomentsFileUploadStatus.success) {
            helper.setImageResource(R.id.statusIv, R.mipmap.icon_upload_success);
        } else
            helper.setImageResource(R.id.statusIv, R.mipmap.icon_play);

        //设置上传进度
        if (TextUtils.isEmpty(item.url) && item.fileType != 0) {
            WKProgressManager.Companion.getInstance().registerProgress(item.key, new WKProgressManager.IProgress() {
                @Override
                public void onProgress(@Nullable Object tag, int progress) {

                    if (tag instanceof String index) {
                        if (index.equals(item.key)) {
                            if (progress >= 100) {
                                progressView.setVisibility(View.GONE);
                            } else {
                                progressView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }

                @Override
                public void onSuccess(@Nullable Object tag, @Nullable String path) {
                    progressView.setVisibility(View.GONE);
                    if (tag != null) {
                        WKProgressManager.Companion.getInstance().unregisterProgress(tag);
                    }
                }

                @Override
                public void onFail(@Nullable Object tag, @Nullable String msg) {

                }
            });
        }

        int showWidth = (AndroidUtilities.getScreenWidth() - AndroidUtilities.dp(45)) / 3;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(showWidth, showWidth);
        layoutParams.setMargins(AndroidUtilities.dp(2.5f), AndroidUtilities.dp(2.5f), AndroidUtilities.dp(2.5f), AndroidUtilities.dp(2.5f));
//        roundedImageView.setLayoutParams(layoutParams);
        frameLayout.setLayoutParams(layoutParams);


    }

    @NonNull
    @Override
    public BaseDraggableModule addDraggableModule(@NonNull BaseQuickAdapter<?, ?> baseQuickAdapter) {
        return new BaseDraggableModule(baseQuickAdapter);
    }
}
