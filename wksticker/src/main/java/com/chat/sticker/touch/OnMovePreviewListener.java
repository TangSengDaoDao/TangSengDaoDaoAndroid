package com.chat.sticker.touch;

import android.view.View;

public interface OnMovePreviewListener {

    void onPreview(View childView, int childPosition);

    void onCancelPreview();
}
