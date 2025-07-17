package com.chat.sticker.ui.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.chat.base.config.WKApiConfig;
import com.chat.base.config.WKSharedPreferencesUtil;
import com.chat.base.emoji.EmojiColorPickerWindow;
import com.chat.base.emoji.EmojiEntry;
import com.chat.base.emoji.EmojiManager;
import com.chat.base.emoji.MoonUtil;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.glide.GlideRequestOptions;
import com.chat.base.msg.model.WKGifContent;
import com.chat.base.ui.Theme;
import com.chat.base.ui.components.CubicBezierInterpolator;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.LayoutHelper;
import com.chat.base.utils.WKReader;
import com.chat.base.utils.WKTimeUtils;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.base.views.RecyclerAnimationScrollHelper;
import com.chat.base.views.RecyclerListView;
import com.chat.sticker.Const;
import com.chat.sticker.R;
import com.chat.sticker.WKStickerApplication;
import com.chat.sticker.adapter.EmojiAdapter;
import com.chat.sticker.adapter.SickerTabAdapter;
import com.chat.sticker.adapter.StickersGridAdapter;
import com.chat.sticker.db.StickerDBManager;
import com.chat.sticker.entity.EmojiEntity;
import com.chat.sticker.entity.Sticker;
import com.chat.sticker.entity.StickerCategory;
import com.chat.sticker.entity.StickerTab;
import com.chat.sticker.entity.StickerUI;
import com.chat.sticker.msg.StickerContent;
import com.chat.sticker.msg.StickerFormat;
import com.chat.sticker.service.StickerModel;
import com.chat.sticker.touch.OnMovePreviewListener;
import com.chat.sticker.touch.SimpleMovePreviewListener;
import com.chat.sticker.ui.StickerStoreActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EmojiView extends FrameLayout {
    EmojiColorPickerWindow colorPickerView;
    private final int[] location = new int[2];
    private float emojiTouchedX;
    private float emojiTouchedY;


    private int currentPage;
    private final View shadowLine;
    private final FrameLayout bottomTabContainer;
    private final Drawable[] tabIcons;
    private final ArrayList<View> views = new ArrayList<>();
    private final Paint dotPaint;
    private final ViewPager pager;
    private final RecyclerListView stickersGridView;
    private final RecyclerListView emojiGridView;
    private final GridLayoutManager stickersLayoutManager;
    private AnimatorSet bottomTabContainerAnimation;
    private AnimatorSet backspaceButtonAnimation;
    private AnimatorSet stickersButtonAnimation;
    private final ImageView stickerSettingsButton;
    private final ImageView backspaceButton;
    private final RecyclerAnimationScrollHelper scrollHelper;
    private boolean backspacePressed;
    private boolean backspaceOnce;
    private final StickersGridAdapter stickersGridAdapter;
    private final SickerTabAdapter sickerTabAdapter;
    private List<StickerTab> stickerTabList;
    private long lastClickTime = 0L;

    @SuppressLint({"ObsoleteSdkInt", "ClickableViewAccessibility"})
    public EmojiView(@NonNull Context context) {
        super(context);
        colorPickerView = EmojiColorPickerWindow.create(getContext());
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(Theme.colorAccount);
        tabIcons = new Drawable[]{
                Theme.createEmojiIconSelectorDrawable(context, R.mipmap.smiles_tab_smiles, 0xff8c9197, Theme.colorAccount),
                Theme.createEmojiIconSelectorDrawable(context, R.mipmap.smiles_tab_stickers, 0xff8c9197, Theme.colorAccount)
        };
        FrameLayout emojiContainer = new FrameLayout(context);
        views.add(emojiContainer);
        emojiGridView = new RecyclerListView(context);
        emojiGridView.setVerticalFadingEdgeEnabled(true);
        emojiGridView.setFadingEdgeLength(AndroidUtilities.dp(10));
        emojiGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                showBottomTab(dy < 0, true);
            }
        });

        emojiGridView.setOnTouchListener((v, event) -> {
            if (colorPickerView != null) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    if (colorPickerView != null && colorPickerView.isShowing() && !colorPickerView.isCompound()) {
                        colorPickerView.dismiss();
                    }

                    emojiTouchedX = -10000;
                    emojiTouchedY = -10000;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    boolean ignore = false;
                    if (emojiTouchedX != -10000) {
                        if (Math.abs(emojiTouchedX - event.getX()) > AndroidUtilities.getPixelsInCM(0.2f, true) || Math.abs(emojiTouchedY - event.getY()) > AndroidUtilities.getPixelsInCM(0.2f, false)) {
                            emojiTouchedX = -10000;
                            emojiTouchedY = -10000;
                        } else {
                            ignore = true;
                        }
                    }
                    if (!ignore) {
                        getLocationOnScreen(location);
                        float x = location[0] + event.getX();
                        colorPickerView.pickerView.getLocationOnScreen(location);
                        x -= location[0] + AndroidUtilities.dp(3);
                        colorPickerView.onTouchMove((int) x);
                    }
                }

                return colorPickerView == null || colorPickerView.isShowing();
            }
            return false;
        });
        GridLayoutManager emojiLayoutManager = new GridLayoutManager(context, 8);

        emojiGridView.setLayoutManager(emojiLayoutManager);
        emojiContainer.addView(emojiGridView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        EmojiAdapter adapter = new EmojiAdapter(AndroidUtilities.getScreenWidth(), colorPickerView);
        adapter.addChildClickViewIds(R.id.emojiIv);
        adapter.setOnItemChildClickListener((adapter1, view, position) -> {
            EmojiEntity emojiEntity = (EmojiEntity) adapter1.getData().get(position);
            if (emojiEntity != null && emojiEntity.getItemType() == 0) {
                emojiClick(emojiEntity);
            }
        });
        String ids = WKSharedPreferencesUtil.getInstance().getSPWithUID(Const.commonUsedEmojisKey);
        List<EmojiEntity> list = new ArrayList<>();
        if (!TextUtils.isEmpty(ids)) {
            if (ids.contains(",")) {
                String[] idArr = ids.split(",");
                for (String id : idArr) {
                    if (list.size() == 24)
                        continue;
                    EmojiEntry entry = EmojiManager.getInstance().getEmojiEntry(id);
                    if (entry != null) {
                        list.add(new EmojiEntity(entry, 0));
                    }
                }
            } else {
                EmojiEntry entry = EmojiManager.getInstance().getEmojiEntry(ids);
                if (entry != null) {
                    list.add(new EmojiEntity(entry, 0));
                }
            }
            list.add(0, new EmojiEntity(new EmojiEntry("", context.getString(R.string.common_emoji), ""), 1));
            list.add(new EmojiEntity(new EmojiEntry("", context.getString(R.string.all_emoji), ""), 1));
        }

        List<EmojiEntry> normalList = EmojiManager.getInstance().getEmojiWithType("0_");
        for (EmojiEntry entry : normalList) {
            list.add(new EmojiEntity(entry, 0));
        }
        normalList = EmojiManager.getInstance().getEmojiWithType("1_");
        for (EmojiEntry entry : normalList) {
            list.add(new EmojiEntity(entry, 0));
        }
        normalList = EmojiManager.getInstance().getEmojiWithType("2_");
        for (EmojiEntry entry : normalList) {
            list.add(new EmojiEntity(entry, 0));
        }
        list.add(new EmojiEntity(new EmojiEntry("", "", ""), 2));
        emojiGridView.setAdapter(adapter);
        adapter.setList(list);
//        emojiGridView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemClick(View v, int position) {
//                EmojiEntity item = adapter.getData().get(position);
//                if (item.getItemType() != 0) {
//                    return true;
//                }
//                if (item.getEntry().getTag().contains("default")) {
//                    int[] location =new int[2];
//                    int[] l =new int[2];
//                    v.getLocationOnScreen(l);
//                    int distanceToRight =
//                            v.getResources().getDisplayMetrics().widthPixels - (l[0] + v.getWidth());
//                    l[0] = distanceToRight;
//                    colorPickerView.setEmoji(item.getEntry().getTag().replace("_default", "_color"));
//                    colorPickerView.setSelection(0);
//                    int x = 0;
//                    int x1 = 0;
//                    int popupWidth = colorPickerView.getPopupWidth();
//                    int popupHeight = colorPickerView.getPopupHeight();
//                    if (!colorPickerView.isCompound()) {
//                        x =
//                                32 * colorPickerView.getSelection() + AndroidUtilities.dp(4 * colorPickerView.getSelection() - 1f);
//                    }
//                    x1 = x;
//                    if (location[0] - x < AndroidUtilities.dp(5f)) {
//                        x += (location[0] - x) - AndroidUtilities.dp(5f);
//                    } else if (location[0] - x + popupWidth > AndroidUtilities.displaySize.x - AndroidUtilities.dp(
//                            5f
//                    )
//                    ) {
//                        x += (location[0] - x + popupWidth) - (AndroidUtilities.displaySize.x - AndroidUtilities.dp(
//                                5f
//                        ));
//                    }
//
//                    if (l[0] > popupWidth) {
//                        x1 +=   - AndroidUtilities.dp(5f);
//                    } else {
//                        x1 +=   popupWidth - l[0] - v.getWidth() - AndroidUtilities.dp(5f);
//                    }
//                    int xOffset = -x;
//                    int yOffset = Math.min(v.getTop(), 0);
//                    colorPickerView.setupArrow(
//                            (int) (AndroidUtilities.dp(( AndroidUtilities.isTablet()? 30 : 22)) + x1 + AndroidUtilities.dpf2(
//                                    0.5f
//                            ))
//                    );
//                    colorPickerView.setFocusable(true);
//                    colorPickerView.showAsDropDown(
//                            v,
//                            xOffset,
//                            -v.getMeasuredHeight() - popupHeight + (v.getMeasuredHeight() - 32) / 2 - yOffset
//                    );
//                    EndpointManager.getInstance().invoke("emoji_color_popup_show", true);
//                }
//                return false;
//            }
//        });
        emojiLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (list.get(position).getItemType() == 0) {
                    return 1;
                }
                return 8;
            }
        });
        FrameLayout stickersContainer = new FrameLayout(context);
        stickersGridView = new RecyclerListView(context);
        stickersGridView.setVerticalFadingEdgeEnabled(true);
        stickersGridView.setFadingEdgeLength(AndroidUtilities.dp(10));
        stickersGridView.addOnItemTouchListener(new SimpleMovePreviewListener(stickersGridView, new OnMovePreviewListener() {
            @Override
            public void onPreview(View childView, int childPosition) {
                StickerUI stickerUI = stickersGridAdapter.getData().get(childPosition);
                if (stickerUI != null && stickerUI.getItemType() != 0)
                    showPreviewAlert(context, stickerUI);
                childView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            }

            @Override
            public void onCancelPreview() {
                if (linearLayout != null) {
                    ViewGroup mRootView = ((Activity) context).findViewById(android.R.id.content);
                    mRootView.removeView(linearLayout);
                    linearLayout = null;
                    textView = null;
                }
            }
        }));

        stickersGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                showBottomTab(dy < 0, true);
                int firstVisibleItem = stickersLayoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItem == RecyclerView.NO_POSITION) {
                    return;
                }
                StickerUI stickerUI = stickersGridAdapter.getData().get(firstVisibleItem);
                int index = 0;
                for (int i = 0, size = stickerTabList.size(); i < size; i++) {
                    if (stickerTabList.get(i).getCategory().equals(stickerUI.getCategory())) {
                        index = i;
                        break;
                    }
                }
                sickerTabAdapter.setSelect(index);
            }
        });

        stickersGridAdapter = new StickersGridAdapter();
        getStickers(context);
        stickersGridAdapter.addChildClickViewIds(R.id.stickerView);
        stickersGridAdapter.setOnItemChildClickListener((adapter12, view, position) -> {
            long nowTime = WKTimeUtils.getInstance().getCurrentMills();
            if (nowTime - lastClickTime <= 500) {
                return;
            }
            lastClickTime = WKTimeUtils.getInstance().getCurrentMills();
            StickerUI stickerUI = (StickerUI) adapter12.getData().get(position);
            if (stickerUI == null || stickerUI.getSticker() == null) return;
            if (stickerUI.getItemType() == 1) {
                StickerContent vectorSticker = new StickerContent();
                vectorSticker.url = stickerUI.getSticker().getPath();
                vectorSticker.placeholder = stickerUI.getSticker().getPlaceholder();
                vectorSticker.category = stickerUI.getSticker().getCategory();
                vectorSticker.content = stickerUI.getSticker().getSearchable_word();
                WKStickerApplication.Companion.getInstance().sendMsg(vectorSticker);
            } else if (stickerUI.getItemType() == 2) {
                WKGifContent gifContent = new WKGifContent();
                gifContent.height = stickerUI.getSticker().getHeight();
                gifContent.width = stickerUI.getSticker().getWidth();
                gifContent.url = stickerUI.getSticker().getPath();
                gifContent.category = stickerUI.getSticker().getCategory();
                gifContent.title = stickerUI.getSticker().getTitle();
                gifContent.placeholder = stickerUI.getSticker().getPlaceholder();
                gifContent.format = stickerUI.getSticker().getFormat();
                WKStickerApplication.Companion.getInstance().sendMsg(gifContent);
//                WKStickerApplication.Companion.getInstance().iConversationContext.sendMessage(gifContent);
            }
        });
        stickersGridView.setAdapter(stickersGridAdapter);
        stickersLayoutManager = new GridLayoutManager(context, 5);
        stickersLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position < stickersGridAdapter.getData().size() - 1 && stickersGridAdapter.getData().get(position).getItemType() == 0) {
                    return 5;
                }
                return 1;
            }
        });
        stickersGridView.setLayoutManager(stickersLayoutManager);
        views.add(stickersContainer);
        stickersContainer.addView(stickersGridView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP, 0, 48, 0, 0));
        scrollHelper = new RecyclerAnimationScrollHelper(stickersGridView, stickersLayoutManager);
        pager = new ViewPager(context) {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(canScrollHorizontally(-1));
                }
                return super.onInterceptTouchEvent(ev);
            }

            @Override
            public void setCurrentItem(int item, boolean smoothScroll) {
                // startStopVisibleGifs(item == 1);
                if (item == getCurrentItem()) {
                    if (item == 0) {
                        emojiGridView.smoothScrollToPosition(0);
                    } else {
                        stickersGridView.smoothScrollToPosition(1);
                    }
                    return;
                }
                super.setCurrentItem(item, smoothScroll);
            }
        };
        EndpointManager.getInstance().setMethod("emoji_color_popup_show", object -> {
            boolean isShow = (boolean) object;
           // emojiGridView.requestDisallowInterceptTouchEvent(isShow);
            pager.requestDisallowInterceptTouchEvent(isShow);
            if (!isShow){
                String emoji = colorPickerView.pickerView.getEmoji();
                if (TextUtils.isEmpty(emoji)){
                    return null;
                }
                int index = colorPickerView.getSelection();
                if (index == 0) {
                    emoji = emoji.replaceAll("_color", "_default");
                } else {
                    emoji = emoji + "_" + index;
                }
                EmojiEntry entry = EmojiManager.getInstance().getEmojiWithTag(emoji);
                if (entry != null) {
                    emojiClick(new EmojiEntity(entry, 1));
                }
                colorPickerView.setSelection(-1);
            }
            return null;
        });
        pager.setAdapter(new EmojiPagesAdapter());
