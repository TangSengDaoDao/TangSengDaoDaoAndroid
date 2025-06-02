package com.chat.moments.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.chat.base.ui.Theme;
import com.chat.base.utils.AndroidUtilities;
import com.chat.moments.R;

public class FeedActionPopup extends PopupWindow {

    public interface IClick {
        void onClick(int type);
    }

    public FeedActionPopup(final Context context, boolean isLiked, IClick clickListener) {
        final View view = LayoutInflater.from(context).inflate(R.layout.pop_feed_action_layout, null, false);
        setAnimationStyle(R.style.FeedActionPopup_anim_style);
        setFocusable(true);
        setWidth(AndroidUtilities.dp(180));
        setHeight(AndroidUtilities.dp(34));
        setOutsideTouchable(true);
        ColorDrawable dw = new ColorDrawable(Color.TRANSPARENT);
        setBackgroundDrawable(dw);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        setContentView(view);
        TextView praiseTv = view.findViewById(R.id.praiseTv);
        ImageView praiseIv = view.findViewById(R.id.praiseIv);
        if (isLiked) {
            praiseTv.setText(R.string.cancel);
            Theme.setColorFilter(context, praiseIv, R.color.red);
        } else {
            praiseTv.setText(R.string.str_praise);
            Theme.setColorFilter(context, praiseIv, R.color.white);
        }
        view.findViewById(R.id.comment).setOnClickListener(v -> {
            dismiss();
            clickListener.onClick(1);
        });
        view.findViewById(R.id.like).setOnClickListener(v -> {
            dismiss();
            clickListener.onClick(0);
        });
    }

}
