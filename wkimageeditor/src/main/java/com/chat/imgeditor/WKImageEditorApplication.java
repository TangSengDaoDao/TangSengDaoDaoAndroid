package com.chat.imgeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.EditImgMenu;
import com.chat.imgeditor.core.util.IMGUtils;
import com.chat.imgeditor.file.IMGAssetFileDecoder;
import com.chat.imgeditor.file.IMGDecoder;
import com.chat.imgeditor.file.IMGFileDecoder;

import java.io.File;

public class WKImageEditorApplication {

    private static final int MAX_WIDTH = 1024;
    private static final int MAX_HEIGHT = 1024;

    private static class ImageEditorApplicationBinder {
        final static WKImageEditorApplication instance = new WKImageEditorApplication();
    }

    public static WKImageEditorApplication getInstance() {
        return ImageEditorApplicationBinder.instance;
    }

    public void init() {
        EndpointManager.getInstance().setMethod("edit_img", object -> {
            EditImgMenu menu = (EditImgMenu) object;
            Uri uri = Uri.fromFile(new File(menu.path));
            TRSPictureEditor.setStyle(buildStyle());
            if (menu.context == null) {
                TRSPictureEditor.edit(menu.fragment, menu.isShowSaveDialog, getBitmap(uri, menu.fragment.getContext()), menu.requestCode, new TRSPictureEditor.EditAdapter() {
                    @Override
                    public void onComplete(Bitmap bitmap, String path) {

                    }
                });
            } else {
                TRSPictureEditor.edit(menu.context, menu.isShowSaveDialog, getBitmap(uri, menu.context), new TRSPictureEditor.EditAdapter() {
                    @Override
                    public void onComplete(Bitmap bitmap, String path) {
                        menu.iBack.onBack(bitmap, path);
                    }
                });
            }

            return null;
        });
    }


    private int buildStyle() {
        int style = TRSPictureEditor.ALL_ENABLE;
        style &= ~TRSPictureEditor.ENABLE_ARRAY[0];
        style &= ~TRSPictureEditor.ENABLE_ARRAY[1];
        style &= ~TRSPictureEditor.ENABLE_ARRAY[4];
        return style;
    }


    private Bitmap getBitmap(Uri uri, Context context) {

        if (uri == null) {
            return null;
        }

        IMGDecoder decoder = null;
        Bitmap bitmap;

        String path = uri.getPath();
        if (!TextUtils.isEmpty(path)) {
            switch (uri.getScheme()) {
                case "asset":
                    decoder = new IMGAssetFileDecoder(context, uri);
                    break;
                case "file":
                    decoder = new IMGFileDecoder(uri);
                    break;
            }
        }

        if (decoder == null) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;

        decoder.decode(options);

        if (options.outWidth > MAX_WIDTH) {
            options.inSampleSize = IMGUtils.inSampleSize(Math.round(1f * options.outWidth / MAX_WIDTH));
        }

        if (options.outHeight > MAX_HEIGHT) {
            options.inSampleSize = Math.max(options.inSampleSize,
                    IMGUtils.inSampleSize(Math.round(1f * options.outHeight / MAX_HEIGHT)));
        }

        options.inJustDecodeBounds = false;

        bitmap = decoder.decode(options);
        return bitmap;
    }
}
