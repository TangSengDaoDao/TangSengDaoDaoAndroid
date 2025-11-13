package com.chat.base.net;

import android.text.TextUtils;

import com.chat.base.config.WKBinder;
import com.chat.base.utils.WKLogUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.Buffer;

/**
 * 2020-07-20 11:50
 * 网络请求日志监听
 */
public class LogInterceptor implements Interceptor {

    private static final long MAX_LOG_BODY_BYTES = 1024 * 1024; // 1MB

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {

        if (WKBinder.isDebug) {
            Request request = chain.request();
            //获取request内容
            RequestBody requestBody = request.body();
            String requestParams = "";

            if (requestBody != null && !requestBody.isOneShot()) {
                MediaType type = requestBody.contentType();
                long contentLength = requestBody.contentLength();
                if (isPlainText(type) && isWithinLimit(contentLength)) {
                    Buffer source = new Buffer();
                    try {
                        requestBody.writeTo(source);
                        Charset charset = type == null ? Charset.defaultCharset() : type.charset(Charset.defaultCharset());
                        Util.readBomAsCharset(source, charset);
                        long sourceSize = source.size();
                        long byteCount = Math.min(sourceSize, MAX_LOG_BODY_BYTES);
                        requestParams = source.readString(byteCount, charset);
                        if (sourceSize > MAX_LOG_BODY_BYTES || (contentLength >= 0 && contentLength > MAX_LOG_BODY_BYTES)) {
                            requestParams += "\n... (truncated)";
                        }
                    } catch (Exception e) {
                        WKLogUtils.e("LogInterceptor request log error: " + e.getMessage());
                    } finally {
                        Util.closeQuietly(source);
                    }
                } else {
                    WKLogUtils.d("wkHttpLog", "Request body skipped - type:" + type + ", length:" + contentLength);
                }
            }
            // 请求日志
            StringBuilder reqSb = new StringBuilder();
            reqSb.append(String.format(Locale.getDefault(), "\n请求地址: %s", request.url())).append("\n")
                    .append("\n**************Request***************\n")
                    .append("==============Method==============\n")
                    .append(request.method()).append("\n")
                    .append("==============Headers=============\n")
                    .append(request.headers());
            if (!TextUtils.isEmpty(requestParams)) {
                reqSb.append("==============Body==============\n")
                        .append(formatBodyText(requestParams)).append("\n");
            }
            reqSb.append("**************Request***************").append("\n");
            WKLogUtils.d("wkHttpLog", reqSb.toString());

            // 响应日志
            long t1 = System.nanoTime();
            Response response = chain.proceed(request);
            long t2 = System.nanoTime();
            StringBuilder sb = new StringBuilder();

            if (!TextUtils.isEmpty(requestParams)) {
                sb.append(" \n").append("============Request Body============\n")
                        .append(formatBodyText(requestParams)).append("\n");
            }
            sb.append("*************Response**************\n")
                    .append(response.request().url()).append("\n")
                    .append(String.format(Locale.getDefault(), "本次请求响应时间: %.1f ms", (t2 - t1) / 1e6d)).append("\n")
                    .append("=============Headers=============\n")
                    .append(response.headers()).append("\n");
            ResponseBody body = response.body();
            String responseBodyText = "";
            if (body != null) {
                MediaType mediaType = body.contentType();
                if (isPlainText(mediaType)) {
                    long contentLength = body.contentLength();
                    long peekSize = contentLength >= 0
                            ? Math.min(contentLength, MAX_LOG_BODY_BYTES)
                            : MAX_LOG_BODY_BYTES;
                    ResponseBody peekBody = response.peekBody(peekSize);
                    responseBodyText = peekBody.string();
                    if ((contentLength >= 0 && contentLength > MAX_LOG_BODY_BYTES)
                            || (contentLength < 0 && responseBodyText.length() >= MAX_LOG_BODY_BYTES)) {
                        responseBodyText += "\n... (truncated)";
                    }
                } else {
                    responseBodyText = "[non-text content or skipped due to size]";
                }
            }
            sb.append("==============Body==============\n");
            if (response.isSuccessful()) {
                sb.append(formatBodyText(responseBodyText)).append("\n");
            } else {
                sb.append("http请求失败，").append(response.networkResponse()).append("\n");
            }
            sb.append("*************Response**************");
            WKLogUtils.d("wkHttpLog", "   " + sb);
            return response;
        } else {
            return chain.proceed(chain.request());
        }
    }

    private String formatJson(String json) {
        if (json != null && json.startsWith("{") && json.endsWith("}")) {
            try {
                JSONObject object = new JSONObject(json);
                return object.toString(2);
            } catch (Exception e) {
                return "json格式化错误," + json + ", errorMsg:" + e.getMessage();
            }
        } else if (json != null && json.startsWith("[") && json.endsWith("]")) {
            JSONArray jsonArray;
            try {
                jsonArray = new JSONArray(json);
                return jsonArray.toString(2);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return "";
    }

    private String formatBodyText(String bodyText) {
        if (TextUtils.isEmpty(bodyText)) {
            return "";
        }
        String trimmed = bodyText.trim();
        if (trimmed.endsWith("... (truncated)") || trimmed.startsWith("[non-text")) {
            return bodyText;
        }
        String formatted = formatJson(bodyText);
        return TextUtils.isEmpty(formatted) ? bodyText : formatted;
    }

    private boolean isPlainText(MediaType mediaType) {
        if (mediaType == null) {
            return false;
        }
        if ("text".equals(mediaType.type())) {
            return true;
        }
        String subtype = mediaType.subtype();
        if (subtype == null) {
            return false;
        }
        subtype = subtype.toLowerCase(Locale.getDefault());
        return subtype.contains("json")
                || subtype.contains("xml")
                || subtype.contains("html")
                || subtype.contains("x-www-form-urlencoded")
                || subtype.contains("plain");
    }

    private boolean isWithinLimit(long contentLength) {
        return contentLength >= 0 && contentLength <= MAX_LOG_BODY_BYTES;
    }

    /**
     * 格式化post form
     *
     * @param postParam
     * @return
     */
    private String formatPostFormData(String postParam) {
        StringBuilder builder = new StringBuilder();
        String[] split = postParam.split("&");
        for (String aSplit : split) {
            builder.append(aSplit).append("\n");
        }
        return builder.toString();
    }
}
