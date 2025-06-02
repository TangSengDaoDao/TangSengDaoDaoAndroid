package com.chat.moments.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.chat.base.base.WKBaseModel;
import com.chat.base.config.WKApiConfig;
import com.chat.base.config.WKConfig;
import com.chat.base.net.ApiService;
import com.chat.base.net.IRequestResultListener;
import com.chat.base.net.entity.UploadFileUrl;
import com.chat.base.utils.WKMediaFileUtils;
import com.chat.base.utils.WKTimeUtils;

import java.io.File;

/**
 * 2020-12-15 10:59
 * 动态文件上传
 */
public class MomentFileUpload extends WKBaseModel {

    private MomentFileUpload() {

    }

    private static class MomentFileUploadBinder {
        private final static MomentFileUpload fileUpload = new MomentFileUpload();
    }

    public static MomentFileUpload getInstance() {
        return MomentFileUploadBinder.fileUpload;
    }

    public void getMomentFileUploadUrl(String localPath, final IGetUploadFileUrl iGetUploadFileUrl) {
        getMomentUploadUrl(localPath, iGetUploadFileUrl);
    }

    private void getMomentUploadUrl(String localPath, final IGetUploadFileUrl iGetUploadFileUrl) {
        File f = new File(localPath);
        String tempFileName = f.getName();
        String prefix = tempFileName.substring(tempFileName.lastIndexOf(".") + 1);
        Bitmap bitmap = BitmapFactory.decodeFile(localPath);
        if (!WKMediaFileUtils.getInstance().isVideoFileType(localPath)) {
            prefix = "png";
        }
        int w = 0, h = 0;
        if (bitmap != null) {
            w = bitmap.getWidth();
            h = bitmap.getHeight();
        }
        String path = "/" + WKConfig.getInstance().getUid() + "/" + WKTimeUtils.getInstance().getCurrentMills() + "." + prefix + "@" + w + "x" + h;
        request(createService(ApiService.class).getUploadFileUrl(WKApiConfig.baseUrl + "file/upload?type=moment&path=" + path), new IRequestResultListener<UploadFileUrl>() {
            @Override
            public void onSuccess(UploadFileUrl result) {
                iGetUploadFileUrl.onResult(result.url, path);
            }

            @Override
            public void onFail(int code, String msg) {
                iGetUploadFileUrl.onResult(null, path);
            }
        });

    }

    public interface IGetUploadFileUrl {
        void onResult(String url, String path);
    }

    public void getMomentCoverUploadUrl(final IGetUploadFileUrl iGetUploadFileUrl) {
        request(createService(ApiService.class).getUploadFileUrl(WKApiConfig.baseUrl + "file/upload?type=momentcover"), new IRequestResultListener<UploadFileUrl>() {

            @Override
            public void onSuccess(UploadFileUrl result) {
                iGetUploadFileUrl.onResult(result.url, "");
            }

            @Override
            public void onFail(int code, String msg) {
                iGetUploadFileUrl.onResult(null, "");
            }
        });
    }
}
