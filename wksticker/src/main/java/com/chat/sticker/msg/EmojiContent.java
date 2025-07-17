package com.chat.sticker.msg;

import android.os.Parcel;

import com.chat.base.msgitem.WKContentType;
import com.chat.base.utils.WKReader;
import com.xinbida.wukongim.msgmodel.WKMessageContent;

import org.json.JSONException;
import org.json.JSONObject;

public class EmojiContent extends WKMessageContent {
    public String url;
    public String placeholder;

    public EmojiContent() {
        type = WKContentType.WK_EMOJI_STICKER;
    }

    @Override
    public JSONObject encodeMsg() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("url", url);
            jsonObject.put("placeholder", placeholder);
            jsonObject.put("content", content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public WKMessageContent decodeMsg(JSONObject jsonObject) {
        this.placeholder = jsonObject.optString("placeholder");
        this.url = jsonObject.optString("url");
        this.content = WKReader.stringValue(jsonObject, "content");
        return this;
    }

    protected EmojiContent(Parcel in) {
        super(in);
        url = in.readString();
        placeholder = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(url);
        dest.writeString(placeholder);
    }


    public static final Creator<EmojiContent> CREATOR = new Creator<EmojiContent>() {
        @Override
        public EmojiContent createFromParcel(Parcel in) {
            return new EmojiContent(in);
        }

        @Override
        public EmojiContent[] newArray(int size) {
            return new EmojiContent[size];
        }
    };

    @Override
    public String getDisplayContent() {
        return content;
    }
}
