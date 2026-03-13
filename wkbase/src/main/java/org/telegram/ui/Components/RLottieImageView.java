package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.RenderMode;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.SimpleLottieValueCallback;

public class RLottieImageView extends LottieAnimationView {

    private RLottieDrawable animatedDrawable;
    private boolean autoRepeat = false;
    private boolean wasPlaying = false;
    private LottieComposition lastComposition;
    private int lastRepeatCount = 0;
    private ColorFilter pendingColorFilter;

    public RLottieImageView(@NonNull Context context) {
        super(context);
        init();
    }

    public RLottieImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RLottieImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setRenderMode(RenderMode.AUTOMATIC);
        setFailureListener(throwable -> {
        });
    }

    public void setAutoRepeat(boolean repeat) {
        this.autoRepeat = repeat;
        lastRepeatCount = repeat ? LottieDrawable.INFINITE : 0;
        setRepeatCount(lastRepeatCount);
        if (animatedDrawable != null) {
            animatedDrawable.setAutoRepeat(repeat ? 1 : 0);
        }
    }

    public void setAnimation(RLottieDrawable drawable) {
        cancelAnimation();
        this.animatedDrawable = drawable;
        this.wasPlaying = false;
        if (autoRepeat) {
            drawable.setAutoRepeat(1);
        }
        drawable.attachToView(this);
        lastComposition = drawable.composition;
        if (pendingColorFilter != null) {
            applyLottieColorFilter(pendingColorFilter);
        }
    }

    public void setAnimation(int rawRes, int w, int h) {
        RLottieDrawable drawable = new RLottieDrawable(getContext(), rawRes, "", w, h);
        setAnimation(drawable);
    }

    @Override
    public void setComposition(@NonNull LottieComposition composition) {
        lastComposition = composition;
        super.setComposition(composition);
    }

    @Override
    public void playAnimation() {
        wasPlaying = true;
        super.playAnimation();
    }

    public void stopAnimation() {
        wasPlaying = false;
        cancelAnimation();
    }

    public void setLottieColorFilter(ColorFilter colorFilter) {
        this.pendingColorFilter = colorFilter;
        if (getComposition() != null) {
            applyLottieColorFilter(colorFilter);
        }
    }

    public void setLottieColorFilter(int color) {
        setLottieColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
    }

    private void applyLottieColorFilter(ColorFilter colorFilter) {
        addValueCallback(new KeyPath("**"), LottieProperty.COLOR_FILTER,
                new SimpleLottieValueCallback<ColorFilter>() {
                    @Override
                    public ColorFilter getValue(com.airbnb.lottie.value.LottieFrameInfo<ColorFilter> frameInfo) {
                        return colorFilter;
                    }
                });
    }

    public RLottieDrawable getAnimatedDrawable() {
        return animatedDrawable;
    }

    public boolean isPlaying() {
        return isAnimating();
    }

    public void clearComposition() {
        cancelAnimation();
        animatedDrawable = null;
        lastComposition = null;
        wasPlaying = false;
        setImageDrawable(null);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (isAnimating()) {
            wasPlaying = true;
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (lastComposition != null && getComposition() == null) {
            setComposition(lastComposition);
            setRepeatCount(lastRepeatCount);
        }
        if (wasPlaying && !isAnimating()) {
            playAnimation();
        }
    }
}
