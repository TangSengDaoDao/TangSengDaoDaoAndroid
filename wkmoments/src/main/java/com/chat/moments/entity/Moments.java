package com.chat.moments.entity;

import android.text.SpannableStringBuilder;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.List;

/**
 * 2020-11-05 15:32
 * 动态
 */
public class Moments implements MultiItemEntity {
    public String moment_no;
    public String publisher;//发布者ID
    public String publisher_name;//发布者
    public String video_path;//视频地址
    public String video_cover_path;//视频封面地址
    public List<String> imgs;
    public String text;
    public String created_at;
    public String showDate;//显示时间
    public String address;
    public String longitude;
    public String latitude;
    public List<String> remind_uids;//提及的用户
    public String privacy_type;//隐私类型 【public：公开】【private：私有】【internal：部分可见】【prohibit：不给谁看】
    public List<String> privacy_uids; //不给谁看｜部分可见的用户uids
    //评论列表
    public List<MomentsReply> comments;
    //点赞列表
    public List<MomentsPraise> likes;
    //本地字段
    public boolean showAllText;
    public boolean isExpand;
    public int itemType;
    public String remindUserNames;//提及到的用户名称
    public boolean isRemindMe;//是否提及到自己
    public String publisherAvatarCacheKey;//发布者头像
    //点赞的文字
    public SpannableStringBuilder praiseSpan;


    @Override
    public int getItemType() {
        return itemType;
    }
}
