package com.chat.advanced.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.advanced.R;
import com.chat.advanced.utils.ReactionStickerUtils;
import com.chat.base.adapter.WKFragmentStateAdapter;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.util.XPopupUtils;
import com.xinbida.wukongim.entity.WKMsgReaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("ViewConstructor")
public class AllMsgReactionsPopupView extends BottomPopupView {
    final AppCompatActivity context;
    private final List<WKMsgReaction> allList = new ArrayList<>();
    private final Map<String, Integer> map = new HashMap<>();
    List<Fragment> list;
    ViewPager2 viewPager;

    public AllMsgReactionsPopupView(@NonNull Context context, List<WKMsgReaction> list, Map<String, Integer> map) {
        super(context);
        this.map.putAll(map);
        this.allList.addAll(list);
        this.list = new ArrayList<>();
        this.context = (AppCompatActivity) context;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.all_msg_reactions_popup_view;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        ViewGroup.MarginLayoutParams params = (MarginLayoutParams) getPopupContentView().getLayoutParams();
        getPopupContentView().setLayoutParams(params);

        viewPager = findViewById(R.id.viewPage);
        RecyclerView recyclerView = findViewById(R.id.topRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        TopAdapter adapter = new TopAdapter();
        recyclerView.setAdapter(adapter);
        List<ReactionData> reactionDataList = new ArrayList<>();
        reactionDataList.add(new ReactionData(-1, allList.size(), true));
        for (String key : map.keySet()) {
            AllMsgReactionsFragment fragment = new AllMsgReactionsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("text", key);
            bundle.putString("msgID", allList.get(0).messageID);
            fragment.setArguments(bundle);
            list.add(fragment);
            Integer integer = map.get(key);
            if (integer != null) {
                reactionDataList.add(new ReactionData(ReactionStickerUtils.getEmojiID(key), integer, false));
            }
        }
        adapter.setList(reactionDataList);

        AllMsgReactionsFragment fragment = new AllMsgReactionsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("text", context.getString(R.string.str_all));
        bundle.putString("msgID", allList.get(0).messageID);
        fragment.setArguments(bundle);
        list.add(0, fragment);

        adapter.setOnItemClickListener((adapter1, view, position) -> {
            ReactionData reactionData = (ReactionData) adapter1.getData().get(position);
            if (reactionData != null) {
                for (int i = 0; i < adapter.getData().size(); i++) {
                    if (adapter.getData().get(i).isSelected) {
                        adapter.getData().get(i).isSelected = false;
                        adapter.notifyItemChanged(i);
                        break;
                    }
                }
                adapter.getData().get(position).isSelected = true;
                adapter.notifyItemChanged(position);
                viewPager.setCurrentItem(position);
            }
        });

        WKFragmentStateAdapter fragmentStateAdapter = new WKFragmentStateAdapter(context, list);
        viewPager.setAdapter(fragmentStateAdapter);
        viewPager.setCurrentItem(0);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                for (int i = 0; i < adapter.getData().size(); i++) {
                    if (adapter.getData().get(i).isSelected) {
                        adapter.getData().get(i).isSelected = false;
                        adapter.notifyItemChanged(i);
                        break;
                    }
                }
                adapter.getData().get(position).isSelected = true;
                adapter.notifyItemChanged(position);
            }
        });
    }

    class TopAdapter extends BaseQuickAdapter<ReactionData, BaseViewHolder> {

        public TopAdapter() {
            super(R.layout.item_msg_reaction_popup);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder baseViewHolder, ReactionData reactionData) {
            if (reactionData.imgID == -1) {
                baseViewHolder.setGone(R.id.imageView, true);
                baseViewHolder.setText(R.id.countTv, String.format("%s %s", context.getString(R.string.str_all), reactionData.count));
            } else {
                baseViewHolder.setGone(R.id.imageView, false);
                baseViewHolder.setImageResource(R.id.imageView, reactionData.imgID);
                baseViewHolder.setText(R.id.countTv, String.valueOf(reactionData.count));
            }
            if (reactionData.isSelected) {
                baseViewHolder.setBackgroundResource(R.id.contentLayout, R.drawable.shape_corner_rectangle);
            } else
                baseViewHolder.setBackgroundColor(R.id.contentLayout, ContextCompat.getColor(context, R.color.transparent));
        }
    }

    static class ReactionData {
        int imgID;
        int count;
        boolean isSelected;

        public ReactionData(int imgID, int count, boolean isSelected) {
            this.imgID = imgID;
            this.count = count;
            this.isSelected = isSelected;
        }
    }

    @Override
    protected int getMaxHeight() {
        return (int) (XPopupUtils.getScreenHeight(getContext()) * .85f);
    }

    @Override
    protected void onDismiss() {
        super.onDismiss();
        allList.clear();
        list.clear();
    }
}
