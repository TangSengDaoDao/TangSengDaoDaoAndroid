package com.chat.video;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.chat.base.ui.Theme;
import com.chat.base.utils.ActManagerUtils;
import com.chat.base.utils.WKFileUtils;
import com.chat.base.utils.systembar.WKStatusBarUtils;
import com.chat.video.camera.JCameraView;
import com.chat.video.camera.listener.ClickListener;
import com.chat.video.camera.listener.JCameraListener;

import java.io.File;
import java.io.IOException;

/**
 * 2020-03-10 19:56
 * 录制页面
 */
public class RecordingActivity extends AppCompatActivity {
    JCameraView jCameraView;
//    private boolean granted = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.act_recording_layout);
        setStatusBarColor();
        //设置视频保存路径
        jCameraView = findViewById(R.id.jCameraView);
        jCameraView.setSaveVideoPath(getExternalFilesDir("video").getPath() + File.separator + "wkim");
        initListener();

//        jCameraView.setSaveVideoPath(Objects.requireNonNull(getExternalFilesDir("video")).getPath() + File.separator + "wkim");
        ActManagerUtils.getInstance().addActivity(this);
    }

    private void setStatusBarColor() {
        Window window = getWindow();
        if (window == null || getStatusBarColor() == -1) return;
        WKStatusBarUtils.transparentStatusBar(window);
        WKStatusBarUtils.setStatusBarColor(window, ContextCompat.getColor(this, getStatusBarColor()), 0);
        if (!Theme.getDarkModeStatus(this))
            WKStatusBarUtils.setDarkMode(window);
        else WKStatusBarUtils.setLightMode(window);
    }

    private int getStatusBarColor() {
        return R.color.transparent;
    }

    private void initListener() {
        //JCameraView监听
        jCameraView.setJCameraLisenter(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                String path = WKFileUtils.getInstance().saveBitmap("JCamera", bitmap);
                WKVideoApplication.getInstance().setRecordingResult(0, path, "", 0);
                finish();
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                String path = WKFileUtils.getInstance().saveBitmap("JCamera", firstFrame);
                File file = new File(url);
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(file.getPath());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                long time = mediaPlayer.getDuration();
                WKVideoApplication.getInstance().setRecordingResult(time / 1000, path, url, file.length());
                finish();
            }
        });
        jCameraView.setLeftClickListener(this::finish);
        jCameraView.setRightClickListener(new ClickListener() {
            @Override
            public void onClick() {

            }
        });
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        //全屏显示
//        if (Build.VERSION.SDK_INT >= 19) {
//            View decorView = getWindow().getDecorView();
//            decorView.setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        } else {
//            View decorView = getWindow().getDecorView();
//            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
//            decorView.setSystemUiVisibility(option);
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            jCameraView.onResume();
        }catch (RuntimeException e){
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            jCameraView.onPause();
        }catch (RuntimeException e){
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        WKVideoApplication.getInstance().clearConversationContext();
        ActManagerUtils.getInstance().removeActivity(this);
    }
}
