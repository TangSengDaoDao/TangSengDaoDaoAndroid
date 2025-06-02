package com.chat.moments.service;

import com.chat.base.base.WKBasePresenter;
import com.chat.base.base.WKBaseView;
import com.chat.moments.entity.MomentSetting;
import com.chat.moments.entity.Moments;

import java.util.List;

/**
 * 2020-11-25 14:47
 */
public class MomentsContact {
    public interface MomentsPresent extends WKBasePresenter {
        void list(int page);

        void listByUid(int page, String uid);

        void momentSetting(String toUID);
    }

    public interface MomentsView extends WKBaseView {
        void setList(List<Moments> list);

        void setMomentSetting(MomentSetting momentSetting);
    }
}
