package com.chat.imgeditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.EndpointSID;
import com.chat.base.endpoint.entity.ChatChooseContacts;
import com.chat.base.endpoint.entity.ChooseChatMenu;
import com.chat.base.entity.BottomSheetItem;
import com.chat.base.utils.ImageUtils;
import com.chat.base.utils.WKDialogUtils;
import com.chat.base.utils.WKFileUtils;
import com.chat.base.utils.WKReader;
import com.chat.base.utils.WKToastUtils;
import com.chat.imgeditor.core.IMGMode;
import com.chat.imgeditor.core.IMGText;
import com.chat.imgeditor.core.file.IMGAssetFileDecoder;
import com.chat.imgeditor.core.file.IMGDecoder;
import com.chat.imgeditor.core.file.IMGFileDecoder;
import com.chat.imgeditor.core.util.IMGUtils;
import com.google.android.material.snackbar.Snackbar;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKChannel;
import com.xinbida.wukongim.msgmodel.WKImageContent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IMGEditActivity extends IMGEditBaseActivity {

    private static final int MAX_WIDTH = 1024;

    private static final int MAX_HEIGHT = 1024;

    public static final String EXTRA_IMAGE_URI = "IMAGE_URI";

    public static final String EXTRA_IMAGE_SAVE_PATH = "IMAGE_SAVE_PATH";

    @Override
    public void onCreated() {

    }

    @Override
    protected void onStart() {
//        TUIUtils.setFullScreen(this);
        super.onStart();
    }

    @Override
    public Bitmap getBitmap() {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        }
        ImageHolder imageHolder = ImageHolder.getInstance();
        Bitmap bitmap = imageHolder.getBitmap();
        imageHolder.setBitmap(null);
        if (bitmap != null) {
            return bitmap;
        }

        Uri uri = intent.getParcelableExtra(EXTRA_IMAGE_URI);
        if (uri == null) {
            return null;
        }

        IMGDecoder decoder = null;

        String path = uri.getPath();
        String scheme = uri.getScheme();
        if (!TextUtils.isEmpty(path) && scheme != null) {
            switch (scheme) {
                case "asset":
                    decoder = new IMGAssetFileDecoder(this, uri);
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

    @Override
    public void onText(IMGText text) {
        mImgView.addStickerText(text);
    }

    @Override
    public void onModeClick(IMGMode mode) {
        IMGMode cm = mImgView.getMode();
        if (cm == mode) {
            mode = IMGMode.NONE;
        }
        mImgView.setMode(mode);
        updateModeUI();

        if (mode == IMGMode.CLIP) {
            setOpDisplay(OP_CLIP);
        }
    }

    @Override
    public void onUndoClick() {
        IMGMode mode = mImgView.getMode();
        if (mode == IMGMode.DOODLE) {
            mImgView.undoDoodle();
        } else if (mode == IMGMode.MOSAIC) {
            mImgView.undoMosaic();
        } else if (mode == IMGMode.ROUND) {
            mImgView.undoRound();
        } else if (mode == IMGMode.BOX) {
            mImgView.undoBox();
        } else if (mode == IMGMode.ARROW) {
            mImgView.undoArrow();
        }
    }

    @Override
    public void onCancelClick() {
        ImageHolder.getInstance().getEditListener().onCancel();
        finish();
    }

    @Override
    public void onDoneClick() {
       /* String path = getIntent().getStringExtra(EXTRA_IMAGE_SAVE_PATH);
        if (!TextUtils.isEmpty(path)) {
            Bitmap bitmap = mImgView.saveBitmap();
            Intent data = new Intent();
            //获取到拍照成功后返回的Bitmap
            saveBitmap(bitmap, path);
            data.putExtra("take_photo", true);
            data.putExtra("path", path);
            setResult(RESULT_OK, data);
            finish();
        }*/
        Bitmap bitmap = mImgView.saveBitmap();
        String path = WKFileUtils.getInstance().saveBitmap(null, bitmap);

        if (ImageHolder.getInstance().getShowSaveDialog()) {
            showBottomDialog(path, bitmap);
        } else {
            Uri uri = Uri.fromFile(new File(path));
            Intent intent = new Intent();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            setResult(RESULT_OK, intent);
            ImageHolder.getInstance().setEditedBitmap(bitmap, path);
            finish();
        }

    }

    public String saveBitmap(Bitmap bm, String fileAbsolutePath) {
        File f = new File(fileAbsolutePath);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileAbsolutePath;
    }

    @Override
    public void onCancelClipClick() {
        mImgView.cancelClip();
        setOpDisplay(mImgView.getMode() == IMGMode.CLIP ? OP_CLIP : OP_NORMAL);
    }

    @Override
    public void onDoneClipClick() {
        mImgView.doClip();
        setOpDisplay(mImgView.getMode() == IMGMode.CLIP ? OP_CLIP : OP_NORMAL);

    }

    @Override
    public void onResetClipClick() {
        mImgView.resetClip();
    }

    @Override
    public void onRotateClipClick() {
        mImgView.doRotate();
    }

    @Override
    public void onColorChanged(int checkedColor) {
        mImgView.setPenColor(checkedColor);
    }

    /**
     * 编辑图片需要的bitmap保管类
     */

    private void showBottomDialog(String filePath, Bitmap bitmap) {
        List<BottomSheetItem> list = new ArrayList<>();
        list.add(new BottomSheetItem(getString(R.string.forward), R.mipmap.msg_forward, () -> {
            WKImageContent imageContent = new WKImageContent();
            imageContent.localPath = filePath;
            imageContent.width = bitmap.getWidth();
            imageContent.height = bitmap.getHeight();
            EndpointManager.getInstance().invoke(EndpointSID.showChooseChatView, new ChooseChatMenu(new ChatChooseContacts(list1 -> {
                if (WKReader.isNotEmpty(list1)) {
                    for (WKChannel channel : list1) {
                        WKIM.getInstance().getMsgManager().send(imageContent, channel);
                    }
                    ViewGroup viewGroup = (ViewGroup) findViewById(android.R.id.content).getRootView();
                    Snackbar.make(viewGroup, getString(R.string.str_forward), 1000)
                            .setAction("", v1 -> {
                            })
                            .show();
                    finish();
                }
            }), imageContent));

        }));

        list.add(new BottomSheetItem(getString(R.string.save_img), R.mipmap.msg_download, () -> {
            ImageUtils.getInstance().saveBitmap(IMGEditActivity.this, bitmap, true, null);
            WKToastUtils.getInstance().showToast(getString(R.string.saved_album));
            finish();
        }));
       WKDialogUtils.getInstance().showBottomSheet(this,"",false,list);
    }
}
