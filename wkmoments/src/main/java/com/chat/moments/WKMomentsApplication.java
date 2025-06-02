package com.chat.moments;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.chat.base.WKBaseApplication;
import com.chat.base.config.WKApiConfig;
import com.chat.base.config.WKConfig;
import com.chat.base.config.WKSharedPreferencesUtil;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.EndpointSID;
import com.chat.base.endpoint.entity.ContactsMenu;
import com.chat.base.endpoint.entity.DBMenu;
import com.chat.base.endpoint.entity.MailListDot;
import com.chat.base.endpoint.entity.UserDetailMenu;
import com.chat.base.endpoint.entity.UserDetailViewMenu;
import com.chat.base.entity.AppModule;
import com.chat.base.glide.GlideUtils;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.LayoutHelper;
import com.chat.base.utils.WKReader;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.moments.activities.MomentSettingActivity;
import com.chat.moments.activities.MomentsActivity;
import com.chat.moments.db.MomentsDBManager;
import com.chat.moments.db.MomentsDBMsg;
import com.chat.moments.entity.Moments;
import com.chat.moments.service.MomentsModel;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKCMDKeys;
import com.xinbida.wukongim.entity.WKChannel;
import com.xinbida.wukongim.entity.WKChannelType;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

/**
 * 2020-11-05 10:37
 * 动态
 */
public class WKMomentsApplication {
    private WKMomentsApplication() {
    }

    private static class MomentsApplicationBinder {
        final static WKMomentsApplication app = new WKMomentsApplication();
    }

    public static WKMomentsApplication getInstance() {
        return MomentsApplicationBinder.app;
    }

    private WeakReference<Context> context;

    public Context getContext() {
        return context.get();
    }

