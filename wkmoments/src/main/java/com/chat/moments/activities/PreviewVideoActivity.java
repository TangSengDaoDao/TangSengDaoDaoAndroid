package com.chat.moments.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.chat.base.act.GSYBaseActivityDetail;
import com.chat.base.act.VideoPlayer;
import com.chat.base.glide.GlideUtils;
import com.chat.base.utils.WKDialogUtils;
import com.chat.base.utils.WKToastUtils;
import com.chat.moments.R;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;

/**
 * 2020-11-10 18:45
 * 视频预览
 */
public class PreviewVideoActivity extends GSYBaseActivityDetail<VideoPlayer> {

    VideoPlayer detailPlayer;
    String playUrl;
    String coverImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.act_preview_video_layout);

        detailPlayer = findViewById(R.id.player);
        //增加title
        detailPlayer.getTitleTextView().setVisibility(View.GONE);
        detailPlayer.getBackButton().setVisibility(View.GONE);
        initVideo();
        initVideoBuilderMode();
        detailPlayer.startPlayLogic();
        ImageView imageView = findViewById(R.id.titleRightIv);
        imageView.setImageResource(R.mipmap.ic_ab_delete);

        findViewById(R.id.titleRightLayout).setOnClickListener(v -> WKDialogUtils.getInstance().showDialog(PreviewVideoActivity.this, getString(R.string.delete_video), getString(R.string.delete_video_tips), true, "", getString(R.string.moments_delete), 0, ContextCompat.getColor(PreviewVideoActivity.this, R.color.red), index -> {
            if (index == 1) {
                Intent intent = new Intent();
                intent.putExtra("isDelete", true);
                setResult(RESULT_OK, intent);
                finish();
            }
        }));
        findViewById(R.id.backIv).setOnClickListener(v -> finish());
    }


    @Override
    public void initVideo() {
        super.initVideo();

        coverImg = getIntent().getStringExtra("coverImg");
        String url = getIntent().getStringExtra("path");
        if (TextUtils.isEmpty(url)) {
            WKToastUtils.getInstance().showToast(getString(com.chat.base.R.string.video_deleted));
            finish();
            return;
        }
        playUrl = url;
        if (!url.startsWith("HTTP") && !url.startsWith("http")) {
            playUrl = "file:///" + url;
        }
    }

    @Override
    public VideoPlayer getGSYVideoPlayer() {
        return detailPlayer;
    }

    @Override
    public GSYVideoOptionBuilder getGSYVideoOptionBuilder() {
        //内置封面可参考SampleCoverVideo
        ImageView imageView = new ImageView(this);
        ViewCompat.setTransitionName(detailPlayer, "coverIv");
        GlideUtils.getInstance().showImg(this, coverImg, imageView);
        return new GSYVideoOptionBuilder()
                .setThumbImageView(imageView)
                .setUrl(playUrl)
                .setCacheWithPlay(false)
                .setVideoTitle("")
                .setIsTouchWiget(true)
                //.setAutoFullWithSize(true)
                .setRotateViewAuto(false)
                .setLockLand(false)
                .setShowFullAnimation(false)//打开动画
                .setNeedLockFull(true)
                .setLooping(true)
                .setSeekRatio(1);
    }

    @Override
    public void clickForFullScreen() {

    }


    /**
     * 是否启动旋转横屏，true表示启动
     */
    @Override
    public boolean getDetailOrientationRotateAuto() {
        return true;
    }

}
