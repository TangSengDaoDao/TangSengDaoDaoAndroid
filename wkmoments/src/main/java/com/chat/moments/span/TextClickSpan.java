package com.chat.moments.span;

import android.content.Context;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.chat.moments.WKMomentsApplication;
import com.chat.moments.R;

import org.jetbrains.annotations.NotNull;

public class TextClickSpan extends ClickableSpan {

    private final Context mContext;

    private final String mUserName;
    private boolean mPressed;
    private final String uid;

    public TextClickSpan(Context context, String uid, String userName) {
        this.mContext = context;
        this.mUserName = userName;
        this.uid = uid;
    }

    public void setPressed(boolean isPressed) {
        this.mPressed = isPressed;
    }

    @Override
    public void updateDrawState(@NotNull TextPaint ds) {
        super.updateDrawState(ds);
        ds.bgColor = mPressed ? ContextCompat.getColor(mContext, R.color.colorB5B5B5) : Color.TRANSPARENT;
        ds.setColor(ContextCompat.getColor(mContext, R.color.color697A9F));
        ds.setUnderlineText(false);
    }

    @Override
    public void onClick(@NotNull View widget) {
        WKMomentsApplication.getInstance().gotoUserDetail(widget.getContext(), uid);
    }
}
