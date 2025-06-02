package com.chat.moments.entity;

/**
 * 2020-11-24 16:23
 * 动态消息
 */
public class MomentsMsg {
    public int id;
    public String uid;
    public String name;
    public String moment_no;
    public String comment;
    public String time;
    public int contentType;
    public String action;
    public int is_deleted;
    public String url;//图片地址或视频封面
    public String content;//发布内容
    public String avatarCacheKey;//发布者头像
}
