package com.chat.video;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import androidx.fragment.app.FragmentActivity;

import com.chat.base.WKBaseApplication;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointHandler;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.ChatFunctionMenu;
import com.chat.base.endpoint.entity.ChatToolBarMenu;
import com.chat.base.endpoint.entity.MsgConfig;
import com.chat.base.endpoint.entity.SearchChatContentMenu;
import com.chat.base.endpoint.entity.VideoReadingMenu;
import com.chat.base.entity.AppModule;
import com.chat.base.msg.IConversationContext;
import com.chat.base.msgitem.WKContentType;
import com.chat.base.msgitem.WKMsgItemViewManager;
import com.chat.base.utils.WKPermissions;
import com.chat.base.utils.WKToastUtils;
import com.chat.video.msgitem.VideoItemProvider;
import com.chat.video.search.SearchChatVideoActivity;
import com.chat.video.search.remote.SearchActivity;
import com.xinbida.wukongim.message.type.WKMsgContentType;
import com.xinbida.wukongim.msgmodel.WKImageContent;
import com.xinbida.wukongim.msgmodel.WKVideoContent;

/**
 * 2020-03-10 20:13
 * 视频model
 */
public class WKVideoApplication {
    private WKVideoApplication() {
    }

    private static class VideoApplicationBinder {
        final static WKVideoApplication VIDEO = new WKVideoApplication();
    }

    public static WKVideoApplication getInstance() {
        return VideoApplicationBinder.VIDEO;
    }

    public void init(Context context) {
        AppModule appModule = WKBaseApplication.getInstance().getAppModuleWithSid("video");
        if (!WKBaseApplication.getInstance().appModuleIsInjection(appModule)) {
            return;
        }
        //添加消息item
        WKMsgItemViewManager.getInstance().addChatItemViewProvider(WKMsgContentType.WK_VIDEO, new VideoItemProvider());
        // 注册长按菜单
        EndpointManager.getInstance().setMethod(EndpointCategory.msgConfig + WKContentType.WK_VIDEO, object -> new MsgConfig(true));
        initListener(context);
//        org.telegram.messenger.VideoConvertUtil.init(new Scheduler() {
//            @Override
//            public void runOnComputationThread(Runnable runnable) {
//                Schedulers.computation().scheduleDirect(runnable);
//            }
//
//            @Override
//            public void runOnUIThread(Runnable runnable) {
//                AndroidSchedulers.mainThread().scheduleDirect(runnable);
//            }
//        });

    }