//        pager.setUserInputEnabled(false);
        backspaceButton = new AppCompatImageView(context) {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    backspacePressed = true;
                    backspaceOnce = false;
                    postBackspaceRunnable(350);
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
                    backspacePressed = false;
                    if (!backspaceOnce) {
                        EndpointManager.getInstance().invoke("emoji_click", "");
                    }
                }
                super.onTouchEvent(event);
                return true;
            }
        };
        backspaceButton.setImageResource(R.mipmap.smiles_tab_clear);
        backspaceButton.setColorFilter(new PorterDuffColorFilter(0xff8c9197, PorterDuff.Mode.MULTIPLY));
        backspaceButton.setScaleType(ImageView.ScaleType.CENTER);
        backspaceButton.setFocusable(true);

        bottomTabContainer = new FrameLayout(context) {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return super.onInterceptTouchEvent(ev);
            }
        };
        shadowLine = new View(context);
        shadowLine.setBackgroundColor(0x12000000);
        bottomTabContainer.addView(shadowLine, new FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, AndroidUtilities.getShadowHeight()));
        View bottomTabContainerBackground = new View(context);
        bottomTabContainerBackground.setBackgroundColor(ContextCompat.getColor(context, R.color.layoutColor));
        bottomTabContainer.addView(bottomTabContainerBackground, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtilities.dp(44), Gravity.START | Gravity.BOTTOM));


        bottomTabContainer.addView(backspaceButton, LayoutHelper.createFrame(52, 44, Gravity.BOTTOM | Gravity.END));
        if (Build.VERSION.SDK_INT >= 21) {
            backspaceButton.setBackground(Theme.createSelectorDrawable(Theme.getPressedColor()));
        }
        // 添加设置
        stickerSettingsButton = new ImageView(context);
        stickerSettingsButton.setImageResource(R.mipmap.smiles_tab_settings);
        stickerSettingsButton.setColorFilter(new PorterDuffColorFilter(0xff8c9197, PorterDuff.Mode.MULTIPLY));
        stickerSettingsButton.setScaleType(ImageView.ScaleType.CENTER);
        stickerSettingsButton.setFocusable(true);
        if (Build.VERSION.SDK_INT >= 21) {
            stickerSettingsButton.setBackground(Theme.createSelectorDrawable(Theme.getPressedColor()));
        }
        showStickerSettingsButton(false, false);
