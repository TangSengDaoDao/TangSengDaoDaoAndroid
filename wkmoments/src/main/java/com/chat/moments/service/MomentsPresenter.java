package com.chat.moments.service;

import com.chat.base.net.HttpResponseCode;

import java.lang.ref.WeakReference;

/**
 * 2020-11-25 14:49
 */
public class MomentsPresenter implements MomentsContact.MomentsPresent {
    private final WeakReference<MomentsContact.MomentsView> momentsView;

    public MomentsPresenter(MomentsContact.MomentsView view) {
        momentsView = new WeakReference<>(view);
    }


    @Override
    public void list(int page) {
        MomentsModel.getInstance().list(page, (code, msg, list) -> {
            if (code == HttpResponseCode.success) {
                if (momentsView.get() != null) {
                    momentsView.get().setList(list);
                }
            } else {
                if (momentsView.get() != null) {
                    momentsView.get().hideLoading();
                }
            }
        });
    }

    @Override
    public void listByUid(int page, String uid) {
        MomentsModel.getInstance().listByUid(page, uid,(code, msg, list) -> {
            if (code == HttpResponseCode.success) {
                if (momentsView.get() != null) {
                    momentsView.get().setList(list);
                }
            } else {
                if (momentsView.get() != null) {
                    momentsView.get().hideLoading();
                }
            }
        });
    }

    @Override
    public void momentSetting(String toUID) {
        MomentsModel.getInstance().momentSetting(toUID, (code, msg, momentSetting) -> {
            if (code == HttpResponseCode.success) {
                if (momentsView.get() != null) momentsView.get().setMomentSetting(momentSetting);
            } else {
                if (momentsView.get() != null) {
                    momentsView.get().hideLoading();
                    momentsView.get().showError(msg);
                }
            }
        });
    }

    @Override
    public void showLoading() {

    }
}
