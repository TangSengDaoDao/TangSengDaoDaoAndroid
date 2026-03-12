package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class RLottieDrawable extends Drawable {

    LottieComposition composition;
    int autoRepeat = 0;
    private int width;
    private int height;
    private Runnable onFinishCallback;
    private int finishFrame = -1;
    private int currentFrame = 0;
    private int framesCount = 0;
    private boolean compositionLoaded = false;
    private LottieAnimationView attachedView;

    public RLottieDrawable(Context context, int rawRes, String name, int w, int h) {
        this(context, rawRes, name, w, h, false, null);
    }

    public RLottieDrawable(Context context, int rawRes, String name, int w, int h, boolean precache, String[] colorReplacement) {
        this.width = w;
        this.height = h;
        LottieResult<LottieComposition> result = LottieCompositionFactory.fromRawResSync(context, rawRes);
        if (result.getValue() != null) {
            composition = result.getValue();
            framesCount = (int) composition.getDurationFrames();
            compositionLoaded = true;
        }
        setBounds(0, 0, w, h);
    }

    public RLottieDrawable(File jsonFile, int w, int h, boolean precache, boolean limitFps) {
        this.width = w;
        this.height = h;
        try {
            FileInputStream fis = new FileInputStream(jsonFile);
            LottieResult<LottieComposition> result = LottieCompositionFactory.fromJsonInputStreamSync(fis, jsonFile.getName());
            fis.close();
            if (result.getValue() != null) {
                composition = result.getValue();
                framesCount = (int) composition.getDurationFrames();
                compositionLoaded = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setBounds(0, 0, w, h);
    }

    public RLottieDrawable(Context context, int rawRes, int w, int h, boolean precache, String[] colorReplacement) {
        this(context, rawRes, "", w, h, precache, colorReplacement);
    }

    public RLottieDrawable(Context context, String assetPath, int w, int h, boolean precache, String[] colorReplacement) {
        this.width = w;
        this.height = h;
        try {
            InputStream rawIs = context.getAssets().open(assetPath);
            InputStream is;
            if (assetPath.endsWith(".lim")) {
                is = new java.util.zip.GZIPInputStream(rawIs);
            } else {
                is = rawIs;
            }
            LottieResult<LottieComposition> result = LottieCompositionFactory.fromJsonInputStreamSync(is, assetPath);
            is.close();
            if (is != rawIs) rawIs.close();
            if (result.getValue() != null) {
                composition = result.getValue();
                framesCount = (int) composition.getDurationFrames();
                compositionLoaded = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setBounds(0, 0, w, h);
    }

    public RLottieDrawable(Context context, int rawRes, int w, int h) {
        this(context, rawRes, "", w, h, false, null);
    }

    void attachToView(LottieAnimationView view) {
        this.attachedView = view;
        if (composition != null) {
            view.setComposition(composition);
            applyRepeatToView(view);
        }
    }

    private void applyRepeatToView(LottieAnimationView view) {
        if (autoRepeat == 0) {
            view.setRepeatCount(0);
        } else if (autoRepeat == 1) {
            view.setRepeatCount(LottieDrawable.INFINITE);
        } else {
            view.setRepeatCount(autoRepeat);
        }
    }

    public void start() {
        if (attachedView != null) {
            attachedView.playAnimation();
        }
    }

    public void stop() {
        if (attachedView != null) {
            attachedView.cancelAnimation();
        }
    }

    public boolean isRunning() {
        return attachedView != null && attachedView.isAnimating();
    }

    public void setAutoRepeat(int repeat) {
        this.autoRepeat = repeat;
        if (attachedView != null) {
            applyRepeatToView(attachedView);
        }
    }

    public int getAutoRepeat() {
        return autoRepeat;
    }

    public void setCurrentFrame(int frame) {
        this.currentFrame = frame;
        if (compositionLoaded && framesCount > 0 && attachedView != null) {
            if (attachedView.isAnimating()) {
                attachedView.pauseAnimation();
            }
            float progress = (float) frame / (float) framesCount;
            attachedView.setProgress(Math.min(1f, Math.max(0f, progress)));
        }
    }

    public void setCurrentFrame(int frame, boolean async, boolean resetFinish) {
        setCurrentFrame(frame);
    }

    public int getCurrentFrame() {
        if (attachedView != null && compositionLoaded && framesCount > 0) {
            return (int) (attachedView.getProgress() * framesCount);
        }
        return currentFrame;
    }

    public int getFramesCount() {
        return framesCount;
    }

    public Drawable getCurrent() {
        if (attachedView != null && compositionLoaded && framesCount > 0) {
            float progress = (float) currentFrame / (float) framesCount;
            attachedView.setProgress(Math.min(1f, Math.max(0f, progress)));
            attachedView.buildDrawingCache();
            Bitmap bmp = attachedView.getDrawingCache();
            if (bmp != null) {
                return new BitmapDrawable(attachedView.getResources(), bmp);
            }
        }
        if (composition != null) {
            LottieDrawable ld = new LottieDrawable();
            ld.setComposition(composition);
            if (framesCount > 0) {
                ld.setProgress((float) currentFrame / (float) framesCount);
            }
            Bitmap bitmap = Bitmap.createBitmap(Math.max(1, width), Math.max(1, height), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            ld.setBounds(0, 0, width, height);
            ld.draw(canvas);
            return new BitmapDrawable(bitmap);
        }
        return null;
    }

    public void setLayerColor(String layerName, int color) {
        // Airbnb Lottie uses KeyPath-based color filtering
    }

    public void setOnFinishCallback(Runnable callback) {
        this.onFinishCallback = callback;
        this.finishFrame = -1;
        setupFinishListener();
    }

    public void setOnFinishCallback(Runnable callback, int frame) {
        this.onFinishCallback = callback;
        this.finishFrame = frame;
        setupFinishListener();
    }

    private void setupFinishListener() {
        if (attachedView == null) return;
        attachedView.removeAllAnimatorListeners();
        attachedView.removeAllUpdateListeners();
        if (finishFrame >= 0 && framesCount > 0) {
            attachedView.addAnimatorUpdateListener(animation -> {
                float progress = animation.getAnimatedFraction();
                int frame = (int) (progress * framesCount);
                if (frame >= finishFrame && onFinishCallback != null) {
                    attachedView.removeAllUpdateListeners();
                    onFinishCallback.run();
                }
            });
        } else {
            attachedView.addAnimatorListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (onFinishCallback != null) {
                        onFinishCallback.run();
                    }
                }
            });
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        // Drawing is handled by LottieAnimationView directly
    }

    @Override
    public void setAlpha(int alpha) {
        if (attachedView != null) {
            attachedView.setAlpha((float) alpha / 255f);
        }
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        if (attachedView != null) {
            attachedView.setColorFilter(colorFilter);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return width;
    }

    @Override
    public int getIntrinsicHeight() {
        return height;
    }
}
