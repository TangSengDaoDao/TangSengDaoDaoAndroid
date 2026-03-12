package org.telegram.ui.Components;

import android.content.Context;

public class RLottieApplication {

    private static volatile RLottieApplication instance;

    public static RLottieApplication getInstance() {
        if (instance == null) {
            synchronized (RLottieApplication.class) {
                if (instance == null) {
                    instance = new RLottieApplication();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        // Airbnb Lottie does not require explicit initialization
    }
}
