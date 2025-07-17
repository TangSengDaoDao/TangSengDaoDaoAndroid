package com.chat.sticker.ui.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.chat.base.config.WKApiConfig;
import com.chat.base.net.ud.WKDownloader;
import com.chat.base.net.ud.WKProgressManager;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.LayoutHelper;
import com.chat.base.utils.SvgHelper;
import com.chat.base.utils.WKFileUtils;
import com.chat.sticker.R;
import com.chat.sticker.WKStickerApplication;

import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;

import java.io.File;

/**
 * 2021/8/10 20:42
 * sticker view
 */
public class StickerView extends FrameLayout implements WKProgressManager.IProgress {

    private String tag;
    private boolean isLoopPlay;
    private boolean isPlay;
    private int size;

    public StickerView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public StickerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    RLottieImageView aXrLottieImageView;

    private void initView(Context context) {
        aXrLottieImageView = new RLottieImageView(context);
        aXrLottieImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(aXrLottieImageView, LayoutHelper.createFrame(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));

    }

    public synchronized void showSticker(String url, String placeholder, int size, boolean isLoopPlay) {
        this.showSticker(url, placeholder, size, isLoopPlay, true);
    }

    public synchronized void showSticker(String url, String placeholder, int size, boolean isLoopPlay, boolean isPlay) {
        this.aXrLottieImageView.getLayoutParams().height = size;
        this.aXrLottieImageView.getLayoutParams().width = size;
        this.isLoopPlay = isLoopPlay;
        this.isPlay = isPlay;
        this.size = size;
        if (!TextUtils.isEmpty(placeholder) && placeholder.startsWith("<")) {
            String startStr = "<svg version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" viewBox=\"0 0 512 512\" xml:space=\"preserve\"><path fill-opacity=\"0.1\" d=\"";
            String endStr = " /></svg>";
            String path = placeholder.replaceAll(startStr, "").replaceAll(endStr, "");
            Bitmap bitmap = SvgHelper.getBitmapByPathOnly(path, ContextCompat.getColor(getContext(), R.color.sticker_placeholder), 512, 512, 512, 512);
            if (bitmap != null) {
                aXrLottieImageView.setImageBitmap(bitmap);
            }
        }
        if (TextUtils.isEmpty(url)) return;

        aXrLottieImageView.setTag(url);
        WKStickerApplication.Companion.getInstance().getDispatchQueuePool().execute(() -> {
            String fileName = String.format("%s", url.replaceAll("/", "_"));
            String fileDir = WKStickerApplication.Companion.getInstance().getStickerDirPath();
            String filePath = fileDir + fileName;
            File file = new File(filePath);
            if (file.exists()) {
                showSticker(aXrLottieImageView, isLoopPlay, file, size, isPlay);
            } else {
                String fileName1 = url.replaceAll("/", "_");
                String filePath1 = fileDir + fileName1;
                File file1 = new File(filePath1);
                if (file1.exists()) {
                    showSticker(aXrLottieImageView, isLoopPlay, file1, size, isPlay);
                    return;
                }
                StickerView.this.tag = WKApiConfig.getShowUrl(url);
                WKDownloader.Companion.getInstance().download(tag, file1.getAbsolutePath(), this);
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }


    public void showSticker(RLottieImageView aXrLottieImageView, boolean isLoopPlay, File file, int size, boolean isPlay) {
        String fileName = file.getName().replace(".lim", "");
        File jsonFile = new File(fileName);
        if (!jsonFile.exists()) {
            String path = WKFileUtils.getInstance().uncompressSticker(WKFileUtils.getInstance().file2byte(file), fileName);
            if (!TextUtils.isEmpty(path)) {
                jsonFile = new File(path);
            }
        }
        if (aXrLottieImageView.getTag() instanceof String imageTag) {
            if (!TextUtils.isEmpty(imageTag)) {
                String newTag = imageTag.replace(".lim", "").replaceAll("/","_");
                if (!jsonFile.getAbsolutePath().endsWith(newTag)) {
                    return;
                }
            }
        }
        RLottieDrawable drawable = new RLottieDrawable(jsonFile, size, size, false, false);
        drawable.setAutoRepeat(isLoopPlay ? 1 : 0);
        AndroidUtilities.runOnUIThread(() -> {
            aXrLottieImageView.setAnimation(drawable);
            if (isPlay)
                aXrLottieImageView.playAnimation();
        });
    }

    public void restart() {
        if (aXrLottieImageView != null && aXrLottieImageView.getAnimatedDrawable() != null) {
            aXrLottieImageView.getAnimatedDrawable().setAutoRepeat(1);
            aXrLottieImageView.playAnimation();
            aXrLottieImageView.getAnimatedDrawable().setOnFinishCallback(() -> aXrLottieImageView.stopAnimation(), aXrLottieImageView.getAnimatedDrawable().getFramesCount() - 1);
        }
    }

    public RLottieImageView getImageView() {
        return aXrLottieImageView;
    }

    @Override
    public void onProgress(@Nullable Object tag, int progress) {

    }

    @Override
    public void onSuccess(@Nullable Object tag, @Nullable String path) {
        if (!TextUtils.isEmpty(path)) {
            showSticker(aXrLottieImageView, isLoopPlay, new File(path), size, isPlay);
        }

    }

    @Override
    public void onFail(@Nullable Object tag, @Nullable String msg) {

    }
}