    public void init(final Context context) {
        //初始化数据库信息
        EndpointManager.getInstance().setMethod("moments", EndpointCategory.wkDBMenus, object -> new DBMenu("moments_sql"));
        AppModule appModule = WKBaseApplication.getInstance().getAppModuleWithSid("moment");
        if (!WKBaseApplication.getInstance().appModuleIsInjection(appModule))
            return;
        this.context = new WeakReference<>(context);
        EndpointManager.getInstance().setMethod(EndpointCategory.mailList + "_moments", EndpointCategory.mailList, 200, object -> {
            ContactsMenu contactsMenu = new ContactsMenu("moments", R.mipmap.icon_moments, context.getString(R.string.str_moments), () -> {
                Intent intent = new Intent(context, MomentsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
            contactsMenu.badgeNum = WKSharedPreferencesUtil.getInstance().getInt(WKConfig.getInstance().getUid() + "_moments_msg_count");
            contactsMenu.uid = WKSharedPreferencesUtil.getInstance().getSP(WKConfig.getInstance().getUid() + "_moments_msg_uid");
            return contactsMenu;
        });

        //监听登录成功后将朋友圈背景清空重新下载
        EndpointManager.getInstance().setMethod("", EndpointCategory.loginMenus, object -> {
            WKSharedPreferencesUtil.getInstance().putSP(String.format("moment_bg_url_%s", WKConfig.getInstance().getUid()), "");
            return null;
        });
        //获取要显示在通讯录的红点数量
        EndpointManager.getInstance().setMethod("moments", EndpointCategory.wkGetMailListRedDot, object -> {
            int count = WKSharedPreferencesUtil.getInstance().getInt(WKConfig.getInstance().getUid() + "_moments_msg_count");
            String uid = WKSharedPreferencesUtil.getInstance().getSP(WKConfig.getInstance().getUid() + "_moments_msg_uid");
            return new MailListDot(count, !TextUtils.isEmpty(uid));
        });

        initListener();
    }


    public void gotoUserDetail(Context context,String uid) {
        EndpointManager.getInstance().invoke(EndpointSID.userDetailView, new UserDetailMenu(context,uid));
    }

    private void initListener() {
        WKIM.getInstance().getCMDManager().addCmdListener("moment_cmd", cmd -> {
            if (cmd != null && !TextUtils.isEmpty(cmd.cmdKey) && cmd.paramJsonObject != null) {
                if (cmd.cmdKey.equals(WKCMDKeys.wk_momentMsg)) {
                    if (cmd.paramJsonObject.has("action")) {
                        String action = cmd.paramJsonObject.optString("action");
                        if (action.equals("publish")) {
                            String uid = cmd.paramJsonObject.optString("uid");
                            WKSharedPreferencesUtil.getInstance().putSP(WKConfig.getInstance().getUid() + "_moments_msg_uid", uid);
                            EndpointManager.getInstance().invokes(EndpointCategory.wkRefreshMailList, null);
                        } else {
                            saveMomentMsg(cmd.paramJsonObject);
                        }
                    }
                }
            }
        });


        EndpointManager.getInstance().setMethod("moment", EndpointCategory.wkUserDetailView, 10, object -> {
            UserDetailViewMenu menu = (UserDetailViewMenu) object;
            if (TextUtils.isEmpty(menu.uid) || context == null)
                return null;
            else {
                return getMomentInUserDetailView(menu.context, menu.uid, menu.parentView);
            }
        });
    }

    private void saveMomentMsg(JSONObject jsonObject) {
        MomentsDBMsg mMomentsDBMsg = new MomentsDBMsg();
        mMomentsDBMsg.action = jsonObject.optString("action");
        mMomentsDBMsg.action_at = jsonObject.optLong("action_at");
        mMomentsDBMsg.moment_no = jsonObject.optString("moment_no");
        mMomentsDBMsg.uid = jsonObject.optString("uid");
        mMomentsDBMsg.name = jsonObject.optString("name");
        mMomentsDBMsg.comment = jsonObject.optString("comment");
        if (jsonObject.has("comment_id")) {
            mMomentsDBMsg.comment_id = jsonObject.optInt("comment_id");
        }
        if (mMomentsDBMsg.action.equals("delete_comment")) {
            mMomentsDBMsg.is_deleted = 1;
        }
        if (mMomentsDBMsg.action.equals("unlike")){
            return;
        }
        if (jsonObject.has("content")) {
            mMomentsDBMsg.content = Objects.requireNonNull(jsonObject.optJSONObject("content")).toString();
        } else {
            mMomentsDBMsg.content = "";
        }
        if (!TextUtils.isEmpty(mMomentsDBMsg.action)
                && context != null && context.get() != null
                && mMomentsDBMsg.action.equals("remind")) {
            mMomentsDBMsg.comment = context.get().getString(R.string.moment_remind);
        }
        MomentsDBManager.getInstance().insert(mMomentsDBMsg);
        if (mMomentsDBMsg.is_deleted == 0) {
            int count = WKSharedPreferencesUtil.getInstance().getInt(WKConfig.getInstance().getUid() + "_moments_msg_count");
            count = count + 1;
            WKSharedPreferencesUtil.getInstance().putInt(WKConfig.getInstance().getUid() + "_moments_msg_count", count);
            //操作者的用户ID
            WKSharedPreferencesUtil.getInstance().putSP(WKConfig.getInstance().getUid() + "_moments_msg_action_uid", mMomentsDBMsg.uid);
            //刷新通讯录红点
            EndpointManager.getInstance().invokes(EndpointCategory.wkRefreshMailList, null);
        }

    }

    private View getMomentInUserDetailView(WeakReference<Context> context, String uid, ViewGroup parentView) {
        View view = LayoutInflater.from(context.get()).inflate(R.layout.moment_in_user_detail_layout, parentView, false);
        SingleClickUtil.onSingleClick(view, v -> {
            Intent intent = new Intent(context.get(), MomentsActivity.class);
            intent.putExtra("uid", uid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.get().startActivity(intent);
        });
        LinearLayout momentImgLayout = view.findViewById(R.id.momentImgLayout);
        getUserMoment(uid, context, momentImgLayout);
        WKChannel userChannel = WKIM.getInstance().getChannelManager().getChannel(uid, WKChannelType.PERSONAL);
        LinearLayout momentSettingLayout = view.findViewById(R.id.momentSettingLayout);
        if (userChannel != null && userChannel.follow == 1) {
            momentSettingLayout.setVisibility(View.VISIBLE);
        }
        SingleClickUtil.onSingleClick(momentSettingLayout, view1 -> {
            if (!TextUtils.isEmpty(uid)) {
                Intent intent = new Intent(context.get(), MomentSettingActivity.class);
                intent.putExtra("uid", uid);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.get().startActivity(intent);
            }
        });
        return view;
    }


    //获取用户的朋友圈数据
    private void getUserMoment(String uid, WeakReference<Context> context, LinearLayout momentImgLayout) {
        MomentsModel.getInstance().listByUIDWithAttachment(uid, (code, msg, list) -> {
            if (code == HttpResponseCode.success) {
                if (context != null && context.get() != null) {
                    addMomentImg(list, context.get(), uid, momentImgLayout);
                }
            }
        });
    }


    private void addMomentImg(List<Moments> list, Context context, String uid, LinearLayout momentImgLayout) {
        int max = Math.min(4, list.size());
        for (int i = 0; i < max; i++) {
            FrameLayout frameLayout = new FrameLayout(context);
            ImageView view = new ImageView(context);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setLayoutParams(new ViewGroup.LayoutParams(AndroidUtilities.dp(50), AndroidUtilities.dp(50)));
            view.setPadding(10, 0, 0, 10);
            SingleClickUtil.onSingleClick(view, view1 -> {
                Intent intent = new Intent(context, MomentsActivity.class);
                intent.putExtra("uid", uid);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
            boolean isAddVideo = false;
            String url;
            if (WKReader.isNotEmpty(list.get(i).imgs)) {
                url = list.get(i).imgs.get(0);
            } else {
                url = list.get(i).video_cover_path;
                isAddVideo = true;
            }
            GlideUtils.getInstance().showImg(context, WKApiConfig.getShowUrl(url), view);
            frameLayout.addView(view);
            if (isAddVideo) {
                ImageView imageView = new ImageView(context);
                imageView.setImageResource(R.mipmap.ic_video);
                frameLayout.addView(imageView, LayoutHelper.createFrame(20, 10, Gravity.BOTTOM, 5, 0, 0, 8));
            }
            momentImgLayout.addView(frameLayout);
        }
    }
}
