package com.chat.imgeditor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.fragment.app.Fragment;


/**
 * 入口类
 * 直接使用edit方法传入需要编辑的bitmap
 * 最后在editListener中的onComplete方法获取编辑后的图片
 * 框架不再保存图片到设备上。
 */
public class TRSPictureEditor {
    public static final int BOX_ENABLE = 0x00000001;//方形选择框
    public static final int CIRCLE_ENABLE = 0x00000002;//圆形选择框
    public static final int TXT_ENABLE = 0x00000004;//文字
    public static final int PAINT_ENABLE = 0x00000010;//画笔
    public static final int ARROW_ENABLE = 0x00000020;//箭头
    public static final int MOSAIC_ENABLE = 0x00000040;//马赛克
    public static final int CLIP_ENABLE = 0x00000100;//裁剪

    public static final int ALL_ENABLE = BOX_ENABLE | CIRCLE_ENABLE | TXT_ENABLE | PAINT_ENABLE | ARROW_ENABLE | MOSAIC_ENABLE | CLIP_ENABLE;
    /**
     * 定义开启的功能
     */
    private static int style = ALL_ENABLE;

    public static final int[] ENABLE_ARRAY = new int[]{BOX_ENABLE, CIRCLE_ENABLE, TXT_ENABLE, PAINT_ENABLE, ARROW_ENABLE, MOSAIC_ENABLE, CLIP_ENABLE};

    public static int getStyle() {
        return style;
    }

    public static void setStyle(int style) {
        TRSPictureEditor.style = style;
    }

    public static void disable(int mask) {
        TRSPictureEditor.style &= ~mask;
    }

    public static void enable(int mask) {
        TRSPictureEditor.style &= mask;
    }


    public static void edit(Context context,boolean isShowSaveDialog, Bitmap bitmap, EditListener editListener) {
        if (editListener == null) {
            return;
        }
        if (bitmap == null) {
            editListener.onError(new RuntimeException("图片为空"));
            return;
        }
        if (context == null) {
            editListener.onError(new RuntimeException("context为空"));
            return;
        }
        ImageHolder.getInstance().reset();
        ImageHolder.getInstance().setEditListener(editListener);
        ImageHolder.getInstance().setBitmap(bitmap);
        ImageHolder.getInstance().setShowSaveDialog(isShowSaveDialog);
        context.startActivity(new Intent(context, IMGEditActivity.class));
    }

    public static void edit(Fragment context,boolean isShowSaveDialog, Bitmap bitmap, int requestCode, EditListener editListener) {
        if (editListener == null) {
            return;
        }
        if (bitmap == null) {
            editListener.onError(new RuntimeException("图片为空"));
            return;
        }
        if (context == null) {
            editListener.onError(new RuntimeException("context为空"));
            return;
        }
        ImageHolder.getInstance().reset();
        ImageHolder.getInstance().setEditListener(editListener);
        ImageHolder.getInstance().setBitmap(bitmap);
        ImageHolder.getInstance().setShowSaveDialog(isShowSaveDialog);
        context.startActivityForResult(new Intent(context.getContext(), IMGEditActivity.class), requestCode);
    }

    public interface EditListener {
        void onCancel();

        void onComplete(Bitmap bitmap,String path);

        void onError(Throwable throwable);
        //失败
    }

    /**
     * 一个空的实现，方便调用者只实现部分方法
     */
    public static class EditAdapter implements EditListener {

        @Override
        public void onCancel() {

        }

        @Override
        public void onComplete(Bitmap bitmap,String path) {

        }


        @Override
        public void onError(Throwable throwable) {

        }
    }
}
