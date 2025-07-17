package com.chat.sticker.msg;

import android.os.Parcel;

import com.chat.base.WKBaseApplication;
import com.chat.base.msgitem.WKContentType;
import com.chat.base.utils.WKReader;
import com.chat.sticker.R;
import com.xinbida.wukongim.msgmodel.WKMessageContent;

import org.json.JSONException;
import org.json.JSONObject;

public class StickerContent extends WKMessageContent {

    public String url;
    public String category;
    public String placeholder;

    public StickerContent() {
        type = WKContentType.WK_VECTOR_STICKER;
    }

    @Override
    public JSONObject encodeMsg() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("url", url);
            jsonObject.put("category", category);
            jsonObject.put("content", content);
            jsonObject.put("placeholder", placeholder);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public WKMessageContent decodeMsg(JSONObject jsonObject) {
        url = jsonObject.optString("url");
        category = jsonObject.optString("category");
        content = WKReader.stringValue(jsonObject, "content");
        placeholder = jsonObject.optString("placeholder");
        return this;
    }

    protected StickerContent(Parcel in) {
        super(in);
        url = in.readString();
        category = in.readString();
        placeholder = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(url);
        dest.writeString(category);
        dest.writeString(placeholder);
    }


    public static final Creator<StickerContent> CREATOR = new Creator<StickerContent>() {
        @Override
        public StickerContent createFromParcel(Parcel in) {
            return new StickerContent(in);
        }

        @Override
        public StickerContent[] newArray(int size) {
            return new StickerContent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String getDisplayContent() {
        return String.format("%s%s", content, WKBaseApplication.getInstance().getContext().getString(R.string.str_msg_content_sticker));
    }
}
