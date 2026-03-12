package com.chat.uikit.chat;

import androidx.recyclerview.widget.RecyclerView;

public class SafeAdapterHelper {
    public static void safeNotify(RecyclerView recyclerView, Runnable action) {
        if (recyclerView == null || action == null) return;

        if (recyclerView.isComputingLayout() || recyclerView.isAnimating()) {
            recyclerView.post(() -> safeNotify(recyclerView, action));
        } else {
            action.run();
        }
    }
}
