// Generated by view binder compiler. Do not edit!
package com.chat.uikit.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.chat.base.ui.components.ContactEditText;
import com.chat.base.ui.components.SwitchView;
import com.chat.uikit.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;
import org.telegram.ui.Components.RLottieImageView;

public final class ChatInputLayoutBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final LinearLayout banView;

  @NonNull
  public final SwitchView burnSwitchView;

  @NonNull
  public final TextView burnTimeTv;

  @NonNull
  public final LinearLayout chatView;

  @NonNull
  public final RLottieImageView closeSearchLottieIV;

  @NonNull
  public final TextView contentTv;

  @NonNull
  public final AppCompatImageView deleteIv;

  @NonNull
  public final TextView deleteTv;

  @NonNull
  public final LinearLayout deleteView;

  @NonNull
  public final ContactEditText editText;

  @NonNull
  public final AppCompatImageView flameIV;

  @NonNull
  public final LinearLayout flameLayout;

  @NonNull
  public final TextView forbiddenTv;

  @NonNull
  public final LinearLayout forbiddenView;

  @NonNull
  public final AppCompatImageView forwardIv;

  @NonNull
  public final TextView forwardTv;

  @NonNull
  public final LinearLayout forwardView;

  @NonNull
  public final TextView hitTv;

  @NonNull
  public final AppCompatImageView markdownIv;

  @NonNull
  public final AppCompatImageView menuIv;

  @NonNull
  public final LinearLayout menuLayout;

  @NonNull
  public final LinearLayout menuView;

  @NonNull
  public final LinearLayout multipleChoiceView;

  @NonNull
  public final RecyclerView robotGifRecyclerView;

  @NonNull
  public final LinearLayout seekBarLayout;

  @NonNull
  public final AppCompatImageView sendIV;

  @NonNull
  public final RecyclerView toolbarRecyclerView;

  @NonNull
  public final AppCompatImageView topCloseIv;

  @NonNull
  public final LinearLayout topLayout;

  @NonNull
  public final AppCompatImageView topLeftIv;

  @NonNull
  public final TextView topTitleTv;

  private ChatInputLayoutBinding(@NonNull LinearLayout rootView, @NonNull LinearLayout banView,
      @NonNull SwitchView burnSwitchView, @NonNull TextView burnTimeTv,
      @NonNull LinearLayout chatView, @NonNull RLottieImageView closeSearchLottieIV,
      @NonNull TextView contentTv, @NonNull AppCompatImageView deleteIv, @NonNull TextView deleteTv,
      @NonNull LinearLayout deleteView, @NonNull ContactEditText editText,
      @NonNull AppCompatImageView flameIV, @NonNull LinearLayout flameLayout,
      @NonNull TextView forbiddenTv, @NonNull LinearLayout forbiddenView,
      @NonNull AppCompatImageView forwardIv, @NonNull TextView forwardTv,
      @NonNull LinearLayout forwardView, @NonNull TextView hitTv,
      @NonNull AppCompatImageView markdownIv, @NonNull AppCompatImageView menuIv,
      @NonNull LinearLayout menuLayout, @NonNull LinearLayout menuView,
      @NonNull LinearLayout multipleChoiceView, @NonNull RecyclerView robotGifRecyclerView,
      @NonNull LinearLayout seekBarLayout, @NonNull AppCompatImageView sendIV,
      @NonNull RecyclerView toolbarRecyclerView, @NonNull AppCompatImageView topCloseIv,
      @NonNull LinearLayout topLayout, @NonNull AppCompatImageView topLeftIv,
      @NonNull TextView topTitleTv) {
    this.rootView = rootView;
    this.banView = banView;
    this.burnSwitchView = burnSwitchView;
    this.burnTimeTv = burnTimeTv;
    this.chatView = chatView;
    this.closeSearchLottieIV = closeSearchLottieIV;
    this.contentTv = contentTv;
    this.deleteIv = deleteIv;
    this.deleteTv = deleteTv;
    this.deleteView = deleteView;
    this.editText = editText;
    this.flameIV = flameIV;
    this.flameLayout = flameLayout;
    this.forbiddenTv = forbiddenTv;
    this.forbiddenView = forbiddenView;
    this.forwardIv = forwardIv;
    this.forwardTv = forwardTv;
    this.forwardView = forwardView;
    this.hitTv = hitTv;
    this.markdownIv = markdownIv;
    this.menuIv = menuIv;
    this.menuLayout = menuLayout;
    this.menuView = menuView;
    this.multipleChoiceView = multipleChoiceView;
    this.robotGifRecyclerView = robotGifRecyclerView;
    this.seekBarLayout = seekBarLayout;
    this.sendIV = sendIV;
    this.toolbarRecyclerView = toolbarRecyclerView;
    this.topCloseIv = topCloseIv;
    this.topLayout = topLayout;
    this.topLeftIv = topLeftIv;
    this.topTitleTv = topTitleTv;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ChatInputLayoutBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ChatInputLayoutBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.chat_input_layout, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ChatInputLayoutBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.banView;
      LinearLayout banView = ViewBindings.findChildViewById(rootView, id);
      if (banView == null) {
        break missingId;
      }

      id = R.id.burnSwitchView;
      SwitchView burnSwitchView = ViewBindings.findChildViewById(rootView, id);
      if (burnSwitchView == null) {
        break missingId;
      }

      id = R.id.burnTimeTv;
      TextView burnTimeTv = ViewBindings.findChildViewById(rootView, id);
      if (burnTimeTv == null) {
        break missingId;
      }

      id = R.id.chatView;
      LinearLayout chatView = ViewBindings.findChildViewById(rootView, id);
      if (chatView == null) {
        break missingId;
      }

      id = R.id.closeSearchLottieIV;
      RLottieImageView closeSearchLottieIV = ViewBindings.findChildViewById(rootView, id);
      if (closeSearchLottieIV == null) {
        break missingId;
      }

      id = R.id.contentTv;
      TextView contentTv = ViewBindings.findChildViewById(rootView, id);
      if (contentTv == null) {
        break missingId;
      }

      id = R.id.deleteIv;
      AppCompatImageView deleteIv = ViewBindings.findChildViewById(rootView, id);
      if (deleteIv == null) {
        break missingId;
      }

      id = R.id.deleteTv;
      TextView deleteTv = ViewBindings.findChildViewById(rootView, id);
      if (deleteTv == null) {
        break missingId;
      }

      id = R.id.deleteView;
      LinearLayout deleteView = ViewBindings.findChildViewById(rootView, id);
      if (deleteView == null) {
        break missingId;
      }

      id = R.id.editText;
      ContactEditText editText = ViewBindings.findChildViewById(rootView, id);
      if (editText == null) {
        break missingId;
      }

      id = R.id.flameIV;
      AppCompatImageView flameIV = ViewBindings.findChildViewById(rootView, id);
      if (flameIV == null) {
        break missingId;
      }

      id = R.id.flameLayout;
      LinearLayout flameLayout = ViewBindings.findChildViewById(rootView, id);
      if (flameLayout == null) {
        break missingId;
      }

      id = R.id.forbiddenTv;
      TextView forbiddenTv = ViewBindings.findChildViewById(rootView, id);
      if (forbiddenTv == null) {
        break missingId;
      }

      id = R.id.forbiddenView;
      LinearLayout forbiddenView = ViewBindings.findChildViewById(rootView, id);
      if (forbiddenView == null) {
        break missingId;
      }

      id = R.id.forwardIv;
      AppCompatImageView forwardIv = ViewBindings.findChildViewById(rootView, id);
      if (forwardIv == null) {
        break missingId;
      }

      id = R.id.forwardTv;
      TextView forwardTv = ViewBindings.findChildViewById(rootView, id);
      if (forwardTv == null) {
        break missingId;
      }

      id = R.id.forwardView;
      LinearLayout forwardView = ViewBindings.findChildViewById(rootView, id);
      if (forwardView == null) {
        break missingId;
      }

      id = R.id.hitTv;
      TextView hitTv = ViewBindings.findChildViewById(rootView, id);
      if (hitTv == null) {
        break missingId;
      }

      id = R.id.markdownIv;
      AppCompatImageView markdownIv = ViewBindings.findChildViewById(rootView, id);
      if (markdownIv == null) {
        break missingId;
      }

      id = R.id.menuIv;
      AppCompatImageView menuIv = ViewBindings.findChildViewById(rootView, id);
      if (menuIv == null) {
        break missingId;
      }

      id = R.id.menuLayout;
      LinearLayout menuLayout = ViewBindings.findChildViewById(rootView, id);
      if (menuLayout == null) {
        break missingId;
      }

      id = R.id.menuView;
      LinearLayout menuView = ViewBindings.findChildViewById(rootView, id);
      if (menuView == null) {
        break missingId;
      }

      id = R.id.multipleChoiceView;
      LinearLayout multipleChoiceView = ViewBindings.findChildViewById(rootView, id);
      if (multipleChoiceView == null) {
        break missingId;
      }

      id = R.id.robotGifRecyclerView;
      RecyclerView robotGifRecyclerView = ViewBindings.findChildViewById(rootView, id);
      if (robotGifRecyclerView == null) {
        break missingId;
      }

      id = R.id.seekBarLayout;
      LinearLayout seekBarLayout = ViewBindings.findChildViewById(rootView, id);
      if (seekBarLayout == null) {
        break missingId;
      }

      id = R.id.sendIV;
      AppCompatImageView sendIV = ViewBindings.findChildViewById(rootView, id);
      if (sendIV == null) {
        break missingId;
      }

      id = R.id.toolbarRecyclerView;
      RecyclerView toolbarRecyclerView = ViewBindings.findChildViewById(rootView, id);
      if (toolbarRecyclerView == null) {
        break missingId;
      }

      id = R.id.topCloseIv;
      AppCompatImageView topCloseIv = ViewBindings.findChildViewById(rootView, id);
      if (topCloseIv == null) {
        break missingId;
      }

      id = R.id.topLayout;
      LinearLayout topLayout = ViewBindings.findChildViewById(rootView, id);
      if (topLayout == null) {
        break missingId;
      }

      id = R.id.topLeftIv;
      AppCompatImageView topLeftIv = ViewBindings.findChildViewById(rootView, id);
      if (topLeftIv == null) {
        break missingId;
      }

      id = R.id.topTitleTv;
      TextView topTitleTv = ViewBindings.findChildViewById(rootView, id);
      if (topTitleTv == null) {
        break missingId;
      }

      return new ChatInputLayoutBinding((LinearLayout) rootView, banView, burnSwitchView,
          burnTimeTv, chatView, closeSearchLottieIV, contentTv, deleteIv, deleteTv, deleteView,
          editText, flameIV, flameLayout, forbiddenTv, forbiddenView, forwardIv, forwardTv,
          forwardView, hitTv, markdownIv, menuIv, menuLayout, menuView, multipleChoiceView,
          robotGifRecyclerView, seekBarLayout, sendIV, toolbarRecyclerView, topCloseIv, topLayout,
          topLeftIv, topTitleTv);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}