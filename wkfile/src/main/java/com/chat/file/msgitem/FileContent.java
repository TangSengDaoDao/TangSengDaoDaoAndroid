package com.chat.file.msgitem;

import android.os.Parcel;

import com.chat.base.WKBaseApplication;
import com.chat.file.R;
import com.xinbida.wukongim.message.type.WKMsgContentType;
import com.xinbida.wukongim.msgmodel.WKMediaMessageContent;
import com.xinbida.wukongim.msgmodel.WKMessageContent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 2020-05-05 22:36
 * 文件类型
 */
public class FileContent extends WKMediaMessageContent {
    public String name;
    public long size;

    public FileContent() {
        type = WKMsgContentType.WK_FILE;
    }

    @Override
    public JSONObject encodeMsg() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("size", size);
            jsonObject.put("url", url);
            jsonObject.put("localPath", localPath);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    @Override
    public WKMessageContent decodeMsg(JSONObject jsonObject) {
        name = jsonObject.optString("name");
        size = jsonObject.optLong("size");
        url = jsonObject.optString("url");
        if (jsonObject.has("localPath"))
            localPath = jsonObject.optString("localPath");
        return this;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(name);
        dest.writeLong(size);
        dest.writeString(url);
        dest.writeString(localPath);
    }

    protected FileContent(Parcel in) {
        super(in);
        name = in.readString();
        size = in.readLong();
        url = in.readString();
        localPath = in.readString();
    }

    public static final Creator<FileContent> CREATOR = new Creator<FileContent>() {
        @Override
        public FileContent createFromParcel(Parcel in) {
            return new FileContent(in);
        }

        @Override
        public FileContent[] newArray(int size) {
            return new FileContent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String getDisplayContent() {
        return WKBaseApplication.getInstance().getContext().getString(R.string.last_msg_file);
    }

    @Override
    public String getSearchableWord() {
        return WKBaseApplication.getInstance().getContext().getString(R.string.last_msg_file);
    }
}
