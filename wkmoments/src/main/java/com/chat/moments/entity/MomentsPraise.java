package com.chat.moments.entity;

/**
 * 2020-11-05 16:29
 * 点赞用户
 */
public class MomentsPraise {
    public String name;
    public String uid;
    public String avatarCacheKey;

    public MomentsPraise() {
    }

    public MomentsPraise(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }
}
