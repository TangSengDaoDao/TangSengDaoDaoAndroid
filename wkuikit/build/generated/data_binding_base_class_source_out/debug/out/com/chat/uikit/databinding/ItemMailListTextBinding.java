// Generated by view binder compiler. Do not edit!
package com.chat.uikit.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.chat.uikit.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ItemMailListTextBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final TextView titleTv;

  private ItemMailListTextBinding(@NonNull LinearLayout rootView, @NonNull TextView titleTv) {
    this.rootView = rootView;
    this.titleTv = titleTv;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ItemMailListTextBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ItemMailListTextBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.item_mail_list_text, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ItemMailListTextBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.titleTv;
      TextView titleTv = ViewBindings.findChildViewById(rootView, id);
      if (titleTv == null) {
        break missingId;
      }

      return new ItemMailListTextBinding((LinearLayout) rootView, titleTv);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}