    private void initListener(final Context context) {
//        ExoSourceManager.setExoMediaSourceInterceptListener(new ExoMediaSourceInterceptListener() {
//            @Override
//            public MediaSource getMediaSource(String dataSource, boolean preview, boolean cacheEnable, boolean isLooping, File cacheDir) {
//                //如果返回 null，就使用默认的
//                return null;
//            }
//
//            /**
//             * 通过自定义的 HttpDataSource ，可以设置自签证书或者忽略证书
//             * demo 里的 GSYExoHttpDataSourceFactory 使用的是忽略证书
//             * */
//            @Override
//            public DataSource.Factory getHttpDataSourceFactory(String userAgent, @Nullable TransferListener listener, int connectTimeoutMillis, int readTimeoutMillis,
//                                                               Map<String, String> mapHeadData, boolean allowCrossProtocolRedirects) {
//                //如果返回 null，就使用默认的
//                GSYExoHttpDataSourceFactory factory = new GSYExoHttpDataSourceFactory(userAgent, listener,
//                        connectTimeoutMillis,
//                        readTimeoutMillis, allowCrossProtocolRedirects);
//                factory.setDefaultRequestProperties(mapHeadData);
//                return factory;
//            }
//        });
        EndpointManager.getInstance().setMethod("is_register_video", object -> true);
        //添加聊天功能面板
        EndpointManager.getInstance().setMethod(EndpointCategory.chatFunction + "_recording", EndpointCategory.chatFunction, 99, object -> new ChatFunctionMenu("recording", R.mipmap.icon_func_recording, context.getString(R.string.wkvideo_recording), (iConversationContext) -> {
            this.iConversationContext = (IConversationContext) object;
            startRecord(iConversationContext.getChatActivity());
        }));
        //监听录制视频
        EndpointManager.getInstance().setMethod("video_recording", object -> {
            Intent intent = new Intent(context, RecordingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            videoReadingMenu = new VideoReadingMenu((VideoReadingMenu.IRedingResult) object);
            return videoReadingMenu;
        });

        EndpointManager.getInstance().setMethod(EndpointCategory.wkChatToolBar + "_album", EndpointCategory.wkChatToolBar, 98, object -> new ChatToolBarMenu("wk_chat_toolbar_camera", R.mipmap.icon_chat_toolbar_camera, -1, null, (isSelected, iConversationContext1) -> {
            if (isSelected) {
                WKVideoApplication.this.iConversationContext = iConversationContext1;
                checkPermission(iConversationContext1.getChatActivity());

            }
        }));

        // 搜索聊天图片
//        EndpointManager.getInstance().setMethod("str_search_chat_video", EndpointCategory.wkSearchChatContent, 95, object -> new SearchChatContentMenu(context.getString(R.string.wk_video), (channelID, channelType) -> {
//            Intent intent = new Intent(context, SearchChatVideoActivity.class);
//            intent.putExtra("channel_id", channelID);
//            intent.putExtra("channel_type", channelType);
//            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);
//        }));
        // 远程搜索
        EndpointManager.getInstance().setMethod("str_search_chat_video", EndpointCategory.wkSearchChatContent, 95, object -> new SearchChatContentMenu(context.getString(R.string.wk_video), (channelID, channelType) -> {
            Intent intent = new Intent(context, SearchActivity.class);
            intent.putExtra("channel_id", channelID);
            intent.putExtra("channel_type", channelType);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }));

        EndpointManager.getInstance().setMethod("is_register_video", object -> true);
        EndpointManager.getInstance().setMethod("videoCompress", new EndpointHandler() {
            @Override
            public Object invoke(Object object) {
                t(object.toString());
                return null;
            }
        });
    }

    private VideoReadingMenu videoReadingMenu;
    private IConversationContext iConversationContext;

    public void clearConversationContext() {
        iConversationContext = null;
    }

    void setRecordingResult(long second, String path, String videoPath, long size) {
        if (videoReadingMenu != null) {
            videoReadingMenu.iRedingResult.onResult(second, path, videoPath, size);
        }
        if (!TextUtils.isEmpty(videoPath)) {
            WKVideoContent videoMsgModel = new WKVideoContent();
            videoMsgModel.coverLocalPath = path;
            videoMsgModel.localPath = videoPath;
            videoMsgModel.size = size;
            videoMsgModel.second = second;
            if (iConversationContext != null) {
                iConversationContext.sendMessage(videoMsgModel);
                iConversationContext = null;
            }
        } else {
            if (iConversationContext != null) {
                iConversationContext.sendMessage(new WKImageContent(path));
                iConversationContext = null;
            }
        }
    }

    private void startRecord(Context context) {
        Object endpoint = EndpointManager.getInstance().invoke("rtc_is_calling", null);
        if (endpoint != null) {
            boolean isCalling = (boolean) endpoint;
            if (isCalling) {
                WKToastUtils.getInstance().showToastNormal(context.getString(R.string.not_use_the_camera));
                return;
            }
        }
        String desc = String.format(iConversationContext.getChatActivity().getString(R.string.camera_permissions_desc), iConversationContext.getChatActivity().getString(R.string.app_name));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            WKPermissions.getInstance().checkPermissions(new WKPermissions.IPermissionResult() {
                @Override
                public void onResult(boolean result) {
                    if (result) {
                        Intent intent = new Intent(iConversationContext.getChatActivity(), RecordingActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        iConversationContext.getChatActivity().startActivity(intent);
                    }
                }

                @Override
                public void clickResult(boolean isCancel) {
                }
            }, iConversationContext.getChatActivity(), desc, Manifest.permission.CAMERA);
        } else {
            WKPermissions.getInstance().checkPermissions(new WKPermissions.IPermissionResult() {
                @Override
                public void onResult(boolean result) {
                    if (result) {
                        Intent intent = new Intent(iConversationContext.getChatActivity(), RecordingActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        iConversationContext.getChatActivity().startActivity(intent);
                    }
                }

                @Override
                public void clickResult(boolean isCancel) {
                }
            }, iConversationContext.getChatActivity(), desc, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void t(String videoPath) {
//        Integer telegramId = org.telegram.messenger.VideoConvertUtil.startVideoConvert(videoPath, WKConstants.videoDir + "test.mp4", new MediaController.ConvertorListener() {
//            @Override
//            public void onConvertStart(VideoEditedInfo info, float progress, long lastFrameTimestamp) {
//
//            }
//
//            @Override
//            public void onConvertProgress(VideoEditedInfo info, long availableSize, float progress, long lastFrameTimestamp) {
//                Log.e("压缩进度", String.valueOf(progress));
//            }
//
//            @Override
//            public void onConvertSuccess(VideoEditedInfo info, long fileLength, long lastFrameTimestamp) {
//                Log.e("压缩成功", info.toString());
//            }
//
//            @Override
//            public void onConvertFailed(VideoEditedInfo info, float progress, long lastFrameTimestamp) {
//                Log.e("压缩失败", "-->" + info.toString());
//            }
//        });
//        if (telegramId == null) {
//            Log.e("压缩失败", "-->");
//        }

//        VideoCompress.VideoCompressTask task = VideoCompress.compressVideoHigh(videoPath, WKConstants.videoDir + "test.mp4", new VideoCompress.CompressListener() {
//            @Override
//            public void onStart() {
//                //Start Compress
//                Log.e("开始压缩", "-->"+ WKTimeUtils.getInstance().getCurrentSeconds());
//            }
//
//            @Override
//            public void onSuccess() {
//                //Finish successfully
//                Log.e("开始压缩成功", "-->"+WKTimeUtils.getInstance().getCurrentSeconds());
//            }
//
//            @Override
//            public void onFail() {
//                //Failed
//                Log.e("开始压缩失败", "-->");
//            }
//
//            @Override
//            public void onProgress(float percent) {
//                //Progress
//                Log.e("压缩进度", "-->" + percent);
//            }
//        });
    }

    private void checkPermission(FragmentActivity activity) {
        String desc = String.format(activity.getString(R.string.microphone_permissions_des), activity.getString(R.string.app_name));
        WKPermissions.getInstance().checkPermissions(new WKPermissions.IPermissionResult() {
            @Override
            public void onResult(boolean result) {
                if (result) {
                    startRecord(iConversationContext.getChatActivity());
                }
            }

            @Override
            public void clickResult(boolean isCancel) {
            }
        }, activity, desc, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO);


    }
}