//        stickerSettingsButton.setVisibility(GONE);
        bottomTabContainer.addView(stickerSettingsButton, LayoutHelper.createFrame(52, 44, Gravity.BOTTOM | Gravity.END));
        SingleClickUtil.onSingleClick(stickerSettingsButton, v -> {
            Intent intent = new Intent(context, StickerStoreActivity.class);
            context.startActivity(intent);
        });

        PagerSlidingTabStrip typeTabs = new PagerSlidingTabStrip(context);
        typeTabs.setViewPager(pager);
        typeTabs.setShouldExpand(false);
        typeTabs.setIndicatorHeight(0);
        typeTabs.setUnderlineHeight(0);
        typeTabs.setTabPaddingLeftRight(AndroidUtilities.dp(10));
        bottomTabContainer.addView(typeTabs, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 44, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM));
        typeTabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                showBottomTab(true, true);
            }

            @Override
            public void onPageSelected(int position) {
                saveNewPage();
                showBackspaceButton(position == 0);
                showStickerSettingsButton(position == 1, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (views.size() == 1 && typeTabs.getVisibility() == VISIBLE) {
            typeTabs.setVisibility(INVISIBLE);
        } else if (views.size() != 1 && typeTabs.getVisibility() != VISIBLE) {
            typeTabs.setVisibility(VISIBLE);
        }

        sickerTabAdapter = new SickerTabAdapter();
        sickerTabAdapter.addChildClickViewIds(R.id.stickerView);
        sickerTabAdapter.setOnItemChildClickListener((adapter13, view, position) -> {
            StickerTab stickerTab = sickerTabAdapter.getData().get(position);
            if (stickerTab != null) {
                sickerTabAdapter.setSelect(position);
                int index = 0;
                for (int i = 0, size = stickersGridAdapter.getData().size(); i < size; i++) {
                    if (stickersGridAdapter.getData().get(i).getCategory().equals(stickerTab.getCategory())) {
                        index = i;
                        break;
                    }
                }
                scrollHelper.setScrollDirection(stickersLayoutManager.findFirstVisibleItemPosition() < index ? RecyclerAnimationScrollHelper.SCROLL_DIRECTION_DOWN : RecyclerAnimationScrollHelper.SCROLL_DIRECTION_UP);
                scrollHelper.scrollToPosition(index, 0, false, true);
            }
        });
        RecyclerView stickerTabRecyclerView = new RecyclerView(context);
        stickerTabRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        stickerTabRecyclerView.setAdapter(sickerTabAdapter);
        sickerTabAdapter.setList(stickerTabList);

        FrameLayout tabLayout = new FrameLayout(context);
        tabLayout.addView(stickerTabRecyclerView, 0, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.START | Gravity.TOP));
        stickersContainer.addView(tabLayout, 0, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.START | Gravity.TOP));

        addView(bottomTabContainer, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtilities.dp(44) + AndroidUtilities.getShadowHeight(), Gravity.START | Gravity.BOTTOM));
        addView(pager, 0, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.START | Gravity.TOP));
        bottomTabContainer.setTag(1);
        showBottomTab(true, false);
