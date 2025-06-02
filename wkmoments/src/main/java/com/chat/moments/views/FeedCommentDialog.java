package com.chat.moments.views;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.chat.base.config.WKConstants;
import com.chat.base.config.WKSharedPreferencesUtil;
import com.chat.base.emoji.EmojiAdapter;
import com.chat.base.emoji.EmojiEntry;
import com.chat.base.emoji.EmojiManager;
import com.chat.base.emoji.MoonUtil;
import com.chat.base.ui.Theme;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.SoftKeyboardUtils;
import com.chat.base.utils.systembar.WKStatusBarUtils;
import com.chat.moments.R;
import com.effective.android.panel.PanelSwitchHelper;
import com.effective.android.panel.interfaces.listener.OnPanelChangeListener;
import com.effective.android.panel.utils.PanelUtil;
import com.effective.android.panel.view.content.LinearContentContainer;
import com.effective.android.panel.view.panel.IPanelView;
import com.effective.android.panel.view.panel.PanelView;
import com.effective.android.panel.window.PanelDialog;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FeedCommentDialog extends PanelDialog implements DialogInterface.OnKeyListener {

    private static final String TAG = FeedCommentDialog.class.getSimpleName();
    private final AppCompatActivity activity;
    private final onDialogStatus status;
    private final EditText editText;
    //    private EmojiFragment emojiFragment;
    private EmojiAdapter emojiAdapter;
    private final IEmojiClick iEmojiClick;

    @Override
    public int getDialogLayout() {
        return R.layout.dialog_feed_comment_layout;
    }

    public FeedCommentDialog(AppCompatActivity activity, String hintText, onDialogStatus status, IEmojiClick iEmojiClick) {
        super(activity);
        this.activity = activity;
        this.status = status;
        setOnKeyListener(this);
        this.iEmojiClick = iEmojiClick;
        editText = rootView.findViewById(R.id.edit_text);
        editText.setHint(hintText);
        View view = rootView.findViewById(R.id.temp);
        view.setOnClickListener(view1 -> dismiss());
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                rootView.findViewById(R.id.sendBtn).setAlpha(s.length() == 0 ? 0.2f : 1.0f);
                rootView.findViewById(R.id.sendBtn).setEnabled(s.length() != 0);
            }
        });
        rootView.findViewById(R.id.sendBtn).setOnClickListener(v -> {
            if (iEmojiClick != null) {
                iEmojiClick.onSendClick(editText.getText().toString());
            }
            editText.setText("");
            dismiss();
        });
        rootView.findViewById(R.id.input_layout).addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (status != null) {
                    status.onStatus(true, top + WKStatusBarUtils.getStatusBarHeight(activity) - PanelUtil.getKeyBoardHeight(getContext()));
                }

            }
        });
        editText.post(new Runnable() {
            @Override
            public void run() {
                SoftKeyboardUtils.getInstance().showInput(activity,editText);
            }
        });
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                SoftKeyboardUtils.getInstance().showInput(activity,editText);
//            }
//        },100);
    }

    @Override
    public void show() {
        if (helper == null) {
            helper = new PanelSwitchHelper.Builder(activity.getWindow(), rootView)
                    //可选
                    .addKeyboardStateListener((visible, height) -> {
                        if (visible && height > 0) {
                            WKConstants.setKeyboardHeight(height);
                        }
                    })
                    //可选
                    .addPanelChangeListener(new OnPanelChangeListener() {

                        @Override
                        public void onKeyboard() {
                            rootView.findViewById(R.id.emotion_btn).setSelected(false);
                        }

                        @Override
                        public void onNone() {
                            rootView.findViewById(R.id.emotion_btn).setSelected(false);
                            dismiss();
                        }

                        @Override
                        public void onPanel(IPanelView view) {
                            if (view instanceof PanelView) {
                                rootView.findViewById(R.id.emotion_btn).setSelected(((PanelView) view).getId() == R.id.panel_emotion);
                            }
                        }


                        @Override
                        public void onPanelSizeChange(IPanelView panelView, boolean portrait, int oldWidth, int oldHeight, int width, int height) {
                            if (panelView instanceof PanelView) {
                                if (((PanelView) panelView).getId() == R.id.panel_emotion) {
                                    WKConstants.setKeyboardHeight(height);
                                    RelativeLayout frameLayout = rootView.findViewById(R.id.emojiLayout);
                                    frameLayout.getLayoutParams().height = height - AndroidUtilities.dp(30f);
                                    AppCompatImageView imageView = rootView.findViewById(R.id.deleteIv);
                                    View deleteLayout = rootView.findViewById(R.id.deleteLayout);
                                    RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);
                                    initEmoji(imageView, deleteLayout, recyclerView);
                                }
                            }
                        }
                    })
                    .logTrack(true)
                    .build(true);
        }
        super.show();
    }

    @Override
    public void dismiss() {
        if (status != null) {
            status.onStatus(false, 0);
        }
        super.dismiss();
    }

    @Override
    public boolean onKey(@Nullable DialogInterface dialog, int keyCode, @NotNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
            dismiss();
            return true;
        }
        return false;
    }

    public interface onDialogStatus {
        void onStatus(boolean visible, int currentTop);
    }

    private void initEmoji(AppCompatImageView deleteIv, View deleteLayout, RecyclerView recyclerView) {
        int width = AndroidUtilities.getScreenWidth() - (AndroidUtilities.dp(30) * 8);
        Theme.setColorFilter(getContext(), deleteIv, R.color.popupTextColor);
        List<EmojiEntry> emojiIndexs = new ArrayList<>();
        List<EmojiEntry> normalList = EmojiManager.getInstance().getEmojiWithType("0_");
        List<EmojiEntry> naturelList = EmojiManager.getInstance().getEmojiWithType("1_");
        List<EmojiEntry> symbolsList = EmojiManager.getInstance().getEmojiWithType("2_");
        emojiIndexs.addAll(normalList);
        emojiIndexs.addAll(naturelList);
        emojiIndexs.addAll(symbolsList);
        emojiAdapter = new EmojiAdapter(new ArrayList<>(), width);
        emojiAdapter.setList(emojiIndexs);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(8, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(emojiAdapter);
        emojiAdapter.addFooterView(getFooterView());
        getCommonEmoji(width);
        emojiAdapter.setOnItemClickListener((adapter, view, position) -> {
            EmojiEntry entry =emojiAdapter.getItem(position);
            emojiClick(entry.getText());
        });
        deleteLayout.setOnClickListener(v -> emojiClick(""));
    }

    private void getCommonEmoji(int width) {
        //查看最近使用到表情
        String ids = WKSharedPreferencesUtil.getInstance().getSPWithUID("common_used_emojis");
        List<EmojiEntry> list = new ArrayList<>();
        String tempIds = "";
        if (!TextUtils.isEmpty(ids)) {
            if (ids.contains(",")) {
                String[] emojiIds = ids.split(",");
                for (String emojiId : emojiIds) {
                    if (list.size() == 32) break;
                    if (!TextUtils.isEmpty(emojiId)) {
                        EmojiEntry entry = EmojiManager.getInstance().getEmojiEntry( emojiId);
                        if (entry != null) {
                            list.add(entry);
                        }
                        if (TextUtils.isEmpty(tempIds)) {
                            tempIds = emojiId;
                        } else tempIds = tempIds + "," + emojiId;
                    }

                }
            } else {
                EmojiEntry entry = EmojiManager.getInstance().getEmojiEntry( ids);
                if (entry != null) {
                    list.add(entry);
                }
                tempIds = ids;
            }
        }
        if (list.isEmpty()) return;
        emojiAdapter.removeAllHeaderView();
        View headerView = LayoutInflater.from(getContext()).inflate(com.chat.base.R.layout.common_used_emoji_header_layout, null);
        RecyclerView recyclerView = headerView.findViewById(com.chat.base.R.id.recyclerView);
        EmojiAdapter headerAdapter = new EmojiAdapter(new ArrayList<>(), width);
        headerAdapter.addData(list);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(8, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(headerAdapter);
        emojiAdapter.addHeaderView(headerView);
        WKSharedPreferencesUtil.getInstance().putSPWithUID("common_used_emojis", tempIds);
        headerAdapter.setOnItemClickListener((adapter, view, position) -> {
//            String index = (String) adapter.getItem(position);
            EmojiEntry entry = headerAdapter.getItem(position);
            emojiClick(entry.getText());
        });
    }

    private void emojiClick(String name) {
        if (!TextUtils.isEmpty(name)) {
            int curPosition = editText.getSelectionStart();
            StringBuilder sb = new StringBuilder(Objects.requireNonNull(editText.getText()).toString());
            sb.insert(curPosition, name);
//                    mEditText.setText(sb.toString());
            MoonUtil.addEmojiSpan(editText, name, getContext());
            // 将光标设置到新增完表情的右侧
            editText.setSelection(curPosition + name.length());

            String usedIndexs = WKSharedPreferencesUtil.getInstance().getSPWithUID("common_used_emojis");
            String tempIndexs = "";
            if (!TextUtils.isEmpty(usedIndexs)) {
                if (usedIndexs.contains(",")) {
                    String[] strings = usedIndexs.split(",");
                    for (String string : strings) {
                        if (!string.equals(name)) {
                            if (TextUtils.isEmpty(tempIndexs)) {
                                tempIndexs = string;
                            } else {
                                tempIndexs = tempIndexs + "," + string;
                            }
                        }
                    }
                }
            }
            tempIndexs = name + "," + tempIndexs;
            WKSharedPreferencesUtil.getInstance().putSPWithUID("common_used_emojis", tempIndexs);
        } else {
            editText.dispatchKeyEvent(new KeyEvent(
                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        }
        if (iEmojiClick != null) {
            iEmojiClick.onEmojiClick(name);
        }
    }

    private View getFooterView() {
        return LayoutInflater.from(getContext()).inflate(R.layout.common_used_emoji_footer_layout, null);
    }


    public interface IEmojiClick {
        void onEmojiClick(String emojiName);

        void onSendClick(String content);
    }
}
