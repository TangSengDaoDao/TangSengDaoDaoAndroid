// Generated by view binder compiler. Do not edit!
package com.chat.base.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.chat.base.R;
import com.chat.base.views.pwdview.PwdView;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class WkNumPwdInputLayoutBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final GridView gvKeybord;

  @NonNull
  public final PwdView pwdDialog;

  private WkNumPwdInputLayoutBinding(@NonNull LinearLayout rootView, @NonNull GridView gvKeybord,
      @NonNull PwdView pwdDialog) {
    this.rootView = rootView;
    this.gvKeybord = gvKeybord;
    this.pwdDialog = pwdDialog;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static WkNumPwdInputLayoutBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static WkNumPwdInputLayoutBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.wk_num_pwd_input_layout, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static WkNumPwdInputLayoutBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.gv_keybord;
      GridView gvKeybord = ViewBindings.findChildViewById(rootView, id);
      if (gvKeybord == null) {
        break missingId;
      }

      id = R.id.pwdDialog;
      PwdView pwdDialog = ViewBindings.findChildViewById(rootView, id);
      if (pwdDialog == null) {
        break missingId;
      }

      return new WkNumPwdInputLayoutBinding((LinearLayout) rootView, gvKeybord, pwdDialog);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}