//        updateStickerTabsPosition();

        EndpointManager.getInstance().setMethod("refresh_custom_sticker", object -> {
            getStickers(context);
            return null;
        });

        EndpointManager.getInstance().setMethod("", EndpointCategory.wkRefreshStickerCategory, object -> {
            getStickers(context);
            sickerTabAdapter.setList(stickerTabList);
            return null;
        });
    }

    private class EmojiPagesAdapter extends PagerAdapter implements PagerSlidingTabStrip.IconTabProvider {

        public void destroyItem(ViewGroup viewGroup, int position, @NonNull Object object) {
            viewGroup.removeView(views.get(position));
        }

        @Override
        public boolean canScrollToTab(int position) {
            return true;
        }

        public int getCount() {
            return views.size();
        }

        public Drawable getPageIconDrawable(int position) {
            return tabIcons[position];
        }

        public CharSequence getPageTitle(int position) {
            return switch (position) {
                case 0 -> "emoji";
                case 1 -> "sticker";
                default -> null;
            };
        }

        @Override
        public void customOnDraw(Canvas canvas, int position) {
            // 绘制红点
            if (position == 2) {
                int x = canvas.getWidth() / 2 + AndroidUtilities.dp(4 + 5);
                int y = canvas.getHeight() / 2 - AndroidUtilities.dp(13 - 5);
                canvas.drawCircle(x, y, AndroidUtilities.dp(5), dotPaint);
            }
        }

        @NonNull
        public Object instantiateItem(ViewGroup viewGroup, int position) {
            View view = views.get(position);
            viewGroup.addView(view);
            return view;
        }

        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }

    private void showBottomTab(boolean show, boolean animated) {
        if (show && bottomTabContainer.getTag() == null || !show && bottomTabContainer.getTag() != null) {
            return;
        }
        if (bottomTabContainerAnimation != null) {
            bottomTabContainerAnimation.cancel();
            bottomTabContainerAnimation = null;
        }
        bottomTabContainer.setTag(show ? null : 1);
        if (animated) {
            bottomTabContainerAnimation = new AnimatorSet();
            bottomTabContainerAnimation.playTogether(
                    ObjectAnimator.ofFloat(bottomTabContainer, View.TRANSLATION_Y, show ? 0 : AndroidUtilities.dp(54)),
                    ObjectAnimator.ofFloat(shadowLine, View.TRANSLATION_Y, show ? 0 : AndroidUtilities.dp(49)));
            bottomTabContainerAnimation.setDuration(200);
            bottomTabContainerAnimation.setInterpolator(CubicBezierInterpolator.EASE_OUT);
            bottomTabContainerAnimation.start();
        } else {
            bottomTabContainer.setTranslationY(show ? 0 : AndroidUtilities.dp(54));
            shadowLine.setTranslationY(show ? 0 : AndroidUtilities.dp(49));
        }
    }

    private void saveNewPage() {
        if (pager == null) {
            return;
        }
        int newPage;
        int currentItem = pager.getCurrentItem();
        if (currentItem == 0) {
            newPage = 1;
        } else {
            newPage = 0;
        }
        if (currentPage != newPage) {
            currentPage = newPage;
        }
    }

    private void showBackspaceButton(boolean show) {
        if (show && backspaceButton.getTag() == null || !show && backspaceButton.getTag() != null) {
            return;
        }
        if (backspaceButtonAnimation != null) {
            backspaceButtonAnimation.cancel();
            backspaceButtonAnimation = null;
        }
        backspaceButton.setTag(show ? null : 1);
        if (show) {
            backspaceButton.setVisibility(VISIBLE);
        }
        backspaceButtonAnimation = new AnimatorSet();
        backspaceButtonAnimation.playTogether(ObjectAnimator.ofFloat(backspaceButton, View.ALPHA, show ? 1.0f : 0.0f),
                ObjectAnimator.ofFloat(backspaceButton, View.SCALE_X, show ? 1.0f : 0.0f),
                ObjectAnimator.ofFloat(backspaceButton, View.SCALE_Y, show ? 1.0f : 0.0f));
        backspaceButtonAnimation.setDuration(200);
        backspaceButtonAnimation.setInterpolator(CubicBezierInterpolator.EASE_OUT);
        backspaceButtonAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!show) {
                    backspaceButton.setVisibility(INVISIBLE);
                }
            }
        });
        backspaceButtonAnimation.start();
    }

    private void showStickerSettingsButton(boolean show, boolean animated) {
        if (stickerSettingsButton == null) {
            return;
        }
        if (show && stickerSettingsButton.getTag() == null || !show && stickerSettingsButton.getTag() != null) {
            return;
        }
        if (stickersButtonAnimation != null) {
            stickersButtonAnimation.cancel();
            stickersButtonAnimation = null;
        }
        stickerSettingsButton.setTag(show ? null : 1);
        if (animated) {
            if (show) {
                stickerSettingsButton.setVisibility(VISIBLE);
            }
            stickersButtonAnimation = new AnimatorSet();
            stickersButtonAnimation.playTogether(ObjectAnimator.ofFloat(stickerSettingsButton, View.ALPHA, show ? 1.0f : 0.0f),
                    ObjectAnimator.ofFloat(stickerSettingsButton, View.SCALE_X, show ? 1.0f : 0.0f),
                    ObjectAnimator.ofFloat(stickerSettingsButton, View.SCALE_Y, show ? 1.0f : 0.0f));
            stickersButtonAnimation.setDuration(200);
            stickersButtonAnimation.setInterpolator(CubicBezierInterpolator.EASE_OUT);
            stickersButtonAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!show) {
                        stickerSettingsButton.setVisibility(INVISIBLE);
                    }
                }
            });
            stickersButtonAnimation.start();
        } else {
            stickerSettingsButton.setAlpha(show ? 1.0f : 0.0f);
            stickerSettingsButton.setScaleX(show ? 1.0f : 0.0f);
            stickerSettingsButton.setScaleY(show ? 1.0f : 0.0f);
            stickerSettingsButton.setVisibility(show ? VISIBLE : INVISIBLE);
        }
    }

    private void postBackspaceRunnable(final int time) {
        AndroidUtilities.runOnUIThread(() -> {
            if (!backspacePressed) {
                return;
            }
            backspaceOnce = true;
            postBackspaceRunnable(Math.max(50, time - 100));
        }, time);
    }

    private void emojiClick(EmojiEntity emojiEntity) {
        EndpointManager.getInstance().invoke("emoji_click", emojiEntity.getEntry().getText());
        String usedIndexList = WKSharedPreferencesUtil.getInstance().getSPWithUID(Const.commonUsedEmojisKey);
        String tempIndexList = "";
        if (!TextUtils.isEmpty(usedIndexList)) {
            if (usedIndexList.contains(",")) {
                String[] strings = usedIndexList.split(",");
                for (String string : strings) {
                    if (!TextUtils.isEmpty(string) && !string.contains(emojiEntity.getEntry().getText())) {
                        if (TextUtils.isEmpty(tempIndexList)) {
                            tempIndexList = string;
                        } else {
                            tempIndexList = tempIndexList + "," + string;
                        }
                    }
                }
            }
        }
        tempIndexList = emojiEntity.getEntry().getText() + "," + tempIndexList;
        WKSharedPreferencesUtil.getInstance().putSPWithUID(Const.commonUsedEmojisKey, tempIndexList);
    }

    private void getStickers(Context context) {
        ArrayList<StickerCategory> categories = StickerDBManager.Companion.getInstance().getUserStickerCategory();
        List<StickerUI> stickerUIList = new ArrayList<>();
        stickerTabList = new ArrayList<>();
        List<Sticker> customList = StickerDBManager.Companion.getInstance().getUserCustomSticker();
        if (WKReader.isNotEmpty(customList)) {
            stickerUIList.add(new StickerUI(0, context.getString(R.string.favorite), "favorite", null));
            stickerTabList.add(new StickerTab(R.mipmap.emoji_tabs_faves, "", "", "favorite", context.getString(R.string.favorite)));
            for (Sticker sticker : customList) {
                if (!TextUtils.isEmpty(sticker.getFormat()) && sticker.getFormat().equals(StickerFormat.getLim())) {
                    stickerUIList.add(new StickerUI(1, sticker.getTitle(), "favorite", sticker));
                } else
                    stickerUIList.add(new StickerUI(2, sticker.getTitle(), "favorite", sticker));
            }
        }
        for (StickerCategory category : categories) {
            stickerTabList.add(new StickerTab(0, category.getCover(), category.getCover_lim(), category.getCategory(), category.getTitle()));
            stickerUIList.add(new StickerUI(0, category.getTitle(), category.getCategory(), null));
            List<Sticker> stickers = StickerDBManager.Companion.getInstance().getStickerWithCategory(category.getCategory());
            for (Sticker sticker : stickers) {
                if (!TextUtils.isEmpty(sticker.getFormat()) && sticker.getFormat().equals(StickerFormat.getLim())) {
                    stickerUIList.add(new StickerUI(1, sticker.getTitle(), sticker.getCategory(), sticker));
                } else
                    stickerUIList.add(new StickerUI(2, sticker.getTitle(), sticker.getCategory(), sticker));
            }
        }
        stickersGridAdapter.setList(stickerUIList);
    }

    FrameLayout linearLayout;
    TextView textView;

    private void showPreviewAlert(Context context, StickerUI stickerUI) {
        ViewGroup mRootView = ((Activity) context).findViewById(android.R.id.content);
        boolean isAdd = false;
        if (linearLayout == null) {
            isAdd = true;
            linearLayout = new FrameLayout(context);
        }
        String searchableWord = "";
        StickerView stickerView = new StickerView(context);

        if (stickerUI.getItemType() == 1) {
            if (stickerUI.getSticker() != null) {
                searchableWord = stickerUI.getSticker().getSearchable_word();
                stickerView.showSticker(stickerUI.getSticker().getPath(), stickerUI.getSticker().getPlaceholder(), 500, true);
            }
        } else if (stickerUI.getItemType() == 2) {
            if (stickerUI.getSticker() != null) {
                String localPath = new StickerModel().getLocalPath(stickerUI.getSticker().getPath());
                File file = new File(localPath);
                if (file.exists()) {
                    Glide.with(context).asGif().load(file)
                            .apply(GlideRequestOptions.getInstance().normalRequestOption())
                            .into(stickerView.getImageView());
                } else {
                    Glide.with(context).asGif().load(WKApiConfig.getShowUrl(stickerUI.getSticker().getPath()))
                            .apply(GlideRequestOptions.getInstance().normalRequestOption())
                            .into(stickerView.getImageView());
                }
                stickerView.getImageView().getLayoutParams().width = AndroidUtilities.dp(150);
                stickerView.getImageView().getLayoutParams().height = AndroidUtilities.dp(150);
            }
        }
        linearLayout.removeAllViews();
        if (textView == null)
            textView = new TextView(context);
        if (!TextUtils.isEmpty(searchableWord)) {
            MoonUtil.identifyFaceExpression(
                    context,
                    textView,
                    searchableWord,
                    MoonUtil.DEF_SCALE
            );
        } else {
            textView.setText(searchableWord);
        }
        textView.setTextColor(ContextCompat.getColor(context, R.color.colorDark));
        linearLayout.setBackgroundColor(0x50eeeeee);
        linearLayout.addView(stickerView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 100, 0, 0));
        linearLayout.addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 280, 0, 0));
        linearLayout.setOnClickListener(view -> {
            mRootView.removeView(linearLayout);
            textView = null;
            linearLayout = null;
        });
        if (isAdd)
            mRootView.addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
    }

}
