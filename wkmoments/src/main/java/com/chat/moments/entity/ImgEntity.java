package com.chat.moments.entity;

import java.util.UUID;

/**
 * 2020-11-10 18:22
 * 本地发布图片
 */
public class ImgEntity {
    public String coverUrl;//封面网络地址
    public String url;//网络地址
    public String coverPath;//封面本地地址
    public String path;//本地地址
    public int fileType;//文件类型 [0:添加][1：图片][2：视频]
    public String key;//文件key
    public MomentsFileUploadStatus uploadStatus;//上传状态[0:等待][1:成功][2:失败]
    public int progress;//上传进度

    public ImgEntity(String path, int fileType) {
        this.path = path;
        this.fileType = fileType;
        this.key = UUID.randomUUID().toString().replaceAll("-", "");
        uploadStatus = MomentsFileUploadStatus.waiting;
    }

    public ImgEntity() {
    }
}
