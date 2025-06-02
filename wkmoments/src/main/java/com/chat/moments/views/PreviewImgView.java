package com.chat.moments.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.chat.base.ui.Theme;
import com.chat.base.utils.WKDialogUtils;
import com.chat.base.utils.WKReader;
import com.chat.moments.R;
import com.lxj.xpopup.core.ImageViewerPopupView;

import java.util.List;

/**
 * 2020-11-11 16:24
 * 预览图片
 */
@SuppressLint("ViewConstructor")
public class PreviewImgView extends ImageViewerPopupView {
    private final Context context;
    private final List<Object> imgList;
    private final IDeleteImg iDeleteImg;
    private final int position;

    public PreviewImgView(@NonNull Context context, int position, @NonNull List<Object> imgList, @NonNull final IDeleteImg iDeleteImg) {
        super(context);
        this.context = context;
        this.imgList = imgList;
        this.iDeleteImg = iDeleteImg;
        this.position = position;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        findViewById(R.id.backLayout).setOnClickListener(view -> dismiss());
        TextView titleTv = findViewById(R.id.titleCenterTv);
        titleTv.setText(String.format("%s/%s", position + 1, imgList.size()));
        ImageView imageView = findViewById(R.id.deleteIv);
        Theme.setColorFilter(context, imageView, R.color.popupTextColor);
        ImageView backIV = findViewById(R.id.backIv);
        Theme.setColorFilter(context, backIV, R.color.colorDark);
        Theme.setPressedBackground(findViewById(R.id.backLayout));
        imageView.setOnClickListener(view -> WKDialogUtils.getInstance().showDialog(context, context.getString(R.string.delete_img), context.getString(R.string.delete_img_tips), true,"", context.getString(R.string.base_delete), 0, ContextCompat.getColor(context, R.color.red), index -> {
            if (index == 1) {
                iDeleteImg.onDelete(imgList.get(pager.getCurrentItem()));
                int lastIndex = pager.getCurrentItem() - 1;
                imgList.remove(pager.getCurrentItem());
                if (lastIndex < 0) {
                    lastIndex = 0;
                }
                if (WKReader.isEmpty(imgList)) {
                    dismiss();
                    return;
                }
                PhotoViewAdapter adapter = new PhotoViewAdapter();
                setImageUrls(imgList);
                pager.setAdapter(adapter);
                pager.setCurrentItem(lastIndex);
                titleTv.setText(String.format("%s/%s", lastIndex + 1, imgList.size()));
            }
        }));
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                titleTv.setText(String.format("%s/%s", position + 1, imgList.size()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.preview_img_layout;
    }

    public interface IDeleteImg {
        void onDelete(Object path);
    }
}
